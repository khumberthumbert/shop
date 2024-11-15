document.addEventListener("DOMContentLoaded", () => {
    document.querySelector("#board-button").addEventListener("click", (event) => {
        event.preventDefault();
        fetchBoardPage(0); // 첫 페이지를 기본으로 로드
    });
});

async function fetchBoardPage(page) {
    try {
        const response = await fetch(`/api/posts/page?page=${page}`);
        if (response.ok) {
            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, "text/html");

            const boardListContent = doc.querySelector("div[th\\:fragment='boardListFragment']").innerHTML;
            document.getElementById("boardContent").innerHTML = boardListContent;
        } else {
            console.error("Failed to load board page");
        }
    } catch (error) {
        console.error("Error fetching board page:", error);
    }
}

//게시글 전체 조회
function loadPosts() {
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

//글 쓰기 폼 불러오기
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
    const formData = new FormData();

    // 게시글 데이터(JSON) 추가
    const board = {
        title: form.title.value,
        content: form.content.value
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
                console.log(response)
                //window.location.href = '/boards'; // 게시글 목록 페이지로 이동
                loadPosts()
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

    fetch(`/boards/${boardId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
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
            displayBoardDetail(data);
        })
        .catch(error => {
            console.error('Error fetching board details:', error);
        });
}


function displayBoardDetail(board) {
    // 게시글 목록 컨테이너와 상세 보기 컨테이너 가져오기
    const boardListContainer = document.getElementById('board-list-container');
    const detailContainer = document.getElementById('board-detail-container');

    if (!detailContainer) {
        console.error('Board detail container not found!');
        return;
    }

    const mainInTheSection = document.getElementById('main-in-the-section');

    // 기존 리스트 숨기기
    mainInTheSection.style.display = 'none';

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
    // 상세 정보 표시
    detailContainer.innerHTML = `
        <h2>${board.title}</h2>
        <p>${board.content}</p>
        <p><strong>작성자:</strong> ${board.user}</p>
        <p><strong>작성일:</strong> ${board.displayedTime}</p>
        ${attachmentsHtml}
        <div style="margin-top: 20px;">
            <button onclick="goBackToList()">목록으로</button>
        </div>
        <div>
            <button onclick="showEditForm(${board.id})">Update</button>
        </div>
    `;

    // 상세 보기 컨테이너 보이기
    detailContainer.style.display = 'block';
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

//수정하기 폼 생성
function showEditForm(boardId) {
    console.log(`Editing board ID: ${boardId}`);

    // REST API 호출로 기존 게시글 데이터 가져오기
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
            // 수정 폼을 렌더링 (기존 데이터를 폼에 채움)
            renderEditForm(data);
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
        <h2>게시글 수정</h2>
        <form id="editForm">
            <label for="title">제목</label>
            <input type="text" id="title" name="title" value="${board.title}" required>

            <label for="content">내용</label>
            <textarea id="content" name="content" required>${board.content}</textarea>

            <label for="files">첨부파일 추가</label>
            <input type="file" id="files" name="files" multiple>

            <div style="margin-top: 20px;">
                <button type="button" onclick="submitEditForm(${board.id})">저장</button>
                <button type="button" onclick="goBackToList()">취소</button>
            </div>
        </form>
    `;
}

function submitEditForm(boardId) {
    const formData = new FormData();

    // 게시글 정보 추가
    formData.append('title', document.getElementById('title').value);
    formData.append('content', document.getElementById('content').value);

    // 첨부파일 추가
    const files = document.getElementById('files').files;
    for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
    }

    // 요청 보내기
    fetch(`/boards/update/${boardId}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
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
            console.log('Board updated successfully:', data);
            goBackToList();
        })
        .catch(error => {
            console.error('Error updating board:', error);
        });
}


