// js/common.js

document.addEventListener("DOMContentLoaded", function () {
    const loginButton = document.querySelector('form[action="/loginPage"] button');

    if (loginButton) {
        console.log("Login button found!");
        loginButton.addEventListener("click", function (event) {
            event.preventDefault();
            loadLoginForm();
        });
    } else {
        console.log("Login button not found!");
    }

    // Board 버튼에 클릭 이벤트 추가
    const boardButton = document.getElementById("boardButton"); // boardButton의 ID를 추가해주세요
    if (boardButton) {
        boardButton.addEventListener("click", function (event) {
            event.preventDefault(); // 기본 폼 제출 방지
            loadPosts(); // loadPosts 함수 호출
        });
    }
});

function loadLoginForm() {
    console.log("loadLoginForm called!"); // 함수 호출 확인

    fetch("/loginPage")
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok " + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log("Received data:", data); // 받은 데이터 확인

            // 기존 콘텐츠를 숨기기
            noneTag();

            // 새로운 폼 요소를 생성
            const formContainer = document.createElement("div");
            formContainer.classList.add("login-form-container"); // 스타일을 적용할 수 있도록 클래스 추가

            const form = document.createElement("form");
            form.setAttribute("action", data.formAction);
            form.setAttribute("method", "post");

            // 사용자명 입력 필드 생성
            const usernameDiv = document.createElement("div");
            usernameDiv.classList.add("mb-3");
            const usernameInput = document.createElement("input");
            usernameInput.setAttribute("type", "text");
            usernameInput.setAttribute("name", "username");
            usernameInput.setAttribute("placeholder", data.usernamePlaceholder);
            usernameInput.classList.add("form-control");
            usernameInput.setAttribute("required", "required");
            usernameDiv.appendChild(usernameInput);
            form.appendChild(usernameDiv);

            // 비밀번호 입력 필드 생성
            const passwordDiv = document.createElement("div");
            passwordDiv.classList.add("mb-3");
            const passwordInput = document.createElement("input");
            passwordInput.setAttribute("type", "password");
            passwordInput.setAttribute("name", "password");
            passwordInput.setAttribute("placeholder", data.passwordPlaceholder);
            passwordInput.classList.add("form-control");
            passwordInput.setAttribute("required", "required");
            passwordDiv.appendChild(passwordInput);
            form.appendChild(passwordDiv);

            // 로그인 버튼 생성
            const loginButton = document.createElement("button");
            loginButton.setAttribute("type", "submit");
            loginButton.classList.add("btn", "btn-primary");
            loginButton.textContent = data.loginButtonText;
            form.appendChild(loginButton);

            // 폼을 폼 컨테이너에 추가
            formContainer.appendChild(form);

            const header = document.querySelector("header");
            if (header) {
                header.insertAdjacentElement("afterend", formContainer);
            }

            // 폼 제출 이벤트 핸들러 추가
            form.addEventListener("submit", function(event) {
                event.preventDefault(); // 기본 폼 제출 방지

                // 폼 데이터를 가져와서 AJAX 요청으로 전송
                const formData = new FormData(form);

                fetch(form.action, {
                    method: "POST",
                    body: formData
                })
                    .then(response => {
                        // 응답이 JSON 형식인지 확인
                        const contentType = response.headers.get("content-type");
                        if (contentType && contentType.includes("application/json")) {
                            return response.json();
                        } else {
                            throw new Error("JSON 응답이 아닙니다.");
                        }
                    })
                    .then(data => {
                        // 토큰을 받아서 로컬 스토리지에 저장
                        const token = data.token;
                        if (token) {
                            localStorage.setItem("token", token); // 또는 sessionStorage.setItem("token", token);
                            console.log("Token stored successfully:", token);

                            // 메인 페이지로 리다이렉트
                            window.location.href = "/";
                        } else {
                            console.error("토큰이 응답에 없습니다.");a
                        }
                    })
                    .catch(error => {
                        console.error("로그인 요청 중 오류 발생:", error);
                    });
            });
        })
        .catch(error => {
            console.error("로그인 폼 데이터를 가져오는 중 오류 발생:", error);
        });
}

// 공통으로 사용할 fetchWithToken 함수
function fetchWithToken(url, options = {}) {
    const token = localStorage.getItem("token");
    if (token) {
        options.headers = {
            ...options.headers,
            "Authorization": `Bearer ${token}`
        };
    }
    return fetch(url, options);
}

// 페이지 로드 시 토큰 확인 후 메인 페이지 요청
document.addEventListener("DOMContentLoaded", function() {
    fetchWithToken("/")
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error("Authorization failed");
            }
        })
        .then(data => console.log("홈페이지 요청 성공:", data))
        .catch(error => console.error("홈페이지 요청 실패:", error));
});

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

    // REST API 호출
    fetch(`/boards/${boardId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`, // JWT 토큰 추가
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json(); // JSON 응답 파싱
        })
        .then(data => {
            console.log('Board details:', data);
            displayBoardDetail(data); // 상세 정보 표시 함수 호출
        })
        .catch(error => {
            console.error('Error fetching board details:', error);
            alert('게시글 조회 중 오류가 발생했습니다.');
        });
}

function displayBoardDetail(board) {
    // 상세 정보를 표시할 컨테이너를 선택하거나 생성
    const detailContainer = document.getElementById('board-detail-container');
    if (!detailContainer) {
        console.error('Board detail container not found!');
        return;
    }

    // 상세 정보 표시
    detailContainer.innerHTML = `
        <h2>${board.title}</h2>
        <p>${board.content}</p>
        <p><strong>작성자:</strong> ${board.user}</p>
        <p><strong>작성일:</strong> ${board.displayedTime}</p>
    `;

    // 컨테이너를 보이게 설정
    detailContainer.style.display = 'block';
}


