let termSlices = window.termChartData || [];
const dashboardConfig = window.dashboardConfig || {};
const contextPath = dashboardConfig.contextPath || '';

if (typeof termSlices === 'string') {
    try {
        termSlices = JSON.parse(termSlices);
    } catch (error) {
        termSlices = [];
    }
}
const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b'];

const ctx = document.getElementById('termChart')?.getContext('2d');

function openTermReview(term) {
    if (!term) {
        return;
    }
    const target = `${contextPath}/dashboard/term-review?term=${encodeURIComponent(term)}`;
    window.location.href = target;
}

if (ctx && termSlices.length) {
    const data = {
        labels: termSlices.map(slice => slice.label),
        datasets: [{
            data: termSlices.map(slice => slice.count),
            backgroundColor: termSlices.map((_, index) => palette[index % palette.length])
        }]
    };
    new Chart(ctx, {
        type: 'pie',
        data,
        options: {
            plugins: {
                tooltip: {
                    callbacks: {
                        title: contextRows => {
                            const slice = termSlices[contextRows[0].dataIndex];
                            return `Term: ${slice.term}`;
                        },
                        label: context => {
                            const slice = termSlices[context.dataIndex];
                            return `${slice.label}: ${slice.count}`;
                        }
                    }
                },
                legend: {
                    display: false
                }
            },
            responsive: true,
            maintainAspectRatio: false,
            onClick: (event, elements) => {
                if (!elements.length) {
                    return;
                }
                const index = elements[0].index;
                const slice = termSlices[index];
                openTermReview(slice.term);
            }
        }
    });
}

const legendEl = document.getElementById('termChartLegend');
if (legendEl && termSlices.length) {
    termSlices.forEach((slice, index) => {
        const chip = document.createElement('button');
        chip.className = 'legend-chip';
        chip.style.background = palette[index % palette.length];
        chip.type = 'button';
        chip.textContent = `${slice.label} (${slice.count})`;
        chip.addEventListener('click', () => openTermReview(slice.term));
        chip.addEventListener('keypress', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                openTermReview(slice.term);
            }
        });
        legendEl.appendChild(chip);
    });
}

// --- Added: fetch total sessions count and top 10 sessions with widget column ---
(async function loadTopSessions() {
    const totalEl = document.getElementById('totalSessions');
    const listEl = document.getElementById('topSessionList');
    if (!listEl || !totalEl) return;

    try {
        // Request top 10 sessions sorted by count desc (server sorts full dataset)
        const url = `${contextPath}/dashboard/sessions.json?page=1&pageSize=10&sortBy=count&sortDir=desc`;
        const resp = await fetch(url, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } });
        if (!resp.ok) {
            totalEl.textContent = 'N/A';
            return;
        }
        const data = await resp.json();
        if (!data || data.status !== 'ok') {
            totalEl.textContent = 'N/A';
            return;
        }

        // total sessions
        if (typeof data.total === 'number') {
            totalEl.textContent = String(data.total);
        } else {
            totalEl.textContent = '—';
        }

        // sessions array
        const sessions = Array.isArray(data.sessions) ? data.sessions : [];
        if (!sessions.length) {
            // leave server-rendered rows if present; else show no rows
            listEl.innerHTML = '<tr><td colspan="5" class="empty-row">No sessions found.</td></tr>';
            return;
        }

        // Render rows - use topWidgetName for display
        listEl.innerHTML = sessions.map((s, idx) => {
            const rank = idx + 1;
            const sessionId = s.sessionId || '';
            const count = typeof s.count === 'number' ? s.count : 0;
            const last = s.last || '—';
            const topWidgetName = s.topWidgetName || '—';
            const reviewUrl = s.reviewUrl || `${contextPath}/dashboard/session-review?sessionId=${encodeURIComponent(sessionId)}`;
            // Escape content minimally
            function esc(v) {
                if (v === null || typeof v === 'undefined') return '';
                return String(v).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
            }
            return `<tr>
                <td>${rank}</td>
                <td>${esc(sessionId)}</td>
                <td>${esc(topWidgetName)}</td>
                <td>${count}</td>
                <td>${esc(last)}</td>
            </tr>`;
        }).join('');

    } catch (e) {
        console.warn('Unable to load top sessions:', e);
        totalEl.textContent = 'N/A';
    }
})();
