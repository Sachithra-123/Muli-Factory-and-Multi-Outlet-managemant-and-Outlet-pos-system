


// --- Navbar Title Sync ---
document.addEventListener('DOMContentLoaded', function () {
    // Grab the page title and sync it with the navbar
    const h2 = document.querySelector('.page-title');
    const navTitle = document.getElementById('pageTitle');
    if (h2 && navTitle) {
        navTitle.textContent = h2.textContent.trim().replace(/[^\w\s&—]/g, '').trim();
    }

    // Dismiss alerts automatically so they don't hang around
    const alerts = document.querySelectorAll('.alert:not(.no-auto-dismiss)');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            if (alert.parentNode) bsAlert.close();
        }, 4000);
    });

    // Simple confirmation for delete actions
    document.querySelectorAll('[data-confirm]').forEach(el => {
        el.addEventListener('click', function (e) {
            if (!confirm(this.dataset.confirm || 'Are you sure?')) {
                e.preventDefault();
            }
        });
    });

    // Close the mobile sidebar if someone clicks outside it
    document.addEventListener('click', function (e) {
        const sidebar = document.querySelector('.sidebar');
        const toggle = document.getElementById('sidebarToggle');
        if (window.innerWidth <= 768 && sidebar && sidebar.classList.contains('open')) {
            if (!sidebar.contains(e.target) && !toggle.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        }
    });
});

// Sidebar toggle logic for mobile and desktop
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (window.innerWidth <= 768) {
        sidebar.classList.toggle('open');
    } else {
        // Desktop handles collapsing differently
        sidebar.classList.toggle('collapsed');
        document.querySelector('.main-content').classList.toggle('expanded');
    }
}
