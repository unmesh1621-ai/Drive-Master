function navigateTo(sectionId) {
    document.querySelectorAll('section').forEach((section) => {
        section.style.display = 'none';
    });
    const target = document.getElementById(sectionId);
    if (target) {
        target.style.display = 'block';
    }
}

function runSearch() {
    fetchVehicles({
        keyword: document.getElementById('searchInput').value
    });
}

document.addEventListener('DOMContentLoaded', () => {
    navigateTo('home');
    fetchVehicles();

    const navAccount = document.getElementById('nav-account');
    if (navAccount) {
        if (isLoggedIn()) {
            const role = currentRole();
            const dashboardHref = role === 'ADMIN' ? '/admin/dashboard.html'
                : role === 'DEALER' ? '/dealer/dashboard.html' : '/index.html#orders';
            navAccount.innerHTML = `<a href="${dashboardHref}">My Account</a> | <a href="#" id="logout-link">Logout</a>`;
            document.getElementById('logout-link').addEventListener('click', (e) => {
                e.preventDefault();
                logout();
            });
        } else {
            navAccount.innerHTML = '<a href="/login.html">Login</a>';
        }
    }

    const ordersLink = document.getElementById('nav-orders');
    if (ordersLink) {
        ordersLink.addEventListener('click', () => fetchMyOrders());
    }
});
