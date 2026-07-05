async function fetchMyOrders() {
    const list = document.getElementById('my-orders');
    if (!list) {
        return;
    }
    try {
        const orders = await apiFetch('/api/orders/me');
        if (orders.length === 0) {
            list.innerHTML = '<p>No orders yet.</p>';
            return;
        }
        list.innerHTML = `
            <table>
                <thead>
                <tr><th>#</th><th>Vehicle</th><th>Type</th><th>Dates</th><th>Total</th><th>Status</th><th></th></tr>
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
                            <td>${o.status === 'FULFILLED' ? `<button onclick="rateOrder(${o.id})">Rate</button>` : ''}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) {
        list.innerHTML = '<p>Could not load orders: ' + e.message + '</p>';
    }
}

async function rateOrder(orderId) {
    const starsValue = promptStars();
    if (!starsValue) {
        return;
    }
    try {
        await apiFetch(`/api/orders/${orderId}/rating`, {
            method: 'POST',
            body: JSON.stringify({stars: starsValue, comment: ''})
        });
        showMessage('Thanks for rating your order!', false);
        fetchMyOrders();
    } catch (e) {
        showMessage('Could not submit rating: ' + e.message, true);
    }
}

function promptStars() {
    const input = window.prompt('Rate this order from 1 to 5 stars:', '5');
    const value = Number(input);
    return value >= 1 && value <= 5 ? value : null;
}
