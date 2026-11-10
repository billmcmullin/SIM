(function () {
    'use strict';

    function ensureOverlay(title, subtitle, inlineRoot) {
        const overlayId = inlineRoot ? 'inlinePageBootOverlay' : 'pageBootOverlay';
        let overlay = document.getElementById(overlayId);
        if (overlay) {
            overlay.style.display = '';
            overlay.setAttribute('aria-hidden', 'false');
            const titleEl = overlay.querySelector('.page-boot-title');
            const subtitleEl = overlay.querySelector('.page-boot-subtitle');
            if (titleEl) {
                titleEl.textContent = title || 'Loading page...';
            }
            if (subtitleEl) {
                subtitleEl.textContent = subtitle || 'Preparing your data view.';
            }
            return overlay;
        }

        overlay = document.createElement('div');
        overlay.id = overlayId;
        overlay.className = 'page-boot-overlay';
        overlay.setAttribute('role', 'status');
        overlay.setAttribute('aria-live', 'polite');

        overlay.innerHTML = [
            '<div class="page-boot-card">',
            '  <div class="page-boot-spinner" aria-hidden="true"></div>',
            `  <p class="page-boot-title">${String(title || 'Loading page...')}</p>`,
            `  <p class="page-boot-subtitle">${String(subtitle || 'Preparing your data view.')}</p>`,
            '</div>'
        ].join('');

        if (inlineRoot) {
            inlineRoot.insertBefore(overlay, inlineRoot.firstChild);
        } else if (document.body) {
            document.body.insertBefore(overlay, document.body.firstChild);
        }
        return overlay;
    }

    function begin(options) {
        const opts = options || {};
        const inlineRoot = document.documentElement.classList.contains('dashboard-inline-active')
            ? document.getElementById('dashboardInlineContent') : null;
        const root = inlineRoot || document.documentElement;

        if (inlineRoot) {
            document.documentElement.classList.remove('page-boot-loading');
        }
        root.classList.add('page-boot-loading');
        root.setAttribute('aria-busy', 'true');
        const overlay = ensureOverlay(opts.title, opts.subtitle, inlineRoot);

        let completed = false;
        const timeoutMs = Number.isFinite(Number(opts.timeoutMs)) ? Number(opts.timeoutMs) : 10000;
        const timer = window.setTimeout(finish, timeoutMs);

        function finish() {
            if (completed) {
                return;
            }
            completed = true;
            window.clearTimeout(timer);

            if (root.contains(overlay)) {
                root.classList.remove('page-boot-loading');
                root.setAttribute('aria-busy', 'false');
                overlay.style.display = 'none';
                overlay.setAttribute('aria-hidden', 'true');
            }
        }

        return { finish };
    }

    window.PageBootLoading = {
        begin
    };
})();
