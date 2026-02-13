// admin_page.js (bootstrap)
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    const config = window.adminPageConfig || {};
    const contextPath = config.contextPath || '';
    const apiKeyStored = Boolean(config.apiKeyStored);

    let initialWidgetList = [];
    let initialTermList = [];
    try {
        initialWidgetList = Array.isArray(config.widgetListJson) ? config.widgetListJson : JSON.parse(config.widgetListJson || '[]');
    } catch (e) { initialWidgetList = []; }
    try {
        initialTermList = Array.isArray(config.termsListJson) ? config.termsListJson : JSON.parse(config.termsListJson || '[]');
    } catch (e) { initialTermList = []; }

    window.AdminPage.Config = {
        contextPath,
        apiKeyStored,
        initialWidgetList,
        initialTermList,
        workspaceName: config.workspaceName
    };

    document.addEventListener('DOMContentLoaded', () => {
        if (apiKeyStored) {
            const note = document.getElementById('apiKeyStoredNote');
            if (note) note.style.display = 'block';
        }

        // Initialize modules. Ensure Sync is initialized before Widgets so Widgets can call it.
        window.AdminPage.Users?.init(contextPath);
        window.AdminPage.Sync?.init(contextPath);                      // <-- moved up
        window.AdminPage.Widgets?.init({ contextPath, widgetList: initialWidgetList });
        window.AdminPage.Terms?.init({ contextPath, initialTerms: initialTermList });
        window.AdminPage.Workspace?.init(window.AdminPage.Config);

        document.getElementById('testConnectionBtn')?.addEventListener('click', testConnection);
        document.getElementById('saveConfigBtn')?.addEventListener('click', saveConfiguration);
        document.getElementById('saveSyncIntervalBtn')?.addEventListener('click', () => window.AdminPage.Sync.saveSyncInterval());

        if (initialWidgetList && initialWidgetList.length) {
            window.AdminPage.Widgets.renderWidgetTable?.call(window.AdminPage.Widgets);
            window.AdminPage.Widgets.renderWidgetTableExplorer?.call(window.AdminPage.Widgets);
        }
    });


    let lastTestSuccess = false;

    function updateSaveButton() {
        const saveBtn = document.getElementById('saveConfigBtn');
        if (saveBtn) saveBtn.disabled = !lastTestSuccess;
    }

    async function testConnection() {
        const host = document.getElementById('serverHost').value.trim();
        const port = document.getElementById('serverPort').value.trim();
        const apiKey = document.getElementById('apiKey').value.trim();
        const resultEl = document.getElementById('testResult');

        if (!host || !port) {
            resultEl.textContent = 'Please provide host and port.';
            resultEl.style.color = '#b91c1c';
            lastTestSuccess = false;
            updateSaveButton();
            return;
        }

        const data = new URLSearchParams();
        data.append('serverHost', host);
        data.append('serverPort', port);
        if (apiKey) data.append('apiKey', apiKey);

        try {
            const { status, payload } = await window.AdminPage.Api.fetchJson(`${contextPath}/admin/test-connection`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: data.toString()
            });
            if (payload?.status === 'ok') {
                resultEl.textContent = 'Connection successful.';
                resultEl.style.color = '#047857';
                lastTestSuccess = true;
            } else {
                resultEl.textContent = payload?.message || `Connection failed (${status}).`;
                resultEl.style.color = '#b91c1c';
                lastTestSuccess = false;
            }
        } catch (err) {
            resultEl.textContent = `Connection error: ${err.message}`;
            resultEl.style.color = '#b91c1c';
            lastTestSuccess = false;
        } finally {
            updateSaveButton();
        }
    }

    async function saveConfiguration() {
        const host = document.getElementById('serverHost').value.trim();
        const port = document.getElementById('serverPort').value.trim();
        const apiKey = document.getElementById('apiKey').value.trim();
        const resultEl = document.getElementById('testResult');

        if (!lastTestSuccess) {
            resultEl.textContent = 'Please test the connection successfully before saving.';
            resultEl.style.color = '#b91c1c';
            return;
        }

        const data = new URLSearchParams();
        data.append('serverHost', host);
        data.append('serverPort', port);
        data.append('apiKey', apiKey);

        try {
            const { payload, ok } = await window.AdminPage.Api.postUrlEncoded(`${contextPath}/admin/save-config`, data);
            if (ok && payload?.status === 'ok') {
                resultEl.textContent = 'Configuration saved successfully.';
                resultEl.style.color = '#047857';
            } else {
                resultEl.textContent = payload?.message || 'Unable to save configuration.';
                resultEl.style.color = '#b91c1c';
            }
        } catch (err) {
            resultEl.textContent = `Save error: ${err.message}`;
            resultEl.style.color = '#b91c1c';
        }
    }
})();
