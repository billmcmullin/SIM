(() => {
    'use strict';

    if (window.SimNavigation) {
        return;
    }

    const script = document.currentScript;
    const contextPath = script.getAttribute('data-context-path') || '';

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
        destination.searchParams.set('returnTo', window.location.href);
        anchor.href = destination.href;
    }

    window.SimNavigation = { back, updateButtons };
    document.addEventListener('click', rememberSource, true);
    document.addEventListener('auxclick', rememberSource, true);
    document.addEventListener('contextmenu', rememberSource, true);
    window.addEventListener('popstate', updateButtons);
    document.addEventListener('DOMContentLoaded', updateButtons);
    window.addEventListener('pageshow', updateButtons);
})();