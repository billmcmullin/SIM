(() => {
    'use strict';

    const cfg = window.dashboardConfig || {};
    const contextPath = cfg.contextPath || '';

    let inlineSection = null;
    let inlineTitle = null;
    let inlineContent = null;
    let currentUrl = '';
    let defaultSections = [];
    let transitionSections = [];
    let injectedScripts = [];
    let injectedEventListeners = [];
    let injectedIntervals = [];
    let injectedTimeouts = [];
    let inlineLoadSequence = 0;

    function toAbsoluteUrl(url) {
        try {
            return new URL(url, window.location.origin);
        } catch {
            return null;
        }
    }

    function isDashboardDetailPath(pathname) {
        if (!pathname) {
            return false;
        }
        const dashboardPrefix = `${contextPath}/dashboard/`;
        if (!pathname.startsWith(dashboardPrefix)) {
            return false;
        }

        // Keep API/internal utility endpoints as direct calls.
        return !pathname.endsWith('.json')
            && !pathname.endsWith('/select')
            && !pathname.endsWith('/data')
            && !pathname.endsWith('/label');
    }

    function getTitleFromUrl(url) {
        if (!url) {
            return 'Dashboard Detail';
        }

        const path = url.pathname || '';
        const clean = path.replace(`${contextPath}/dashboard/`, '');
        if (!clean) {
            return 'Dashboard Detail';
        }

        return clean
            .split('/')
            .map(part => part.replace(/[-_]+/g, ' '))
            .map(part => part.charAt(0).toUpperCase() + part.slice(1))
            .join(' / ');
    }

    function isNumericOnlyTitle(title) {
        if (!title) {
            return false;
        }
        const compact = String(title).trim().replace(/,/g, '');
        return /^[+-]?\d+(?:\.\d+)?$/.test(compact);
    }

    function resolveInlineTitle(title, absUrl) {
        const normalized = typeof title === 'string' ? title.trim() : '';
        if (!normalized || isNumericOnlyTitle(normalized)) {
            return getTitleFromUrl(absUrl);
        }
        return normalized;
    }

    function clearInjectedScripts() {
        for (const scriptEl of injectedScripts) {
            try {
                scriptEl.remove();
            } catch {
                // ignore
            }
        }
        injectedScripts = [];

        for (const binding of injectedEventListeners) {
            try {
                binding.target.removeEventListener(binding.type, binding.listener, binding.options);
            } catch {
                // ignore
            }
        }
        injectedEventListeners = [];

        for (const intervalId of injectedIntervals) {
            try {
                window.clearInterval(intervalId);
            } catch {
                // ignore
            }
        }
        injectedIntervals = [];

        for (const timeoutId of injectedTimeouts) {
            try {
                window.clearTimeout(timeoutId);
            } catch {
                // ignore
            }
        }
        injectedTimeouts = [];
    }

    function normalizePathAndQuery(absUrl) {
        if (!absUrl) {
            return '';
        }
        return `${absUrl.pathname}${absUrl.search || ''}`;
    }

    function setHomeButtonActive(active) {
        const homeBtn = document.querySelector('.dashboard-switch-btn[data-home="true"]');
        if (!homeBtn) {
            return;
        }
        homeBtn.classList.toggle('active', Boolean(active));
        homeBtn.setAttribute('aria-pressed', active ? 'true' : 'false');
    }

    function setActiveSwitchButton(url) {
        setHomeButtonActive(false);

        const abs = toAbsoluteUrl(url);
        const targetFull = normalizePathAndQuery(abs);
        const targetPath = abs ? abs.pathname : '';

        const buttons = document.querySelectorAll('.dashboard-switch-btn[data-target]');
        buttons.forEach(button => {
            const rawTarget = button.getAttribute('data-target') || '';
            const absTarget = toAbsoluteUrl(rawTarget);
            const full = normalizePathAndQuery(absTarget);
            const path = absTarget ? absTarget.pathname : '';

            let active = full === targetFull;
            if (!active && targetPath && path && targetPath === path) {
                // Handles minor query differences while still indicating selected page.
                active = true;
            }

            button.classList.toggle('active', active);
            button.setAttribute('aria-pressed', active ? 'true' : 'false');
        });
    }

    function clearActiveSwitchButtons() {
        setHomeButtonActive(false);

        const buttons = document.querySelectorAll('.dashboard-switch-btn[data-target]');
        buttons.forEach(button => {
            button.classList.remove('active');
            button.setAttribute('aria-pressed', 'false');
        });
    }

    async function runScriptsSequentially(scripts, baseUrl) {
        if (!Array.isArray(scripts) || !scripts.length) {
            return;
        }

        const currentLoadId = ++inlineLoadSequence;

        const originalAddEventListener = document.addEventListener.bind(document);
        const originalWindowAddEventListener = window.addEventListener.bind(window);
        const originalSetInterval = window.setInterval.bind(window);
        const originalSetTimeout = window.setTimeout.bind(window);

        document.addEventListener = function patchedAddEventListener(type, listener, options) {
            if (type === 'DOMContentLoaded') {
                try {
                    const EventCtor = window.Event;
                    const evt = typeof EventCtor === 'function' ? new EventCtor('DOMContentLoaded') : null;
                    if (typeof listener === 'function') {
                        listener.call(document, evt);
                    } else if (listener && typeof listener.handleEvent === 'function') {
                        listener.handleEvent(evt);
                    }
                } catch {
                    // ignore callback failures and continue loading
                }
                return undefined;
            }

            injectedEventListeners.push({ target: document, type, listener, options });
            originalAddEventListener(type, listener, options);
            return undefined;
        };

        window.addEventListener = function patchedWindowAddEventListener(type, listener, options) {
            injectedEventListeners.push({ target: window, type, listener, options });
            return originalWindowAddEventListener(type, listener, options);
        };

        window.setInterval = function patchedSetInterval(handler, timeout, ...args) {
            const id = originalSetInterval(handler, timeout, ...args);
            injectedIntervals.push(id);
            return id;
        };

        window.setTimeout = function patchedSetTimeout(handler, timeout, ...args) {
            const id = originalSetTimeout(handler, timeout, ...args);
            injectedTimeouts.push(id);
            return id;
        };

        try {
            for (const srcScript of scripts) {
                const scriptEl = document.createElement('script');

                const type = (srcScript.getAttribute('type') || '').trim();
                if (type) {
                    scriptEl.type = type;
                }

                const noModule = srcScript.hasAttribute('nomodule');
                if (noModule) {
                    scriptEl.noModule = true;
                }

                const crossOrigin = (srcScript.getAttribute('crossorigin') || '').trim();
                if (crossOrigin) {
                    scriptEl.crossOrigin = crossOrigin;
                }

                const referrerPolicy = (srcScript.getAttribute('referrerpolicy') || '').trim();
                if (referrerPolicy) {
                    scriptEl.referrerPolicy = referrerPolicy;
                }

                const integrity = (srcScript.getAttribute('integrity') || '').trim();
                if (integrity) {
                    scriptEl.integrity = integrity;
                }

                const src = (srcScript.getAttribute('src') || '').trim();
                if (src) {
                    const srcUrl = new URL(src, baseUrl);
                    if ((scriptEl.type || '').trim().toLowerCase() === 'module') {
                        // ES modules are cached per-URL and may not re-run on reinjection.
                        // Add a per-transition marker so entry modules execute every time.
                        srcUrl.searchParams.set('_inlineLoad', String(currentLoadId));
                    }
                    scriptEl.src = srcUrl.toString();
                } else {
                    scriptEl.text = srcScript.textContent || '';
                }

                scriptEl.async = false;
                document.body.appendChild(scriptEl);
                injectedScripts.push(scriptEl);

                if (scriptEl.src) {
                    await new Promise((resolve, reject) => {
                        scriptEl.addEventListener('load', resolve, { once: true });
                        scriptEl.addEventListener('error', () => reject(new Error(`Failed to load script: ${scriptEl.src}`)), { once: true });
                    });
                }
            }
        } finally {
            document.addEventListener = originalAddEventListener;
            window.addEventListener = originalWindowAddEventListener;
            window.setInterval = originalSetInterval;
            window.setTimeout = originalSetTimeout;
        }
    }

    async function loadDetailIntoInlineSection(absUrl, title) {
        if (!inlineSection || !inlineContent || !inlineTitle) {
            window.location.href = absUrl.toString();
            return;
        }

        clearInjectedScripts();
        inlineTitle.textContent = resolveInlineTitle(title, absUrl);
        inlineContent.innerHTML = '<p class="helper-note">Loading page content...</p>';
        inlineContent.scrollTop = 0;
        inlineContent.scrollLeft = 0;

        const res = await fetch(absUrl.toString(), {
            credentials: 'same-origin',
            headers: { Accept: 'text/html' }
        });

        if (!res.ok) {
            throw new Error(`Unable to load page (${res.status})`);
        }

        const html = await res.text();
        const DomParserCtor = window.DOMParser;
        if (typeof DomParserCtor !== 'function') {
            throw new Error('DOMParser is unavailable in this browser runtime.');
        }
        const doc = new DomParserCtor().parseFromString(html, 'text/html');

        const fetchedContainer = doc.querySelector('.container');
        if (!fetchedContainer) {
            throw new Error('Loaded page has no content container.');
        }

        inlineContent.innerHTML = fetchedContainer.innerHTML;
        inlineContent.scrollTop = 0;
        inlineContent.scrollLeft = 0;
        showInlineSection();

        const scripts = Array.from(doc.querySelectorAll('script'));
        await runScriptsSequentially(scripts, absUrl.toString());
    }

    function showInlineSection() {
        if (!inlineSection) {
            return;
        }

        transitionSections.forEach(section => {
            section.style.display = 'none';
        });

        inlineSection.style.display = '';
        if (inlineContent) {
            inlineContent.scrollTop = 0;
            inlineContent.scrollLeft = 0;
        }
        inlineSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function showDefaultSections() {
        defaultSections.forEach(section => {
            section.style.display = '';
        });

        if (inlineSection) {
            inlineSection.style.display = 'none';
        }

        if (inlineContent) {
            inlineContent.innerHTML = '';
        }

        clearInjectedScripts();
        clearActiveSwitchButtons();
        setHomeButtonActive(true);

        currentUrl = '';
    }

    async function openOrNavigate(url, title) {
        const abs = toAbsoluteUrl(url);
        if (!abs) {
            return false;
        }

        if (!isDashboardDetailPath(abs.pathname)) {
            window.location.href = abs.toString();
            return true;
        }

        if (!inlineSection || !inlineContent || !inlineTitle) {
            window.location.href = abs.toString();
            return true;
        }

        currentUrl = abs.toString();
        try {
            await loadDetailIntoInlineSection(abs, title);
            setActiveSwitchButton(currentUrl);
        } catch (e) {
            inlineContent.innerHTML = `<p class="helper-note" style="color:#b91c1c;">${(e && e.message) ? e.message : 'Unable to load this dashboard view inline.'}</p>`;
            showInlineSection();
            setActiveSwitchButton(currentUrl);
        }
        return true;
    }

    function bindDelegatedDashboardLinks() {
        document.addEventListener('click', event => {
            if (event.defaultPrevented) {
                return;
            }
            if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
                return;
            }

            const anchor = event.target.closest('a[href]');
            if (!anchor) {
                return;
            }

            // Dynamic metric links resolve their destination at click-time.
            // Let the dedicated metric handler run first.
            if (anchor.classList && anchor.classList.contains('metric-dynamic-link')) {
                return;
            }

            if (anchor.target && anchor.target !== '_self') {
                return;
            }

            const href = anchor.getAttribute('href');
            if (!href || href.startsWith('#')) {
                return;
            }

            const abs = toAbsoluteUrl(href);
            if (!abs || !isDashboardDetailPath(abs.pathname)) {
                return;
            }

            event.preventDefault();
            void openOrNavigate(abs.toString(), anchor.textContent.trim() || undefined);
        });
    }

    function init() {
        inlineSection = document.getElementById('dashboardInlineSection');
        inlineTitle = document.getElementById('dashboardInlineTitle');
        inlineContent = document.getElementById('dashboardInlineContent');

        if (!inlineSection || !inlineTitle || !inlineContent) {
            return;
        }

        defaultSections = Array.from(document.querySelectorAll('.container > section')).filter(section => section.id !== 'dashboardInlineSection');
        transitionSections = defaultSections.filter(section => section.id !== 'dashboardHomeSection');

        bindDelegatedDashboardLinks();
        setHomeButtonActive(true);
    }

    window.DashboardInlineView = {
        openOrNavigate,
        showDefaultSections
    };

    document.addEventListener('DOMContentLoaded', init);
})();
