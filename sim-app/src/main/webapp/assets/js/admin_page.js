// File: src/main/webapp/assets/js/admin_page.js
// admin_page.js (bootstrap + tabbed sections)
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    const config = window.adminPageConfig || {};
    const contextPath = config.contextPath || '';
    const apiKeyStored = String(config.apiKeyStored) === 'true' || config.apiKeyStored === true;
    const salesforceApiKeyStored = String(config.salesforceApiKeyStored) === 'true' || config.salesforceApiKeyStored === true;
    const salesforceClientSecretStored = String(config.salesforceClientSecretStored) === 'true' || config.salesforceClientSecretStored === true;
    const salesforceRefreshTokenStored = String(config.salesforceRefreshTokenStored) === 'true' || config.salesforceRefreshTokenStored === true;
    const salesforcePasswordStored = String(config.salesforcePasswordStored) === 'true' || config.salesforcePasswordStored === true;
    const salesforceApiTokenStored = String(config.salesforceApiTokenStored) === 'true' || config.salesforceApiTokenStored === true;

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
        salesforcePasswordStored,
        salesforceApiTokenStored,
        salesforceInstanceUrl: config.salesforceInstanceUrl || '',
        salesforceLoginUrl: config.salesforceLoginUrl || '',
        salesforceClientId: config.salesforceClientId || '',
        salesforceUsername: config.salesforceUsername || '',
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

        // Use resilient section discovery: some runtimes can fail on :scope selectors,
        // which would prevent tabs from rendering and leave all sections visible.
        let sections = Array.from(container.children || [])
            .filter((el) => el && el.matches && el.matches('section.section'));
        if (!sections.length) {
            sections = Array.from(container.querySelectorAll('section.section'));
        }
        if (!sections.length) {
            return;
        }

        tabsHost.innerHTML = '';
        tabsHost.setAttribute('role', 'tablist');

        const sectionEntries = sections.map((section, idx) => {
            const h2 = section.querySelector('h2');
            const title = h2 ? (h2.textContent || '').trim() : '';
            return { section, idx, title };
        }).filter((entry) => Boolean(entry.title));

        const byTitle = new Map();
        sectionEntries.forEach((entry) => {
            byTitle.set(entry.title, entry.section);
        });

        const groupedSpecs = [
            {
                key: 'database-import-export',
                label: 'Database',
                titles: ['Database Data Export', 'Database Data Import']
            },
            {
                key: 'server-workspace',
                label: 'Server & Workspace',
                titles: ['Server Configuration', 'Workspace Management']
            },
            {
                key: 'widget-registry-explorer',
                label: 'Widgets',
                titles: ['Widget Registry', 'Widget Table Explorer']
            },
            {
                key: 'create-user-term-definitions',
                label: 'Users & Terms',
                titles: ['Create User', 'Term Definitions']
            }
        ];

        const groupedByTitle = new Map();
        groupedSpecs.forEach((spec) => {
            spec.titles.forEach((title) => groupedByTitle.set(title, spec));
        });

        const tabs = [];
        const processedSections = new Set();

        function toPanelId(raw, fallback) {
            const slug = String(raw || '')
                .toLowerCase()
                .replace(/[^a-z0-9]+/g, '-')
                .replace(/(^-|-$)/g, '');
            return `tab-${slug || fallback}`;
        }

        function createTab(label, panelKey, panelSections) {
            const validSections = (panelSections || []).filter(Boolean);
            if (!validSections.length) {
                return;
            }

            const panelId = toPanelId(panelKey || label, `section-${tabs.length}`);

            validSections.forEach((section, index) => {
                section.classList.add('tab-panel');
                section.setAttribute('role', 'tabpanel');
                section.setAttribute('aria-labelledby', `${panelId}-tab`);
                section.id = index === 0 ? panelId : `${panelId}-part-${index + 1}`;
            });

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'admin-tab-btn';
            btn.id = `${panelId}-tab`;
            btn.setAttribute('role', 'tab');
            btn.setAttribute('aria-controls', panelId);
            btn.setAttribute('aria-selected', 'false');
            btn.textContent = label;

            tabsHost.appendChild(btn);
            tabs.push({ panelId, button: btn, sections: validSections });
            validSections.forEach((section) => processedSections.add(section));
        }

        sectionEntries.forEach((entry) => {
            if (processedSections.has(entry.section)) {
                return;
            }

            const groupSpec = groupedByTitle.get(entry.title);
            if (groupSpec) {
                const groupedSections = groupSpec.titles
                    .map((title) => byTitle.get(title))
                    .filter(Boolean)
                    .filter((section) => !processedSections.has(section));

                createTab(groupSpec.label, groupSpec.key, groupedSections);
                return;
            }

            createTab(entry.title, entry.title, [entry.section]);
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

        if (salesforcePasswordStored) {
            const sfPasswordNote = document.getElementById('salesforcePasswordStoredNote');
            if (sfPasswordNote) {
                sfPasswordNote.style.display = 'block';
            }
        }

        if (salesforceApiTokenStored) {
            const sfApiTokenNote = document.getElementById('salesforceApiTokenStoredNote');
            if (sfApiTokenNote) {
                sfApiTokenNote.style.display = 'block';
            }
        }

        safeInit('AdminTabs', initAdminTabs);

        // Initialize modules safely so one failure doesn't break all others.
        safeInit('Users', () => window.AdminPage.Users?.init(contextPath));
        safeInit('Sync', () => window.AdminPage.Sync?.init(contextPath));
        safeInit('Widgets', () => window.AdminPage.Widgets?.init({ contextPath, widgetList: initialWidgetList }));
        safeInit('Terms', () => window.AdminPage.Terms?.init({ contextPath, initialTerms: initialTermList }));
        safeInit('AutoEmailAlerts', () => window.AdminPage.AutoEmailAlerts?.init(window.AdminPage.Config));
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
        const workspaceName = document.getElementById('workspaceNameInput')?.value.trim() || '';
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
        if (workspaceName) {
            data.append('workspaceName', workspaceName);
        }
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
                    const probe = payload?.probe ? ` [${payload.probe}]` : '';
                    const upstream = Number.isInteger(payload?.upstreamStatus) ? ` (upstream ${payload.upstreamStatus})` : '';
                    resultEl.textContent = (payload?.message || `Connection failed (${status}).`) + probe + upstream;
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
        const workspaceName = document.getElementById('workspaceNameInput')?.value.trim() || '';
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
        if (workspaceName) {
            data.append('workspaceName', workspaceName);
        }

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
