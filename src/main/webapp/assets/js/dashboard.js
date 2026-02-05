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
const ctx = document.getElementById('termChart')?.getContext('2d');
const palette = ['#1d4ed8', '#047857', '#c0392b', '#d97706', '#0f172a', '#6366f1', '#af7b1b'];

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
