// assets/js/admin_utils/salesforce.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    window.AdminPage.Salesforce = {
        init
    };

    function init(config) {
        const contextPath = config?.contextPath || '';

        const testBtn = document.getElementById('testSalesforceConnectionBtn');
        if (testBtn) {
            testBtn.addEventListener('click', () => testSalesforceConnection(contextPath));
        }

        const saveBtn = document.getElementById('saveSalesforceConfigBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => saveSalesforceConfiguration(contextPath));
        }

        const connectOauthBtn = document.getElementById('connectSalesforceOAuthBtn');
        if (connectOauthBtn) {
            connectOauthBtn.addEventListener('click', () => {
                window.location.href = `${contextPath}/admin/salesforce/oauth/start`;
            });
        }

        // Optional callback message surfaced via AdminConfigServlet -> window.adminPageConfig
        const oauthStatus = trimToNull(config?.salesforceOAuthStatus);
        const oauthMessage = trimToNull(config?.salesforceOAuthMessage);
        if (oauthStatus || oauthMessage) {
            const success = oauthStatus === 'ok';
            setResult(
                document.getElementById('salesforceTestResult'),
                oauthMessage || (success ? 'Salesforce OAuth connected successfully.' : 'Salesforce OAuth connection failed.'),
                success
            );
        }
    }

    async function testSalesforceConnection(contextPath) {
        const instanceUrl = document.getElementById('salesforceInstanceUrl')?.value.trim() || '';
        const salesforceApiKey = document.getElementById('salesforceApiKey')?.value.trim() || '';
        const resultEl = document.getElementById('salesforceTestResult');
        const btn = document.getElementById('testSalesforceConnectionBtn');

        if (!instanceUrl) {
            setResult(resultEl, 'Please provide Salesforce instance URL.', false);
            return;
        }

        const data = new URLSearchParams();
        data.append('salesforceInstanceUrl', instanceUrl);
        if (salesforceApiKey) data.append('salesforceApiKey', salesforceApiKey);

        const originalText = btn ? btn.textContent : '';
        setButtonState(btn, true, 'Testing...');

        try {
            const { status, payload } = await window.AdminPage.Api.fetchJson(
                `${contextPath}/admin/test-salesforce-connection`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: data.toString()
                }
            );

            if (payload?.status === 'ok') {
                setResult(resultEl, payload?.message || 'Salesforce connection successful.', true);
            } else {
                setResult(resultEl, payload?.message || `Salesforce connection failed (${status}).`, false);
            }
        } catch (err) {
            setResult(resultEl, `Salesforce connection error: ${err.message}`, false);
        } finally {
            setButtonState(btn, false, originalText || 'Test Salesforce Connection');
        }
    }

    async function saveSalesforceConfiguration(contextPath) {
        const instanceUrl = document.getElementById('salesforceInstanceUrl')?.value.trim() || '';
        const salesforceApiKey = document.getElementById('salesforceApiKey')?.value.trim() || '';
        const salesforceLoginUrl = document.getElementById('salesforceLoginUrl')?.value.trim() || '';
        const salesforceClientId = document.getElementById('salesforceClientId')?.value.trim() || '';
        const salesforceClientSecret = document.getElementById('salesforceClientSecret')?.value.trim() || '';
        const salesforceRefreshToken = document.getElementById('salesforceRefreshToken')?.value.trim() || '';

        const resultEl = document.getElementById('salesforceTestResult');
        const btn = document.getElementById('saveSalesforceConfigBtn');

        if (!instanceUrl) {
            setResult(resultEl, 'Salesforce instance URL is required.', false);
            return;
        }

        // If user is configuring OAuth refresh, login URL + client ID are required
        const anyOauthFieldProvided =
            !!salesforceLoginUrl || !!salesforceClientId || !!salesforceClientSecret || !!salesforceRefreshToken;

        if (anyOauthFieldProvided && (!salesforceLoginUrl || !salesforceClientId)) {
            setResult(resultEl, 'Salesforce Login URL and Client ID are required for OAuth refresh setup.', false);
            return;
        }

        const data = new URLSearchParams();
        data.append('salesforceInstanceUrl', instanceUrl);

        // blank => backend keeps existing values
        data.append('salesforceApiKey', salesforceApiKey);
        data.append('salesforceLoginUrl', salesforceLoginUrl);
        data.append('salesforceClientId', salesforceClientId);
        data.append('salesforceClientSecret', salesforceClientSecret);
        data.append('salesforceRefreshToken', salesforceRefreshToken);

        const originalText = btn ? btn.textContent : '';
        setButtonState(btn, true, 'Saving...');

        try {
            const { ok, payload } = await window.AdminPage.Api.postUrlEncoded(
                `${contextPath}/admin/save-config`,
                data
            );

            if (ok && payload?.status === 'ok') {
                setResult(resultEl, payload?.message || 'Salesforce configuration saved.', true);

                // Reveal stored notes
                const apiKeyNote = document.getElementById('salesforceApiKeyStoredNote');
                if (apiKeyNote) apiKeyNote.style.display = 'block';

                const secretNote = document.getElementById('salesforceClientSecretStoredNote');
                if (secretNote) secretNote.style.display = 'block';

                const refreshNote = document.getElementById('salesforceRefreshTokenStoredNote');
                if (refreshNote) refreshNote.style.display = 'block';

                // Clear sensitive fields after save
                const keyInput = document.getElementById('salesforceApiKey');
                if (keyInput) keyInput.value = '';

                const secretInput = document.getElementById('salesforceClientSecret');
                if (secretInput) secretInput.value = '';

                const refreshInput = document.getElementById('salesforceRefreshToken');
                if (refreshInput) refreshInput.value = '';
            } else {
                setResult(resultEl, payload?.message || 'Unable to save Salesforce configuration.', false);
            }
        } catch (err) {
            setResult(resultEl, `Save error: ${err.message}`, false);
        } finally {
            setButtonState(btn, false, originalText || 'Save Salesforce Configuration');
        }
    }

    function setResult(el, message, success) {
        if (!el) return;
        el.textContent = message;
        el.style.color = success ? '#047857' : '#b91c1c';
    }

    function setButtonState(btn, disabled, text) {
        if (!btn) return;
        btn.disabled = disabled;
        if (typeof text === 'string') btn.textContent = text;
    }

    function trimToNull(v) {
        if (v == null) return null;
        const t = String(v).trim();
        return t ? t : null;
    }
})();
