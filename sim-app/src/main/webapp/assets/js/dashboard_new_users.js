(function () {
    'use strict';

    const cfg = window.dashboardNewUsersConfig || {};
    const contextPath = cfg.contextPath || '';

    const dataUrl = `${contextPath}/dashboard/new-users/data`;
    const dayUrl = `${contextPath}/dashboard/new-users/day`;

    const daysSelect = document.getElementById('daysRangeSelect');
    const applyBtn = document.getElementById('applyRangeBtn');
    const tableBody = document.getElementById('latestNewUsersBody');
    const statusEl = document.getElementById('newUsersStatus');
    const dayResultsBody = document.getElementById('dayResultsBody');
    const dayResultsTitle = document.getElementById('dayResultsTitle');
    const chartCanvas = document.getElementById('newUsersTrendChart');

    let trendChart = null;

    function esc(v) {
        if (v === null || v === undefined) {
            return '';
        }
        return String(v)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function setStatus(msg, isError = false) {
        if (!statusEl) {
            return;
        }
        statusEl.textContent = msg || '';
        statusEl.style.color = isError ? '#b91c1c' : '#047857';
    }

    function parseInitialTrendData() {
        let payload = window.newUsersTrendData || {};
        if (typeof payload === 'string') {
            try {
                payload = JSON.parse(payload);
            } catch {
                payload = {};
            }
        }
        return payload;
    }

    function buildParams() {
        const p = new URLSearchParams();
        const days = (daysSelect?.value || '7').trim();
        p.set('days', days);
        return p;
    }

    function renderLatest(rows) {
        if (!tableBody) {
            return;
        }

        if (!Array.isArray(rows) || !rows.length) {
            tableBody.innerHTML = '<tr><td colspan="4" class="empty-row">No new session IDs found.</td></tr>';
            return;
        }

        tableBody.innerHTML = rows.map(r => {
            const rank = r.rank ?? '';
            const sessionId = r.sessionId || '';
            const display = r.display || sessionId;
            const firstSeen = r.firstSeen || '—';
            const totalChats = Number.isFinite(r.totalChats) ? r.totalChats : 0;
            const chatEntriesUrl = r.chatEntriesUrl || `${contextPath}/dashboard/sessions/drilldown/session-review?sessionId=${encodeURIComponent(sessionId)}`;

            return `<tr>
                <td>${rank}</td>
                <td>
                    <div>${esc(display)}</div>
                    ${(display !== sessionId && sessionId) ? `<div class="session-id-muted">${esc(sessionId)}</div>` : ''}
                </td>
                <td>${esc(firstSeen)}</td>
                <td><a class="session-count-link" href="${esc(chatEntriesUrl)}">${totalChats} chats</a></td>
            </tr>`;
        }).join('');
    }

    function renderDayRows(day, rows) {
        if (!dayResultsBody || !dayResultsTitle) {
            return;
        }

        dayResultsTitle.textContent = `New users first seen on ${day}`;

        if (!Array.isArray(rows) || !rows.length) {
            dayResultsBody.innerHTML = '<tr><td colspan="4" class="empty-row">No new users for this day.</td></tr>';
            return;
        }

        dayResultsBody.innerHTML = rows.map(r => {
            const rank = r.rank ?? '';
            const sessionId = r.sessionId || '';
            const display = r.display || sessionId;
            const firstSeen = r.firstSeen || '—';
            const totalChats = Number.isFinite(r.totalChats) ? r.totalChats : 0;
            const chatEntriesUrl = r.chatEntriesUrl || `${contextPath}/dashboard/sessions/drilldown/session-review?sessionId=${encodeURIComponent(sessionId)}`;

            return `<tr>
                <td>${rank}</td>
                <td>
                    <div>${esc(display)}</div>
                    ${(display !== sessionId && sessionId) ? `<div class="session-id-muted">${esc(sessionId)}</div>` : ''}
                </td>
                <td>${esc(firstSeen)}</td>
                <td><a class="session-count-link" href="${esc(chatEntriesUrl)}">${totalChats} chats</a></td>
            </tr>`;
        }).join('');
    }

    async function loadDay(dayLabel) {
        if (!dayLabel) {
            return;
        }

        try {
            const p = new URLSearchParams();
            p.set('day', dayLabel);

            const res = await fetch(`${dayUrl}?${p.toString()}`, {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });

            const text = await res.text();
            let data = {};
            try { data = JSON.parse(text); } catch { data = {}; }

            if (!res.ok || data.status !== 'ok') {
                throw new Error(data.message || `HTTP ${res.status}`);
            }

            renderDayRows(data.day || dayLabel, data.rows || []);
            setStatus(`Loaded ${data.count ?? 0} users first seen on ${data.day || dayLabel}`);
        } catch (err) {
            console.error('Failed to load day details:', err);
            if (dayResultsBody) {
                dayResultsBody.innerHTML = `<tr><td colspan="4" class="empty-row">Unable to load day details: ${esc(err.message)}</td></tr>`;
            }
            setStatus(`Unable to load day details: ${err.message}`, true);
        }
    }

    function renderTrend(labels, values) {
        if (!chartCanvas || typeof Chart === 'undefined') {
            return;
        }

        const safeLabels = Array.isArray(labels) ? labels : [];
        const safeValues = Array.isArray(values) ? values : [];

        if (trendChart) {
            trendChart.destroy();
            trendChart = null;
        }

        trendChart = new Chart(chartCanvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: safeLabels,
                datasets: [{
                    label: 'New Session IDs',
                    data: safeValues,
                    borderColor: '#1d4ed8',
                    backgroundColor: 'rgba(29, 78, 216, 0.18)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.2,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { precision: 0 }
                    }
                },
                onClick: (event, elements) => {
                    if (!elements || !elements.length) {
                        return;
                    }
                    const idx = elements[0].index;
                    const dayLabel = safeLabels[idx];
                    if (!dayLabel) {
                        return;
                    }

                    // Navigate to drilldown page filtered by selected day
                    window.location.href =
                        `${contextPath}/dashboard/new-users/drilldown?day=${encodeURIComponent(dayLabel)}&page=1&pageSize=10`;
                }
            }
        });
    }

    async function loadData() {
        setStatus('Loading metrics...');

        try {
            const res = await fetch(`${dataUrl}?${buildParams().toString()}`, {
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });

            const text = await res.text();
            let data = {};
            try { data = JSON.parse(text); } catch { data = {}; }

            if (!res.ok || data.status !== 'ok') {
                throw new Error(data.message || `HTTP ${res.status}`);
            }

            const trend = data.trend || {};
            const labels = Array.isArray(trend.labels) ? trend.labels : [];
            const values = Array.isArray(trend.values) ? trend.values : [];

            renderTrend(labels, values);
            renderLatest(data.latest || []);

            setStatus(`Showing ${daysSelect?.value || '7'} day range`);

            // Keep existing functionality: still auto-load day details table below
            if (labels.length) {
                loadDay(labels[labels.length - 1]);
            } else if (dayResultsBody) {
                dayResultsBody.innerHTML = '<tr><td colspan="4" class="empty-row">No trend data available.</td></tr>';
            }
        } catch (err) {
            console.error('Failed to load /dashboard/new-users/data:', err);
            if (tableBody) {
                tableBody.innerHTML = `<tr><td colspan="4" class="empty-row">Unable to load data: ${esc(err.message)}</td></tr>`;
            }
            if (dayResultsBody) {
                dayResultsBody.innerHTML = '<tr><td colspan="4" class="empty-row">No day details available.</td></tr>';
            }
            setStatus(`Unable to load data: ${err.message}`, true);
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const initialTrend = parseInitialTrendData();
        if (Array.isArray(initialTrend.labels) && Array.isArray(initialTrend.values)) {
            renderTrend(initialTrend.labels, initialTrend.values);
        }

        if (daysSelect && !daysSelect.value) {
            daysSelect.value = String(cfg.selectedDays || '7');
        }

        daysSelect?.addEventListener('change', loadData);
        applyBtn?.addEventListener('click', loadData);

        loadData();
    });
})();
