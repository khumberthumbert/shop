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
                            console.error("토큰이 응답에 없습니다.");
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




