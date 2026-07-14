// utils.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const Utils = {
        escapeHtml(value) {
            if (value === null || value === undefined) return '';
            return String(value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        },

        formatHumanReadableTimestamp(value) {
            if (!value) return 'never';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return value;
            return date.toLocaleString(undefined, {
                year: 'numeric', month: 'short', day: 'numeric',
                hour: '2-digit', minute: '2-digit'
            });
        },

        parseContentDispositionFilename(cd) {
            try {
                if (!cd) return null;
                const m = /filename\*=UTF-8''([^;]+)|filename=\"?([^\";]+)\"?/.exec(cd);
                if (!m) return null;
                return decodeURIComponent(m[1] || m[2]);
            } catch {
                return null;
            }
        }
    };
    window.AdminPage.Utils = Utils;
})();
