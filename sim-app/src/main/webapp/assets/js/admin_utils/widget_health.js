(function () {
    'use strict';

    let currentConfig = null;

    function $(id) {
        return document.getElementById(id);
    }

    function getContextPath() {
        if (window.adminPageConfig && typeof window.adminPageConfig.contextPath === 'string') {
            return window.adminPageConfig.contextPath;
        }
        return '';
    }

    function endpoint(path) {
        return getContextPath() + path;
    }

    function setMessage(text, isError) {
        const el = $('widgetHealthConfigResult');
        if (!el) {
            return;
        }
        el.textContent = text || '';
        el.style.color = isError ? '#991b1b' : '#047857';
        el.style.fontWeight = '600';
        el.style.marginTop = '10px';
    }

    function toNullIfBlank(v) {
        const t = (v || '').trim();
        return t ? t : null;
    }

    function logHealthDebug(level, message, data) {
        const logger = (window.console && typeof window.console[level] === 'function')
            ? window.console[level].bind(window.console)
            : (window.console && typeof window.console.log === 'function'
                ? window.console.log.bind(window.console)
                : null);
        if (!logger) {
            return;
        }
        if (typeof data === 'undefined') {
            logger(`[WidgetHealthcheck] ${message}`);
        } else {
            logger(`[WidgetHealthcheck] ${message}`, data);
        }
    }

    function readForm() {
        const base = currentConfig && typeof currentConfig === 'object' ? currentConfig : {};
        const hasHealthcheckUrlField = Boolean($('whcHealthcheckUrl'));
        const healthcheckUrl = hasHealthcheckUrlField
            ? toNullIfBlank($('whcHealthcheckUrl')?.value)
            : toNullIfBlank(base.healthcheckUrl);
        let timeoutMs = Number.parseInt(base.timeoutMs, 10);
        let checkIntervalMinutes = Number.parseInt(base.checkIntervalMinutes, 10);
        if (!Number.isFinite(timeoutMs) || timeoutMs <= 0) {
            timeoutMs = 8000;
        }
        if (timeoutMs > 120000) {
            timeoutMs = 120000;
        }
        if (!Number.isFinite(checkIntervalMinutes) || checkIntervalMinutes <= 0) {
            checkIntervalMinutes = 5;
        }
        if (checkIntervalMinutes > 1440) {
            checkIntervalMinutes = 1440;
        }

        const payload = {
            healthcheckUrl,
            healthcheckEnabled: typeof base.healthcheckEnabled === 'boolean' ? base.healthcheckEnabled : true,
            checkIntervalMinutes,
            method: String(base.method || 'GET').trim().toUpperCase(),
            timeoutMs,
            expectJsonField: toNullIfBlank(base.expectJsonField),
            expectJsonValue: toNullIfBlank(base.expectJsonValue),
            widgetId: toNullIfBlank(base.widgetId),

            // New optional request-shaping fields
            requestOrigin: toNullIfBlank(base.requestOrigin),
            requestReferer: toNullIfBlank(base.requestReferer),
            requestUserAgent: toNullIfBlank(base.requestUserAgent),
            apiKeyHeaderName: toNullIfBlank(base.apiKeyHeaderName || 'Authorization'),
            apiKeyValue: toNullIfBlank($('whcApiKeyValue')?.value)
        };

        if (payload.apiKeyValue && !payload.apiKeyHeaderName) {
            payload.apiKeyHeaderName = 'Authorization';
        }

        if (!['GET', 'HEAD', 'POST'].includes(payload.method)) {
            payload.method = 'GET';
        }

        return payload;
    }

    function fillForm(cfg) {
        if (!cfg || typeof cfg !== 'object') {
            return;
        }

        currentConfig = cfg;

        if ($('whcHealthcheckUrl')) {
            $('whcHealthcheckUrl').value = cfg.healthcheckUrl || '';
        }
        if ($('whcEnabled')) {
            $('whcEnabled').checked = cfg.healthcheckEnabled !== false;
        }
        if ($('whcCheckIntervalMinutes')) {
            $('whcCheckIntervalMinutes').value = String(cfg.checkIntervalMinutes || 5);
        }
        if ($('whcMethod')) {
            $('whcMethod').value = (cfg.method || 'GET').toUpperCase();
        }
        if ($('whcTimeoutMs')) {
            $('whcTimeoutMs').value = String(cfg.timeoutMs || 8000);
        }
        if ($('whcExpectJsonField')) {
            $('whcExpectJsonField').value = cfg.expectJsonField || '';
        }
        if ($('whcExpectJsonValue')) {
            $('whcExpectJsonValue').value = cfg.expectJsonValue || '';
        }
        if ($('whcWidgetId')) {
            $('whcWidgetId').value = cfg.widgetId || '';
        }

        // New optional request-shaping fields
        if ($('whcRequestOrigin')) {
            $('whcRequestOrigin').value = cfg.requestOrigin || '';
        }
        if ($('whcRequestReferer')) {
            $('whcRequestReferer').value = cfg.requestReferer || '';
        }
        if ($('whcRequestUserAgent')) {
            $('whcRequestUserAgent').value = cfg.requestUserAgent || '';
        }
        if ($('whcRequestCookie')) {
            $('whcRequestCookie').value = '';
        }
        if ($('whcRequestCookieStoredNote')) {
            const cookieStored = cfg.requestCookieStored === true || cfg.requestCookieStored === 'true';
            $('whcRequestCookieStoredNote').style.display = cookieStored ? 'inline' : 'none';
        }
        if ($('whcApiKeyHeaderName')) {
            $('whcApiKeyHeaderName').value = cfg.apiKeyHeaderName || 'Authorization';
        }
        if ($('whcApiKeyValue')) {
            $('whcApiKeyValue').value = '';
        }
        if ($('whcApiKeyStoredNote')) {
            const stored = cfg.apiKeyStored === true || cfg.apiKeyStored === 'true';
            $('whcApiKeyStoredNote').style.display = stored ? 'inline' : 'none';
        }
    }

    async function loadWidgetHealthConfig() {
        setMessage('Loading widget health config...', false);
        try {
            const res = await fetch(endpoint('/admin/widget-health-config'), {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });

            if (res.status === 401) {
                setMessage('Not authenticated. Please sign in again.', true);
                return;
            }
            if (res.status === 403) {
                setMessage('Admin role required.', true);
                return;
            }
            if (!res.ok) {
                const text = await res.text();
                throw new Error(`Load failed (${res.status}): ${text}`);
            }

            const data = await res.json();
            fillForm(data);
            setMessage('Widget health config loaded.', false);
        } catch (err) {
            setMessage(`Failed to load widget health config: ${err?.message || err}`, true);
        }
    }

    async function saveWidgetHealthConfig() {
        const payload = readForm();

        setMessage('Saving widget health config...', false);

        try {
            const res = await fetch(endpoint('/admin/widget-health-config'), {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (res.status === 401) {
                setMessage('Not authenticated. Please sign in again.', true);
                return;
            }
            if (res.status === 403) {
                setMessage('Admin role required.', true);
                return;
            }
            if (!res.ok) {
                const text = await res.text();
                throw new Error(`Save failed (${res.status}): ${text}`);
            }

            const saved = await res.json();
            fillForm(saved);

            const meta = [];
            if (saved.updatedBy) {
                meta.push(`updatedBy=${saved.updatedBy}`);
            }
            if (saved.updatedAt) {
                meta.push(`updatedAt=${saved.updatedAt}`);
            }

            const metadataText = meta.length ? ` (${meta.join(', ')})` : '';
            setMessage(`Widget health config saved.${metadataText}`, false);
        } catch (err) {
            setMessage(`Failed to save widget health config: ${err?.message || err}`, true);
        }
    }

    async function testWidgetHealthNow() {
        setMessage('Running availability test...', false);
        try {
            logHealthDebug('info', 'Starting availability test request', {
                endpoint: endpoint('/admin/widget-availability.json?force=true&runWhenDisabled=true')
            });

            const res = await fetch(endpoint('/admin/widget-availability.json?force=true&runWhenDisabled=true'), {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });

            logHealthDebug('info', 'Availability test response status', {
                status: res.status,
                ok: res.ok
            });

            if (res.status === 401) {
                setMessage('Not authenticated. Please sign in again.', true);
                logHealthDebug('warn', 'Availability test unauthorized (401)');
                return;
            }
            if (res.status === 403) {
                setMessage('Admin role required.', true);
                logHealthDebug('warn', 'Availability test forbidden (403)');
                return;
            }
            if (!res.ok) {
                const text = await res.text();
                logHealthDebug('error', 'Availability test HTTP error body', text);
                throw new Error(`Test failed (${res.status}): ${text}`);
            }

            const data = await res.json();
            logHealthDebug('info', 'Availability test payload', data);
            const ok = Boolean(data.available);
            const status = String(data && data.status ? data.status : '').toUpperCase();
            const detail = data && data.details ? ` Details: ${data.details}` : '';
            const latency = (data && typeof data.latencyMs !== 'undefined') ? ` Latency: ${data.latencyMs}ms.` : '';
            const checked = data && data.checkedAt ? ` Checked: ${data.checkedAt}.` : '';

            if (status === 'DISABLED') {
                setMessage(`Availability test was not executed because healthcheck service is disabled.${checked}${detail}`, true);
            } else if (ok) {
                setMessage(`Availability test passed.${latency}${checked}${detail}`, false);
            } else {
                setMessage(`Availability test failed.${latency}${checked}${detail}`, true);
            }
        } catch (err) {
            logHealthDebug('error', 'Availability test request failed', err);
            setMessage(`Availability test error: ${err?.message || err}`, true);
        }
    }

    function wireEvents() {
        const loadBtn = $('loadWidgetHealthConfigBtn');
        const saveBtn = $('saveWidgetHealthConfigBtn');
        const testBtn = $('testWidgetHealthConfigBtn');

        if (loadBtn) {
            loadBtn.addEventListener('click', () => {
                loadWidgetHealthConfig();
            });
        }

        if (saveBtn) {
            saveBtn.addEventListener('click', () => {
                saveWidgetHealthConfig();
            });
        }

        if (testBtn) {
            testBtn.addEventListener('click', () => {
                testWidgetHealthNow();
            });
        }
    }

    function init() {
        if (!$('widgetHealthConfigForm')) {
            return;
        }
        wireEvents();
        loadWidgetHealthConfig();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    window.widgetHealthConfigUI = {
        load: loadWidgetHealthConfig,
        save: saveWidgetHealthConfig,
        testNow: testWidgetHealthNow
    };
})();
