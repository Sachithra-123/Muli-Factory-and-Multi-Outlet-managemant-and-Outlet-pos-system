

let cart = [];
let searchTimeout = null;

// Handle product search with a small delay to avoid spamming the server
document.getElementById('productSearch').addEventListener('input', function () {
    clearTimeout(searchTimeout);
    const q = this.value.trim();
    if (q.length < 2) {
        document.getElementById('searchResults').style.display = 'none';
        return;
    }
    searchTimeout = setTimeout(() => searchProducts(q), 300);
});

function searchProducts(q) {
    fetch(`/api/products/search?q=${encodeURIComponent(q)}`)
        .then(res => res.json())
        .then(products => {
            renderSearchResults(products);
            renderProductGrid(products);
        })
        .catch(err => console.error('Search error:', err));
}

function renderSearchResults(products) {
    const container = document.getElementById('searchResults');
    if (products.length === 0) {
        container.innerHTML = '<div class="p-3 text-muted text-center">No products found</div>';
        container.style.display = 'block';
        return;
    }
    container.innerHTML = products.map(p => `
        <div class="search-result-item d-flex justify-content-between align-items-center"
             onclick="addToCart(${p.id}, '${escapeHtml(p.name)}', ${p.unitPrice})">
            <div class="d-flex align-items-center gap-2">
                <img src="${p.imageUrl || 'https://placehold.co/40x40?text=' + encodeURIComponent(p.name)}" 
                     class="rounded border" style="width:40px; height:40px; object-fit:cover;">
                <div>
                    <strong>${escapeHtml(p.name)}</strong>
                    <small class="text-muted d-block">${escapeHtml(p.category || '')}</small>
                </div>
            </div>
            <div class="text-end">
                <div class="fw-bold text-success">LKR ${formatPrice(p.unitPrice)}</div>
            </div>
        </div>
    `).join('');
    container.style.display = 'block';
}

function renderProductGrid(products) {
    const grid = document.getElementById('productGrid');
    if (products.length === 0) {
        grid.innerHTML = '<div class="col-12 text-muted text-center py-4">No products found</div>';
        return;
    }
    grid.innerHTML = products.map(p => `
        <div class="col-sm-6 col-md-4 col-lg-2">
            <div class="card-box prod-card p-2 text-center" onclick="addToCart(${p.id}, '${escapeHtml(p.name)}', ${p.unitPrice})">
                <div class="mb-2 overflow-hidden rounded" style="height:100px; background:#f1f5f9;">
                    <img src="${p.imageUrl || 'https://placehold.co/200x200/e2e8f0/475569?text=' + encodeURIComponent(p.name)}" 
                         class="w-100 h-100" style="object-fit:cover; transition: transform 0.3s ease;">
                </div>
                <div class="fw-bold extra-small text-truncate text-dark mb-0">${escapeHtml(p.name)}</div>
                <div class="text-muted" style="font-size:0.6rem;">${escapeHtml(p.category || 'General')}</div>
                <div class="text-emerald fw-bold small">LKR ${formatPrice(p.unitPrice)}</div>
            </div>
        </div>
    `).join('');
}

// Hide findings when user clicks away
document.addEventListener('click', function (e) {
    if (!e.target.closest('#productSearch') && !e.target.closest('#searchResults')) {
        document.getElementById('searchResults').style.display = 'none';
    }
});

// --- Managing the Cart ---
function addToCart(productId, productName, unitPrice) {
    document.getElementById('searchResults').style.display = 'none';
    document.getElementById('productSearch').value = '';

    const existing = cart.find(i => i.productId === productId);
    if (existing) {
        existing.quantity++;
        existing.subtotal = existing.quantity * existing.unitPrice;
    } else {
        cart.push({
            productId,
            productName,
            unitPrice: parseFloat(unitPrice),
            quantity: 1,
            subtotal: parseFloat(unitPrice)
        });
    }
    renderCart();
}

function updateQty(productId, delta) {
    const item = cart.find(i => i.productId === productId);
    if (!item) return;
    item.quantity += delta;
    if (item.quantity <= 0) {
        removeFromCart(productId);
        return;
    }
    item.subtotal = item.quantity * item.unitPrice;
    renderCart();
}

function removeFromCart(productId) {
    cart = cart.filter(i => i.productId !== productId);
    renderCart();
}

function clearCart() {
    cart = [];
    renderCart();
    document.getElementById('productGrid').innerHTML =
        '<div class="col-12 text-muted text-center py-4"><i class="bi bi-search fs-2 d-block mb-2"></i>Search for products above to get started</div>';
}

function renderCart() {
    const container = document.getElementById('cartItems');
    const emptyCart = document.getElementById('emptyCart');

    if (cart.length === 0) {
        container.innerHTML = `
            <div class="empty-cart" id="emptyCart">
                <i class="bi bi-cart-x fs-1 d-block mb-3"></i>Cart is empty
            </div>`;
        document.getElementById('cartTotal').textContent = '0.00';
        document.getElementById('cartCount').textContent = '0 items';
        return;
    }

    const total = cart.reduce((sum, i) => sum + i.subtotal, 0);
    const count = cart.reduce((sum, i) => sum + i.quantity, 0);

    container.innerHTML = cart.map(item => `
        <div class="cart-item">
            <div class="d-flex justify-content-between align-items-start mb-1">
                <span class="fw-semibold small">${escapeHtml(item.productName)}</span>
                <button class="btn-close btn-sm" onclick="removeFromCart(${item.productId})" style="font-size:0.6rem;"></button>
            </div>
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <button class="qty-btn" onclick="updateQty(${item.productId}, -1)">−</button>
                    <span class="fw-bold">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateQty(${item.productId}, 1)">+</button>
                </div>
                <div class="text-end">
                    <div class="text-muted small">@ LKR ${formatPrice(item.unitPrice)}</div>
                    <div class="fw-bold text-success">LKR ${formatPrice(item.subtotal)}</div>
                </div>
            </div>
        </div>
    `).join('');

    document.getElementById('cartTotal').textContent = formatPrice(total);
    document.getElementById('cartCount').textContent = `${count} item${count !== 1 ? 's' : ''}`;
    updateChange();
}

// Keep track of the change needed for cash payments
document.getElementById('cashReceived').addEventListener('input', updateChange);

function updateChange() {
    const total = cart.reduce((sum, i) => sum + i.subtotal, 0);
    const cash = parseFloat(document.getElementById('cashReceived').value) || 0;
    const change = cash - total;
    document.getElementById('changeAmount').textContent =
        `LKR ${change >= 0 ? formatPrice(change) : '0.00'}`;
}

// Finally, complete the sale and send it to the server
function completeSale() {
    const outletId = document.getElementById('outletSelect').value;
    const paymentMethod = document.getElementById('paymentMethod').value;

    if (!outletId) {
        showAlert('Please select an outlet first!', 'warning');
        return;
    }
    if (cart.length === 0) {
        showAlert('Cart is empty! Add products before completing the sale.', 'warning');
        return;
    }

    const total = cart.reduce((sum, i) => sum + i.subtotal, 0);
    const csrfToken = document.getElementById('csrfToken').value;
    const csrfHeader = document.getElementById('csrfToken').getAttribute('data-header') || 'X-CSRF-TOKEN';

    const payload = {
        outletId: parseInt(outletId),
        paymentMethod,
        totalAmount: total,
        items: cart.map(i => ({
            productId: i.productId,
            productName: i.productName,
            quantity: i.quantity,
            unitPrice: i.unitPrice,
            subtotal: i.subtotal
        }))
    };

    const btn = document.getElementById('completeSaleBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';

    fetch('/pos/complete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            window.location.href = `/pos/bill/${data.saleId}`;
        } else {
            showAlert('Error: ' + data.message, 'danger');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check-circle me-2"></i>Complete Sale';
        }
    })
    .catch(err => {
        showAlert('Network error. Please try again.', 'danger');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-check-circle me-2"></i>Complete Sale';
    });
}

// --- Helpers ---
function formatPrice(amount) {
    return parseFloat(amount).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function showAlert(message, type) {
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3`;
    alert.style.zIndex = '9999';
    alert.style.minWidth = '300px';
    alert.innerHTML = `${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    document.body.appendChild(alert);
    setTimeout(() => alert.remove(), 4000);
}
