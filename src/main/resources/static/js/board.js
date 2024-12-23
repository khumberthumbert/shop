document.addEventListener("DOMContentLoaded", () => {
    // 게시판 버튼 클릭 시 첫 페이지 로드
    document.querySelector("#boardButton").addEventListener("click", (event) => {
        event.preventDefault();
        fetchBoardPage(0); // 첫 페이지 로드
    });
});

// 게시글 목록과 페이징 데이터를 가져오는 함수
async function fetchBoardPage(page) {
    try {
        const response = await fetch(`/api/posts/page?page=${page}`, {
            headers: {
                "Accept": "application/json", // JSON 응답을 요청
            },
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch page: ${response.status}`);
        }

        const data = await response.json(); // JSON 응답 파싱
        renderBoardList(data.content); // 게시글 목록 렌더링
        updatePaginationButtons(data); // 페이징 버튼 상태 업데이트
    } catch (error) {
        console.error("Error fetching board page:", error);
    }
}


// 게시글 목록 렌더링
function renderBoardList(boards) {
    const container = document.getElementById("board-list");
    container.innerHTML = ""; // 기존 내용 제거

    if (boards.length === 0) {
        container.innerHTML = "<p>There's no post</p>";
        return;
    }

    boards.forEach((board) => {
        const boardItem = document.createElement("div");
        boardItem.classList.add("board-item");
        boardItem.innerHTML = `
            <h3>${board.title}</h3>
            <p>${board.content}</p>
            <p>Writer: ${board.user}</p>
            <p>Time: ${board.displayedTime}</p>
            <hr>
        `;
        boardItem.onclick = () => fetchBoardDetail(board.id); // 게시글 상세보기로 이동
        container.appendChild(boardItem);
    });
}

// 페이징 버튼 상태 업데이트
function updatePaginationButtons(data) {
    const prevButton = document.getElementById("prev-button");
    const nextButton = document.getElementById("next-button");

    // 이전 버튼 업데이트
    if (prevButton) {
        if (data.first) {
            prevButton.disabled = true; // 첫 페이지에서는 비활성화
        } else {
            prevButton.disabled = false; // 첫 페이지가 아니면 활성화
            prevButton.onclick = () => fetchBoardPage(data.number - 1);
        }
    }

    // 다음 버튼 업데이트
    if (nextButton) {
        if (data.last) {
            nextButton.disabled = true; // 마지막 페이지에서는 비활성화
        } else {
            nextButton.disabled = false; // 마지막 페이지가 아니면 활성화
            nextButton.onclick = () => fetchBoardPage(data.number + 1);
        }
    }

    // 현재 페이지 정보 업데이트
    const currentPageElement = document.getElementById("current-page");
    const totalPagesElement = document.getElementById("total-pages");

    if (currentPageElement) {
        currentPageElement.textContent = `${data.number + 1}`;
    }
    if (totalPagesElement) {
        totalPagesElement.textContent = `${data.totalPages}`;
    }
}




// 게시글 전체 조회
function loadPosts() {
    noneTag();
    fetchWithToken("/api/posts/page")
        .then(response => {
            console.log("Status Code:", response.status);
            console.log("Content-Type:", response.headers.get("content-type"));

            if (!response.ok) {
                if (response.status === 401) {
                    console.error("Unauthorized: Please check your login status.");
                    throw new Error("Unauthorized: Please check your login status.");
                } else if (response.status === 403) {
                    console.error("Forbidden: You don't have permission to access this resource.");
                    throw new Error("Forbidden: You don't have permission to access this resource.");
                } else {
                    console.error("Server error with status code:", response.status);
                    throw new Error("Authorization failed or server error");
                }
            }

            const contentType = response.headers.get("content-type");
            // HTML 응답인 경우 처리
            if (contentType && contentType.includes("text/html")) {
                return response.text(); // HTML을 텍스트로 반환
            } else {
                throw new Error("Received unexpected content type");
            }
        })
        .then(html => {
            // 가져온 HTML 프래그먼트를 DOM에 삽입
            noneTag(); // Hide other sections
            const boardContainer = document.querySelector("#main-in-the-section"); // 프래그먼트를 삽입할 대상
            boardContainer.innerHTML = html;
            console.log("Board fragment loaded successfully.");
        })
        .catch(error => console.error("게시물 요청 실패:", error));
}

function noneTag() {
    // 기존 콘텐츠를 숨기기
    const mainContent = document.querySelector("#main-in-the-section");
    if (mainContent) {
        mainContent.innerHTML = "";
    }
}

// 글 쓰기 폼 불러오기
function loadWriteFragment() {
    fetchWithToken("/board/writeFragment")
        .then(response => {
            console.log("Response status:", response.status);
            if (!response.ok) {
                throw new Error("Failed to load fragment");
            }
            return response.text();
        })
        .then(html => {
            noneTag(); // 기존 콘텐츠 숨기기

            const mainContent = document.getElementById("main-in-the-section");
            if (!mainContent) {
                console.error("main-in-the-section element not found.");
                return;
            }

            mainContent.innerHTML = html; // HTML 삽입
            console.log("Fragment loaded successfully");
        })
        .catch(error => console.error("Error loading fragment:", error));
}

// 게시글 쓰기
function submitForm() {
    const form = document.getElementById('boardForm');
    const title = form.title.value.trim(); // title의 공백 제거
    const content = form.content.value.trim(); // content의 공백 제거

    // title 또는 content가 비어 있으면 폼을 제출하지 않음
    if (!title || !content) {
        alert("Title and content are required.");
        return; // 함수 종료 (폼 제출 중단)
    }

    const formData = new FormData();

    // 게시글 데이터(JSON) 추가
    const board = {
        title: title,
        content: content
    };
    formData.append("board", new Blob([JSON.stringify(board)], { type: "application/json" }));

    // 첨부파일 추가
    const files = form.files.files;
    for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
    }

    // 요청 전송
    fetchWithToken('/boards/save', {
        method: 'POST',
        body: formData,
    })
        .then(response => {
            if (response.ok) {
                alert('게시글이 성공적으로 작성되었습니다.');
                console.log(response);
                loadPosts();
            } else {
                return response.json().then(err => { throw new Error(err.message); });
            }
        })
        .catch(error => {
            alert('게시글 작성 중 오류가 발생했습니다: ' + error.message);
        });
}


function fetchBoardDetail(boardId) {
    console.log(`Fetching details for board ID: ${boardId}`);
    const token = localStorage.getItem("token");

    fetch(`/boards/${boardId}`, {
        method: 'GET',
        headers: {
            "Authorization": `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Board data received:", data);
            // 서버에서 받은 데이터에서 필요한 부분을 추출
            const board = data.board; // 게시글 정보
            const authentication = data.authentication; // 인증 정보
            displayBoardDetail(board, authentication);
        })
        .catch(error => {
            console.error('Error fetching board details:', error);
        });
}

function displayBoardDetail(board, authentication) {
    const token = localStorage.getItem("token");
    console.log("Board user:", board); // 인증된 사용자 이름
    console.log("Board roles:", authentication); // 사용자의 권한 정보
    console.log(authentication.username); // 사용자의 권한 정보

    // 게시글 목록 컨테이너와 상세 보기 컨테이너 가져오기
    const detailContainer = document.getElementById('board-detail-container');

    if (!detailContainer) {
        console.error('Board detail container not found!');
        return;
    }

    const mainInTheSection = document.getElementById('main-in-the-section');

    // 기존 리스트 숨기기
    mainInTheSection.style.display = 'none';
    console.log("이거 뭐라고 찍히는지 보자." + board.fileMetadataList)

    // 첨부파일 목록 렌더링
    let attachmentsHtml = '';
    if (board.fileMetadataList && board.fileMetadataList.length > 0) {
        attachmentsHtml = `
        <h3>첨부파일</h3>
        <div class="attachments">
            ${board.fileMetadataList.map(file => {
            if (!file.fileUrl || !file.fileType) {
                console.warn("Invalid file metadata:", file);
                return `<p>파일 정보가 올바르지 않습니다.</p>`;
            }
            console.log("File type:", file.fileType);
            console.log("File fileUrl:", file.fileUrl);
            // 이미지 파일만 미리보기로 표시
            if (file.fileType.startsWith('image/')) {
                return `<img src="${file.fileUrl}" alt="${file.fileName}" style="max-width: 100%; height: auto; margin-bottom: 10px;">`;
            } else {
                // 이미지 파일이 아닌 경우 파일 이름만 표시
                return `<p>${file.fileName}</p>`;
            }
        }).join('')}
        </div>
    `;
    }

    let updateButtonHtml = '';
    if (token) {
        updateButtonHtml = `
        <div>
            <button onclick="showEditForm(${board.id})">Update</button>
        </div>
        <div>
            <button onclick="deleteBoard(${board.id})">Delete</button>
        </div>
        `;
    }

    // 상세 정보 표시
    detailContainer.innerHTML = `
        <h2>${board.title}</h2>
        <p>${board.content}</p>
        <p><strong>Writer:</strong> ${board.user}</p>
        <p><strong>Time:</strong> ${board.displayedTime}</p>
        ${attachmentsHtml}
        <div style="margin-top: 20px;">
            <button onclick="goBackToList()">Go List </button>
        </div>
        ${updateButtonHtml}
    `;

    // 상세 보기 컨테이너 보이기
    detailContainer.style.display = 'block';
}
//게시글 삭제.
function deleteBoard(boardId) {
    if (!confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
        return;
    }

    fetch(`/boards/delete/${boardId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.message); });
            }
            return response;
        })
        .then(() => {
            alert('Board deleted');
            removePostFromDom(boardId);
            goBackToList(); // 게시글 목록 다시 로드
            loadPosts();
        })
        .catch(error => {
            console.error('Error deleting board:', error);
            alert('Error deleting board: ' + error.message);
        });
}

function removePostFromDom(boardId) {
    const postElement = document.querySelector(`#post-${boardId}`);
    if (postElement) {
        postElement.remove(); // 삭제된 게시글을 DOM에서 제거
        console.log(`Post with ID ${boardId} removed from DOM.`);
    } else {
        console.warn(`Post with ID ${boardId} not found in DOM.`);
    }
}



function goBackToList() {
    // 게시글 목록 컨테이너와 상세 보기 컨테이너 가져오기
    const mainInTheSection = document.getElementById('main-in-the-section');
    const detailContainer = document.getElementById('board-detail-container');

    // 상세 보기 숨기기
    detailContainer.style.display = 'none';

    // 기존 리스트 보이기
    mainInTheSection.style.display = 'block';
}

// 수정하기 폼 생성
function showEditForm(boardId) {
    console.log(`Editing board ID: ${boardId}`);

    fetch(`/boards/${boardId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch board data. Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Editing board:', data);
            const board = data.board
            // 수정 폼 렌더링
            renderEditForm(board);

            /*// 수정 폼 DOM 요소가 렌더링된 이후에 데이터 바인딩
            const titleInput = document.getElementById('title');
            const contentInput = document.getElementById('content');

            if (!titleInput || !contentInput) {
                console.error('Title or content input field not found in DOM.');
                return;
            }

            titleInput.value = data.title;
            contentInput.value = data.content;*/

            // 기존 첨부파일 로드
            if (data.board.fileMetadataList) {
                loadExistingAttachments(data.board.fileMetadataList);
            }
        })
        .catch(error => {
            console.error('Error fetching board data for editing:', error);
        });
}

// 수정할 게시글 데이터를 폼에 렌더링
function renderEditForm(board) {
    const detailContainer = document.getElementById('board-detail-container');


    // 수정 폼 렌더링
    detailContainer.innerHTML = `
        <h2>Board Update</h2>
        <form id="editForm">
            <label for="title">Title</label>
            <input type="text" id="title" name="title" value="${board.title}">

            <label for="content">Content</label>
            <textarea id="content" name="content" >${board.content}</textarea>

            <label for="files">Add Files</label>
            <input type="file" id="files" name="files" multiple onchange="previewSelectedFiles(event)">

            <!-- 미리보기 컨테이너 추가 -->
            <div id="previewContainer" style="margin-top: 20px;"></div>

            <div style="margin-top: 20px;">
                <button type="button" onclick="submitEditForm(${board.id})">Save</button>
                <button type="button" onclick="goBackToList()">Cancel</button>
            </div>
        </form>
    `;
}

// 첨부파일 미리보기 및 제거 기능
function previewSelectedFiles(event) {
    const files = event.target.files;
    const previewContainer = document.getElementById("previewContainer");

    if (!files || files.length === 0) {
        console.warn("No files selected for preview.");
        return;
    }

    if (!previewContainer) {
        console.error("Preview container not found in DOM.");
        return;
    }

    // 기존 미리보기 초기화
    previewContainer.innerHTML = "";

    Array.from(files).forEach((file, index) => {
        const reader = new FileReader();

        reader.onload = function (e) {
            // 미리보기 이미지 컨테이너 생성
            const previewDiv = document.createElement("div");
            previewDiv.classList.add("preview-item");
            previewDiv.style.position = "relative";
            previewDiv.style.display = "inline-block";
            previewDiv.style.margin = "10px";

            const img = document.createElement("img");
            img.src = e.target.result;
            img.alt = file.name;
            img.style.maxWidth = "150px";
            img.style.marginBottom = "10px";

            // "X" 버튼 생성
            const removeButton = document.createElement("button");
            removeButton.innerText = "X";
            removeButton.style.position = "absolute";
            removeButton.style.top = "5px";
            removeButton.style.right = "5px";
            removeButton.style.background = "red";
            removeButton.style.color = "white";
            removeButton.style.border = "none";
            removeButton.style.cursor = "pointer";

            removeButton.onclick = function () {
                // 선택된 파일 제거
                previewDiv.remove();
                const dataTransfer = new DataTransfer();
                Array.from(files).forEach((f, i) => {
                    if (i !== index) {
                        dataTransfer.items.add(f);
                    }
                });
                document.getElementById("files").files = dataTransfer.files;
            };

            previewDiv.appendChild(img);
            previewDiv.appendChild(removeButton);
            previewContainer.appendChild(previewDiv);
        };

        reader.readAsDataURL(file);
    });
}

// 게시글 수정 시 기존 첨부파일 로드 및 관리
function loadExistingAttachments(attachments) {
    const previewContainer = document.getElementById("previewContainer");

    if (!previewContainer) {
        console.error("Preview container not found in DOM.");
        return;
    }

    const deletedFileIds = []; // 삭제된 파일 ID를 추적할 배열

    attachments.forEach(attachment => {
        const previewDiv = document.createElement("div");
        previewDiv.classList.add("preview-item");
        previewDiv.style.position = "relative";
        previewDiv.style.display = "inline-block";
        previewDiv.style.margin = "10px";

        const img = document.createElement("img");
        img.src = attachment.fileUrl; // 파일 URL
        img.alt = attachment.id;     // 파일 ID를 alt 속성에 저장
        img.style.maxWidth = "150px";
        img.style.marginBottom = "10px";

        const removeButton = document.createElement("button");
        removeButton.innerText = "X";
        removeButton.style.position = "absolute";
        removeButton.style.top = "5px";
        removeButton.style.right = "5px";
        removeButton.style.background = "red";
        removeButton.style.color = "white";
        removeButton.style.border = "none";
        removeButton.style.cursor = "pointer";

        // 삭제 버튼 클릭 이벤트
        removeButton.onclick = function () {
            previewDiv.remove(); // 화면에서 제거
            deletedFileIds.push(parseInt(img.alt, 10)); // 삭제할 파일 ID 추가
            console.log("Deleted file ID:", img.alt); // 로그 출력
        };

        previewDiv.appendChild(img);
        previewDiv.appendChild(removeButton);
        previewContainer.appendChild(previewDiv);
    });

    // 삭제된 파일 ID를 전역적으로 추적
    window.deletedFileIds = deletedFileIds;
}



// 수정 시 저장 버튼
function submitEditForm(boardId) {
    const formData = new FormData();

    // 게시글 정보 추가
    formData.append("title", document.getElementById("title").value);
    formData.append("content", document.getElementById("content").value);

    // 삭제된 파일 ID 추가
    if (window.deletedFileIds && window.deletedFileIds.length > 0) {
        console.log("삭제된 파일 IDs:", window.deletedFileIds); // 로그 확인
        formData.append("deleteFileIds", JSON.stringify(window.deletedFileIds));
    } else {
        console.log("삭제된 파일 없음");
    }

    // 새로 추가된 첨부파일 처리
    const files = document.getElementById("files").files;
    for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
    }

    // 요청 전송
    fetch(`/boards/update/${boardId}`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${localStorage.getItem("token")}`
        },
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to update board. Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Board updated successfully:", data);
            goBackToList(); // 수정 완료 후 목록으로 이동
        })
        .catch(error => {
            console.error("Error updating board:", error);
        });
}


