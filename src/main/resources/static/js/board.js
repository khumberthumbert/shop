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

