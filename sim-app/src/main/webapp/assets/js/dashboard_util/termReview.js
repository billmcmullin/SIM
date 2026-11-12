(() => {
    'use strict';

    function wireChatPreview(contextPath) {
        const body = document.getElementById('otherParasoftLatestBody');
        const root = document.getElementById('dashboardChatPreview');
        if (!body || !root || !window.SimChatPreview) return;
        const cache = new Map();
        let apiPromise = null;
        const key = row => JSON.stringify([row.widgetId, row.chatId]);
        const readRow = element => ({
            widgetId: element.dataset.widgetId || '', chatId: element.dataset.chatId || '',
            prompt: element.cells[2]?.textContent || '', widgetName: element.cells[1]?.textContent || ''
        });
        const rows = () => Array.from(body.rows).map(readRow).filter(row => row.widgetId && row.chatId);
        const rowButton = row => Array.from(body.rows).find(element => key(readRow(element)) === key(row))
            ?.querySelector('.chat-preview-open');
        const preview = window.SimChatPreview.create({
            root, prefix: 'dashboard', capabilities: { share: false, translate: true },
            placement: 'anchored-right', getAnchor: rowButton,
            onTranslate: payload => import(`${contextPath}/assets/js/widget_review_util/api_client.js`)
                .then(api => api.postJson(`${contextPath}/dashboard/widgets/drilldown/review/translate`, payload)),
            getRows: rows, getKey: key, onNavigate: row => void open(row),
            getReturnFocus: rowButton
        });
        async function open(row) {
            if (cache.has(key(row))) {
                preview.show(cache.get(key(row)));
                return;
            }
            preview.show({ ...row, response: 'Loading response...' }, true);
            const token = preview.beginRequest('detail');
            const isCurrent = () => preview.isCurrent(token) && body.getClientRects().length > 0;
            try {
                apiPromise ||= import(`${contextPath}/assets/js/widget_review_util/api_client.js`).catch(error => {
                    apiPromise = null;
                    throw error;
                });
                const api = await apiPromise;
                if (!isCurrent()) return;
                const detail = await api.resolvePreviewRow({
                    selectUrl: `${contextPath}/dashboard/widgets/review/start`,
                    detailUrl: `${contextPath}/dashboard/widgets/drilldown/view/review-data`,
                    payload: { widgetId: row.widgetId, selectedChatIds: [row.chatId] },
                    chatId: row.chatId, isCurrent
                });
                if (!detail || !isCurrent()) return;
                if (String(detail.widgetId || '') !== row.widgetId) throw new Error('Chat details did not match the requested widget.');
                cache.set(key(row), detail);
                preview.show(detail);
            } catch (error) {
                if (isCurrent()) preview.show({ ...row, response: `Unable to load response: ${error.message}`, translationAvailable: false });
            }
        }
        function decorateRows() {
            for (const element of body.rows) {
                if (element.hasAttribute('data-created-at') && element.cells[4]) {
                    let time = element.cells[4].querySelector('time.chat-created-at');
                    if (!time) {
                        time = document.createElement('time');
                        time.className = 'chat-created-at';
                        element.cells[4].replaceChildren(time);
                    }
                    window.SimChatPreview.setTimestamp(time, element.dataset.createdAt);
                }
                const row = readRow(element);
                const cell = element.cells[2];
                if (!cell || !row.widgetId || !row.chatId || cell.querySelector('.chat-preview-open')) continue;
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'chat-preview-open';
                button.textContent = row.prompt;
                button.setAttribute('aria-label', `Preview chat ${row.chatId}`);
                cell.replaceChildren(button);
            }
        }
        body.addEventListener('click', event => {
            if (!(event.target instanceof Element) || event.target.closest('a, input')) return;
            const element = event.target.closest('tr[data-chat-id][data-widget-id]');
            if (!element || !body.contains(element)) return;
            const row = readRow(element);
            if (row.widgetId && row.chatId) void open(row);
        });
        new MutationObserver(() => {
            preview.hide(false);
            cache.clear();
            decorateRows();
        }).observe(body, { childList: true });
        decorateRows();
    }

    document.addEventListener('DOMContentLoaded', () => wireChatPreview(window.dashboardConfig?.contextPath || ''), { once: true });

    async function buildTermReviewSelectionLink(contextPath, term, increaseOnly) {
        const qp = new URLSearchParams();
        qp.set('term', term || '');
        if (increaseOnly) {
            qp.set('mode', 'increaseOnly');
        }

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
