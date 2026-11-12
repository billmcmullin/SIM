(() => {
    'use strict';

    if (window.SimNavigation) {
        return;
    }

    const script = document.currentScript;
    const contextPath = script.getAttribute('data-context-path') || '';
    const scopeKey = `${contextPath}:sim.navigation.scope`;
    const listCaptures = new Map();

    function owner() {
        const name = document.querySelector('.top-bar .button-row span strong')?.textContent.trim() || '';
        const role = document.querySelector('.top-bar')?.getAttribute('data-role') || '';
        return `${name}|${role}`;
    }

    function sessionScope() {
        try {
            let scope = sessionStorage.getItem(scopeKey);
            if (!scope) {
                scope = Array.from(crypto.getRandomValues(new Uint32Array(4)), value => value.toString(16)).join('-');
                sessionStorage.setItem(scopeKey, scope);
            }
            return scope;
        } catch {
            return null;
        }
    }

    function saveListState(key, value) {
        const scope = sessionScope();
        if (!scope) {
            return;
        }
        const current = window.history.state || {};
        try {
            window.history.replaceState({ ...current, simLists: { ...current.simLists,
                [key]: { scope, owner: owner(), path: window.location.pathname, value } } }, '');
        } catch {
            return;
        }
    }

    function readListState(key) {
        const saved = window.history.state?.simLists?.[key];
        return saved && saved.scope === sessionScope() && saved.owner === owner()
            && saved.path === window.location.pathname ? saved.value : null;
    }

    function captureListStates() {
        listCaptures.forEach((entry, key) => {
            if (entry.root.isConnected) {
                saveListState(key, entry.capture());
            } else {
                listCaptures.delete(key);
            }
        });
    }

    function registerListState(key, root, capture) {
        if (root) listCaptures.set(key, { root, capture });
    }

    function captureScroll() {
        const inline = document.getElementById('dashboardInlineContent');
        return { x: window.scrollX, y: window.scrollY, inlineX: inline?.scrollLeft || 0, inlineY: inline?.scrollTop || 0 };
    }

    function restoreScroll(position) {
        if (!position) {
            return;
        }
        requestAnimationFrame(() => {
            window.scrollTo(Number(position.x) || 0, Number(position.y) || 0);
            document.getElementById('dashboardInlineContent')?.scrollTo(Number(position.inlineX) || 0, Number(position.inlineY) || 0);
        });
    }

    function safePageUrl(value) {
        if (!value || value.length > 8192) {
            return null;
        }
        try {
            const url = new URL(value, window.location.href);
            if (url.origin !== window.location.origin || !url.pathname.startsWith(`${contextPath}/`)
                || /\/(?:login|logout|api|assets)(?:\/|$)/.test(url.pathname)) {
                return null;
            }
            return url;
        } catch {
            return null;
        }
    }

    function previousPage() {
        const current = new URL(window.location.href);
        const previous = safePageUrl(current.searchParams.get('returnTo'))
            || safePageUrl(document.referrer);
        if (!previous) {
            return null;
        }
        const comparable = new URL(current);
        comparable.searchParams.delete('returnTo');
        const previousComparable = new URL(previous);
        previousComparable.searchParams.delete('returnTo');
        return comparable.href === previousComparable.href ? null : previous;
    }

    function hasPreviousHistory() {
        return window.navigation ? window.navigation.canGoBack : window.history.length > 1;
    }

    function canGoBack() {
        return hasPreviousHistory() || Boolean(previousPage());
    }

    function updateButtons() {
        document.querySelectorAll('[data-sim-back]').forEach(button => {
            button.disabled = !canGoBack();
            button.title = button.disabled ? 'No previous page' : 'Back to previous page';
        });
    }

    function back() {
        if (!canGoBack()) {
            return;
        }
        captureListStates();
        if (hasPreviousHistory()) {
            window.history.back();
            return;
        }
        const previous = previousPage();
        if (previous) {
            window.location.replace(previous.href);
        }
    }

    function rememberSource(event) {
        const command = event.target.closest('button, a');
        if (command?.textContent.trim() === 'Logout') {
            listCaptures.clear();
            try {
                sessionStorage.removeItem(scopeKey);
            } catch {
                return;
            }
            return;
        }
        const anchor = event.target.closest('a[href]');
        if (!anchor || anchor.hasAttribute('download')) {
            return;
        }
        const href = anchor.getAttribute('href');
        if (!href || href.startsWith('#')) {
            return;
        }
        const destination = safePageUrl(href);
        if (!destination || destination.href === window.location.href) {
            return;
        }
        captureListStates();
        destination.searchParams.set('returnTo', window.location.href);
        anchor.href = destination.href;
    }

    function navigate(value, title) {
        const destination = safePageUrl(value);
        if (!destination) {
            return false;
        }
        captureListStates();
        destination.searchParams.set('returnTo', window.location.href);
        if (window.DashboardInlineView) {
            void window.DashboardInlineView.openOrNavigate(destination.href, title);
        } else {
            window.location.href = destination.href;
        }
        return true;
    }

    window.SimNavigation = { back, updateButtons, saveListState, readListState, registerListState,
        captureListStates, captureScroll, restoreScroll, navigate };
    document.addEventListener('click', rememberSource, true);
    document.addEventListener('auxclick', rememberSource, true);
    document.addEventListener('contextmenu', rememberSource, true);
    document.addEventListener('click', captureListStates);
    document.addEventListener('change', captureListStates);
    document.addEventListener('input', captureListStates);
    window.addEventListener('scrollend', captureListStates, true);
    window.addEventListener('popstate', updateButtons);
    document.addEventListener('DOMContentLoaded', updateButtons);
    window.addEventListener('pageshow', updateButtons);
    window.addEventListener('pagehide', captureListStates);
})();