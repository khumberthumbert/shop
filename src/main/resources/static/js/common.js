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
            const mainContent = document.querySelector("#mainunder");
            if (mainContent) {
                mainContent.style.display = "none"; // 기존 콘텐츠 숨기기
            }

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

            // 폼 컨테이너를 body에 추가
            document.body.appendChild(formContainer);
        })
        .catch(error => {
            console.error("로그인 폼 데이터를 가져오는 중 오류 발생:", error);
        });
}
