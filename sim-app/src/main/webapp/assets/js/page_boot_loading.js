(function () {
    'use strict';

    function ensureOverlay(title, subtitle) {
        let overlay = document.getElementById('pageBootOverlay');
        if (overlay) {
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
        overlay.id = 'pageBootOverlay';
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

        if (document.body) {
            document.body.insertBefore(overlay, document.body.firstChild);
        }
        return overlay;
    }

    function begin(options) {
        const opts = options || {};
        const root = document.documentElement;

        root.classList.add('page-boot-loading');
        ensureOverlay(opts.title, opts.subtitle);

        let completed = false;
        const timeoutMs = Number.isFinite(Number(opts.timeoutMs)) ? Number(opts.timeoutMs) : 10000;
        const timer = window.setTimeout(finish, timeoutMs);

        function finish() {
            if (completed) {
                return;
            }
            completed = true;
            window.clearTimeout(timer);
            root.classList.remove('page-boot-loading');

            const overlay = document.getElementById('pageBootOverlay');
            if (overlay) {
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
