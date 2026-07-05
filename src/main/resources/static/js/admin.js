async function loadStats() {
    const container = document.getElementById('admin-stats');
    if (!container) {
        return;
    }
    try {
        const stats = await apiFetch('/api/admin/stats');
        container.innerHTML = `
            <div class="stat-tile"><div class="value">${stats.totalUsers}</div><div class="label">Users</div></div>
            <div class="stat-tile"><div class="value">${stats.totalDealers}</div><div class="label">Dealers</div></div>
            <div class="stat-tile"><div class="value">${stats.approvedDealers}</div><div class="label">Approved Dealers</div></div>
            <div class="stat-tile"><div class="value">${stats.activeVehicles}</div><div class="label">Active Listings</div></div>
            <div class="stat-tile"><div class="value">${stats.totalOrders}</div><div class="label">Total Orders</div></div>
            <div class="stat-tile"><div class="value">${stats.pendingOrders}</div><div class="label">Pending Orders</div></div>
        `;
    } catch (e) {
        container.innerHTML = '<p>Could not load stats: ' + e.message + '</p>';
    }
}

async function loadPendingDealers() {
    const container = document.getElementById('pending-dealers');
    if (!container) {
        return;
    }
    try {
        const dealers = await apiFetch('/api/admin/dealers/pending');
        if (dealers.length === 0) {
            container.innerHTML = '<p>No pending dealer applications.</p>';
            return;
        }
        container.innerHTML = `
            <table>
                <thead><tr><th>User ID</th><th>Business Name</th><th>Actions</th></tr></thead>
                <tbody>
                    ${dealers.map((d) => `
                        <tr>
                            <td>${d.userId}</td>
                            <td>${d.businessName}</td>
                            <td>
                                <button onclick="approveDealer(${d.userId})">Approve</button>
                                <button class="danger" onclick="rejectDealer(${d.userId})">Reject</button>
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) {
        container.innerHTML = '<p>Could not load pending dealers: ' + e.message + '</p>';
    }
}

async function approveDealer(dealerProfileId) {
    try {
        await apiFetch(`/api/admin/dealers/${dealerProfileId}/approve`, {method: 'POST'});
        showMessage('Dealer approved.', false);
        loadPendingDealers();
        loadStats();
    } catch (e) {
        showMessage('Could not approve dealer: ' + e.message, true);
    }
}

async function rejectDealer(dealerProfileId) {
    try {
        await apiFetch(`/api/admin/dealers/${dealerProfileId}/reject`, {method: 'POST'});
        showMessage('Dealer rejected.', false);
        loadPendingDealers();
    } catch (e) {
        showMessage('Could not reject dealer: ' + e.message, true);
    }
}

async function loadUsers() {
    const container = document.getElementById('admin-users');
    if (!container) {
        return;
    }
    try {
        const page = await apiFetch('/api/admin/users?page=0&size=50');
        container.innerHTML = `
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th></th></tr></thead>
                <tbody>
                    ${page.content.map((u) => `
                        <tr>
                            <td>${u.id}</td>
                            <td>${u.name}</td>
                            <td>${u.email}</td>
                            <td>${u.role}</td>
                            <td>${u.status}</td>
                            <td>${u.status !== 'DISABLED' ? `<button class="danger" onclick="disableUser(${u.id})">Disable</button>` : ''}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) {
        container.innerHTML = '<p>Could not load users: ' + e.message + '</p>';
    }
}

async function disableUser(userId) {
    try {
        await apiFetch(`/api/admin/users/${userId}/disable`, {method: 'PATCH'});
        showMessage('User disabled.', false);
        loadUsers();
    } catch (e) {
        showMessage('Could not disable user: ' + e.message, true);
    }
}

async function loadAdminVehicles() {
    const container = document.getElementById('admin-vehicles');
    if (!container) {
        return;
    }
    try {
        const page = await apiFetch('/api/admin/vehicles?page=0&size=50');
        container.innerHTML = `
            <table>
                <thead><tr><th>Name</th><th>Dealer</th><th>Price</th><th>Status</th><th></th></tr></thead>
                <tbody>
                    ${page.content.map((v) => `
                        <tr>
                            <td>${v.name}</td>
                            <td>${v.dealerBusinessName || v.dealerId}</td>
                            <td>$${Number(v.price).toLocaleString()}</td>
                            <td>${v.status}</td>
                            <td>${v.status === 'ACTIVE' ? `<button class="danger" onclick="removeVehicle(${v.id})">Remove</button>` : ''}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) {
        container.innerHTML = '<p>Could not load vehicles: ' + e.message + '</p>';
    }
}

async function removeVehicle(id) {
    try {
        await apiFetch('/api/admin/vehicles/' + id, {method: 'DELETE'});
        showMessage('Vehicle removed.', false);
        loadAdminVehicles();
        loadStats();
    } catch (e) {
        showMessage('Could not remove vehicle: ' + e.message, true);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('admin-stats')) {
        requireRole('ADMIN');
        loadStats();
        loadPendingDealers();
        loadUsers();
        loadAdminVehicles();
        const logoutLink = document.getElementById('logout-link');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                logout();
            });
        }
    }
});
