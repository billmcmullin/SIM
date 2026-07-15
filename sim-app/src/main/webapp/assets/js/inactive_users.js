// File: src/main/webapp/assets/js/inactive_users.js
(function () {
    const cfg = window.inactiveUsersConfig || {};
    const contextPath = cfg.contextPath || '';
    const defaultDays = Number(cfg.defaultDays || 7);

    let data = { all: [], widgets: {}, widgetNames: {} };
    const encodedData = typeof cfg.dataB64 === 'string' ? cfg.dataB64 : '';

    if (encodedData) {
        try {
            data = JSON.parse(window.atob(encodedData));
        } catch {
            data = { all: [], widgets: {}, widgetNames: {} };
        }
    } else {
        data = cfg.data || data;
    }

    if (typeof data === 'string') {
        try { data = JSON.parse(data); } catch { data = { all: [], widgets: {}, widgetNames: {} }; }
    }
    if (!data || typeof data !== 'object') {
        data = { all: [], widgets: {}, widgetNames: {} };
    }
    if (!Array.isArray(data.all)) {
        data.all = [];
    }
    if (!data.widgets || typeof data.widgets !== 'object') {
        data.widgets = {};
    }
    if (!data.widgetNames || typeof data.widgetNames !== 'object') {
        data.widgetNames = {};
    }

    const allWidgetsBody = document.getElementById('allWidgetsBody');
    const widgetTablesContainer = document.getElementById('widgetTablesContainer');
    const daysSelect = document.getElementById('daysSelect');
    const applyDaysBtn = document.getElementById('applyDaysBtn');

    function esc(v) {
        if (v === null || v === undefined) {
            return '';
        }
        return String(v).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;').replaceAll("'", '&#39;');
    }

    function fmt(ts) {
        if (!ts) {
            return '—';
        }
        try { return new Date(ts).toLocaleString(); } catch { return ts; }
    }

    function reviewUrl(sessionId) {
        return `${contextPath}/dashboard/sessions/drilldown/session-review?sessionId=${encodeURIComponent(sessionId || '')}`;
    }

    function fullListUrl(scope, widgetId) {
        const params = new URLSearchParams();
        params.set('scope', scope);
        params.set('days', String(defaultDays));
        params.set('page', '1');
        params.set('limit', '10');
        if (widgetId) {
            params.set('widgetId', widgetId);
        }
        return `${contextPath}/dashboard/inactive-users/list?${params.toString()}`;
    }

    function frLabel(r) {
        const score = Number(r?.frustrationScore || 0);
        if (score >= 0.7) {
            return 'High';
        }
        if (score >= 0.4) {
            return 'Medium';
        }
        if (score > 0) {
            return 'Low';
        }
        return 'None';
    }

    function frClass(label) {
        if (label === 'Low') {
            return 'fr-badge fr-badge-low';
        }
        if (label === 'Medium') {
            return 'fr-badge fr-badge-medium';
        }
        if (label === 'High') {
            return 'fr-badge fr-badge-high';
        }
        return '';
    }

    function frTooltip(r, label) {
        const reasonRaw = String(r?.frustrationReason || '').trim().toLowerCase();
        if (label === 'None') {
            return '';
        }

        if (!reasonRaw) {
            return `Frustration: ${label}`;
        }

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

    function rowsHtml(rows) {
        if (!Array.isArray(rows) || rows.length === 0) {
            return `<tr><td colspan="4" class="empty-row">No inactive users found.</td></tr>`;
        }

        return rows.map(r => {
            const sid = r.sessionId || '';
            const label = r.displayLabel || sid;
            const count = Number(r.chatCount ?? r.chats ?? 0);
            const fr = frLabel(r);
            const tip = frTooltip(r, fr);

            return `<tr>
                <td>
                    <div>${esc(label)}</div>
                    ${label !== sid ? `<div class="session-id-muted">${esc(sid)}</div>` : ''}
                </td>
                <td>
                    <button type="button" class="ghost-btn inactive-count-btn" data-session-id="${esc(sid)}">
                        ${count} chats
                    </button>
                </td>
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

    function renderWidgetSection(widgetId, rows) {
        const friendly = data.widgetNames[widgetId] || widgetId || 'Unknown Widget';
        const section = document.createElement('section');
        section.className = 'inactive-widget-section';

        section.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center; gap:10px; flex-wrap:wrap;">
                <h3 style="margin:0;">Widget: ${esc(friendly)}</h3>
                <button type="button" class="ghost-btn widget-view-all-btn" data-widget-id="${esc(widgetId)}">
                    View all inactive users
                </button>
            </div>
            <div class="table-scroll">
                <table class="session-table">
                    <thead>
                        <tr>
                            <th>Session</th>
                            <th>Chat Count</th>
                            <th>Frustration</th>
                            <th>Last Entry</th>
                        </tr>
                    </thead>
                    <tbody>${rowsHtml(rows)}</tbody>
                </table>
            </div>
        `;
        widgetTablesContainer?.appendChild(section);
    }

    function render() {
        if (allWidgetsBody) {
            allWidgetsBody.innerHTML = rowsHtml(data.all || []);
        }

        const allSection = allWidgetsBody?.closest('.section');
        if (allSection && !allSection.querySelector('.all-view-all-btn')) {
            const wrap = document.createElement('div');
            wrap.style.display = 'flex';
            wrap.style.justifyContent = 'flex-end';
            wrap.style.marginBottom = '10px';
            wrap.innerHTML = `<button type="button" class="ghost-btn all-view-all-btn">View all inactive users</button>`;
            const tableScroll = allSection.querySelector('.table-scroll');
            if (tableScroll) {
                allSection.insertBefore(wrap, tableScroll);
            }
        }

        if (widgetTablesContainer) {
            widgetTablesContainer.innerHTML = '';
        }
        const widgetIds = Object.keys(data.widgets || {});
        if (!widgetIds.length) {
            if (widgetTablesContainer) {
                widgetTablesContainer.innerHTML = `<div class="empty-row">No widget data found.</div>`;
            }
            return;
        }
        widgetIds.forEach(widgetId => renderWidgetSection(widgetId, Array.isArray(data.widgets[widgetId]) ? data.widgets[widgetId] : []));
    }

    function bindEvents() {
        if (daysSelect) {
            daysSelect.value = String(defaultDays);
        }

        applyDaysBtn?.addEventListener('click', () => {
            const days = parseInt(daysSelect?.value || String(defaultDays), 10);
            const safeDays = Number.isFinite(days) && days > 0 ? days : defaultDays;
            window.location.href = `${contextPath}/dashboard/inactive-users?days=${encodeURIComponent(safeDays)}`;
        });

        document.addEventListener('click', (e) => {
            const countBtn = e.target.closest('.inactive-count-btn');
            if (countBtn) {
                const sid = countBtn.dataset.sessionId;
                if (sid) {
                    window.location.href = reviewUrl(sid);
                }
                return;
            }

            const allBtn = e.target.closest('.all-view-all-btn');
            if (allBtn) {
                window.location.href = fullListUrl('all');
                return;
            }

            const widgetBtn = e.target.closest('.widget-view-all-btn');
            if (widgetBtn) {
                const wid = widgetBtn.dataset.widgetId;
                window.location.href = fullListUrl('widget', wid);
            }
        });
    }

    render();
    bindEvents();
})();
