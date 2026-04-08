// File: src/main/webapp/assets/js/inactive_users_list.js
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
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const clearSearchBtn = document.getElementById('clearSearchBtn');

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

    function frLabel(r) {
        const score = Number(r?.frustrationScore || 0);
        if (score >= 0.7) return 'High';
        if (score >= 0.4) return 'Medium';
        if (score > 0) return 'Low';
        return 'None';
    }

    function frClass(label) {
        if (label === 'Low') return 'fr-badge fr-badge-low';
        if (label === 'Medium') return 'fr-badge fr-badge-medium';
        if (label === 'High') return 'fr-badge fr-badge-high';
        return '';
    }

    function frTooltip(r, label) {
        const reasonRaw = String(r?.frustrationReason || '').trim().toLowerCase();
        if (label === 'None') return '';

        if (!reasonRaw) return `Frustration: ${label}`;

        if (reasonRaw.startsWith('keyword:')) {
            const kw = reasonRaw.split(':')[1] || '';
            return `Frustration: ${label} • Keyword: ${kw}`;
        }
        if (reasonRaw.startsWith('misunderstood:')) {
            return `Frustration: ${label} • User feels misunderstood`;
        }
        if (reasonRaw === 'punctuation') {
            return `Frustration: ${label} • Strong punctuation`;
        }
        if (reasonRaw === 'all_caps') {
            return `Frustration: ${label} • Emphasis in ALL CAPS`;
        }

        return `Frustration: ${label}`;
    }

    function buildUrl(newPage, newLimit, searchValue) {
        const params = new URLSearchParams();
        params.set('scope', scope);
        params.set('days', String(days));
        params.set('page', String(newPage));
        params.set('limit', String(newLimit));
        if (scope === 'widget' && widgetId) params.set('widgetId', widgetId);

        const q = (searchValue ?? '').trim();
        if (q) params.set('search', q);

        return `${contextPath}/dashboard/inactive-users/list?${params.toString()}`;
    }

    function renderPager() {
        if (!pager) return;

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
            if (page > 1) window.location.href = buildUrl(page - 1, limit, searchInput?.value || cfg.search || '');
        });
        pager.querySelector('#nextBtn')?.addEventListener('click', () => {
            if (page < totalPages) window.location.href = buildUrl(page + 1, limit, searchInput?.value || cfg.search || '');
        });
        pager.querySelector('#limitSel')?.addEventListener('change', (e) => {
            const newLimit = parseInt(e.target.value, 10) || 10;
            window.location.href = buildUrl(1, newLimit, searchInput?.value || cfg.search || '');
        });
    }

    function renderRows() {
        if (!body) return;
        const rows = Array.isArray(data.rows) ? data.rows : [];
        if (!rows.length) {
            body.innerHTML = `<tr><td colspan="5" class="empty-row">No inactive users found.</td></tr>`;
            return;
        }

        body.innerHTML = rows.map(r => {
            const sid = r.sessionId || '';
            const label = r.displayLabel || sid;
            const widget = r.widgetLabel || r.widgetId || '—';
            const count = Number(r.chatCount || 0);
            const fr = frLabel(r);
            const tip = frTooltip(r, fr);

            return `<tr>
                <td>
                    <div>${esc(label)}</div>
                    ${label !== sid ? `<div class="session-id-muted">${esc(sid)}</div>` : ''}
                </td>
                <td>${esc(widget)}</td>
                <td><button type="button" class="ghost-btn count-btn" data-sid="${esc(sid)}">${count} chats</button></td>
                <td>
                    ${fr === 'None'
                    ? `<span title="${esc(tip)}">${esc(fr)}</span>`
                    : `<span class="${frClass(fr)}" title="${esc(tip)}">${esc(fr)}</span>`
                }
                </td>
                <td>${esc(fmt(r.lastEntry))}</td>
            </tr>`;
        }).join('');
    }

    function bindEvents() {
        const initialSearch = (cfg.search || '').trim();
        if (searchInput) searchInput.value = initialSearch;

        searchBtn?.addEventListener('click', () => {
            const q = searchInput?.value || '';
            window.location.href = buildUrl(1, limit, q);
        });

        clearSearchBtn?.addEventListener('click', () => {
            if (searchInput) searchInput.value = '';
            window.location.href = buildUrl(1, limit, '');
        });

        searchInput?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                const q = searchInput.value || '';
                window.location.href = buildUrl(1, limit, q);
            }
        });

        body?.addEventListener('click', (e) => {
            const btn = e.target.closest('.count-btn');
            if (!btn) return;
            const sid = btn.dataset.sid;
            if (sid) window.location.href = reviewUrl(sid);
        });
    }

    renderPager();
    renderRows();
    bindEvents();
})();
