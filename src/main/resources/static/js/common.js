// js/common.js
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('accessToken');

    // 기본 헤더 설정
    options.headers = {
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };

    return fetch(url, options);
}
