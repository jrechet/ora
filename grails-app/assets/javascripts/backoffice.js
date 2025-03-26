// JavaScript pour le backoffice

// Initialisation du document
document.addEventListener('DOMContentLoaded', function() {
    initToggles();
    initNotifications();
});

// Initialisation des interrupteurs de toggle
function initToggles() {
    const toggles = document.querySelectorAll('.toggle-switch input[type="checkbox"]');
    
    toggles.forEach(function(toggle) {
        toggle.addEventListener('change', function() {
            const label = this.nextElementSibling;
            if (this.checked) {
                label.textContent = this.name === 'setActive' ? 'Configuration active' : 'Activé';
            } else {
                label.textContent = this.name === 'setActive' ? 'Configuration inactive' : 'Désactivé';
            }
        });
    });
}

// Initialisation du système de notifications
function initNotifications() {
    // Masquer les messages flash après 5 secondes
    const flashMessages = document.querySelectorAll('.alert');
    
    flashMessages.forEach(function(message) {
        setTimeout(function() {
            message.style.opacity = '0';
            setTimeout(function() {
                message.style.display = 'none';
            }, 500);
        }, 5000);
    });
}

// Fonction pour afficher une notification
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type}`;
    notification.textContent = message;
    
    const mainContent = document.querySelector('.main-content');
    mainContent.insertBefore(notification, mainContent.firstChild);
    
    setTimeout(function() {
        notification.style.opacity = '0';
        setTimeout(function() {
            notification.remove();
        }, 500);
    }, 5000);
}

// Fonction pour confirmer une action
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}
