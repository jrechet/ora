// refresh.js
var RefreshManager = {
    config: {},

    state: {
        lastSuccessfulResponse: Date.now(),
        refreshCounter: 0,
        problemsMap: new Map(),
        currentMode: 'server',
        totalCards: 0,
        isAutonomousMode: false
    },

    initialize: function () {
        // Configuration initialisée ici, une fois que document.body est disponible
        this.config = {
            REFRESH_INTERVAL: parseInt(document.body.dataset.refreshInterval) || 30000,
            MAX_TIME_WITHOUT_RESPONSE: parseInt(document.body.dataset.refreshInterval) * 2 || 60000,
            STATUS_CHECK_TIMEOUT: 5000
        };

        var self = this;
        this.state.totalCards = document.querySelectorAll('.card:not(#tab-hotspot .card)').length;
        this.state.isAutonomousMode = document.body.getAttribute('data-initial-mode') === 'autonomous';

        this.setupEventListeners();
        this.setupPeriodicChecks();
        this.setupInitialRefresh();
        this.setupTabsHandling();
    },
    setupEventListeners: function () {
        var self = this;

        // Mode changed event
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.MODE_CHANGED, function (event) {
            self.state.isAutonomousMode = event.detail.mode === 'autonomous';
            self.state.currentMode = event.detail.mode;
            console.log('Mode switched to:', event.detail.mode);
        });

        // Refresh button
        var refreshAllButton = document.getElementById('refreshAll');
        if (refreshAllButton) {
            refreshAllButton.addEventListener('click', function (e) {
                self.handleRefreshAll(e);
            });
        }
        // Dans la méthode setupEventListeners, ajoutez :
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, function (event) {
            self.handleProblemDetected(event.detail);
        });

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, function (event) {
            self.handleRefreshCompleted();
        });
    },
    setupPeriodicChecks: function () {
        var self = this;
        setInterval(function () {
            self.checkServerConnection();
        }, this.config.REFRESH_INTERVAL);

        // Refresh périodique des cartes
        var cards = document.querySelectorAll('.card:not(#tab-hotspot .card)');
        for (var i = 0; i < cards.length; i++) {
            var card = cards[i];
            var envId = card.dataset.envId;
            if (!envId) continue;

            this.refreshEnvironment(envId);

            (function (id) {
                setInterval(function () {
                    self.refreshEnvironment(id);
                }, self.config.REFRESH_INTERVAL);
            })(envId);
        }
    },

    setupInitialRefresh: function () {
        var cards = document.querySelectorAll('.card:not(#tab-hotspot .card)');
        for (var i = 0; i < cards.length; i++) {
            var card = cards[i];
            var envId = card.dataset.envId;
            if (!envId) continue;

            this.refreshEnvironment(envId);
        }
    },

    setupTabsHandling: function () {
        var tabs = document.querySelectorAll('.tabs li');
        var tabContents = document.querySelectorAll('.tab-content');

        for (var i = 0; i < tabs.length; i++) {
            var tab = tabs[i];
            tab.addEventListener('click', function () {
                for (var j = 0; j < tabs.length; j++) {
                    tabs[j].classList.remove('is-active');
                }
                for (var k = 0; k < tabContents.length; k++) {
                    tabContents[k].classList.remove('is-active');
                }
                this.classList.add('is-active');
                var target = this.querySelector('a').getAttribute('href');
                document.querySelector(target).classList.add('is-active');
            });
        }
    },

    refreshEnvironment: function (envId) {
        var self = this;

        return new Promise(function (resolve, reject) {
            var cardSelector = '[data-env-id="' + envId + '"]';
            var card = document.querySelector(cardSelector);
            if (!card) {
                resolve();
                return;
            }

            self.fetchApplicationStatus(envId, card)
                .then(function (applicationsStatus) {
                    self.state.lastSuccessfulResponse = Date.now();

                    var alertElement = document.getElementById('server-alert');
                    if (alertElement) {
                        alertElement.classList.add('is-hidden');
                    }

                    for (var i = 0; i < applicationsStatus.length; i++) {
                        var app = applicationsStatus[i];
                        var appRow = self.findApplicationRow(card, app.name);
                        if (appRow) {
                            self.updateApplicationRow(appRow, app);
                        }

                        var hasProblems = !app.status ||
                            (app.supervisionReport &&
                                app.supervisionReport.failedServices &&
                                app.supervisionReport.failedServices.length > 0);

                        if (hasProblems) {
                            MonitoringEventBus.emit(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, {
                                app: app,
                                envId: envId,
                                environmentLevel: card.dataset.envLevel
                            });
                        }
                    }

                    MonitoringEventBus.emit(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, {
                        envId: envId
                    });

                    resolve();
                })
                .catch(function (error) {
                    console.error('Error refreshing environment ' + envId + ':', error);
                    reject(error);
                });
        });
    },

    fetchApplicationStatus: function (envId, card) {
        var self = this;

        if (this.state.isAutonomousMode) {
            return new Promise(function (resolve, reject) {
                var rows = card.querySelectorAll('tr');
                var promises = [];

                for (var i = 0; i < rows.length; i++) {
                    var row = rows[i];
                    var nameSpan = row.querySelector('span.has-text-weight-medium');
                    if (!nameSpan) continue;

                    promises.push(self.checkApplicationStatus({
                        name: nameSpan.textContent.trim(),
                        healthUrl: row.querySelector('td[id^="health"] a')?.href,
                        supervisionUrl: row.querySelector('td[id^="supervision"] a')?.href,
                        gitlabProjectId: row.querySelector('td[id^="pipeline-tests"]')?.dataset?.gitlabProject
                    }));
                }

                Promise.all(promises)
                    .then(function (results) {
                        resolve(results.filter(Boolean));
                    })
                    .catch(reject);
            });
        } else {
            return fetch('/monitoring/status?envId=' + encodeURIComponent(envId))
                .then(function (response) {
                    return response.json();
                })
                .then(function (data) {
                    return data.applicationsStatus;
                });
        }
    },

    checkApplicationStatus: function (app) {
        var self = this;

        return new Promise(function (resolve) {
            var status = false;
            var supervisionReport = {success: false, failedServices: []};
            var gitlabStatus = null;

            var promises = [];

            // Health check
            if (app.healthUrl) {
                promises.push(
                    self.httpGet(app.healthUrl)
                        .then(function (health) {
                            status = health && health.status === "OK";
                        })
                        .catch(function () {
                            status = false;
                        })
                );
            }

            // Supervision check
            if (app.supervisionUrl) {
                promises.push(
                    self.httpGet(app.supervisionUrl)
                        .then(function (supervisionHtml) {
                            supervisionReport = self.parseSupervisionHtml(supervisionHtml);
                        })
                        .catch(function () {
                            supervisionReport = {success: false, failedServices: []};
                        })
                );
            }

            // GitLab status check
            if (app.gitlabProjectId) {
                promises.push(
                    fetch('/monitoring/gitlabStatus?id=' + encodeURIComponent(app.gitlabProjectId))
                        .then(function (response) {
                            return response.json();
                        })
                        .then(function (status) {
                            gitlabStatus = status;
                        })
                        .catch(function () {
                            gitlabStatus = null;
                        })
                );
            }

            Promise.all(promises)
                .then(function () {
                    var result = {
                        name: app.name,
                        status: status,
                        supervisionReport: supervisionReport,
                        lastChecked: new Date().toLocaleString()
                    };

                    if (gitlabStatus) {
                        result.testJobs = gitlabStatus.jobs || [];
                        result.pipelineUrl = gitlabStatus.pipelineUrl;
                    }

                    resolve(result);
                })
                .catch(function () {
                    resolve({
                        name: app.name,
                        status: false,
                        supervisionReport: {success: false, failedServices: []},
                        lastChecked: new Date().toLocaleString()
                    });
                });
        });
    },

    handleRefreshAll: function (e) {
        var self = this;
        e.preventDefault();

        var icon = e.currentTarget.querySelector('.icon');
        if (icon) {
            icon.classList.add('is-rotating');
        }

        if (!this.state.isAutonomousMode) {
            fetch('/monitoring/synchronize')
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error('Server refresh failed');
                    }
                })
                .catch(function (error) {
                    console.warn('Server refresh failed:', error);
                });
        }

        var cards = document.querySelectorAll('.card:not(#tab-hotspot .card)');
        var promises = [];

        for (var i = 0; i < cards.length; i++) {
            var card = cards[i];
            var envId = card.dataset.envId;
            if (!envId) continue;

            promises.push(this.refreshEnvironment(envId));
        }

        Promise.all(promises)
            .catch(function (error) {
                console.error('Refresh all failed:', error);
            })
            .finally(function () {
                if (icon) {
                    setTimeout(function () {
                        icon.classList.remove('is-rotating');
                    }, 1000);
                }
            });
    },


    checkServerConnection: function () {
        var timeSinceLastResponse = Date.now() - this.state.lastSuccessfulResponse;
        var alertElement = document.getElementById('server-alert');
        if (timeSinceLastResponse > this.config.MAX_TIME_WITHOUT_RESPONSE && alertElement) {
            alertElement.classList.remove('is-hidden');
        }
    },

    findApplicationRow: function (card, appName) {
        var rows = card.querySelectorAll('tr');
        for (var i = 0; i < rows.length; i++) {
            var row = rows[i];
            var nameSpan = row.querySelector('span.has-text-weight-medium');
            if (nameSpan && nameSpan.textContent.trim() === appName) {
                return row;
            }
        }
        return null;
    },

    updateApplicationRow: function (row, appData) {
        // Health status
        var healthCell = row.querySelector('td#health-' + appData.name);
        if (healthCell) {
            var healthTag = healthCell.querySelector('.tag');
            if (healthTag) {
                healthTag.className = 'tag ' + (appData.status ? 'is-success' : 'is-danger');
                healthTag.textContent = appData.status ? 'UP' : 'DOWN';
            }
        }

        // Supervision status
        var supervisionCell = row.querySelector('td#supervision-' + appData.name);
        if (supervisionCell) {
            var supervisionTag = supervisionCell.querySelector('.tag');
            if (supervisionTag) {
                var hasFailures = appData.supervisionReport &&
                    appData.supervisionReport.failedServices &&
                    appData.supervisionReport.failedServices.length > 0;
                supervisionTag.className = 'tag ' +
                    (appData.supervisionReport && appData.supervisionReport.success ? 'is-success' : 'is-warning') +
                    (hasFailures ? ' has-tooltip' : '');
                if (hasFailures) {
                    supervisionTag.dataset.tooltip = appData.supervisionReport.failedServices.join('\n');
                }
            }
        }

        // Test pipeline status
        var testsCell = row.querySelector('td#pipeline-tests-' + appData.name);
        if (testsCell) {
            var testsBadge = testsCell.querySelector('.pipeline-status-badge');
            if (testsBadge) {
                var allTestsSuccess = false;
                if (appData.testJobs && appData.testJobs.length > 0) {
                    allTestsSuccess = true;
                    for (var i = 0; i < appData.testJobs.length; i++) {
                        if (!appData.testJobs[i].success) {
                            allTestsSuccess = false;
                            break;
                        }
                    }
                }

                testsBadge.href = appData.pipelineUrl;
                testsBadge.className = 'pipeline-status-badge ' +
                    (allTestsSuccess ? 'has-text-success' : 'has-text-danger');

                if (appData.testJobs && appData.testJobs.length) {
                    var tooltipContent = '';
                    for (var i = 0; i < appData.testJobs.length; i++) {
                        tooltipContent += appData.testJobs[i].name +
                            (appData.testJobs[i].success ? ' ✓' : ' ✗');
                        if (i < appData.testJobs.length - 1) {
                            tooltipContent += '\n';
                        }
                    }
                    testsBadge.dataset.tooltip = tooltipContent;
                }

                var icon = testsBadge.querySelector('.fa-xl');
                if (icon) {
                    icon.className = 'fa-regular fa-xl ' +
                        (allTestsSuccess ? 'fa-circle-check' : 'fa-circle-xmark');
                }
            }
        }

        // Update last check time
        var card = row.closest('.card');
        if (card) {
            var lastCheckSpan = card.querySelector('.last-check');
            if (lastCheckSpan) {
                lastCheckSpan.textContent = appData.lastChecked;
            }
        }
    },

    parseSupervisionHtml: function (html) {
        var result = {success: true, failedServices: []};
        try {
            var parser = new DOMParser();
            var doc = parser.parseFromString(html, 'text/html');
            var rows = doc.querySelectorAll('tr[id^="V"]');

            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var statusCell = row.querySelector('td[class]');
                if (statusCell && statusCell.textContent.trim().toUpperCase() !== 'OK') {
                    var serviceCell = row.querySelector('td');
                    if (serviceCell) {
                        var serviceText = serviceCell.textContent
                            .replace(/V\d+\s*:\s*/, '')
                            .split('=')[0]
                            .replace('url', '')
                            .trim();
                        result.failedServices.push(serviceText);
                        result.success = false;
                    }
                }
            }
        } catch (error) {
            console.error('Error parsing supervision HTML:', error);
            result.success = false;
        }
        return result;
    },

    handleProblemDetected: function (problem) {
        var envId = problem.envId;
        var card = document.querySelector(`[data-env-id="${envId}"]`);
        var header = card.querySelector('.card-header');
        var logicalName = header.dataset.logicalName;
        var tenant = header.dataset.tenant;
        
        if (!this.state.problemsMap.has(envId)) {
            this.state.problemsMap.set(envId, {
                envId: envId,
                apps: new Set(),
                environmentLevel: problem.environmentLevel,
                logicalName: logicalName,
                tenant: tenant
            });
        }
        this.state.problemsMap.get(envId).apps.add(problem.app);
    },

    handleRefreshCompleted: function () {
        this.state.refreshCounter++;
        if (this.state.refreshCounter >= this.state.totalCards) {
            var problems = [];
            this.state.problemsMap.forEach(function (envData) {
                envData.apps.forEach(function (app) {
                    problems.push({
                        app: app,
                        envId: envData.envId,
                        environmentLevel: envData.environmentLevel,
                        logicalName: envData.logicalName,
                        tenant: envData.tenant
                    });
                });
            });
            this.updateHotspotTable(problems);
            this.state.refreshCounter = 0;
            this.state.problemsMap.clear();
        }
    },

    updateHotspotTable: function (problems) {
        var hotspotBody = document.querySelector('#tab-hotspot tbody');
        var cicdBody = document.querySelector('#cicd-issues');
        if (!hotspotBody || !cicdBody) return;

        // Maps pour stocker les URLs
        var appUrls = new Map();
        var serviceUrls = new Map();
        var envProblems = new Map();
        var cicdProblems = new Map();

        // Traitement des problèmes
        for (var i = 0; i < problems.length; i++) {
            var problem = problems[i];
            var app = problem.app;
            var logicalName = problem.logicalName;
            var tenant = problem.tenant;
            var envKey = problem.envId;

            if (!envProblems.has(envKey)) {
                envProblems.set(envKey, {
                    logicalName: logicalName,
                    tenant: tenant,
                    environmentLevel: problem.environmentLevel || 0,
                    downApps: new Set(),
                    failedServices: new Set()
                });
            }

            var envData = envProblems.get(envKey);

            // Applications down
            if (!app.status) {
                envData.downApps.add(app.name);
                appUrls.set(app.name + '-' + logicalName + '-' + tenant, app.healthUrl);
            }

            // Services en échec
            if (app.supervisionReport && app.supervisionReport.failedServices) {
                for (var j = 0; j < app.supervisionReport.failedServices.length; j++) {
                    var service = app.supervisionReport.failedServices[j];
                    envData.failedServices.add(service);
                    serviceUrls.set(service + '-' + logicalName + '-' + tenant, app.supervisionUrl);
                }
            }

            // CI/CD issues
            if (app.testJobs && !app.testJobs.every(function (job) {
                return job.success;
            })) {
                if (!cicdProblems.has(app.name)) {
                    cicdProblems.set(app.name, {apps: new Map()});
                }
                cicdProblems.get(app.name).apps.set(app.name, app.pipelineUrl);
            }
        }

        // Tri des environnements
        var sortedEnvs = Array.from(envProblems.entries()).sort(function (a, b) {
            var levelDiff = a[1].environmentLevel - b[1].environmentLevel;
            return levelDiff === 0 ? a[0].localeCompare(b[0]) : levelDiff;
        });

        // Construire le HTML du hotspot
        var hotspotContent = this.buildHotspotContent(sortedEnvs, appUrls, serviceUrls);
        hotspotBody.innerHTML = hotspotContent;

        // Construire le HTML CI/CD
        var cicdContent = this.buildCicdContent(cicdProblems);
        cicdBody.innerHTML = cicdContent;
    },

    buildHotspotContent: function (sortedEnvs, appUrls, serviceUrls) {
        var content = '';
        var totalIssuesCount = 0;
        
        for (var i = 0; i < sortedEnvs.length; i++) {
            var entry = sortedEnvs[i];
            var envKey = entry[0];
            var data = entry[1];

            // Calculer le nombre total de problèmes pour cet environnement
            var envIssues = data.downApps.size + data.failedServices.size;
            totalIssuesCount += envIssues;

            content += '<tr>';
            content += '<td><span class="has-text-weight-medium">' + 
                data.logicalName + '-' + data.tenant + 
                '</span></td>';

            content += '<td>' + envIssues + ' issue' + (envIssues > 1 ? 's' : '') + '</td>';
            content += '<td><div class="tags">';

            data.downApps.forEach(function (appName) {
                var appKey = appName + '-' + data.logicalName + '-' + data.tenant;
                var healthUrl = appUrls.get(appKey);
                content += '<a href="' + healthUrl + '" target="_blank" class="tag is-danger" style="line-height: 1.5rem">' +
                    appName + ' DOWN</a>';
            });

            data.failedServices.forEach(function (service) {
                var serviceKey = service + '-' + data.logicalName + '-' + data.tenant;
                var supervisionUrl = serviceUrls.get(serviceKey);
                content += '<a href="' + supervisionUrl + '" target="_blank" class="tag is-warning" style="line-height: 1.5rem">' +
                    service + '</a>';
            });

            content += '</div></td></tr>';
        }

        // Mettre à jour le compteur total dans l'en-tête
        var hotspotHeader = document.querySelector('#tab-hotspot .card-header-title');
        if (hotspotHeader) {
            hotspotHeader.textContent = 'Hotspot (' + totalIssuesCount + ')';
        }

        return content;
    },

    buildCicdContent: function (cicdProblems) {
        if (cicdProblems.size === 0) {
            return '<tr><td><em class="has-text-grey">No CI/CD issues</em></td></tr>';
        }

        var content = '<tr><td><div class="tags">';
        cicdProblems.forEach(function (envData) {
            envData.apps.forEach(function (pipelineUrl, appName) {
                content +=
                    '<a href="' + pipelineUrl + '" target="_blank" style="text-decoration: none; margin-right: 0.5rem;">' +
                    '<div class="tags has-addons my-0">' +
                    '<span class="tag is-medium my-0">' + appName + '</span>' +
                    '<span class="tag is-medium is-danger my-0">KO</span>' +
                    '</div></a>';
            });
        });
        content += '</div></td></tr>';
        return content;
    },

    httpGet: function (url, timeout) {
        var self = this;
        if (!timeout) {
            timeout = this.config.STATUS_CHECK_TIMEOUT;
        }

        var controller = new AbortController();
        var timeoutId = setTimeout(function () {
            controller.abort();
        }, timeout);

        return fetch(url, {signal: controller.signal})
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP error: ' + response.status);
                }
                return response.text();
            })
            .then(function (text) {
                try {
                    return JSON.parse(text);
                } catch (e) {
                    return text;
                }
            })
            .finally(function () {
                clearTimeout(timeoutId);
            });
    }
};

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function () {
    RefreshManager.initialize();
});