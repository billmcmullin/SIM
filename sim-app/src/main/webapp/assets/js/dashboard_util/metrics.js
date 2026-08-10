(() => {
    'use strict';

    const core = window.DashboardCore;

    function computeDelta(today, yesterday) {
        const t = Number.isFinite(today) ? today : 0;
        const y = Number.isFinite(yesterday) ? yesterday : 0;
        const delta = t - y;
        const pct = y === 0 ? (t > 0 ? 100 : 0) : (delta * 100) / y;
        const direction = delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat';
        return { today: t, yesterday: y, delta, pct, direction };
    }

    function formatSignedInt(n) {
        if (!Number.isFinite(n)) {
            return '0';
        }
        return `${n > 0 ? '+' : ''}${Math.trunc(n)}`;
    }

    function formatPct(n) {
        if (!Number.isFinite(n)) {
            return '0.0';
        }
        return n.toFixed(1);
    }

    function renderProgressPill(deltaObj, forcedDirection) {
        const direction = (forcedDirection || deltaObj.direction || 'flat').toLowerCase();
        const cls = direction === 'up' ? 'progression-up' : direction === 'down' ? 'progression-down' : 'progression-flat';
        const text = `${formatSignedInt(deltaObj.delta)} (${formatPct(deltaObj.pct)}%) vs yesterday`;
        return `<span class="progression ${cls}" data-direction="${core.esc(direction)}">${core.esc(text)}</span>`;
    }

    function parseIntFromText(text) {
        if (!text) {
            return null;
        }
        const m = String(text).match(/-?\d+/);
        if (!m) {
            return null;
        }
        const n = parseInt(m[0], 10);
        return Number.isFinite(n) ? n : null;
    }

    function parseChatSummaryValues() {
        const summary = document.querySelector('.chat-progression-summary');
        if (!summary) {
            return { today: null, yesterday: null };
        }
        const txt = summary.textContent || '';
        const todayMatch = txt.match(/Today:\s*([0-9]+)/i);
        const yMatch = txt.match(/Yesterday:\s*([0-9]+)/i);
        return {
            today: todayMatch ? parseInt(todayMatch[1], 10) : null,
            yesterday: yMatch ? parseInt(yMatch[1], 10) : null
        };
    }

    function parseNewUsersFromServerRenderedDom() {
        return {
            today: parseIntFromText(document.getElementById('serverNewUsersToday')?.textContent),
            yesterday: parseIntFromText(document.getElementById('serverNewUsersYesterday')?.textContent)
        };
    }

    function parseTermsFromServerRenderedDom() {
        return {
            today: parseIntFromText(document.getElementById('serverTermsToday')?.textContent),
            yesterday: parseIntFromText(document.getElementById('serverTermsYesterday')?.textContent)
        };
    }

    function setConditionalMetricLink(el, value, hrefOrBuilder) {
        if (!el) {
            return;
        }
        const n = Number(value);
        if (!Number.isFinite(n)) {
            el.textContent = 'N/A';
            return;
        }
        if (n > 0 && hrefOrBuilder) {
            const href = typeof hrefOrBuilder === 'string' ? hrefOrBuilder : '#';
            el.innerHTML = `<a class="metric-link metric-dynamic-link" href="${core.esc(href)}">${core.esc(String(n))}</a>`;
            if (typeof hrefOrBuilder === 'function') {
                const a = el.querySelector('a.metric-dynamic-link');
                if (a) {
                    a.__buildHref = hrefOrBuilder;
                }
            }
        } else {
            el.textContent = String(n);
        }
    }

    function wireDynamicLinks() {
        document.addEventListener('click', async (event) => {
            const a = event.target.closest('a.metric-dynamic-link');
            if (!a || !a.__buildHref) {
                return;
            }
            event.preventDefault();
            if (a.dataset.loading === '1') {
                return;
            }
            try {
                a.dataset.loading = '1';
                const href = await a.__buildHref();
                if (href) {
                    if (window.DashboardInlineView && typeof window.DashboardInlineView.openOrNavigate === 'function') {
                        window.DashboardInlineView.openOrNavigate(href, 'Review Data');
                    } else {
                        window.location.href = href;
                    }
                }
            } catch (e) {
                console.warn('Unable to open review selection:', e);
                const fallbackHref = (a.getAttribute('href') || '').trim();
                if (fallbackHref && fallbackHref !== '#') {
                    if (window.DashboardInlineView && typeof window.DashboardInlineView.openOrNavigate === 'function') {
                        window.DashboardInlineView.openOrNavigate(fallbackHref, 'Review Data');
                    } else {
                        window.location.href = fallbackHref;
                    }
                } else {
                    console.warn('Unable to open chat review for this metric right now.');
                }
            } finally {
                a.dataset.loading = '0';
            }
        }, true);
    }

    function applyDeltaClasses(scope = document) {
        const nodes = scope.querySelectorAll('.progression');
        nodes.forEach(node => {
            const txt = (node.textContent || '').trim();
            node.classList.remove('progression-up', 'progression-down', 'progression-flat');
            if (txt.startsWith('+')) {
                node.classList.add('progression-up');
            } else if (txt.startsWith('-')) {
                node.classList.add('progression-down');
            } else {
                node.classList.add('progression-flat');
            }
        });
    }

    function applyProgressionDirectionStyling() {
        const direction = (window.chatProgressionDirection || '').toLowerCase().trim();
        if (!direction) {
            return;
        }
        const el = document.querySelector('.chat-progression-summary .progression');
        if (!el) {
            return;
        }
        el.classList.remove('progression-up', 'progression-down', 'progression-flat');
        if (direction === 'up') {
            el.classList.add('progression-up');
        } else if (direction === 'down') {
            el.classList.add('progression-down');
        } else {
            el.classList.add('progression-flat');
        }
        el.setAttribute('data-direction', direction);
    }

    async function buildWidgetReviewSelectionLink(dayToken, contextPath) {
        return `${contextPath}/dashboard/sessions/drilldown/date-review-relative?day=${encodeURIComponent(dayToken)}`;
    }

    function hydrateDailyProgressSection(contextPath) {
        const section = document.getElementById('dailyProgressSection');
        if (!section) {
            return;
        }

        const todayChatsEl = document.getElementById('dpTodayChats');
        const yesterdayChatsEl = document.getElementById('dpYesterdayChats');
        const chatDeltaEl = document.getElementById('dpChatDelta');

        const todayUsersEl = document.getElementById('dpTodayUsers');
        const yesterdayUsersEl = document.getElementById('dpYesterdayUsers');
        const usersDeltaEl = document.getElementById('dpUsersDelta');

        const todayTermsEl = document.getElementById('dpTodayTerms');
        const yesterdayTermsEl = document.getElementById('dpYesterdayTerms');
        const termsDeltaEl = document.getElementById('dpTermsDelta');

        const chatVals = parseChatSummaryValues();
        if (Number.isFinite(chatVals.today) && Number.isFinite(chatVals.yesterday)) {
            const d = computeDelta(chatVals.today, chatVals.yesterday);
            setConditionalMetricLink(todayChatsEl, d.today, () => buildWidgetReviewSelectionLink('today', contextPath));
            setConditionalMetricLink(yesterdayChatsEl, d.yesterday, () => buildWidgetReviewSelectionLink('yesterday', contextPath));
            if (chatDeltaEl) {
                const forcedDir = (window.chatProgressionDirection || d.direction || 'flat').toLowerCase();
                chatDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayChatsEl) {
                todayChatsEl.textContent = 'N/A';
            }
            if (yesterdayChatsEl) {
                yesterdayChatsEl.textContent = 'N/A';
            }
            if (chatDeltaEl) {
                chatDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
            }
        }

        const newUserVals = parseNewUsersFromServerRenderedDom();
        if (Number.isFinite(newUserVals.today) && Number.isFinite(newUserVals.yesterday)) {
            const d = computeDelta(newUserVals.today, newUserVals.yesterday);
            const dates = core.getTodayYesterday();

            setConditionalMetricLink(todayUsersEl, d.today, `${contextPath}/dashboard/new-users/drilldown?day=${encodeURIComponent(dates.today)}`);
            setConditionalMetricLink(yesterdayUsersEl, d.yesterday, `${contextPath}/dashboard/new-users/drilldown?day=${encodeURIComponent(dates.yesterday)}`);

            if (usersDeltaEl) {
                const forcedDir = (window.newUsersProgressionDirection || d.direction || 'flat').toLowerCase();
                usersDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayUsersEl) {
                todayUsersEl.textContent = 'N/A';
            }
            if (yesterdayUsersEl) {
                yesterdayUsersEl.textContent = 'N/A';
            }
            if (usersDeltaEl) {
                usersDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
            }
        }

        const termVals = parseTermsFromServerRenderedDom();
        if (Number.isFinite(termVals.today) && Number.isFinite(termVals.yesterday)) {
            const d = computeDelta(termVals.today, termVals.yesterday);

            setConditionalMetricLink(
                todayTermsEl,
                d.today,
                `${contextPath}/dashboard/sessions/drilldown/date-review-relative?day=today&scope=termEntries`
            );
            setConditionalMetricLink(
                yesterdayTermsEl,
                d.yesterday,
                `${contextPath}/dashboard/sessions/drilldown/date-review-relative?day=yesterday&scope=termEntries`
            );

            if (termsDeltaEl) {
                const forcedDir = (window.termsProgressionDirection || d.direction || 'flat').toLowerCase();
                termsDeltaEl.innerHTML = renderProgressPill(d, forcedDir);
            }
        } else {
            if (todayTermsEl) {
                todayTermsEl.textContent = 'N/A';
            }
            if (yesterdayTermsEl) {
                yesterdayTermsEl.textContent = 'N/A';
            }
            if (termsDeltaEl) {
                termsDeltaEl.innerHTML = '<span class="progression progression-flat">N/A</span>';
            }
        }

        const summaryToday = document.getElementById('summaryTodayChats');
        const summaryYesterday = document.getElementById('summaryYesterdayChats');
        if (Number.isFinite(chatVals.today)) {
            setConditionalMetricLink(summaryToday, chatVals.today, () => buildWidgetReviewSelectionLink('today', contextPath));
        }
        if (Number.isFinite(chatVals.yesterday)) {
            setConditionalMetricLink(summaryYesterday, chatVals.yesterday, () => buildWidgetReviewSelectionLink('yesterday', contextPath));
        }

        applyDeltaClasses(section);
    }

    function renderActiveUsersDelta(data) {
        const deltaEl = document.getElementById('activeUsersDelta');
        if (!deltaEl) {
            return;
        }

        const activeUsers = typeof data.activeUsers === 'number' ? data.activeUsers : null;
        const activeUsersYesterday = typeof data.activeUsersYesterday === 'number' ? data.activeUsersYesterday : null;

        let delta = typeof data.activeUsersDelta === 'number' ? data.activeUsersDelta : null;
        let pct = typeof data.activeUsersDeltaPct === 'number' ? data.activeUsersDeltaPct : null;
        let direction = (data.activeUsersDirection || '').toLowerCase();

        if (delta === null && activeUsers !== null && activeUsersYesterday !== null) {
            const d = computeDelta(activeUsers, activeUsersYesterday);
            delta = d.delta;
            pct = d.pct;
            direction = d.direction;
        }

        if (delta === null) {
            deltaEl.className = 'progression progression-flat';
            deltaEl.textContent = '—';
            deltaEl.setAttribute('data-direction', 'flat');
            return;
        }

        if (!Number.isFinite(pct)) {
            pct = 0;
        }
        if (!direction) {
            direction = delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat';
        }

        deltaEl.classList.remove('progression-up', 'progression-down', 'progression-flat');
        deltaEl.classList.add(direction === 'up' ? 'progression-up' : direction === 'down' ? 'progression-down' : 'progression-flat');
        deltaEl.setAttribute('data-direction', direction);
        deltaEl.textContent = `${formatSignedInt(delta)} (${formatPct(pct)}%) vs yesterday`;
    }

    window.DashboardMetrics = {
        computeDelta,
        renderProgressPill,
        wireDynamicLinks,
        applyDeltaClasses,
        applyProgressionDirectionStyling,
        hydrateDailyProgressSection,
        renderActiveUsersDelta
    };
})();
