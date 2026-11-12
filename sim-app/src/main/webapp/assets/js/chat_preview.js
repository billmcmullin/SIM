(function () {
    'use strict';

    if (window.SimChatPreview) {
        return;
    }

    function timestampDetails(value) {
        const raw = value === null || value === undefined ? '' : String(value);
        const unknown = { text: 'Unknown', dateTime: '', title: raw ? `Original timestamp: ${raw}` : 'Timestamp not supplied' };
        const parts = /^(\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12]\d|3[01]))T([01]\d|2[0-3]):([0-5]\d)(?::([0-5]\d)(?:\.(\d{1,9}))?)?(Z|[+-](?:[01]\d|2[0-3]):[0-5]\d)$/.exec(raw.trim());
        if (!parts) return unknown;
        const calendar = new Date(`${parts[1]}T00:00:00Z`);
        if (Number.isNaN(calendar.getTime()) || calendar.toISOString().slice(0, 10) !== parts[1]) return unknown;
        const fraction = parts[5] ? `.${parts[5].slice(0, 3).padEnd(3, '0')}` : '';
        const date = new Date(`${parts[1]}T${parts[2]}:${parts[3]}:${parts[4] || '00'}${fraction}${parts[6]}`);
        if (Number.isNaN(date.getTime())) return unknown;
        const formatter = new Intl.DateTimeFormat(undefined, {
            year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
            hour12: true, timeZoneName: 'shortOffset'
        });
        return { text: formatter.format(date), dateTime: date.toISOString(),
            title: `Original timestamp: ${raw}\nUTC: ${date.toISOString()}\nTime zone: ${formatter.resolvedOptions().timeZone}` };
    }

    function formatTimestamp(value) {
        return timestampDetails(value).text;
    }

    function setTimestamp(element, value) {
        if (!element) return;
        const details = timestampDetails(value);
        element.textContent = details.text;
        element.title = details.title;
        element.setAttribute('aria-label', `${details.text}. ${details.title}`);
        if (details.dateTime) element.setAttribute('datetime', details.dateTime);
        else element.removeAttribute('datetime');
    }

    function create(options) {
        const { root } = options;
        const id = name => options.ids?.[name]
            || (options.prefix ? `${options.prefix}${name.charAt(0).toUpperCase()}${name.slice(1)}` : name);
        const languages = [['auto', 'Auto Detect'], ['ja', 'Japanese'], ['en', 'English'], ['es', 'Spanish'],
            ['fr', 'French'], ['de', 'German'], ['pt', 'Portuguese'], ['it', 'Italian'], ['nl', 'Dutch'],
            ['ko', 'Korean'], ['zh', 'Chinese'], ['ru', 'Russian'], ['ar', 'Arabic']];
        root.innerHTML = `
            <div class="chat-preview-header">
                <div class="summary-label" id="${id('detailTitle')}">Selected Chat Details</div>
                <div class="chat-preview-controls">
                    <button type="button" id="${id('detailPreviousBtn')}" class="ghost-btn chat-preview-step" aria-label="Previous chat" title="Previous chat">&larr;</button>
                    <span id="${id('detailPosition')}" class="helper-inline"></span>
                    <button type="button" id="${id('detailNextBtn')}" class="ghost-btn chat-preview-step" aria-label="Next chat" title="Next chat">&rarr;</button>
                    <button type="button" id="${id('detailCardCloseBtn')}" class="ghost-btn">Close Preview</button>
                </div>
            </div>
            <div class="helper-note"><time id="${id('detailCreatedAt')}"></time></div>
            <div class="chat-preview-actions">
                <button type="button" id="${id('shareChatRevealBtn')}" class="primary">Send To Teammate</button>
                <span id="${id('shareChatRevealHint')}" hidden></span>
            </div>
            <div class="form-group" id="${id('shareChatPanel')}" hidden aria-hidden="true">
                <label for="${id('shareEmailTo')}">Teammate Email</label>
                <input type="email" id="${id('shareEmailTo')}" placeholder="teammate@example.com" autocomplete="email">
                <label for="${id('shareCustomMessage')}">Custom Message (optional)</label>
                <textarea id="${id('shareCustomMessage')}" rows="5" placeholder="Add context for your teammate"></textarea>
                <label class="chat-preview-checkbox">
                    <input type="checkbox" id="${id('shareTranslateToEnglish')}" checked>
                    Translate prompt and response to English when needed
                </label>
                <div class="chat-preview-actions">
                    <button type="button" id="${id('shareChatByEmailBtn')}" class="primary">Send Email</button>
                    <span id="${id('shareChatEmailBusyHint')}" class="helper-inline"></span>
                </div>
                <div id="${id('shareChatEmailStatus')}" class="helper-note" aria-live="polite"></div>
            </div>
            <strong>Prompt</strong>
            <pre id="${id('detailPrompt')}" tabindex="0"></pre>
            <div class="chat-preview-actions">
                <label for="${id('translateSourceLang')}">Translate From:</label>
                <select id="${id('translateSourceLang')}">${languages.map(([value, label]) => `<option value="${value}">${label}</option>`).join('')}</select>
                <label for="${id('translateTargetLang')}">Translate To:</label>
                <select id="${id('translateTargetLang')}">${languages.filter(([value]) => ['en', 'es', 'fr', 'de', 'pt'].includes(value)).map(([value, label]) => `<option value="${value}">${label}</option>`).join('')}</select>
            </div>
            <div class="chat-preview-actions">
                <button type="button" id="${id('translatePromptBtn')}" class="ghost-btn">Translate Prompt</button>
                <span id="${id('promptTranslationBusyHint')}" class="helper-inline"></span>
            </div>
            <div id="${id('promptTranslationMeta')}" class="helper-note"></div>
            <pre id="${id('promptTranslationOutput')}" tabindex="0" style="display:none;"></pre>
            <strong>Response</strong>
            <pre id="${id('detailResponse')}" tabindex="0"></pre>
            <div class="chat-preview-actions">
                <button type="button" id="${id('translateResponseBtn')}" class="ghost-btn">Translate Response</button>
                <span id="${id('responseTranslationBusyHint')}" class="helper-inline"></span>
            </div>
            <div id="${id('responseTranslationMeta')}" class="helper-note"></div>
            <pre id="${id('responseTranslationOutput')}" tabindex="0" style="display:none;"></pre>`;

        const element = name => root.querySelector(`#${CSS.escape(id(name))}`);
        const title = element('detailTitle');
        const prompt = element('detailPrompt');
        const response = element('detailResponse');
        const close = element('detailCardCloseBtn');
        const previous = element('detailPreviousBtn');
        const next = element('detailNextBtn');
        const canShare = options.capabilities?.share === true;
        const canTranslate = options.capabilities?.translate === true;
        const anchored = options.placement === 'anchored-right';
        const managesTranslation = typeof options.onTranslate === 'function';
        element('shareChatRevealBtn').style.display = canShare ? '' : 'none';
        for (const name of ['translateSourceLang', 'translatePromptBtn', 'translateResponseBtn']) {
            element(name).parentElement.style.display = canTranslate ? '' : 'none';
        }
        const requests = new Map();
        let activeRow = null;
        let contentReady = false;
        let revision = 0;
        let languageRevision = 0;
        const key = row => options.getKey ? options.getKey(row) : String(row?.chatId || '');
        root.classList.add('chat-preview');
        root.classList.toggle('chat-preview-anchored-right', anchored);
        root.setAttribute('role', 'region');
        root.setAttribute('aria-live', 'polite');
        root.setAttribute('aria-labelledby', id('detailTitle'));
        root.setAttribute('aria-hidden', 'true');
        root.tabIndex = -1;

        function positionPanel() {
            if (!anchored || !activeRow || !root.isConnected || root.style.display === 'none') return;
            const anchor = options.getAnchor?.(activeRow);
            const bounds = anchor?.getBoundingClientRect();
            const rowBounds = anchor?.closest('tr')?.getBoundingClientRect() || bounds;
            const headerBottom = Math.max(0, document.querySelector('.top-bar')?.getBoundingClientRect().bottom || 0);
            const verticalBounds = options.getVerticalBounds?.(activeRow);
            const minimumTop = Math.min(window.innerHeight - 12,
                Math.max(12, headerBottom + 12, verticalBounds?.top || 0));
            const maximumBottom = Math.max(minimumTop, Math.min(window.innerHeight - 12, verticalBounds?.bottom ?? window.innerHeight - 12));
            const minimumHeight = Math.min(options.minimumHeight || 240, Math.max(0, maximumBottom - minimumTop));
            const maximumTop = Math.max(minimumTop, maximumBottom - minimumHeight);
            const top = Math.min(Math.max(minimumTop, rowBounds?.top ?? minimumTop), maximumTop);
            const availableRight = bounds ? window.innerWidth - bounds.right - 28 : 0;
            const preferredWidth = Math.min(options.maximumWidth || 560, window.innerWidth - 32);
            root.style.width = `${availableRight >= 420 ? Math.min(preferredWidth, availableRight) : preferredWidth}px`;
            const width = root.getBoundingClientRect().width;
            const maximumLeft = window.innerWidth - width - 16;
            root.style.left = `${Math.max(16, Math.min(bounds ? bounds.right + 12 : maximumLeft, maximumLeft))}px`;
            root.style.right = 'auto';
            root.style.top = `${top}px`;
            root.style.maxHeight = `${Math.max(0, maximumBottom - top)}px`;
        }

        function clearTranslations() {
            for (const name of ['promptTranslationMeta', 'responseTranslationMeta']) element(name).textContent = '';
            for (const name of ['promptTranslationOutput', 'responseTranslationOutput']) {
                element(name).textContent = '';
                element(name).style.display = 'none';
            }
            if (managesTranslation) {
                for (const part of ['prompt', 'response']) {
                    element(`${part}TranslationBusyHint`).textContent = '';
                    element(part === 'prompt' ? 'translatePromptBtn' : 'translateResponseBtn').disabled =
                        !contentReady || !canTranslate || activeRow?.translationAvailable === false;
                }
            }
        }

        function refreshNavigation() {
            const rows = options.getRows?.() || [];
            const index = activeRow ? rows.findIndex(row => key(row) === key(activeRow)) : -1;
            previous.disabled = index <= 0;
            next.disabled = index < 0 || index >= rows.length - 1;
            element('detailPosition').textContent = index < 0 ? '' : `${index + 1} / ${rows.length}`;
            positionPanel();
        }

        function move(offset) {
            const rows = options.getRows?.() || [];
            const index = activeRow ? rows.findIndex(row => key(row) === key(activeRow)) : -1;
            if (index >= 0 && index + offset >= 0 && index + offset < rows.length) options.onNavigate?.(rows[index + offset]);
        }

        function setReady(ready) {
            contentReady = ready;
            root.setAttribute('aria-busy', ready ? 'false' : 'true');
            for (const name of ['translatePromptBtn', 'translateResponseBtn', 'shareChatRevealBtn', 'shareChatByEmailBtn']) {
                element(name).disabled = !ready || (name.startsWith('share') ? !canShare : !canTranslate || activeRow?.translationAvailable === false);
            }
        }

        async function translatePart(part) {
            if (!contentReady || !canTranslate || !activeRow || activeRow.translationAvailable === false) return;
            const label = part === 'prompt' ? 'Prompt' : 'Response';
            const button = element(part === 'prompt' ? 'translatePromptBtn' : 'translateResponseBtn');
            const meta = element(`${part}TranslationMeta`);
            const output = element(`${part}TranslationOutput`);
            const busy = element(`${part}TranslationBusyHint`);
            const text = String(activeRow[part] || '');
            if (!text.trim()) {
                meta.textContent = `${label} is empty; nothing to translate.`;
                return;
            }
            const payload = { text, sourceLang: element('translateSourceLang').value || 'auto',
                targetLang: element('translateTargetLang').value || 'en' };
            const token = controller.beginRequest(`translation:${part}`, true);
            button.disabled = true;
            close.focus({ preventScroll: true });
            busy.textContent = 'Translating...';
            meta.textContent = '';
            output.textContent = '';
            output.style.display = 'none';
            try {
                const result = await options.onTranslate(payload);
                if (!controller.isCurrent(token)) return;
                if (!result || result.status !== 'ok') throw new Error(result?.message || 'Unable to translate at this time.');
                output.textContent = String(result.translatedText || '(empty translation)');
                output.style.display = 'block';
                meta.textContent = `${label}: ${result.sourceLang || payload.sourceLang} -> ${result.targetLang || payload.targetLang}`;
            } catch (error) {
                if (controller.isCurrent(token)) meta.textContent = `${label} translation failed: ${error.message}`;
            } finally {
                if (controller.isCurrent(token)) {
                    busy.textContent = '';
                    button.disabled = !contentReady || activeRow?.translationAvailable === false;
                }
            }
        }

        const controller = {
            element,
            refreshNavigation,
            clearTranslations,
            show(row, loading = false) {
                const shouldFocus = !root.contains(document.activeElement);
                activeRow = row;
                revision++;
                title.textContent = `Selected Chat Details${row.chatId ? ` \u2022 Chat ${row.chatId}` : ''}`;
                setTimestamp(element('detailCreatedAt'), row.createdAt);
                prompt.textContent = row.prompt || '(empty prompt)';
                response.textContent = row.response || '(empty response)';
                clearTranslations();
                element('shareChatPanel').hidden = true;
                element('shareChatPanel').setAttribute('aria-hidden', 'true');
                element('shareChatRevealBtn').textContent = 'Send To Teammate';
                element('shareCustomMessage').value = '';
                element('shareChatEmailStatus').textContent = '';
                element('shareChatEmailBusyHint').textContent = '';
                root.style.display = 'block';
                root.setAttribute('aria-hidden', 'false');
                setReady(!loading);
                refreshNavigation();
                if (shouldFocus || !root.contains(document.activeElement) || document.activeElement.disabled) {
                    close.focus({ preventScroll: true });
                }
                if (!anchored) root.scrollIntoView({ block: 'nearest' });
            },
            hide(returnFocus = true) {
                if (root.style.display === 'none') return;
                const previousRow = activeRow;
                activeRow = null;
                revision++;
                root.style.display = 'none';
                root.setAttribute('aria-hidden', 'true');
                options.onClose?.();
                if (returnFocus !== false) options.getReturnFocus?.(previousRow)?.focus(anchored ? { preventScroll: true } : undefined);
            },
            beginRequest(channel, translation = false) {
                const sequence = (requests.get(channel) || 0) + 1;
                requests.set(channel, sequence);
                return { revision, channel, sequence, language: translation ? languageRevision : null };
            },
            isCurrent(token) {
                return token && root.isConnected && root.style.display !== 'none' && token.revision === revision
                    && requests.get(token.channel) === token.sequence
                    && (token.language === null || token.language === languageRevision);
            }
        };
        close.addEventListener('click', controller.hide);
        previous.addEventListener('click', () => move(-1));
        next.addEventListener('click', () => move(1));
        if (managesTranslation) {
            element('translatePromptBtn').addEventListener('click', () => { void translatePart('prompt'); });
            element('translateResponseBtn').addEventListener('click', () => { void translatePart('response'); });
        }
        root.addEventListener('keydown', event => {
            if (event.key === 'Escape') {
                event.preventDefault();
                event.stopPropagation();
                controller.hide();
            }
        });
        for (const name of ['translateSourceLang', 'translateTargetLang']) {
            element(name).addEventListener('change', () => {
                languageRevision++;
                clearTranslations();
                options.onLanguageChange?.();
            });
        }
        element('shareChatRevealBtn').addEventListener('click', () => {
            const panel = element('shareChatPanel');
            panel.hidden = !panel.hidden;
            panel.setAttribute('aria-hidden', String(panel.hidden));
            element('shareChatRevealBtn').textContent = panel.hidden ? 'Send To Teammate' : 'Hide Email Form';
            if (!panel.hidden) element('shareEmailTo').focus();
        });
        if (anchored) {
            window.addEventListener('scroll', positionPanel, { capture: true, passive: true });
            window.addEventListener('resize', positionPanel, { passive: true });
        }
        return controller;
    }

    window.SimChatPreview = { create, formatTimestamp, setTimestamp };
})();