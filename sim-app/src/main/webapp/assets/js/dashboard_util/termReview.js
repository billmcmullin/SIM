(() => {
    'use strict';

    async function buildTermReviewSelectionLink(contextPath, term, increaseOnly) {
        const qp = new URLSearchParams();
        qp.set('term', term || '');
        if (increaseOnly) qp.set('mode', 'increaseOnly');

        const url = `${contextPath}/dashboard/term-review/select?${qp.toString()}`;
        const resp = await fetch(url, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { Accept: 'application/json' }
        });

        const text = await resp.text();
        let data = null;
        try {
            data = text ? JSON.parse(text) : null;
        } catch {
            data = null;
        }

        if (!resp.ok) {
            const msg = data?.message || `Unable to open term review (HTTP ${resp.status})`;
            throw new Error(msg);
        }

        if (!data || data.status !== 'ok' || !data.selectionId) {
            throw new Error(data?.message || 'Unable to create term review selection.');
        }

        return data.reviewUrl
            || `${contextPath}/dashboard/widgets/drilldown/review?selectionId=${encodeURIComponent(data.selectionId)}`;
    }

    window.DashboardTermReview = {
        buildTermReviewSelectionLink
    };
})();
