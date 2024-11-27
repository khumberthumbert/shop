// 페이지가 로드되면 DOMContentLoaded 이벤트 리스너 등록
document.addEventListener("DOMContentLoaded", function () {
    // 로그인 버튼 이벤트 추가
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

    // 게시판 버튼 클릭 이벤트 추가
    const boardButton = document.getElementById("boardButton");
    if (boardButton) {
        boardButton.addEventListener("click", function (event) {
            event.preventDefault();
            loadPosts(); // 게시글 목록 로드
        });
    }
});

// 로그인 폼을 동적으로 생성 및 삽입
function loadLoginForm() {
    console.log("loadLoginForm called!");

    fetch("/loginPage")
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok " + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log("Received data:", data);
            noneTag(); // 기존 콘텐츠 숨기기

            const formContainer = document.createElement("div");
            formContainer.classList.add("login-form-container");

            const form = document.createElement("form");
            form.setAttribute("action", data.formAction);
            form.setAttribute("method", "post");

            // 사용자명 입력 필드
            form.innerHTML = `
                <div class="mb-3">
                    <input type="text" name="username" placeholder="${data.usernamePlaceholder}" class="form-control" required>
                </div>
                <div class="mb-3">
                    <input type="password" name="password" placeholder="${data.passwordPlaceholder}" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary">${data.loginButtonText}</button>
            `;

            formContainer.appendChild(form);
            const header = document.querySelector("header");
            if (header) {
                header.insertAdjacentElement("afterend", formContainer);
            }

            form.addEventListener("submit", handleLoginSubmit); // 폼 제출 이벤트 처리
        })
        .catch(error => console.error("로그인 폼 데이터를 가져오는 중 오류 발생:", error));
}

// 로그인 폼 제출 핸들러
function handleLoginSubmit(event) {
    event.preventDefault(); // 기본 폼 제출 방지
    const form = event.target;
    const formData = new FormData(form);

    fetch(form.action, {
        method: "POST",
        body: formData
    })
        .then(response => {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return response.json();
            } else {
                throw new Error("JSON 응답이 아닙니다.");
            }
        })
        .then(data => {
            const token = data.token;
            if (token) {
                localStorage.setItem("token", token); // 토큰 저장
                console.log("Token stored successfully:", token);
                window.location.href = "/"; // 메인 페이지로 리다이렉트
            } else {
                console.error("토큰이 응답에 없습니다.");
            }
        })
        .catch(error => console.error("로그인 요청 중 오류 발생:", error));
}

// 게시글 목록 로드
function loadPosts() {
    fetchWithToken("/api/posts/page")
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) throw new Error("Unauthorized: Please log in.");
                if (response.status === 403) throw new Error("Forbidden: Access denied.");
                throw new Error("Server error with status code: " + response.status);
            }
            return response.text();
        })
        .then(html => {
            noneTag();
            const boardContainer = document.querySelector("#main-in-the-section");
            if (boardContainer) {
                boardContainer.innerHTML = html;
                console.log("Board list loaded successfully.");
            } else {
                console.error("#main-in-the-section element not found.");
            }
        })
        .catch(error => console.error("게시물 요청 실패:", error));
}

// 기존 콘텐츠 숨기기
function noneTag() {
    const mainContent = document.querySelector("#main-in-the-section");
    if (mainContent) {
        mainContent.innerHTML = ""; // 기존 콘텐츠 초기화
    }
}

// 공통 fetchWithToken 함수: Authorization 헤더에 JWT 토큰 추가
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

// 페이지 로드 시 자동으로 메인 페이지 요청
document.addEventListener("DOMContentLoaded", function() {
    fetchWithToken("/")
        .then(response => {
            if (!response.ok) {
                throw new Error("Authorization failed");
            }
            return response.text();
        })
        .then(data => console.log("홈페이지 요청 성공:", data))
        .catch(error => console.error("홈페이지 요청 실패:", error));
});
