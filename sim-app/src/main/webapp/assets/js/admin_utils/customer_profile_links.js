// assets/js/admin_utils/customer-profile-links.js
(function () {
    'use strict';

    window.AdminPage = window.AdminPage || {};
    window.AdminPage.CustomerProfileLinks = {
        init,
        linkifyWithin
    };

    function init(options) {
        const contextPath = options?.contextPath || '';
        const root = options?.root || document;
        linkifyWithin(root, contextPath);
    }

    function linkifyWithin(root, contextPath) {
        if (!root) {
            return;
        }

        // 1) Generic hook: any element marked with data attributes
        root.querySelectorAll('.js-customer-profile-link').forEach((el) => {
            linkElement(el, contextPath);
        });

        // 2) Session table fallback conventions (if existing pages don't add hooks yet)
        // session id cells
        root.querySelectorAll(
            '.session-id-col, td[data-column="session_id"], td[data-field="sessionId"]'
        ).forEach((el) => {
            if (!el.classList.contains('js-customer-profile-link')) {
                el.classList.add('js-customer-profile-link');
            }
            if (!el.getAttribute('data-session-id')) {
                const sid = text(el);
                if (sid) {
                    el.setAttribute('data-session-id', sid);
                }
            }
            linkElement(el, contextPath);
        });

        // friendly name cells
        root.querySelectorAll(
            '.friendly-name-col, td[data-column="friendly_name"], td[data-field="friendlyName"]'
        ).forEach((el) => {
            if (!el.classList.contains('js-customer-profile-link')) {
                el.classList.add('js-customer-profile-link');
            }
            if (!el.getAttribute('data-friendly-name')) {
                const fn = text(el);
                if (fn) {
                    el.setAttribute('data-friendly-name', fn);
                }
            }

            // try finding session id in same row so route is canonical by sessionId
            const row = el.closest('tr');
            const sidCell = row?.querySelector('.session-id-col, td[data-column="session_id"], td[data-field="sessionId"]');
            const sid = sidCell ? text(sidCell) : '';
            if (sid && !el.getAttribute('data-session-id')) {
                el.setAttribute('data-session-id', sid);
            }

            linkElement(el, contextPath);
        });
    }

    function linkElement(el, contextPath) {
        if (!el || el.dataset.cpLinked === 'true') {
            return;
        }

        // Already an anchor?
        if (el.tagName === 'A') {
            el.classList.add('customer-profile-link');
            el.dataset.cpLinked = 'true';
            return;
        }

        const sid = (el.getAttribute('data-session-id') || '').trim();
        const friendlyName = (el.getAttribute('data-friendly-name') || '').trim();
        if (!sid && !friendlyName) {
            return;
        }

        const qs = new URLSearchParams();
        if (sid) {
            qs.set('sessionId', sid);
        } else {
            qs.set('friendlyName', friendlyName);
        }

        const href = `${contextPath}/customer-profile?${qs.toString()}`;

        const a = document.createElement('a');
        a.href = href;
        a.className = 'customer-profile-link';
        a.textContent = text(el) || (sid || friendlyName);
        a.title = `Open customer profile${sid ? ` (${sid})` : ''}`;

        // replace cell content, keep cell itself
        el.textContent = '';
        el.appendChild(a);

        el.dataset.cpLinked = 'true';
    }

    function text(el) {
        return (el?.textContent || '').trim();
    }
})();
