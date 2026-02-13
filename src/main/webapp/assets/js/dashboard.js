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

// --- Added: fetch total sessions count for Top 10 Sessions display ---
(async function loadTotalSessions() {
    const totalEl = document.getElementById('totalSessions');
    if (!totalEl) return;

    try {
        // Request a small page but read the 'total' field that the JSON API returns
        const url = `${contextPath}/dashboard/sessions.json?page=1&pageSize=1`;
        const resp = await fetch(url, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } });
        if (!resp.ok) {
            // try alternative query param format if .json mapping not present
            const alt = `${contextPath}/dashboard/sessions?format=json&page=1&pageSize=1`;
            const altResp = await fetch(alt, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } });
            if (!altResp.ok) {
                totalEl.textContent = 'N/A';
                return;
            }
            const altJson = await altResp.json().catch(() => null);
            if (altJson && typeof altJson.total === 'number') {
                totalEl.textContent = String(altJson.total);
                return;
            } else {
                totalEl.textContent = 'N/A';
                return;
            }
        }
        const data = await resp.json().catch(() => null);
        if (data && typeof data.total === 'number') {
            totalEl.textContent = String(data.total);
        } else {
            totalEl.textContent = '—';
        }
    } catch (e) {
        console.warn('Unable to load total sessions count:', e);
        totalEl.textContent = 'N/A';
    }
})();
