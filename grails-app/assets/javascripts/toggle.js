// toggle.js
var ModeToggle = {
    COOKIE_NAME: 'monitoring_mode',
    COOKIE_EXPIRES_DAYS: 30,

    initialize: function() {
        this.toggle = document.querySelector('.mode-toggle');
        if (!this.toggle) return; // Protection si l'élément n'existe pas

        this.slider = this.toggle.querySelector('.mode-slider');
        this.options = this.toggle.querySelectorAll('.mode-option');

        this.currentMode = this.determineInitialMode();
        this.initializeMode();
        this.setupWebSocketListener();
    },

    determineInitialMode: function() {
        // Vérifier d'abord le cookie
        var savedMode = this.getCookie(this.COOKIE_NAME);
        if (savedMode) {
            var option = this.toggle.querySelector('.mode-option[data-mode="' + savedMode + '"]');
            if (option && !option.classList.contains('disabled-mode')) {
                return savedMode;
            }
        }

        // Vérifier ensuite le mode server
        var serverOption = this.toggle.querySelector('.mode-option[data-mode="server"]');
        if (serverOption && !serverOption.classList.contains('disabled-mode')) {
            return 'server';
        }

        // Par défaut, retourner autonomous
        return 'autonomous';
    },

    setupWebSocketListener: function() {
        var self = this;
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.WEBSOCKET_STATUS, function(event) {
            var wsOption = self.toggle.querySelector('.mode-option[data-mode="websocket"]');
            if (wsOption) {
                if (event.detail.available) {
                    wsOption.classList.remove('disabled-mode');
                    wsOption.title = 'WebSocket Mode';
                } else {
                    wsOption.classList.add('disabled-mode');
                    wsOption.title = 'WebSocket Not Available';
                    if (self.currentMode === 'websocket') {
                        var newMode = self.determineFirstAvailableMode();
                        self.switchMode(newMode);
                    }
                }
            }
        });
    },

    initializeMode: function() {
        var self = this;
        for (var i = 0; i < this.options.length; i++) {
            var option = this.options[i];

            if (option.classList.contains('disabled-mode')) {
                var modeName = option.querySelector('span:not(.icon)').textContent;
                option.title = modeName + ' Not Available';
            } else {
                option.addEventListener('click', function(e) {
                    var mode = e.currentTarget.getAttribute('data-mode');
                    self.switchMode(mode);
                });
            }
        }

        this.updateSlider(this.currentMode);
        document.body.setAttribute('data-current-mode', this.currentMode);
    },

    switchMode: function(newMode) {
        var self = this;
        if (this.currentMode === newMode) return;

        var targetOption = this.toggle.querySelector('.mode-option[data-mode="' + newMode + '"]');
        if (targetOption && targetOption.classList.contains('disabled-mode')) {
            console.warn('Attempted to switch to disabled mode:', newMode);
            return;
        }

        return fetch('/monitoring/setMode?mode=' + encodeURIComponent(newMode), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(function(response) {
                return response.json();
            })
            .then(function(result) {
                if (!result.success) {
                    throw new Error(result.error || 'Mode switch failed');
                }

                self.setCookie(self.COOKIE_NAME, newMode, self.COOKIE_EXPIRES_DAYS);
                self.updateSlider(newMode);
                self.currentMode = newMode;
                document.body.setAttribute('data-current-mode', newMode);

                MonitoringEventBus.emit(MonitoringEventBus.EventTypes.MODE_CHANGED, {
                    mode: newMode
                });
            })
            .catch(function(error) {
                console.error('Error switching mode:', error);
                self.updateSlider(self.currentMode);
            });
    },

    determineFirstAvailableMode: function() {
        var availableOptions = [];
        for (var i = 0; i < this.options.length; i++) {
            var option = this.options[i];
            if (!option.classList.contains('disabled-mode')) {
                availableOptions.push(option);
            }
        }
        return availableOptions.length > 0 ?
            availableOptions[0].getAttribute('data-mode') : 'autonomous';
    },

    updateSlider: function(mode) {
        if (!this.slider) return;

        this.slider.className = 'mode-slider ' + mode;
        for (var i = 0; i < this.options.length; i++) {
            var option = this.options[i];
            if (option.getAttribute('data-mode') === mode) {
                option.classList.add('active');
            } else {
                option.classList.remove('active');
            }
        }
    },

    setCookie: function(name, value, days) {
        var expires = new Date();
        expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
        document.cookie = name + '=' + value + ';expires=' + expires.toUTCString() + ';path=/;SameSite=Lax';
    },

    getCookie: function(name) {
        var nameEQ = name + '=';
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    }
};

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    ModeToggle.initialize();
});

// Exposer globalement
window.ModeToggle = ModeToggle;