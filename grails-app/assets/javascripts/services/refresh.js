// refresh.js
const RefreshManager = {
    state: {
        lastSuccessfulResponse: Date.now(),
        refreshCounter: 0,
        totalCards: 0,
        isAutonomousMode: false,
        currentMode: 'server',
        problemsMap: new Map()
    },

    initialize: function () {
        this.state.totalCards = document.querySelectorAll('.card:not(#tab-hotspot .card)').length;
        this.state.isAutonomousMode = document.body.getAttribute('data-initial-mode') === 'autonomous';

        this.setupEventListeners();
        this.setupPeriodicChecks();
        this.setupTabsHandling();
    },

    setupEventListeners: function () {
        // Écoute des changements de mode
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.MODE_CHANGED, function (event) {
            this.state.isAutonomousMode = event.detail.mode === 'autonomous';
            this.state.currentMode = event.detail.mode;
            console.log('Mode switched to:', event.detail.mode);
        }.bind(this));

        // Bouton de rafraîchissement
        const refreshAllButton = document.getElementById('refreshAll');
        if (refreshAllButton) {
            refreshAllButton.addEventListener('click', this.handleRefreshAll.bind(this));
        }

        // Gestion des problèmes
        MonitoringEventBus.on(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, function (event) {
            this.handleProblemDetected(event.detail);
        }.bind(this));

        MonitoringEventBus.on(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, function (event) {
            this.handleRefreshCompleted();
        }.bind(this));
    },

    setupPeriodicChecks: function () {
        setInterval(this.checkServerConnection.bind(this), MonitoringConfig.REFRESH_INTERVAL);

        // Rafraîchissement périodique des cartes
        const cards = document.querySelectorAll('.card:not(#tab-hotspot .card)');
        cards.forEach(function (card) {
            const envId = card.dataset.envId;
            if (!envId) return;

            this.refreshEnvironment(envId);
            setInterval(function () {
                this.refreshEnvironment(envId);
            }.bind(this), MonitoringConfig.REFRESH_INTERVAL);
        }.bind(this));
    },

    setupTabsHandling: function () {
        const tabs = document.querySelectorAll('.tabs li');
        const tabContents = document.querySelectorAll('.tab-content');

        tabs.forEach(function (tab) {
            tab.addEventListener('click', function () {
                tabs.forEach(function (t) {
                    t.classList.remove('is-active');
                });
                tabContents.forEach(function (c) {
                    c.classList.remove('is-active');
                });

                tab.classList.add('is-active');
                const target = tab.querySelector('a').getAttribute('href');
                document.querySelector(target)?.classList.add('is-active');
            });
        });
    },

    refreshEnvironment: function (envId) {
        let self = this;
        const card = document.querySelector(`[data-env-id="${envId}"]`);
        if (!card) return;

        this.fetchApplicationStatus(envId, card)
            .then(function (applicationsStatus) {
                self.state.lastSuccessfulResponse = Date.now();
                self.updateUIAfterRefresh(applicationsStatus, envId, card);
                MonitoringEventBus.emit(MonitoringEventBus.EventTypes.REFRESH_COMPLETED, {envId: envId});
            })
            .catch(function (error) {
                console.error(`Error refreshing environment ${envId}:`, error);
            });
    },

    fetchApplicationStatus: function (envId, card) {
        if (this.state.isAutonomousMode) {
            return this.fetchAutonomousStatus(card);
        }

        return fetch(`/monitoring/status?envId=${encodeURIComponent(envId)}`)
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                return data.applicationsStatus;
            });
    },

    fetchAutonomousStatus: function (card) {
        let self = this;
        const rows = card.querySelectorAll('tr');
        const statusPromises = Array.from(rows)
            .map(function (row) {
                const nameSpan = row.querySelector('span.has-text-weight-medium');
                if (!nameSpan) return null;

                return self.checkApplicationStatus({
                    name: nameSpan.textContent.trim(),
                    healthUrl: row.querySelector('td[id^="health"] a')?.href,
                    supervisionUrl: row.querySelector('td[id^="supervision"] a')?.href,
                    gitlabProjectId: row.querySelector('td[id^="pipeline-tests"]')?.dataset?.gitlabProject
                });
            })
            .filter(Boolean);

        return Promise.all(statusPromises);
    },

    checkApplicationStatus: function (app) {
        let self = this;
        return new Promise(function (resolve) {
            let status = false;
            let supervisionReport = {success: false, failedServices: []};
            let gitlabStatus = null;

            let promises = [];

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
                    resolve({
                        name: app.name,
                        status: status,
                        supervisionReport: supervisionReport,
                        lastChecked: new Date().toLocaleString(),
                        testJobs: gitlabStatus ? gitlabStatus.jobs : [],
                        pipelineUrl: gitlabStatus ? gitlabStatus.pipelineUrl : null
                    });
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
        e.preventDefault();
        let self = this;

        const icon = e.currentTarget.querySelector('.icon');
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

        const cards = document.querySelectorAll('.card:not(#tab-hotspot .card)');
        const promises = [];

        cards.forEach(function (card) {
            const envId = card.dataset.envId;
            if (!envId) return;

            promises.push(self.refreshEnvironment(envId));
        });

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
        const timeSinceLastResponse = Date.now() - this.state.lastSuccessfulResponse;
        const alertElement = document.getElementById('server-alert');
        if (timeSinceLastResponse > MonitoringConfig.MAX_TIME_WITHOUT_RESPONSE && alertElement) {
            alertElement.classList.remove('is-hidden');
        }
    },

    findApplicationRow: function (card, appName) {
        let rows = card.querySelectorAll('tr');
        for (let i = 0; i < rows.length; i++) {
            let row = rows[i];
            let nameSpan = row.querySelector('span.has-text-weight-medium');
            if (nameSpan && nameSpan.textContent.trim() === appName) {
                return row;
            }
        }
        return null;
    },

    updateApplicationRow: function (row, appData) {
        // Health status
        let healthCell = row.querySelector('td#health-' + appData.name);
        if (healthCell) {
            let healthTag = healthCell.querySelector('.tag');
            if (healthTag) {
                healthTag.className = 'tag ' + (appData.status ? 'is-success' : 'is-danger');
                healthTag.textContent = appData.status ? 'UP' : 'DOWN';
            }
        }

        // Supervision status
        let supervisionCell = row.querySelector('td#supervision-' + appData.name);
        if (supervisionCell) {
            let supervisionTag = supervisionCell.querySelector('.tag');
            if (supervisionTag) {
                let hasFailures = appData.supervisionReport &&
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
        let testsCell = row.querySelector('td#pipeline-tests-' + appData.name);
        if (testsCell) {
            let testsBadge = testsCell.querySelector('.pipeline-status-badge');
            if (testsBadge) {
                let allTestsSuccess = appData.testJobs.length > 0 && appData.testJobs.every(function (job) {
                    return job.success;
                });

                testsBadge.href = appData.pipelineUrl;
                testsBadge.className = 'pipeline-status-badge ' +
                    (allTestsSuccess ? 'has-text-success' : 'has-text-danger');

                if (appData.testJobs && appData.testJobs.length) {
                    testsBadge.dataset.tooltip = appData.testJobs.map(function (job) {
                        return job.name + '\u00A0' + (job.success ? '✓' : '✗');
                    }).join('\n');
                }

                let icon = testsBadge.querySelector('.fa-xl');
                if (icon) {
                    icon.className = 'fa-regular fa-xl ' +
                        ((!appData.testJobs || appData.testJobs.length === 0) ? 'far fa-circle fa-xl' : allTestsSuccess ? 'fa-circle-check' : 'fa-circle-xmark');
                }
            }
        }

        // Update last check time
        let card = row.closest('.card');
        if (card) {
            let lastCheckSpan = card.querySelector('.last-check');
            if (lastCheckSpan) {
                lastCheckSpan.textContent = appData.lastChecked;
            }
        }
    },

    parseSupervisionHtml: function (html) {
        let result = {success: true, failedServices: []};
        try {
            let parser = new DOMParser();
            let doc = parser.parseFromString(html, 'text/html');
            let rows = doc.querySelectorAll('tr[id^="V"]');

            rows.forEach(function (row) {
                let statusCell = row.querySelector('td[class]');
                if (statusCell && statusCell.textContent.trim().toUpperCase() !== 'OK') {
                    let serviceCell = row.querySelector('td');
                    if (serviceCell) {
                        let serviceText = serviceCell.textContent
                            .replace(/V\d+\s*:\s*/, '')
                            .split('=')[0]
                            .replace('url', '')
                            .trim();
                        result.failedServices.push(serviceText);
                        result.success = false;
                    }
                }
            });
        } catch (error) {
            console.error('Error parsing supervision HTML:', error);
            result.success = false;
        }
        return result;
    },

    handleProblemDetected: function (problem) {
        const envId = problem.envId;
        const card = document.querySelector(`[data-env-id="${envId}"]`);
        const header = card.querySelector('.card-header');
        const logicalName = header.dataset.logicalName;
        const tenant = header.dataset.tenant;

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
            const problems = [];
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
        let hotspotBody = document.querySelector('#tab-hotspot tbody');
        let cicdBody = document.querySelector('#cicd-issues');
        if (!hotspotBody || !cicdBody) return;

        let appUrls = new Map();
        let serviceUrls = new Map();
        let envProblems = new Map();
        let cicdProblems = new Map();

        problems.forEach(function (problem) {
            let app = problem.app;
            let logicalName = problem.logicalName;
            let tenant = problem.tenant;
            let envKey = problem.envId;

            if (!envProblems.has(envKey)) {
                envProblems.set(envKey, {
                    logicalName: logicalName,
                    tenant: tenant,
                    environmentLevel: problem.environmentLevel || 0,
                    downApps: new Set(),
                    failedServices: new Set()
                });
            }

            let envData = envProblems.get(envKey);

            if (!app.status) {
                envData.downApps.add(app.name);
                appUrls.set(app.name + '-' + logicalName + '-' + tenant, app.healthUrl);
            }

            if (app.supervisionReport && app.supervisionReport.failedServices) {
                app.supervisionReport.failedServices.forEach(function (service) {
                    envData.failedServices.add(service);
                    serviceUrls.set(service + '-' + logicalName + '-' + tenant, app.supervisionUrl);
                });
            }

            if (app.testJobs && !app.testJobs.every(function (job) {
                return job.success;
            })) {
                if (!cicdProblems.has(app.name)) {
                    cicdProblems.set(app.name, {apps: new Map()});
                }
                cicdProblems.get(app.name).apps.set(app.name, app.pipelineUrl);
            }
        });

        // Tri des environnements
        let sortedEnvs = Array.from(envProblems.entries()).sort(function (a, b) {
            let levelDiff = a[1].environmentLevel - b[1].environmentLevel;
            return levelDiff === 0 ? a[0].localeCompare(b[0]) : levelDiff;
        });

        // Construire le HTML du hotspot
        let hotspotContent = this.buildHotspotContent(sortedEnvs, appUrls, serviceUrls);
        hotspotBody.innerHTML = hotspotContent;

        // Construire le HTML CI/CD
        let cicdContent = this.buildCicdContent(cicdProblems);
        cicdBody.innerHTML = cicdContent;
    },

    buildHotspotContent: function (sortedEnvs, appUrls, serviceUrls) {
        let content = '';
        let totalIssuesCount = 0;

        sortedEnvs.forEach(function (entry) {
            let envKey = entry[0];
            let data = entry[1];

            let envIssues = data.downApps.size + data.failedServices.size;
            totalIssuesCount += envIssues;

            content += '<tr>';
            content += '<td><span class="has-text-weight-medium">' +
                data.logicalName + '-' + data.tenant +
                '</span></td>';

            content += '<td>' + envIssues + ' issue' + (envIssues > 1 ? 's' : '') + '</td>';
            content += '<td><div class="tags">';

            data.downApps.forEach(function (appName) {
                let appKey = appName + '-' + data.logicalName + '-' + data.tenant;
                let healthUrl = appUrls.get(appKey);
                content += '<a href="' + healthUrl + '" target="_blank" class="tag is-danger" style="line-height: 1.5rem">' +
                    appName + ' DOWN</a>';
            });

            data.failedServices.forEach(function (service) {
                let serviceKey = service + '-' + data.logicalName + '-' + data.tenant;
                let supervisionUrl = serviceUrls.get(serviceKey);
                content += '<a href="' + supervisionUrl + '" target="_blank" class="tag is-warning" style="line-height: 1.5rem">' +
                    service + '</a>';
            });

            content += '</div></td></tr>';
        });

        // Mettre à jour le compteur total dans l'en-tête
        let hotspotHeader = document.querySelector('#tab-hotspot .card-header-title');
        if (hotspotHeader) {
            hotspotHeader.textContent = 'Hotspot (' + totalIssuesCount + ')';
        }

        return content;
    },

    buildCicdContent: function (cicdProblems) {
        if (cicdProblems.size === 0) {
            return '<tr><td><em class="has-text-grey">No CI/CD issues</em></td></tr>';
        }

        let content = '<tr><td><div class="tags">';
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
        if (!timeout) {
            timeout = MonitoringConfig.STATUS_CHECK_TIMEOUT;
        }

        const controller = new AbortController();
        const timeoutId = setTimeout(function () {
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
    },

    updateUIAfterRefresh: function (applicationsStatus, envId, card) {
        let self = this;
        // Masquer l'alerte serveur si présente
        const alertElement = document.getElementById('server-alert');
        if (alertElement) {
            alertElement.classList.add('is-hidden');
        }

        // Mettre à jour chaque application
        applicationsStatus.forEach(function (app) {
            const appRow = self.findApplicationRow(card, app.name);
            if (!appRow) return;

            self.updateApplicationRow(appRow, app);

            // Vérifier les problèmes
            const hasProblems = !app.status ||
                (app.supervisionReport?.failedServices?.length > 0);

            if (hasProblems) {
                MonitoringEventBus.emit(MonitoringEventBus.EventTypes.PROBLEM_DETECTED, {
                    app: app,
                    envId: envId,
                    environmentLevel: card.dataset.envLevel
                });
            }
        });
    }
};

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function () {
    RefreshManager.initialize();
});

// Exposer globalement
window.RefreshManager = RefreshManager;