(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    function splitCsv(v) {
        return (v || '').split(',').map(s => s.trim()).filter(Boolean);
    }

    function setMsg(id, text, ok) {
        const el = document.getElementById(id);
        if (!el) {
            return;
        }
        el.textContent = text || '';
        el.style.color = ok ? '#047857' : '#b91c1c';
    }

    function boolVal(id) {
        return !!document.getElementById(id)?.checked;
    }

    function val(id) {
        return document.getElementById(id)?.value?.trim() || '';
    }

    async function apiFetchJson(url, options = {}) {
        if (window.AdminPage?.Api?.fetchJson) {
            return window.AdminPage.Api.fetchJson(url, options);
        }
        const response = await fetch(url, options);
        const payload = await response.json().catch(() => null);
        return {
            ok: response.ok,
            status: response.status,
            payload,
            response
        };
    }

    async function apiPostJson(url, payload) {
        if (window.AdminPage?.Api?.postJson) {
            return window.AdminPage.Api.postJson(url, payload);
        }
        return apiFetchJson(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(payload)
        });
    }

    function fillSmtpForm(eff) {
        if (!eff) {
            return;
        }
        if (document.getElementById('smtpHost')) {
            document.getElementById('smtpHost').value = eff.host || '';
        }
        if (document.getElementById('smtpPort')) {
            document.getElementById('smtpPort').value = eff.port || '';
        }
        if (document.getElementById('smtpAuth')) {
            document.getElementById('smtpAuth').checked = !!eff.auth;
        }
        if (document.getElementById('smtpStartTls')) {
            document.getElementById('smtpStartTls').checked = !!eff.starttls;
        }
        if (document.getElementById('smtpSsl')) {
            document.getElementById('smtpSsl').checked = !!eff.ssl;
        }
        if (document.getElementById('smtpUsername')) {
            document.getElementById('smtpUsername').value = eff.username || '';
        }
        if (document.getElementById('smtpFrom')) {
            document.getElementById('smtpFrom').value = eff.defaultFrom || '';
        }

        const pwNote = document.getElementById('smtpPasswordStoredNote');
        if (pwNote) {
            pwNote.style.display = eff.passwordConfigured ? 'inline' : 'none';
        }

        const src = document.getElementById('smtpEffectiveSource');
        if (src) {
            src.textContent = eff.source || 'UNKNOWN';
        }
    }

    async function loadSmtpConfig(contextPath) {
        try {
            const result = await apiFetchJson(`${contextPath}/admin/email/config`, { method: 'GET' });
            const body = result.payload;

            if (!result.ok || body?.status !== 'ok') {
                setMsg('smtpConfigResult', body?.message || 'Failed to load SMTP config.', false);
                return;
            }

            fillSmtpForm(body.effective);
            setMsg('smtpConfigResult', `Loaded SMTP config. Active source: ${body?.effective?.source || 'UNKNOWN'}`, true);
        } catch (e) {
            setMsg('smtpConfigResult', `Load error: ${e.message}`, false);
        }
    }

    async function saveSmtpConfig(contextPath) {
        const payload = {
            action: 'save',
            host: val('smtpHost'),
            port: Number(val('smtpPort') || 0),
            auth: boolVal('smtpAuth'),
            starttls: boolVal('smtpStartTls'),
            ssl: boolVal('smtpSsl'),
            username: val('smtpUsername'),
            password: document.getElementById('smtpPassword')?.value || '',
            defaultFrom: val('smtpFrom')
        };

        if (!payload.host || payload.port < 1 || payload.port > 65535) {
            setMsg('smtpConfigResult', 'SMTP host and valid port are required.', false);
            return;
        }

        try {
            const result = await apiPostJson(`${contextPath}/admin/email/config`, payload);
            const body = result.payload;

            if (result.ok && body?.status === 'ok') {
                setMsg('smtpConfigResult', body.message || 'SMTP config saved.', true);
                const pw = document.getElementById('smtpPassword');
                if (pw) {
                    pw.value = '';
                }
                await loadSmtpConfig(contextPath);
            } else {
                setMsg('smtpConfigResult', body?.message || 'Failed to save SMTP config.', false);
            }
        } catch (e) {
            setMsg('smtpConfigResult', `Save error: ${e.message}`, false);
        }
    }

    async function testSmtp(contextPath) {
        const payload = {
            action: 'test',
            host: val('smtpHost'),
            port: Number(val('smtpPort') || 0),
            auth: boolVal('smtpAuth'),
            starttls: boolVal('smtpStartTls'),
            ssl: boolVal('smtpSsl'),
            username: val('smtpUsername'),
            password: document.getElementById('smtpPassword')?.value || '',
            defaultFrom: val('smtpFrom'),
            from: val('smtpTestFrom'),
            testTo: val('smtpTestTo')
        };

        if (!payload.testTo) {
            setMsg('smtpTestResult', 'Test recipient (smtpTestTo) is required.', false);
            return;
        }

        try {
            const result = await apiPostJson(`${contextPath}/admin/email/config`, payload);
            const body = result.payload;

            if (result.ok && body?.status === 'ok') {
                setMsg('smtpTestResult', body.message || 'SMTP test sent.', true);
            } else {
                setMsg('smtpTestResult', body?.message || 'SMTP test failed.', false);
            }
        } catch (e) {
            setMsg('smtpTestResult', `SMTP test error: ${e.message}`, false);
        }
    }

    async function sendManualEmail(contextPath) {
        const result = document.getElementById('adminEmailResult');
        const payload = {
            from: val('emailFrom'),
            to: splitCsv(document.getElementById('emailTo')?.value),
            cc: splitCsv(document.getElementById('emailCc')?.value),
            bcc: splitCsv(document.getElementById('emailBcc')?.value),
            subject: val('emailSubject'),
            textBody: document.getElementById('emailTextBody')?.value || '',
            htmlBody: document.getElementById('emailHtmlBody')?.value || '',
            markdownBody: document.getElementById('emailMarkdownBody')?.value || ''
        };

        try {
            const response = await apiPostJson(`${contextPath}/admin/email/send`, payload);
            const body = response.payload;
            result.textContent = body?.message || (response.ok ? 'Email sent' : 'Failed');
            result.style.color = response.ok ? '#047857' : '#b91c1c';
        } catch (e) {
            result.textContent = `Send error: ${e.message}`;
            result.style.color = '#b91c1c';
        }
    }

    window.AdminPage.Email = {
        init(contextPath) {
            document.getElementById('sendAdminEmailBtn')?.addEventListener('click', () => sendManualEmail(contextPath));
            document.getElementById('loadSmtpConfigBtn')?.addEventListener('click', () => loadSmtpConfig(contextPath));
            document.getElementById('saveSmtpConfigBtn')?.addEventListener('click', () => saveSmtpConfig(contextPath));
            document.getElementById('testSmtpConfigBtn')?.addEventListener('click', () => testSmtp(contextPath));

            loadSmtpConfig(contextPath);
        }
    };
})();
