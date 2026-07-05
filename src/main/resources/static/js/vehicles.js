let selectedVehicle = null;

async function fetchVehicles(filters = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            params.set(key, value);
        }
    });
    const query = params.toString();
    try {
        const data = await apiFetch('/api/vehicles' + (query ? '?' + query : ''));
        renderVehicles(data.content);
    } catch (e) {
        showMessage('Could not load vehicles: ' + e.message, true);
    }
}

function renderVehicles(vehicles) {
    const list = document.getElementById('vehicle-list');
    if (!list) {
        return;
    }
    list.innerHTML = '';
    if (vehicles.length === 0) {
        list.innerHTML = '<p>No vehicles found.</p>';
        return;
    }
    vehicles.forEach((vehicle) => {
        const card = document.createElement('div');
        card.className = 'vehicle-item';
        card.innerHTML = `
            <h3>${escapeHtml(vehicle.name)}</h3>
            <p>${escapeHtml(vehicle.make)} ${escapeHtml(vehicle.model)} (${vehicle.year})</p>
            <p>${vehicle.mileage.toLocaleString()} km &middot; ${escapeHtml(vehicle.fuelType)}</p>
            <p><strong>$${Number(vehicle.price).toLocaleString()}</strong> &middot; ${vehicle.listingType}</p>
            <p>Sold by ${escapeHtml(vehicle.dealerBusinessName || 'Unknown dealer')}</p>
        `;
        card.addEventListener('click', () => showVehicleDetails(vehicle));
        list.appendChild(card);
    });
}

function escapeHtml(value) {
    const div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
}

function showVehicleDetails(vehicle) {
    selectedVehicle = vehicle;
    const details = document.getElementById('vehicle-details');
    const info = document.getElementById('vehicle-info');
    if (!details || !info) {
        return;
    }
    let text = `${vehicle.name} - ${vehicle.make} ${vehicle.model} (${vehicle.year}), ` +
        `${vehicle.mileage} km, ${vehicle.fuelType}, $${vehicle.price} (${vehicle.listingType})`;
    if (vehicle.features) {
        const f = vehicle.features;
        const extras = [];
        if (f.color) extras.push(f.color);
        if (f.transmission) extras.push(f.transmission);
        if (f.engineCapacity) extras.push(f.engineCapacity);
        if (f.horsepower) extras.push(f.horsepower);
        if (f.seatingCapacity) extras.push(f.seatingCapacity + ' seats');
        if (f.safetyRating) extras.push('Safety: ' + f.safetyRating);
        const badges = [];
        if (f.hasGps) badges.push('GPS');
        if (f.hasBluetooth) badges.push('Bluetooth');
        if (f.hasSunroof) badges.push('Sunroof');
        if (extras.length) text += ' — ' + extras.join(', ');
        if (badges.length) text += ' [' + badges.join(', ') + ']';
    }
    info.textContent = text;
    details.style.display = 'block';
    details.scrollIntoView({behavior: 'smooth'});
}

async function addToCart() {
    if (!requireLoggedInForOrder() || !selectedVehicle) {
        return;
    }
    try {
        await apiFetch('/api/orders', {
            method: 'POST',
            body: JSON.stringify({vehicleId: selectedVehicle.id, type: 'PURCHASE'})
        });
        showMessage('Purchase order placed for ' + selectedVehicle.name, false);
        fetchVehicles();
    } catch (e) {
        showMessage('Could not place order: ' + e.message, true);
    }
}

async function addToRental() {
    if (!requireLoggedInForOrder() || !selectedVehicle) {
        return;
    }
    const start = document.getElementById('rentalStart').value;
    const end = document.getElementById('rentalEnd').value;
    if (!start || !end) {
        showMessage('Choose a rental start and end date first.', true);
        return;
    }
    try {
        await apiFetch('/api/orders', {
            method: 'POST',
            body: JSON.stringify({vehicleId: selectedVehicle.id, type: 'RENTAL', rentalStart: start, rentalEnd: end})
        });
        showMessage('Rental order placed for ' + selectedVehicle.name, false);
        fetchVehicles();
    } catch (e) {
        showMessage('Could not place rental: ' + e.message, true);
    }
}

function requireLoggedInForOrder() {
    if (!isLoggedIn()) {
        showMessage('Please log in as a user to buy or rent a vehicle.', true);
        return false;
    }
    return true;
}
