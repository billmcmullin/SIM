// File: src/main/webapp/assets/js/admin_page.js
// admin_page.js (bootstrap + tabbed sections with merged groups)
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    const config = window.adminPageConfig || {};
    const contextPath = config.contextPath || '';
    const apiKeyStored = String(config.apiKeyStored) === 'true' || config.apiKeyStored === true;
    const salesforceApiKeyStored = String(config.salesforceApiKeyStored) === 'true' || config.salesforceApiKeyStored === true;
    const salesforceClientSecretStored = String(config.salesforceClientSecretStored) === 'true' || config.salesforceClientSecretStored === true;
    const salesforceRefreshTokenStored = String(config.salesforceRefreshTokenStored) === 'true' || config.salesforceRefreshTokenStored === true;

    let initialWidgetList = [];
    let initialTermList = [];

    try {
        initialWidgetList = Array.isArray(config.widgetListJson)
            ? config.widgetListJson
            : JSON.parse(config.widgetListJson || '[]');
    } catch (e) {
        initialWidgetList = [];
        console.warn('[AdminPage] Failed parsing widgetListJson:', e);
    }

    try {
        initialTermList = Array.isArray(config.termsListJson)
            ? config.termsListJson
            : JSON.parse(config.termsListJson || '[]');
    } catch (e) {
        initialTermList = [];
        console.warn('[AdminPage] Failed parsing termsListJson:', e);
    }

    window.AdminPage.Config = {
        contextPath,
        apiKeyStored,
        salesforceApiKeyStored,
        salesforceClientSecretStored,
        salesforceRefreshTokenStored,
        salesforceInstanceUrl: config.salesforceInstanceUrl || '',
        salesforceLoginUrl: config.salesforceLoginUrl || '',
        salesforceClientId: config.salesforceClientId || '',
        initialWidgetList,
        initialTermList,
        workspaceName: config.workspaceName
    };

    let lastTestSuccess = false;

    function safeInit(name, fn) {
        try {
            if (typeof fn === 'function') {
                fn();
            }
        } catch (e) {
            console.error(`[AdminPage] ${name} init failed:`, e);
        }
    }

    function initAdminTabs() {
        const container = document.querySelector('.container');
        const tabsHost = document.getElementById('adminTabs');
        if (!container || !tabsHost) {
            return;
        }

        const sections = Array.from(container.querySelectorAll(':scope > section.section'));
        if (!sections.length) {
            return;
        }

        tabsHost.innerHTML = '';
        tabsHost.setAttribute('role', 'tablist');

        // Map by h2 title
        const byTitle = new Map();
        sections.forEach((section) => {
            const h2 = section.querySelector('h2');
            if (!h2) {
                return;
            }
            byTitle.set(h2.textContent.trim(), section);
        });

        // Merged + single tabs
        const tabGroups = [
            {
                panelId: 'tab-widget-health',
                label: 'Widget Health',
                sectionTitles: ['Widget Availability Health Check']
            },
            {
                panelId: 'tab-database',
                label: 'Database Import / Export',
                sectionTitles: ['Database Data Export', 'Database Data Import']
            },
            {
                panelId: 'tab-server-workspace',
                label: 'Server & Workspace',
                sectionTitles: ['Server Configuration', 'Workspace Management']
            },
            {
                panelId: 'tab-salesforce',
                label: 'Salesforce',
                sectionTitles: ['Salesforce Configuration']
            },
            {
                panelId: 'tab-widgets',
                label: 'Widgets & Explorer',
                sectionTitles: ['Widget Registry', 'Widget Table Explorer']
            },
            {
                panelId: 'tab-terms',
                label: 'Terms',
                sectionTitles: ['Term Definitions']
            },
            {
                panelId: 'tab-users',
                label: 'Users',
                sectionTitles: ['Create User']
            },
            {
                panelId: 'tab-smtp',
                label: 'SMTP',
                sectionTitles: ['SMTP Configuration']
            },
            {
                panelId: 'tab-email',
                label: 'Manual Email',
                sectionTitles: ['Manual Email (Admin)']
            }
        ];

        const tabs = [];

        tabGroups.forEach((group) => {
            const groupedSections = group.sectionTitles
                .map((title) => byTitle.get(title))
                .filter(Boolean);

            if (!groupedSections.length) {
                return;
            }

            groupedSections.forEach((section, idx) => {
                section.classList.add('tab-panel');
                section.setAttribute('role', 'tabpanel');
                section.setAttribute('aria-labelledby', `${group.panelId}-tab`);
                section.setAttribute('data-tab-group', group.panelId);

                // make first section of group hash-targetable
                if (idx === 0) {
                    section.id = group.panelId;
                }
            });

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'admin-tab-btn';
            btn.id = `${group.panelId}-tab`;
            btn.setAttribute('role', 'tab');
            btn.setAttribute('aria-controls', group.panelId);
            btn.setAttribute('aria-selected', 'false');
            btn.textContent = group.label;

            tabsHost.appendChild(btn);
            tabs.push({ panelId: group.panelId, button: btn, sections: groupedSections });
        });

        if (!tabs.length) {
            return;
        }

        function activate(panelId, updateHash) {
            tabs.forEach((t) => {
                const active = t.panelId === panelId;
                t.button.classList.toggle('active', active);
                t.button.setAttribute('aria-selected', String(active));

                t.sections.forEach((section) => {
                    section.classList.toggle('active', active);
                });
            });

            try {
                localStorage.setItem('admin.activeTab', panelId);
            } catch { /* ignore */ }

            if (updateHash) {
                history.replaceState(null, '', `#${panelId}`);
            }
        }

        tabs.forEach((t) => {
            t.button.addEventListener('click', () => activate(t.panelId, true));
        });

        const hashId = (window.location.hash || '').replace(/^#/, '');
        let stored = '';
        try {
            stored = localStorage.getItem('admin.activeTab') || '';
        } catch { /* ignore */ }

        const initial = tabs.find((t) => t.panelId === hashId)?.panelId
            || tabs.find((t) => t.panelId === stored)?.panelId
            || tabs[0].panelId;

        activate(initial, false);
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (apiKeyStored) {
            const note = document.getElementById('apiKeyStoredNote');
            if (note) {
                note.style.display = 'block';
            }
        }

        if (salesforceApiKeyStored) {
            const sfNote = document.getElementById('salesforceApiKeyStoredNote');
            if (sfNote) {
                sfNote.style.display = 'block';
            }
        }

        if (salesforceClientSecretStored) {
            const sfSecretNote = document.getElementById('salesforceClientSecretStoredNote');
            if (sfSecretNote) {
                sfSecretNote.style.display = 'block';
            }
        }

        if (salesforceRefreshTokenStored) {
            const sfRefreshNote = document.getElementById('salesforceRefreshTokenStoredNote');
            if (sfRefreshNote) {
                sfRefreshNote.style.display = 'block';
            }
        }

        safeInit('AdminTabs', initAdminTabs);

        // Initialize modules safely so one failure doesn't break all others.
        safeInit('Users', () => window.AdminPage.Users?.init(contextPath));
        safeInit('Sync', () => window.AdminPage.Sync?.init(contextPath));
        safeInit('Widgets', () => window.AdminPage.Widgets?.init({ contextPath, widgetList: initialWidgetList }));
        safeInit('Terms', () => window.AdminPage.Terms?.init({ contextPath, initialTerms: initialTermList }));
        safeInit('Workspace', () => window.AdminPage.Workspace?.init(window.AdminPage.Config));
        safeInit('Salesforce', () => window.AdminPage.Salesforce?.init(window.AdminPage.Config));
        safeInit('DbImport', () => window.AdminPage.DbImport?.init({ contextPath }));
        safeInit('Email', () => window.AdminPage.Email?.init(contextPath));

        // Page actions
        document.getElementById('testConnectionBtn')?.addEventListener('click', testConnection);
        document.getElementById('saveConfigBtn')?.addEventListener('click', saveConfiguration);
        document.getElementById('saveSyncIntervalBtn')?.addEventListener('click', () => window.AdminPage.Sync?.saveSyncInterval?.());
        document.getElementById('backupDbBtn')?.addEventListener('click', backupDatabase);

        if (initialWidgetList && initialWidgetList.length) {
            safeInit('Widgets.renderWidgetTable', () => window.AdminPage.Widgets?.renderWidgetTable?.call(window.AdminPage.Widgets));
            safeInit('Widgets.renderWidgetTableExplorer', () => window.AdminPage.Widgets?.renderWidgetTableExplorer?.call(window.AdminPage.Widgets));
        }

        console.log('[AdminPage] DOMContentLoaded init complete');
    });

    function updateSaveButton() {
        const saveBtn = document.getElementById('saveConfigBtn');
        if (saveBtn) {
            saveBtn.disabled = !lastTestSuccess;
        }
    }

    async function testConnection() {
        const host = document.getElementById('serverHost')?.value.trim() || '';
        const port = document.getElementById('serverPort')?.value.trim() || '';
        const apiKey = document.getElementById('apiKey')?.value.trim() || '';
        const resultEl = document.getElementById('testResult');

        if (!host || !port) {
            if (resultEl) {
                resultEl.textContent = 'Please provide host and port.';
                resultEl.style.color = '#b91c1c';
            }
            lastTestSuccess = false;
            updateSaveButton();
            return;
        }

        const data = new URLSearchParams();
        data.append('serverHost', host);
        data.append('serverPort', port);
        if (apiKey) {
            data.append('apiKey', apiKey);
        }

        try {
            const { status, payload } = await window.AdminPage.Api.fetchJson(`${contextPath}/admin/test-connection`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: data.toString()
            });

            if (payload?.status === 'ok') {
                if (resultEl) {
                    resultEl.textContent = 'Connection successful.';
                    resultEl.style.color = '#047857';
                }
                lastTestSuccess = true;
            } else {
                if (resultEl) {
                    resultEl.textContent = payload?.message || `Connection failed (${status}).`;
                    resultEl.style.color = '#b91c1c';
                }
                lastTestSuccess = false;
            }
        } catch (err) {
            if (resultEl) {
                resultEl.textContent = `Connection error: ${err.message}`;
                resultEl.style.color = '#b91c1c';
            }
            lastTestSuccess = false;
        } finally {
            updateSaveButton();
        }
    }

    async function saveConfiguration() {
        const host = document.getElementById('serverHost')?.value.trim() || '';
        const port = document.getElementById('serverPort')?.value.trim() || '';
        const apiKey = document.getElementById('apiKey')?.value.trim() || '';
        const resultEl = document.getElementById('testResult');

        if (!lastTestSuccess) {
            if (resultEl) {
                resultEl.textContent = 'Please test the connection successfully before saving.';
                resultEl.style.color = '#b91c1c';
            }
            return;
        }

        const data = new URLSearchParams();
        data.append('serverHost', host);
        data.append('serverPort', port);
        data.append('apiKey', apiKey);

        try {
            const { payload, ok } = await window.AdminPage.Api.postUrlEncoded(`${contextPath}/admin/save-config`, data);
            if (ok && payload?.status === 'ok') {
                if (resultEl) {
                    resultEl.textContent = 'Configuration saved successfully.';
                    resultEl.style.color = '#047857';
                }

                const apiKeyNote = document.getElementById('apiKeyStoredNote');
                if (apiKeyNote) {
                    apiKeyNote.style.display = 'block';
                }

                const apiKeyInput = document.getElementById('apiKey');
                if (apiKeyInput) {
                    apiKeyInput.value = '';
                }
            } else {
                if (resultEl) {
                    resultEl.textContent = payload?.message || 'Unable to save configuration.';
                    resultEl.style.color = '#b91c1c';
                }
            }
        } catch (err) {
            if (resultEl) {
                resultEl.textContent = `Save error: ${err.message}`;
                resultEl.style.color = '#b91c1c';
            }
        }
    }

    async function backupDatabase() {
        const msg = document.getElementById('backupDbMessage');
        if (msg) {
            msg.textContent = 'Preparing backup...';
            msg.style.color = '#374151';
        }

        try {
            // Trigger file download
            window.location.href = `${contextPath}/admin/db/backup`;

            if (msg) {
                msg.textContent = 'Backup request started. Your download should begin shortly.';
                msg.style.color = '#047857';
            }
        } catch (err) {
            if (msg) {
                msg.textContent = `Backup failed: ${err.message}`;
                msg.style.color = '#b91c1c';
            }
        }
    }
})();
