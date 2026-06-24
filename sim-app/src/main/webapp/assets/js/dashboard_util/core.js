(() => {
    'use strict';

    function parseSlices(input) {
        if (Array.isArray(input)) return input;
        if (typeof input !== 'string') return [];
        try {
            const parsed = JSON.parse(input);
            return Array.isArray(parsed) ? parsed : [];
        } catch {
            return [];
        }
    }

    function parseObject(input) {
        if (input && typeof input === 'object' && !Array.isArray(input)) return input;
        if (typeof input !== 'string') return {};
        try {
            const parsed = JSON.parse(input);
            return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
        } catch {
            return {};
        }
    }

    function parseTrendData(input) {
        if (input && typeof input === 'object' && !Array.isArray(input)) return input;
        if (typeof input !== 'string') return { labels: [], values: [], days: 5 };
        try {
            const parsed = JSON.parse(input);
            return (parsed && typeof parsed === 'object' && !Array.isArray(parsed))
                ? parsed
                : { labels: [], values: [], days: 5 };
        } catch {
            return { labels: [], values: [], days: 5 };
        }
    }

    function buildSeries(slices, palette) {
        const labels = new Array(slices.length);
        const values = new Array(slices.length);
        const colors = new Array(slices.length);
        for (let i = 0; i < slices.length; i++) {
            const slice = slices[i] || {};
            labels[i] = slice.label ?? '';
            values[i] = typeof slice.count === 'number' ? slice.count : 0;
            colors[i] = palette[i % palette.length];
        }
        return { labels, values, colors };
    }

    function esc(v) {
        if (v === null || typeof v === 'undefined') return '';
        const str = String(v);
        if (!/[&<>"']/.test(str)) return str;
        return str
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function toYmd(d) {
        if (!(d instanceof Date) || Number.isNaN(d.getTime())) return '';
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    }

    function getTodayYesterday() {
        if (window.dashboardDates?.today && window.dashboardDates?.yesterday) {
            return { today: window.dashboardDates.today, yesterday: window.dashboardDates.yesterday };
        }
        const today = new Date();
        const yesterday = new Date();
        yesterday.setDate(today.getDate() - 1);
        return { today: toYmd(today), yesterday: toYmd(yesterday) };
    }

    window.DashboardCore = {
        parseSlices,
        parseObject,
        parseTrendData,
        buildSeries,
        esc,
        toYmd,
        getTodayYesterday
    };
})();
