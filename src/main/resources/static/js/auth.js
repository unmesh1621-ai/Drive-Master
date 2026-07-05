async function loginUser(email, password) {
    const data = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({email, password})
    });
    localStorage.setItem('token', data.token);
    localStorage.setItem('role', data.role);
    return data;
}

async function registerUser(name, email, password) {
    return apiFetch('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({name, email, password})
    });
}

async function registerDealer(name, email, password, businessName) {
    return apiFetch('/api/auth/register/dealer', {
        method: 'POST',
        body: JSON.stringify({name, email, password, businessName})
    });
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.location.href = '/index.html';
}

function currentRole() {
    return localStorage.getItem('role');
}

function isLoggedIn() {
    return !!localStorage.getItem('token');
}

function requireRole(role) {
    if (currentRole() !== role) {
        window.location.href = '/login.html';
    }
}

function redirectAfterLogin(role) {
    if (role === 'ADMIN') {
        window.location.href = '/admin/dashboard.html';
    } else if (role === 'DEALER') {
        window.location.href = '/dealer/dashboard.html';
    } else {
        window.location.href = '/index.html';
    }
}
