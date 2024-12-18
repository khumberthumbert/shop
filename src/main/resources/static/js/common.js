document.addEventListener("DOMContentLoaded", function () {

    checkLoginStatus(); // 로그인 상태 확인 함수 호출
    loadPosts();

    const boardButton = document.getElementById("boardButton");
    const loginButton = document.querySelector('form[action="/loginPage"] button');
    const logoutButton = document.getElementById("logoutButton");

    if (boardButton) {
        boardButton.addEventListener("click", function (event) {
            event.preventDefault();
            loadPosts(); // 게시글 목록 로드
        });
    } else {
        console.error("boardButton not found");
    }

    if (loginButton) {
        loginButton.addEventListener("click", function (event) {
            event.preventDefault();
            loadLoginForm(); // 로그인 폼 로드
        });
    } else {
        console.error("loginButton not found");
    }

    if (logoutButton) {
        logoutButton.addEventListener("click", function (event) {
            event.preventDefault();
            logout(); // 로그아웃 처리
        });
    } else {
        console.error("logoutButton not found");
    }
});

// 로그인 상태 확인 함수
function checkLoginStatus() {
    const token = localStorage.getItem("token");

    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");

    if (token) {
        // 로그인 상태인 경우: 로그인 버튼을 숨기고 로그아웃 버튼을 표시
        if (loginButton) loginButton.style.display = "none";
        if (logoutButton) logoutButton.style.display = "block";
    } else {
        // 로그아웃 상태인 경우: 로그인 버튼을 표시하고 로그아웃 버튼을 숨김
        if (loginButton) loginButton.style.display = "block";
        if (logoutButton) logoutButton.style.display = "none";
    }
}

function checkWriteStatus() {
    const token = localStorage.getItem("token");
    const writeButton = document.getElementById("writeButton");

    if(token) {
        writeButton.style.display ="block";
    } else {
        writeButton.style.display = "none";
    }

}

// 로그인 폼 동적 생성 및 삽입
function loadLoginForm() {
    noneTag(); // 기존 콘텐츠 숨기기
    console.log("loadLoginForm called!");

    // 기존 로그인 폼이 있으면 중복 생성 방지
    const existingFormContainer = document.querySelector(".login-form-container");
    if (existingFormContainer) {
        console.log("Login form already rendered.");
        return;
    }

    fetch("/loginPage")
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok " + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log("Received data:", data);

            const formContainer = document.createElement("div");
            formContainer.classList.add("login-form-container");

            const form = document.createElement("form");
            form.setAttribute("action", data.formAction);
            form.setAttribute("method", "post");

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
            if (!response.ok) {
                // 로그인 실패 시, 서버에서 보내는 오류 메시지를 처리
                return response.json().then(data => {
                    throw new Error(data.error || "로그인에 실패했습니다.");
                });
            }
            return response.json();
        })
        .then(data => {
            const token = data.token;
            if (token) {
                localStorage.setItem("token", token); // 토큰 저장
                console.log("Token stored successfully:", token);
                checkLoginStatus(); // 로그인 상태 확인 후 버튼 변경
                window.location.href = "/"; // 메인 페이지로 리다이렉트
            } else {
                console.error("No token received.");
            }
        })
        .catch(error => {
            console.error("로그인 요청 중 오류 발생:", error)
            alert("Invalid username or password");
        });
}

// 게시글 목록 로드
function loadPosts() {
    noneTag(); // 기존 콘텐츠 숨기기 (로그인 폼 포함)

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
                boardContainer.innerHTML = html; // 게시글 목록 HTML 삽입
                boardContainer.style.display = "block"; // 보이기
                console.log("Board list loaded successfully.");
            } else {
                console.error("#main-in-the-section element not found.");
            }
        })
        .catch(error => console.error("게시물 요청 실패:", error));
}

// 게시판 페이지네이션 로드
async function fetchBoardPage(page) {
    console.log("Fetching board page:", page);

    noneTag(); // 기존 콘텐츠 숨기기

    try {
        const response = await fetch(`/api/posts/page?page=${page}`);
        if (response.ok) {
            const html = await response.text();
            const boardContainer = document.querySelector("#main-in-the-section");
            if (boardContainer) {
                boardContainer.innerHTML = html;
                console.log(`Page ${page} loaded successfully.`);
            } else {
                console.error("#main-in-the-section element not found.");
            }
        } else {
            console.error(`Failed to load page ${page}, status: ${response.status}`);
        }
    } catch (error) {
        console.error("Error fetching board page:", error);
    }
}

// 기존 콘텐츠 숨기기
function noneTag() {
    const mainContent = document.querySelector("#main-in-the-section");
    const loginFormContainer = document.querySelector(".login-form-container");

    if (mainContent) {
        mainContent.innerHTML = ""; // 게시글 목록 초기화
        mainContent.style.display = "none"; // 숨기기
    }
    if (loginFormContainer) {
        loginFormContainer.remove(); // 로그인 폼 제거
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

// 로그아웃 함수
function logout() {
    // 로컬 스토리지에서 토큰 삭제
    localStorage.removeItem("token");

    // 로그아웃 후 리다이렉트 (선택사항)
    window.location.href = "/"; // 로그인 페이지로 리다이렉트
}
