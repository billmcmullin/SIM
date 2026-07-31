// utils.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Utils = {
        escapeHtml(value) {
            if (value === null || value === undefined) {
                return '';
            }
            return String(value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        },

        formatHumanReadableTimestamp(value, fallback = 'never') {
            if (!value) {
                return fallback;
            }
            const date = value instanceof Date ? value : new Date(value);
            if (Number.isNaN(date.getTime())) {
                return String(value);
            }

            const formatted = date.toLocaleString(undefined, {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: 'numeric',
                minute: '2-digit',
                hour12: true
            });
            return formatted.endsWith('.') ? formatted : `${formatted}.`;
        },

        parseContentDispositionFilename(cd) {
            try {
                if (!cd) {
                    return null;
                }
                const m = /filename\*=UTF-8''([^;]+)|filename=\"?([^\";]+)\"?/.exec(cd);
                if (!m) {
                    return null;
                }
                return decodeURIComponent(m[1] || m[2]);
            } catch {
                return null;
            }
        }
    };
    window.AdminPage.Utils = Utils;
})();
