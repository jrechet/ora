// ui-manager.js
const UIManager = {
    initialize: function() {
        this.setupNavbarBurger();
        this.setupNotifications();
    },

    setupNavbarBurger: function() {
        const navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);

        navbarBurgers.forEach(function(burger) {
            burger.addEventListener('click', function() {
                const targetId = burger.dataset.target;
                const targetMenu = document.getElementById(targetId);

                if (!targetMenu) return;

                burger.classList.toggle('is-active');
                targetMenu.classList.toggle('is-active');
            });
        });
    },

    setupNotifications: function() {
        const deleteButtons = document.querySelectorAll('.notification .delete');

        deleteButtons.forEach(function(deleteButton) {
            const notification = deleteButton.parentNode;

            deleteButton.addEventListener('click', function() {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            });
        });
    },

    // Méthode pour ajouter une notification dynamiquement
    addNotification: function(message, type) {
        type = type || 'info';

        const notification = document.createElement('div');
        notification.className = 'notification is-' + type + ' is-light';

        const deleteButton = document.createElement('button');
        deleteButton.className = 'delete';
        deleteButton.addEventListener('click', function() {
            notification.remove();
        });

        notification.appendChild(deleteButton);
        notification.appendChild(document.createTextNode(message));

        const container = document.querySelector('.container.is-fluid');
        if (container) {
            container.insertBefore(notification, container.firstChild);
        }
    }
};

document.addEventListener('DOMContentLoaded', function() {
    UIManager.initialize();
});

window.UIManager = UIManager;