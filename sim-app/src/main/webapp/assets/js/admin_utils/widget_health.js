(function () {
    'use strict';

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

    function readForm() {
        const timeoutRaw = $('whcTimeoutMs')?.value;
        let timeoutMs = Number.parseInt(timeoutRaw, 10);
        if (!Number.isFinite(timeoutMs) || timeoutMs <= 0) {
            timeoutMs = 8000;
        }
        if (timeoutMs > 120000) {
            timeoutMs = 120000;
        }

        const payload = {
            healthcheckUrl: ($('whcHealthcheckUrl')?.value || '').trim(),
            method: (($('whcMethod')?.value || 'GET').trim().toUpperCase()),
            timeoutMs: timeoutMs,
            expectJsonField: toNullIfBlank($('whcExpectJsonField')?.value),
            expectJsonValue: toNullIfBlank($('whcExpectJsonValue')?.value),
            widgetId: toNullIfBlank($('whcWidgetId')?.value),

            // New optional request-shaping fields
            requestOrigin: toNullIfBlank($('whcRequestOrigin')?.value),
            requestReferer: toNullIfBlank($('whcRequestReferer')?.value),
            requestUserAgent: toNullIfBlank($('whcRequestUserAgent')?.value),
            requestCookie: toNullIfBlank($('whcRequestCookie')?.value)
        };

        if (!['GET', 'HEAD', 'POST'].includes(payload.method)) {
            payload.method = 'GET';
        }

        return payload;
    }

    function fillForm(cfg) {
        if (!cfg || typeof cfg !== 'object') {
            return;
        }

        if ($('whcHealthcheckUrl')) {
            $('whcHealthcheckUrl').value = cfg.healthcheckUrl || '';
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
            $('whcRequestCookie').value = cfg.requestCookie || '';
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
                throw new Error('Load failed (' + res.status + '): ' + text);
            }

            const data = await res.json();
            fillForm(data);
            setMessage('Widget health config loaded.', false);
        } catch (err) {
            setMessage('Failed to load widget health config: ' + (err?.message || err), true);
        }
    }

    async function saveWidgetHealthConfig() {
        const payload = readForm();

        if (!payload.healthcheckUrl) {
            setMessage('Healthcheck URL is required.', true);
            return;
        }

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
                throw new Error('Save failed (' + res.status + '): ' + text);
            }

            const saved = await res.json();
            fillForm(saved);

            const meta = [];
            if (saved.updatedBy) {
                meta.push('updatedBy=' + saved.updatedBy);
            }
            if (saved.updatedAt) {
                meta.push('updatedAt=' + saved.updatedAt);
            }

            setMessage('Widget health config saved.' + (meta.length ? ' (' + meta.join(', ') + ')' : ''), false);
        } catch (err) {
            setMessage('Failed to save widget health config: ' + (err?.message || err), true);
        }
    }

    async function testWidgetHealthNow() {
        setMessage('Running availability test...', false);
        try {
            const res = await fetch(endpoint('/admin/widget-availability.json'), {
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
                throw new Error('Test failed (' + res.status + '): ' + text);
            }

            const data = await res.json();
            const ok = !!data.available;
            const detail = data && data.details ? ` Details: ${data.details}` : '';
            const latency = (data && typeof data.latencyMs !== 'undefined') ? ` Latency: ${data.latencyMs}ms.` : '';
            const checked = data && data.checkedAt ? ` Checked: ${data.checkedAt}.` : '';

            if (ok) {
                setMessage('Availability test passed.' + latency + checked + detail, false);
            } else {
                setMessage('Availability test failed.' + latency + checked + detail, true);
            }
        } catch (err) {
            setMessage('Availability test error: ' + (err?.message || err), true);
        }
    }

    function wireEvents() {
        const loadBtn = $('loadWidgetHealthConfigBtn');
        const saveBtn = $('saveWidgetHealthConfigBtn');
        const testBtn = $('testWidgetHealthConfigBtn');

        if (loadBtn) {
            loadBtn.addEventListener('click', function () {
                loadWidgetHealthConfig();
            });
        }

        if (saveBtn) {
            saveBtn.addEventListener('click', function () {
                saveWidgetHealthConfig();
            });
        }

        if (testBtn) {
            testBtn.addEventListener('click', function () {
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
