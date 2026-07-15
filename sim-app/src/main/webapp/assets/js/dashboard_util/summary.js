(() => {
    'use strict';

    const core = window.DashboardCore;

    function ensureSummaryProgressUi() {
        const bodyEl = document.getElementById('dailySummaryBody');
        if (!bodyEl) {
            return;
        }
        if (document.getElementById('dailySummaryProgressWrap')) {
            return;
        }

        const wrap = document.createElement('div');
        wrap.id = 'dailySummaryProgressWrap';
        wrap.style.marginBottom = '10px';

        wrap.innerHTML = `
            <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
                <div style="flex:1 1 260px; min-width:220px; background:#e5e7eb; border-radius:999px; height:10px; overflow:hidden;">
                    <div id="dailySummaryProgressBar" style="width:0%; height:100%; background:#2563eb; transition:width .25s ease;"></div>
                </div>
                <span id="dailySummaryProgressText" class="helper-note" style="font-size:12px;">0%</span>
            </div>
        `;
        bodyEl.parentNode.insertBefore(wrap, bodyEl);
    }

    function setSummaryProgress(pct, message) {
        const bar = document.getElementById('dailySummaryProgressBar');
        const txt = document.getElementById('dailySummaryProgressText');
        const p = Math.max(0, Math.min(100, Number(pct) || 0));
        if (bar) {
            bar.style.width = `${p}%`;
        }
        if (txt) {
            txt.textContent = `${p}%${message ? ` • ${message}` : ''}`;
        }
    }

    function hideSummaryProgressIfDone(inProgress) {
        const wrap = document.getElementById('dailySummaryProgressWrap');
        if (!wrap) {
            return;
        }
        wrap.style.display = inProgress ? '' : 'none';
    }

    function containsAny(text, terms) {
        const s = String(text || '').toLowerCase();
        return terms.some(t => s.includes(String(t).toLowerCase()));
    }

    function inferSuggestedNextAction(summary, meta) {
        const status = String(meta?.statusText || '').toLowerCase();
        const quality = String(summary?.quality || '').toLowerCase();
        const response = String(summary?.response || '').toLowerCase();
        const usage = String(summary?.usage || '').toLowerCase();

        if (status === 'running' || status === 'queued') {
            return 'Summary is still generating. Wait for completion, then review low-performing areas and rerun checks.';
        }
        if (containsAny(quality, ['low', 'inconsistent', 'hallucination', 'incorrect', 'poor'])) {
            return 'Review low-quality conversations first and tighten prompt instructions/guardrails for affected widgets.';
        }
        if (containsAny(response, ['slow', 'latency', 'timeout', 'delayed'])) {
            return 'Investigate response latency by widget and reduce prompt/context size where possible.';
        }
        if (containsAny(usage, ['low', 'drop', 'decline', 'underused'])) {
            return 'Promote underused high-value widgets and add clearer in-app guidance for users.';
        }
        return 'Review Top Terms and Latest Chats, pick one repeated issue, and apply a focused prompt update.';
    }

    function buildCopySummaryText(summary, meta, suggested) {
        const day = meta?.day || '—';
        const slot = Number.isFinite(Number(meta?.slot)) ? Number(meta.slot) : '—';
        const status = meta?.statusText || 'idle';
        const progress = Number.isFinite(Number(meta?.progressPct)) ? Number(meta.progressPct) : 0;
        const entryCount = Number.isFinite(Number(summary?.entryCount)) ? Number(summary.entryCount) : 0;
        const generatedAt = meta?.generatedAt || '—';
        const startedAt = meta?.startedAt || '—';
        const updatedAt = meta?.updatedAt || '—';
        const message = meta?.message || '';

        return [
            'Daily Dashboard Summary',
            '',
            `Day: ${day}`,
            `Slot: ${slot}`,
            `Status: ${status}`,
            `Progress: ${progress}%`,
            `Entries analyzed: ${entryCount}`,
            `Generated at: ${generatedAt}`,
            `Started at: ${startedAt}`,
            `Updated at: ${updatedAt}`,
            ...(message ? [`Message: ${message}`] : []),
            '',
            'Overall',
            String(summary?.overall || '—'),
            '',
            'Quality',
            String(summary?.quality || '—'),
            '',
            'Response',
            String(summary?.response || '—'),
            '',
            'Usage',
            String(summary?.usage || '—'),
            '',
            'Suggested Next Action',
            String(suggested || '—')
        ].join('\n');
    }

    function wireSummaryCopyButton() {
        const btn = document.getElementById('copyDailySummaryBtn');
        const src = document.getElementById('dailySummaryCopyText');
        const status = document.getElementById('dailySummaryCopyStatus');
        if (!btn || !src) {
            return;
        }

        const setStatus = (msg) => {
            if (status) {
                status.textContent = msg || '';
            }
        };

        btn.addEventListener('click', async () => {
            const text = src.value || '';
            if (!text) {
                setStatus('No summary text available.');
                setTimeout(() => setStatus(''), 2000);
                return;
            }

            let ok = false;
            try {
                await window.navigator.clipboard.writeText(text);
                ok = true;
            } catch {
                const ta = document.createElement('textarea');
                ta.value = text;
                ta.setAttribute('readonly', 'readonly');
                ta.style.position = 'fixed';
                ta.style.left = '-9999px';
                document.body.appendChild(ta);
                ta.focus();
                ta.select();
                try {
                    ok = document.execCommand('copy');
                } catch {
                    ok = false;
                } finally {
                    ta.remove();
                }
            }

            setStatus(ok ? 'Copied summary text.' : 'Unable to copy automatically. Press Ctrl/Cmd+C.');
            setTimeout(() => setStatus(''), 2200);
        });
    }

    async function loadDailySummary(contextPath) {
        const bodyEl = document.getElementById('dailySummaryBody');
        const metaEl = document.getElementById('dailySummaryMeta');
        const copyEl = document.getElementById('dailySummaryCopyText');
        if (!bodyEl) {
            return;
        }

        ensureSummaryProgressUi();
        bodyEl.innerHTML = '<p style="margin:0;">Loading latest summary…</p>';
        if (metaEl) {
            metaEl.textContent = 'Loading latest daily analysis…';
        }
        setSummaryProgress(5, 'loading');

        let pollCount = 0;
        const maxPolls = 30;

        while (pollCount < maxPolls) {
            pollCount++;
            let data = null;

            try {
                const resp = await fetch(`${contextPath}/dashboard/daily-summary.json`, {
                    credentials: 'same-origin',
                    headers: { Accept: 'application/json' }
                });

                if (!resp.ok) {
                    bodyEl.innerHTML = '<p style="margin:0;">Unable to load summary right now.</p>';
                    if (metaEl) {
                        metaEl.textContent = `Status: ${resp.status}`;
                    }
                    setSummaryProgress(0, 'error');
                    hideSummaryProgressIfDone(false);
                    return;
                }

                data = await resp.json();
            } catch (e) {
                console.warn('Unable to load daily summary:', e);
                bodyEl.innerHTML = '<p style="margin:0;">Unable to load summary right now.</p>';
                if (metaEl) {
                    metaEl.textContent = 'Request failed.';
                }
                setSummaryProgress(0, 'request failed');
                hideSummaryProgressIfDone(false);
                return;
            }

            if (!data || data.status !== 'ok' || !data.summary) {
                bodyEl.innerHTML = '<p style="margin:0;">Summary is not available yet.</p>';
                if (metaEl) {
                    metaEl.textContent = 'No summary returned.';
                }
                setSummaryProgress(0, 'not ready');
                hideSummaryProgressIfDone(false);
                return;
            }

            const s = data.summary || {};
            const m = data.meta || {};
            const inProgress = !!m.inProgress;
            const pct = Number.isFinite(Number(m.progressPct)) ? Number(m.progressPct) : (inProgress ? 30 : 100);
            const suggested = s.suggestedNextAction || m.suggestedNextAction || inferSuggestedNextAction(s, m);

            setSummaryProgress(pct, m.message || (inProgress ? 'generating' : 'complete'));

            bodyEl.innerHTML = `
                <div>
                    <h4 style="margin:0 0 6px 0;">Overall</h4>
                    <p style="margin:0 0 12px 0; white-space:pre-wrap;">${core.esc(s.overall || '—')}</p>

                    <h4 style="margin:0 0 6px 0;">Quality</h4>
                    <p style="margin:0 0 12px 0; white-space:pre-wrap;">${core.esc(s.quality || '—')}</p>

                    <h4 style="margin:0 0 6px 0;">Response</h4>
                    <p style="margin:0 0 12px 0; white-space:pre-wrap;">${core.esc(s.response || '—')}</p>

                    <h4 style="margin:0 0 6px 0;">Usage</h4>
                    <p style="margin:0 0 12px 0; white-space:pre-wrap;">${core.esc(s.usage || '—')}</p>

                    <h4 style="margin:0 0 6px 0;">Suggested Next Action</h4>
                    <p style="margin:0; white-space:pre-wrap;">${core.esc(suggested || '—')}</p>
                </div>
            `;

            if (copyEl) {
                copyEl.value = buildCopySummaryText(s, m, suggested);
            }

            const entryCount = Number.isFinite(Number(s.entryCount)) ? Number(s.entryCount) : 0;
            const generatedAt = m.generatedAt ? String(m.generatedAt) : '';
            const slot = Number.isFinite(Number(m.slot)) ? Number(m.slot) : 0;
            if (metaEl) {
                metaEl.textContent = `Entries analyzed: ${entryCount} • Slot: ${slot} • Generated: ${generatedAt || '—'}${inProgress ? ' • updating…' : ''}`;
            }

            hideSummaryProgressIfDone(inProgress);

            if (!inProgress) {
                return;
            }
            await new Promise(r => setTimeout(r, 2000));
        }

        if (metaEl) {
            metaEl.textContent = 'Summary is still generating. Please refresh shortly.';
        }
    }

    window.DashboardSummary = {
        wireSummaryCopyButton,
        loadDailySummary
    };
})();
