const API_BASE = '';

async function apiFetch(path, options = {}) {
    const token = localStorage.getItem('token');
    const headers = Object.assign({'Content-Type': 'application/json'}, options.headers || {});
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const response = await fetch(API_BASE + path, Object.assign({}, options, {headers}));

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        const message = (data && data.message) ? data.message : ('Request failed with status ' + response.status);
        throw new Error(message);
    }
    return data;
}

function showMessage(text, isError) {
    const el = document.getElementById('message');
    if (!el) {
        return;
    }
    el.textContent = text;
    el.className = isError ? 'error' : 'success';
    window.setTimeout(() => {
        el.className = '';
        el.textContent = '';
    }, 5000);
}
