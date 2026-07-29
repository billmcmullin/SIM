(() => {
    'use strict';

    const FIRST_LOAD_WAIT_MS = 3500;

    function runSafely(label, fn) {
        try {
            return fn();
        } catch (e) {
            console.error(`[DashboardInit] ${label} failed`, e);
            return null;
        }
    }

    function afterFirstPaint(fn) {
        const raf = typeof window.requestAnimationFrame === 'function'
            ? window.requestAnimationFrame.bind(window)
            : (cb) => window.setTimeout(cb, 16);

        raf(() => {
            window.setTimeout(() => {
                runSafely('post-paint task', fn);
            }, 0);
        });
    }

    function wait(ms) {
        return new Promise(resolve => window.setTimeout(resolve, ms));
    }

    function revealDashboard() {
        document.documentElement.classList.remove('dashboard-loading');

        const overlay = document.getElementById('dashboardBootOverlay');
        if (overlay) {
            overlay.style.display = 'none';
            overlay.setAttribute('aria-hidden', 'true');
        }
    }

    function loadScript(src, timeoutMs = 7000) {
        return new Promise(resolve => {
            if (!src) {
                resolve(false);
                return;
            }

            const selector = `script[data-dashboard-loader="${src}"]`;
            const existing = document.querySelector(selector);
            if (existing) {
                if (existing.dataset.loaded === '1' || typeof window.Chart !== 'undefined') {
                    resolve(true);
                    return;
                }
                existing.addEventListener('load', () => resolve(true), { once: true });
                existing.addEventListener('error', () => resolve(false), { once: true });
                window.setTimeout(() => resolve(typeof window.Chart !== 'undefined'), timeoutMs);
                return;
            }

            const scriptEl = document.createElement('script');
            scriptEl.src = src;
            scriptEl.async = true;
            scriptEl.defer = true;
            scriptEl.dataset.dashboardLoader = src;

            let settled = false;
            const done = (ok) => {
                if (settled) {
                    return;
                }
                settled = true;
                scriptEl.dataset.loaded = ok ? '1' : '0';
                resolve(ok);
            };

            scriptEl.addEventListener('load', () => done(true), { once: true });
            scriptEl.addEventListener('error', () => done(false), { once: true });

            document.head.appendChild(scriptEl);
            window.setTimeout(() => done(typeof window.Chart !== 'undefined'), timeoutMs);
        });
    }

    async function bootCharts(contextPath, chartJsUrl) {
        if (!window.DashboardCharts) {
            console.warn('[DashboardInit] DashboardCharts module missing; chart rendering skipped.');
            return;
        }

        if (typeof window.Chart === 'undefined') {
            const loaded = await loadScript(chartJsUrl || 'https://cdn.jsdelivr.net/npm/chart.js');
            if (!loaded || typeof window.Chart === 'undefined') {
                console.warn('[DashboardInit] Chart.js unavailable; dashboard remains usable without charts.');
                return;
            }
        }

        runSafely('bootPieAndTermCharts', () => {
            window.DashboardCharts.bootPieAndTermCharts(contextPath);
        });
        runSafely('renderLastFiveDaysTrendChart', () => {
            window.DashboardCharts.renderLastFiveDaysTrendChart(contextPath);
        });
    }

    async function boot() {
        const dashboardConfig = window.dashboardConfig || {};
        const contextPath = dashboardConfig.contextPath || '';
        const chartJsUrl = dashboardConfig.chartJsUrl || 'https://cdn.jsdelivr.net/npm/chart.js';

        if (!window.DashboardCore) {
            console.error('[DashboardInit] DashboardCore missing. Check script load order and file contents.');
            revealDashboard();
            return;
        }

        if (!window.DashboardMetrics || !window.DashboardSummary || !window.DashboardSessions) {
            console.error('[DashboardInit] One or more dashboard modules are missing. Running partial bootstrap.');
        }

        if (window.DashboardSummary) {
            runSafely('wireSummaryCopyButton', () => {
                window.DashboardSummary.wireSummaryCopyButton();
            });
        }

        if (window.DashboardMetrics) {
            runSafely('wireDynamicLinks', () => {
                window.DashboardMetrics.wireDynamicLinks();
            });
            runSafely('applyProgressionDirectionStyling', () => {
                window.DashboardMetrics.applyProgressionDirectionStyling();
            });
            runSafely('hydrateDailyProgressSection', () => {
                window.DashboardMetrics.hydrateDailyProgressSection(contextPath);
            });
            runSafely('applyDeltaClasses', () => {
                window.DashboardMetrics.applyDeltaClasses(document);
            });
        }

        const criticalTasks = [];
        if (window.DashboardSessions) {
            criticalTasks.push(
                Promise.resolve(
                    runSafely('loadTopSessions', () => window.DashboardSessions.loadTopSessions(contextPath))
                )
            );
        }

        criticalTasks.push(
            Promise.resolve(
                runSafely('bootCharts', () => bootCharts(contextPath, chartJsUrl))
            )
        );

        try {
            await Promise.race([
                Promise.allSettled(criticalTasks),
                wait(FIRST_LOAD_WAIT_MS)
            ]);
        } catch (e) {
            console.error('[DashboardInit] initial task wait failed', e);
        } finally {
            revealDashboard();
        }

        afterFirstPaint(() => {
            if (window.DashboardSummary) {
                window.DashboardSummary.loadDailySummary(contextPath);
            }
        });
    }

    window.DashboardInit = { boot };
})();
