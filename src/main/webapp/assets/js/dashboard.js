(() => {
    'use strict';

    // -------------------------
    // Helpers
    // -------------------------
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

        // Fast path: no escapable chars
        if (!/[&<>"']/.test(str)) return str;

        return str
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    // -------------------------
    // Inputs
    // -------------------------
    const dashboardConfig = window.dashboardConfig || {};
    const contextPath = dashboardConfig.contextPath || '';

    const termSlices = parseSlices(window.termChartData || []);
    const widgetSlices = parseSlices(window.widgetPieChartData || []);

    const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b'];

    const termChartEl = document.getElementById('termChart');
    const widgetChartEl = document.getElementById('widgetOverviewPieChart');
    const ctx = termChartEl?.getContext('2d');
    const widgetPieCtx = widgetChartEl?.getContext('2d');

    function openTermReview(term) {
        if (!term) return;
        window.location.href = `${contextPath}/dashboard/term-review?term=${encodeURIComponent(term)}`;
    }

    // -------------------------
    // Term pie chart
    // -------------------------
    if (ctx && termSlices.length) {
        const { labels, values, colors } = buildSeries(termSlices, palette);

        new Chart(ctx, {
            type: 'pie',
            data: {
                labels,
                datasets: [{
                    data: values,
                    backgroundColor: colors
                }]
            },
            options: {
                plugins: {
                    tooltip: {
                        callbacks: {
                            title: contextRows => {
                                const i = contextRows?.[0]?.dataIndex;
                                return (i !== undefined ? termSlices[i]?.term : '') || '';
                            },
                            label: context => {
                                const slice = termSlices[context.dataIndex];
                                return slice ? `${slice.label}: ${slice.count}` : '';
                            }
                        }
                    },
                    legend: { display: false }
                },
                responsive: true,
                maintainAspectRatio: false,
                onClick: (_event, elements) => {
                    if (!elements?.length) return;
                    const slice = termSlices[elements[0].index];
                    if (!slice) return;
                    openTermReview(slice.term);
                }
            }
        });
    }

    // -------------------------
    // Widget pie chart
    // -------------------------
    if (widgetPieCtx && widgetSlices.length) {
        const { labels, values, colors } = buildSeries(widgetSlices, palette);

        new Chart(widgetPieCtx, {
            type: 'pie',
            data: {
                labels,
                datasets: [{
                    data: values,
                    backgroundColor: colors
                }]
            },
            options: {
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: context => {
                                const slice = widgetSlices[context.dataIndex];
                                return slice ? `${slice.label}: ${slice.count}` : '';
                            }
                        }
                    },
                    legend: { position: 'bottom' }
                },
                responsive: true,
                maintainAspectRatio: false,
                onClick: (_event, elements) => {
                    if (!elements?.length) return;
                    const slice = widgetSlices[elements[0].index];
                    if (!slice) return;

                    const widgetId = slice.widgetId || slice.label;
                    if (!widgetId) return;

                    window.location.href = `${contextPath}/dashboard/widgets/view?widgetId=${encodeURIComponent(widgetId)}`;
                }
            }
        });
    }

    // -------------------------
    // Legend chips (batched DOM write + delegated events)
    // -------------------------
    const legendEl = document.getElementById('termChartLegend');
    if (legendEl && termSlices.length) {
        const frag = document.createDocumentFragment();

        for (let i = 0; i < termSlices.length; i++) {
            const slice = termSlices[i] || {};
            const chip = document.createElement('button');
            chip.className = 'legend-chip';
            chip.style.background = palette[i % palette.length];
            chip.type = 'button';
            chip.textContent = `${slice.label ?? ''} (${typeof slice.count === 'number' ? slice.count : 0})`;
            chip.dataset.term = slice.term || '';
            frag.appendChild(chip);
        }

        legendEl.appendChild(frag);

        // One click listener
        legendEl.addEventListener('click', event => {
            const chip = event.target.closest('.legend-chip');
            if (!chip || !legendEl.contains(chip)) return;
            openTermReview(chip.dataset.term);
        });

        // Keep keyboard support; keypress -> keydown
        legendEl.addEventListener('keydown', event => {
            const chip = event.target.closest('.legend-chip');
            if (!chip || !legendEl.contains(chip)) return;
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                openTermReview(chip.dataset.term);
            }
        });
    }

    // -------------------------
    // Top sessions
    // -------------------------
    (async function loadTopSessions() {
        const totalEl = document.getElementById('totalSessions');
        const listEl = document.getElementById('topSessionList');
        if (!listEl || !totalEl) return;

        // Attach delegated listener once
        listEl.addEventListener('click', event => {
            const btn = event.target.closest('.session-count-btn');
            if (!btn || !listEl.contains(btn)) return;
            const reviewUrl = btn.dataset.reviewUrl;
            if (!reviewUrl) return;
            window.location.href = reviewUrl;
        });

        try {
            const url = `${contextPath}/dashboard/sessions.json?page=1&pageSize=10&sortBy=count&sortDir=desc`;
            const resp = await fetch(url, {
                credentials: 'same-origin',
                headers: { Accept: 'application/json' }
            });

            if (!resp.ok) {
                totalEl.textContent = 'N/A';
                return;
            }

            const data = await resp.json();
            if (!data || data.status !== 'ok') {
                totalEl.textContent = 'N/A';
                return;
            }

            totalEl.textContent = typeof data.total === 'number' ? String(data.total) : '—';

            const sessions = Array.isArray(data.sessions) ? data.sessions : [];
            if (!sessions.length) {
                listEl.innerHTML = '<tr><td colspan="5" class="empty-row">No sessions found.</td></tr>';
                return;
            }

            // Keep existing HTML rendering behavior/functionality
            let html = '';
            for (let idx = 0; idx < sessions.length; idx++) {
                const s = sessions[idx] || {};
                const rank = idx + 1;
                const sessionId = s.sessionId || '';
                const label = s.displayLabel || sessionId;
                const count = typeof s.count === 'number' ? s.count : 0;
                const last = s.last || '—';
                const topWidgetName = s.topWidgetName || '—';
                const reviewUrl = s.reviewUrl || `${contextPath}/dashboard/sessions/drilldown/session-review?sessionId=${encodeURIComponent(sessionId)}`;

                html += `<tr>
                    <td>${rank}</td>
                    <td>
                        <div>${esc(label)}</div>
                        ${label !== sessionId && sessionId ? `<div class="session-id-muted">${esc(sessionId)}</div>` : ''}
                    </td>
                    <td>${esc(topWidgetName)}</td>
                    <td>
                        <button type="button"
                                class="ghost-btn session-count-btn"
                                data-review-url="${esc(reviewUrl)}"
                                aria-label="Review ${esc(label || sessionId)} chats">
                            ${count} chats
                        </button>
                    </td>
                    <td>${esc(last)}</td>
                </tr>`;
            }

            listEl.innerHTML = html;
        } catch (e) {
            console.warn('Unable to load top sessions:', e);
            totalEl.textContent = 'N/A';
        }
    })();
})();
