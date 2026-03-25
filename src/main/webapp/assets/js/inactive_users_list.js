(function () {
    const cfg = window.inactiveUsersListConfig || {};
    const contextPath = cfg.contextPath || '';
    const scope = cfg.scope || 'all';
    const widgetId = cfg.widgetId || '';
    const days = Number(cfg.days || 7);
    let page = Number(cfg.page || 1);
    let limit = Number(cfg.limit || 10);
    const total = Number(cfg.total || 0);
    const totalPages = Number(cfg.totalPages || 1);

    let data = cfg.data || { rows: [] };
    if (typeof data === 'string') {
        try { data = JSON.parse(data); } catch (_) { data = { rows: [] }; }
    }

    const body = document.getElementById('inactiveListBody');
    const pager = document.getElementById('pagerTop');

    function esc(v) {
        if (v == null) return '';
        return String(v).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;').replaceAll("'", '&#39;');
    }

    function fmt(ts) {
        if (!ts) return '—';
        try { return new Date(ts).toLocaleString(); } catch (_) { return ts; }
    }

    function reviewUrl(sessionId) {
        return `${contextPath}/dashboard/sessions/drilldown/session-review?sessionId=${encodeURIComponent(sessionId || '')}`;
    }

    function buildUrl(newPage, newLimit) {
        const params = new URLSearchParams();
        params.set('scope', scope);
        params.set('days', String(days));
        params.set('page', String(newPage));
        params.set('limit', String(newLimit));
        if (scope === 'widget' && widgetId) params.set('widgetId', widgetId);
        return `${contextPath}/dashboard/inactive-users/list?${params.toString()}`;
    }

    function renderPager() {
        pager.innerHTML = `
            <button type="button" class="ghost-btn" id="backBtn" ${page <= 1 ? 'disabled' : ''}>Back</button>
            <button type="button" class="ghost-btn" id="nextBtn" ${page >= totalPages ? 'disabled' : ''}>Next</button>
            <span>Page ${page} of ${totalPages}</span>
            <label for="limitSel" style="margin:0 0 0 8px;">Show:</label>
            <select id="limitSel" style="width:auto;">
                <option value="10" ${limit === 10 ? 'selected' : ''}>10</option>
                <option value="20" ${limit === 20 ? 'selected' : ''}>20</option>
                <option value="50" ${limit === 50 ? 'selected' : ''}>50</option>
                <option value="100" ${limit === 100 ? 'selected' : ''}>100</option>
            </select>
        `;

        pager.querySelector('#backBtn')?.addEventListener('click', () => {
            if (page > 1) window.location.href = buildUrl(page - 1, limit);
        });
        pager.querySelector('#nextBtn')?.addEventListener('click', () => {
            if (page < totalPages) window.location.href = buildUrl(page + 1, limit);
        });
        pager.querySelector('#limitSel')?.addEventListener('change', (e) => {
            const newLimit = parseInt(e.target.value, 10) || 10;
            window.location.href = buildUrl(1, newLimit);
        });
    }

    function renderRows() {
        const rows = Array.isArray(data.rows) ? data.rows : [];
        if (!rows.length) {
            body.innerHTML = `<tr><td colspan="4" class="empty-row">No inactive users found.</td></tr>`;
            return;
        }

        body.innerHTML = rows.map(r => {
            const sid = r.sessionId || '';
            const label = r.displayLabel || sid;
            const widget = r.widgetLabel || r.widgetId || '—';
            const count = Number(r.chatCount || 0);
            return `<tr>
                <td>
                    <div>${esc(label)}</div>
                    ${label !== sid ? `<div class="session-id-muted">${esc(sid)}</div>` : ''}
                </td>
                <td>${esc(widget)}</td>
                <td><button type="button" class="ghost-btn count-btn" data-sid="${esc(sid)}">${count} chats</button></td>
                <td>${esc(fmt(r.lastEntry))}</td>
            </tr>`;
        }).join('');

        body.addEventListener('click', (e) => {
            const btn = e.target.closest('.count-btn');
            if (!btn) return;
            const sid = btn.dataset.sid;
            if (sid) window.location.href = reviewUrl(sid);
        });
    }

    renderPager();
    renderRows();
})();
