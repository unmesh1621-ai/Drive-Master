async function loadDealerProfile() {
    const el = document.getElementById('dealer-profile');
    if (!el) {
        return;
    }
    try {
        const profile = await apiFetch('/api/dealer/profile');
        const rating = profile.averageRating > 0 ? ` ★ ${profile.averageRating.toFixed(1)}/5` : '';
        el.textContent = `${profile.businessName} - ${profile.approved ? 'Approved' : 'Pending admin approval'}${rating}`;
    } catch (e) {
        el.textContent = 'Could not load profile: ' + e.message;
    }
}

async function loadMyVehicles() {
    const container = document.getElementById('dealer-vehicles');
    if (!container) {
        return;
    }
    try {
        const vehicles = await apiFetch('/api/dealer/vehicles');
        if (vehicles.length === 0) {
            container.innerHTML = '<p>No listings yet. Add your first vehicle above.</p>';
            return;
        }
        container.innerHTML = `
            <table>
                <thead>
                <tr><th>Name</th><th>Make/Model</th><th>Year</th><th>Price</th><th>Type</th><th>Status</th><th></th></tr>
                </thead>
                <tbody>
                    ${vehicles.map((v) => `
                        <tr>
                            <td>${v.name}</td>
                            <td>${v.make} ${v.model}</td>
                            <td>${v.year}</td>
                            <td>$${Number(v.price).toLocaleString()}</td>
                            <td>${v.listingType}</td>
                            <td>${v.status}</td>
                            <td>${v.status === 'ACTIVE' ? `<button class="danger" onclick="deleteVehicle(${v.id})">Remove</button>` : ''}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) {
        container.innerHTML = '<p>Could not load listings: ' + e.message + '</p>';
    }
}

async function addVehicle(event) {
    event.preventDefault();
    const form = event.target;
    try {
        await apiFetch('/api/vehicles', {
            method: 'POST',
            body: JSON.stringify({
                name: form.name.value,
                make: form.make.value,
                model: form.model.value,
                year: Number(form.year.value),
                mileage: Number(form.mileage.value),
                fuelType: form.fuelType.value,
                price: Number(form.price.value),
                listingType: form.listingType.value,
                features: {
                    color: form.color.value || null,
                    engineCapacity: form.engineCapacity.value || null,
                    transmission: form.transmission.value || null,
                    horsepower: form.horsepower.value || null,
                    seatingCapacity: form.seatingCapacity.value ? Number(form.seatingCapacity.value) : null,
                    safetyRating: form.safetyRating.value || null,
                    hasGps: form.hasGps.checked,
                    hasBluetooth: form.hasBluetooth.checked,
                    hasSunroof: form.hasSunroof.checked
                }
            })
        });
        showMessage('Vehicle listed successfully.', false);
        form.reset();
        loadMyVehicles();
    } catch (e) {
        showMessage('Could not list vehicle: ' + e.message, true);
    }
}

async function deleteVehicle(id) {
    try {
        await apiFetch('/api/vehicles/' + id, {method: 'DELETE'});
        showMessage('Vehicle removed.', false);
        loadMyVehicles();
    } catch (e) {
        showMessage('Could not remove vehicle: ' + e.message, true);
    }
}

async function loadDealerOrders() {
    const container = document.getElementById('dealer-orders');
    if (!container) {
        return;
    }
    try {
        const orders = await apiFetch('/api/dealer/orders');
        if (orders.length === 0) {
            container.innerHTML = '<p>No orders yet.</p>';
            return;
        }
        container.innerHTML = `
            <table>
                <thead>
                <tr><th>#</th><th>Vehicle</th><th>Type</th><th>Dates</th><th>Total</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                    ${orders.map((o) => `
                        <tr>
                            <td>${o.id}</td>
                            <td>${o.vehicleName || o.vehicleId}</td>
                            <td>${o.type}</td>
                            <td>${o.rentalStart ? o.rentalStart + ' to ' + o.rentalEnd : '-'}</td>
                            <td>$${Number(o.totalPrice).toLocaleString()}</td>
                            <td>${o.status}</td>
                            <td>${dealerOrderActions(o)}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) {
        container.innerHTML = '<p>Could not load orders: ' + e.message + '</p>';
    }
}

function dealerOrderActions(order) {
    if (order.status === 'PENDING') {
        return `<button onclick="transitionOrder(${order.id}, 'confirm')">Confirm</button>
                <button class="danger" onclick="transitionOrder(${order.id}, 'reject')">Reject</button>`;
    }
    if (order.status === 'CONFIRMED') {
        return `<button onclick="transitionOrder(${order.id}, 'fulfill')">Mark Fulfilled</button>`;
    }
    return '';
}

async function transitionOrder(orderId, action) {
    try {
        await apiFetch(`/api/dealer/orders/${orderId}/${action}`, {method: 'PATCH'});
        showMessage('Order updated.', false);
        loadDealerOrders();
    } catch (e) {
        showMessage('Could not update order: ' + e.message, true);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('dealer-vehicles')) {
        requireRole('DEALER');
        loadDealerProfile();
        loadMyVehicles();
        loadDealerOrders();
        const form = document.getElementById('add-vehicle-form');
        if (form) {
            form.addEventListener('submit', addVehicle);
        }
        const logoutLink = document.getElementById('logout-link');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                logout();
            });
        }
    }
});
