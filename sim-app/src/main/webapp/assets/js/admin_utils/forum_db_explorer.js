// forum_db_explorer.js
(function () {
    'use strict';

    window.AdminPage = window.AdminPage || {};

    const Api = window.AdminPage.Api;
    const Utils = window.AdminPage.Utils;

    const BATCH_SIZE = 200;
    const MAX_BATCH_LOOPS = 5000;
    const HUMAN_PAGE_SIZE = 10;
    const MAX_EMAIL_BODY_CHARS = 180000;
    const EXCLUDED_MESSAGE_PATTERNS = [
        /you\s+have\s+been\s+sent\s+this\s+communication\s+from\b/i
    ];

    function byId(id) {
        return document.getElementById(id);
    }

    function value(id) {
        const el = byId(id);
        return el ? String(el.value || '').trim() : '';
    }

    function escapeHtml(v) {
        if (Utils && typeof Utils.escapeHtml === 'function') {
            return Utils.escapeHtml(v);
        }
        return String(v || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    const module = {
        contextPath: '',
        connected: false,
        loadedColumns: [],
        loadedRows: [],
        loadedTable: '',
        loadedEntity: '',
        loadedHints: null,
        detectedColumns: null,
        activeChainColumn: '',
        humanRowsAll: [],
        humanRowsFiltered: [],
        selectedReplyRows: [],
        repliesLoading: false,
        repliesError: '',
        repliesRequestToken: 0,
        selectedHumanAbsoluteIndex: -1,
        humanCurrentPage: 1,
        humanPageSize: HUMAN_PAGE_SIZE,

        init(opts) {
            this.contextPath = (opts && opts.contextPath) ? String(opts.contextPath) : '';

            byId('forumDbConnectBtn')?.addEventListener('click', () => this.connectAndLoad());
            byId('forumDbLoadBtn')?.addEventListener('click', () => this.loadFromDatabase());
            byId('forumDbSaveConfigBtn')?.addEventListener('click', () => this.saveConfiguration());
            byId('forumDbClearBtn')?.addEventListener('click', () => this.clearResults());
            byId('forumDbConfirmedOnly')?.addEventListener('change', () => this.applyClientFilter());
            byId('forumDbType')?.addEventListener('change', () => this.applyDefaultPort());
            byId('forumDbHumanSearch')?.addEventListener('input', () => this.applyClientFilter());
            byId('forumDbHumanPrevBtn')?.addEventListener('click', () => this.goToHumanPage(this.humanCurrentPage - 1));
            byId('forumDbHumanNextBtn')?.addEventListener('click', () => this.goToHumanPage(this.humanCurrentPage + 1));
            byId('forumDbHumanPageJumpBtn')?.addEventListener('click', () => this.jumpToHumanPageFromInput());
            byId('forumDbHumanPageJumpInput')?.addEventListener('keydown', (event) => {
                if (event && event.key === 'Enter') {
                    event.preventDefault();
                    this.jumpToHumanPageFromInput();
                }
            });
            byId('forumDbExportThreadsBtn')?.addEventListener('click', () => this.exportMessagesAndRepliesCsv());
            byId('forumDbOverlayEmailBtn')?.addEventListener('click', () => this.emailCleanedMessages());
            byId('forumDbOverlayTranslateBtn')?.addEventListener('click', () => this.translateSelectedMessage());
            byId('forumDbOverlayCloseBtn')?.addEventListener('click', () => this.closeOverlay());
            byId('forumDbHumanBody')?.addEventListener('click', (event) => this.onHumanRowClick(event));

            this.applyDefaultPort();
            this.renderHumanTable([], 0);
            this.renderHumanDetail(null);
            this.renderHumanSummary(0, 0);
            this.renderHumanPagination();
            this.setShareMessage('', false);
            this.clearTranslationUi();
            this.updateOverlayActionState();
            this.loadSavedConfiguration();
        },

        applyDefaultPort() {
            const typeEl = byId('forumDbType');
            const portEl = byId('forumDbPort');
            if (!typeEl || !portEl) {
                return;
            }

            const selected = String(typeEl.value || '').toLowerCase();
            if (portEl.value.trim()) {
                return;
            }

            portEl.value = selected === 'mysql' ? '3306' : '5432';
        },

        validateInputs() {
            const dbType = value('forumDbType');
            const host = value('forumDbHost');
            const port = value('forumDbPort');
            const database = value('forumDbName');
            const username = value('forumDbUser');
            const password = value('forumDbPassword');

            if (!dbType || !host || !port || !database || !username || !password) {
                return 'All connection fields are required.';
            }

            const parsedPort = Number(port);
            if (!Number.isInteger(parsedPort) || parsedPort < 1 || parsedPort > 65535) {
                return 'Port must be between 1 and 65535.';
            }

            return '';
        },

        buildParams(action) {
            const params = new URLSearchParams();
            params.set('action', action);
            params.set('dbType', value('forumDbType'));
            params.set('host', value('forumDbHost'));
            params.set('port', value('forumDbPort'));
            params.set('database', value('forumDbName'));
            params.set('username', value('forumDbUser'));
            params.set('password', value('forumDbPassword'));
            return params;
        },

        buildSaveConfigParams() {
            const params = this.buildParams('saveConfig');
            params.set('readableOnly', 'true');
            params.set('shareTo', value('forumDbOverlayShareTo'));
            params.set('shareSubject', value('forumDbOverlayShareSubject'));
            return params;
        },

        setMessage(message, isError) {
            const msg = byId('forumDbResultMessage');
            if (!msg) {
                return;
            }
            msg.textContent = message || '';
            msg.style.color = isError ? '#b91c1c' : '#047857';
        },

        setShareMessage(message, isError) {
            const msg = byId('forumDbShareMessage');
            if (!msg) {
                return;
            }
            msg.textContent = message || '';
            msg.style.color = isError ? '#b91c1c' : '#047857';
        },

        setTranslateMeta(message, isError) {
            const meta = byId('forumDbOverlayTranslateMeta');
            if (!meta) {
                return;
            }
            meta.textContent = message || '';
            meta.style.color = isError ? '#b91c1c' : '#334155';
        },

        setTranslateOutput(text) {
            const output = byId('forumDbOverlayTranslateOutput');
            if (!output) {
                return;
            }
            const valueText = String(text || '').trim();
            if (!valueText) {
                output.textContent = '';
                output.style.display = 'none';
                return;
            }
            output.textContent = valueText;
            output.style.display = 'block';
        },

        resetReplyState(invalidatePendingRequest) {
            this.selectedReplyRows = [];
            this.repliesLoading = false;
            this.repliesError = '';
            if (invalidatePendingRequest) {
                this.repliesRequestToken++;
            }
        },

        clearTranslationUi() {
            this.setTranslateMeta('', false);
            this.setTranslateOutput('');
        },

        setPasswordStoredNote(isStored) {
            const note = byId('forumDbPasswordStoredNote');
            if (!note) {
                return;
            }
            note.style.display = isStored ? 'inline' : 'none';
        },

        async loadSavedConfiguration() {
            const params = new URLSearchParams();
            params.set('action', 'loadConfig');

            try {
                const result = await Api.postUrlEncoded(`${this.contextPath}/admin/forum-db/explorer`, params);
                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    return;
                }

                this.applySavedConfiguration(result.payload);
                this.setMessage('Loaded saved forum explorer configuration.', false);
            } catch {
                // Non-blocking: page should still function even if settings cannot be loaded.
            }
        },

        applySavedConfiguration(payload) {
            if (!payload || typeof payload !== 'object') {
                return;
            }

            const dbTypeEl = byId('forumDbType');
            if (dbTypeEl && payload.dbType) {
                dbTypeEl.value = String(payload.dbType);
            }

            const hostEl = byId('forumDbHost');
            if (hostEl) {
                hostEl.value = String(payload.host || '');
            }

            const portEl = byId('forumDbPort');
            if (portEl) {
                portEl.value = String(payload.port || '');
            }

            const dbNameEl = byId('forumDbName');
            if (dbNameEl) {
                dbNameEl.value = String(payload.database || '');
            }

            const userEl = byId('forumDbUser');
            if (userEl) {
                userEl.value = String(payload.username || '');
            }

            const passwordEl = byId('forumDbPassword');
            if (passwordEl) {
                passwordEl.value = '';
            }

            const shareToEl = byId('forumDbOverlayShareTo');
            if (shareToEl) {
                shareToEl.value = String(payload.shareTo || '');
            }

            const shareSubjectEl = byId('forumDbOverlayShareSubject');
            if (shareSubjectEl) {
                shareSubjectEl.value = String(payload.shareSubject || 'Cleaned Forum Human Message');
            }

            this.setPasswordStoredNote(payload.passwordStored === true);
        },

        async saveConfiguration() {
            const saveBtn = byId('forumDbSaveConfigBtn');
            if (saveBtn) {
                saveBtn.disabled = true;
            }

            this.setMessage('Saving forum explorer configuration...', false);
            const params = this.buildSaveConfigParams();

            try {
                const result = await Api.postUrlEncoded(`${this.contextPath}/admin/forum-db/explorer`, params);
                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    const message = result.payload && result.payload.message
                        ? result.payload.message
                        : `Save configuration failed (${result.status}).`;
                    this.setMessage(message, true);
                    return;
                }

                this.applySavedConfiguration(result.payload);
                const message = result.payload.message || 'Forum explorer configuration saved.';
                this.setMessage(message, false);
            } catch (e) {
                this.setMessage(`Save configuration error: ${e.message}`, true);
            } finally {
                if (saveBtn) {
                    saveBtn.disabled = false;
                }
            }
        },

        async connectAndLoad() {
            const validation = this.validateInputs();
            if (validation) {
                this.setMessage(validation, true);
                return;
            }

            const params = this.buildParams('connect');
            this.setMessage('Connecting to database...', false);

            try {
                const result = await Api.postUrlEncoded(`${this.contextPath}/admin/forum-db/explorer`, params);
                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    const message = result.payload && result.payload.message
                        ? result.payload.message
                        : `Connection failed (${result.status}).`;
                    this.setMessage(message, true);
                    return;
                }

                const availableEntities = Array.isArray(result.payload.availableEntities)
                    ? result.payload.availableEntities
                    : [];
                if (!availableEntities.length) {
                    this.connected = false;
                    this.setMessage('Connected, but no Users/Conversations/Messages table could be found.', true);
                    this.clearLoadedRowsOnly();
                    return;
                }

                const preferred = this.selectPreferredEntity(availableEntities, result.payload.preferredEntity);
                const entityEl = byId('forumDbEntity');
                if (entityEl && preferred) {
                    entityEl.value = preferred;
                }

                this.connected = true;
                await this.loadFromDatabase(true, result.payload);
            } catch (e) {
                this.connected = false;
                this.setMessage(`Connection error: ${e.message}`, true);
            }
        },

        async loadFromDatabase(connectedJustNow = false, connectPayload = null) {
            const validation = this.validateInputs();
            if (validation) {
                this.setMessage(validation, true);
                return;
            }

            this.setMessage('Loading all rows from database...', false);

            try {
                const loaded = await this.fetchAllRows();
                const payload = loaded.payload;
                if (!payload) {
                    this.setMessage('No payload returned from database query.', true);
                    return;
                }

                this.connected = true;
                this.loadedColumns = Array.isArray(payload.columns) ? payload.columns : [];
                this.loadedRows = Array.isArray(loaded.rows) ? loaded.rows : [];
                this.loadedTable = String(payload.table || '');
                this.loadedEntity = String(payload.entity || value('forumDbEntity') || 'messages');
                this.loadedHints = this.readHints(payload);

                this.applyClientFilter();

                const total = Number.isInteger(loaded.total) ? loaded.total : this.loadedRows.length;
                const base = connectedJustNow ? 'Connected and loaded data.' : 'Data loaded.';
                const available = this.formatAvailableEntities(connectPayload);
                const chainMeta = this.activeChainColumn ? ` Chain key: ${this.activeChainColumn}.` : '';
                this.setMessage(
                    `${base} Loaded ${this.loadedRows.length} row(s) from ${this.loadedTable} (table total: ${total}).${available}${chainMeta}`,
                    false
                );
            } catch (e) {
                this.setMessage(`Query error: ${e.message}`, true);
            }
        },

        async fetchAllRows() {
            const selectedEntity = value('forumDbEntity') || 'messages';
            let offset = 0;
            let totalHint = null;
            let firstPayload = null;
            const allRows = [];

            for (let i = 0; i < MAX_BATCH_LOOPS; i++) {
                const params = this.buildParams('query');
                params.set('entity', selectedEntity);
                params.set('limit', String(BATCH_SIZE));
                params.set('offset', String(offset));

                const result = await Api.postUrlEncoded(`${this.contextPath}/admin/forum-db/explorer`, params);
                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    const message = result.payload && result.payload.message
                        ? result.payload.message
                        : `Query failed (${result.status}).`;
                    throw new Error(message);
                }

                const payload = result.payload;
                if (!firstPayload) {
                    firstPayload = payload;
                }

                if (Number.isInteger(payload.total)) {
                    totalHint = payload.total;
                }

                const rows = Array.isArray(payload.rows) ? payload.rows : [];
                allRows.push(...rows);
                offset += rows.length;

                this.setMessage(
                    `Loading rows... ${allRows.length}${totalHint !== null ? ` / ${totalHint}` : ''}`,
                    false
                );

                if (rows.length === 0) {
                    break;
                }
                if (totalHint !== null && allRows.length >= totalHint) {
                    break;
                }
                if (rows.length < BATCH_SIZE) {
                    break;
                }
            }

            return {
                payload: firstPayload,
                rows: allRows,
                total: totalHint === null ? allRows.length : totalHint
            };
        },

        selectPreferredEntity(availableEntities, serverPreferred) {
            if (!Array.isArray(availableEntities) || !availableEntities.length) {
                return '';
            }

            const normalizedAvailable = availableEntities.map((item) => String(item || '').toLowerCase());
            const priorities = ['messages', 'conversations', 'users'];

            for (const candidate of priorities) {
                const idx = normalizedAvailable.indexOf(candidate);
                if (idx >= 0) {
                    return availableEntities[idx];
                }
            }

            if (serverPreferred) {
                const serverIdx = normalizedAvailable.indexOf(String(serverPreferred).toLowerCase());
                if (serverIdx >= 0) {
                    return availableEntities[serverIdx];
                }
            }

            return availableEntities[0];
        },

        readHints(payload) {
            if (!payload || typeof payload !== 'object') {
                return null;
            }
            return {
                messageTextColumn: this.normalizeHint(payload.messageTextColumn),
                responseTextColumn: this.normalizeHint(payload.responseTextColumn),
                actorColumn: this.normalizeHint(payload.actorColumn),
                roleColumn: this.normalizeHint(payload.roleColumn),
                conversationColumn: this.normalizeHint(payload.conversationColumn),
                createdAtColumn: this.normalizeHint(payload.createdAtColumn)
            };
        },

        normalizeHint(value) {
            if (typeof value !== 'string') {
                return '';
            }
            return value.trim();
        },

        formatAvailableEntities(connectPayload) {
            if (!connectPayload || !Array.isArray(connectPayload.availableEntities)) {
                return '';
            }
            if (!connectPayload.availableEntities.length) {
                return '';
            }

            const tableMap = connectPayload.resolvedTables && typeof connectPayload.resolvedTables === 'object'
                ? connectPayload.resolvedTables
                : {};
            const mapped = connectPayload.availableEntities.map((entity) => {
                const resolved = tableMap[entity];
                return resolved ? `${entity} -> ${resolved}` : entity;
            });
            return ` Available: ${mapped.join(', ')}.`;
        },

        applyClientFilter() {
            this.renderHumanMessageView(this.loadedRows);
        },

        renderHumanMessageView(rows) {
            const extracted = this.extractHumanMessages(rows);
            const search = value('forumDbHumanSearch').toLowerCase();
            const confirmedOnly = byId('forumDbConfirmedOnly')?.checked === true;
            const filtered = extracted.filter((entry) => {
                if (confirmedOnly && !entry.isConfirmed) {
                    return false;
                }
                if (!search) {
                    return true;
                }
                const haystack = [
                    entry.messageText,
                    entry.actor,
                    entry.conversationId,
                    entry.createdAt,
                    entry.confirmedLabel,
                    entry.qnaStatus,
                    entry.statusName,
                    entry.tagNames
                ]
                    .join(' ')
                    .toLowerCase();
                return haystack.includes(search);
            });

            this.humanRowsAll = extracted;
            this.humanRowsFiltered = filtered;
            this.humanCurrentPage = 1;
            this.selectedHumanAbsoluteIndex = -1;
            this.resetReplyState(true);

            this.renderHumanSummary(extracted.length, filtered.length);
            this.renderHumanPagination();
            this.renderCurrentHumanPage();
            this.renderHumanDetail(null);
            this.updateOverlayActionState();
        },

        extractHumanMessages(rows) {
            if (!Array.isArray(rows) || !rows.length || !Array.isArray(this.loadedColumns) || !this.loadedColumns.length) {
                return [];
            }

            const map = this.buildColumnMap(this.loadedColumns);
            const usedColumns = new Set();

            const messageTextCol = this.resolveMessageTextColumn(map, rows);
            if (messageTextCol) {
                usedColumns.add(messageTextCol.toLowerCase());
            }

            const responseTextCol = this.resolveResponseTextColumn(map, rows, messageTextCol, usedColumns);
            if (responseTextCol) {
                usedColumns.add(responseTextCol.toLowerCase());
            }

            const actorCol = this.resolveActorColumn(map, rows, usedColumns);
            if (actorCol) {
                usedColumns.add(actorCol.toLowerCase());
            }

            const roleCol = this.resolveRoleColumn(map, usedColumns);
            if (roleCol) {
                usedColumns.add(roleCol.toLowerCase());
            }

            const conversationCol = this.resolveConversationColumn(map, rows, usedColumns);
            if (conversationCol) {
                usedColumns.add(conversationCol.toLowerCase());
            }

            const subjectCol = this.resolveSubjectColumn(map, rows, usedColumns);
            if (subjectCol) {
                usedColumns.add(subjectCol.toLowerCase());
            }

            const sourceMessageIdCol = this.resolveSourceMessageIdColumn(map, rows, usedColumns);
            if (sourceMessageIdCol) {
                usedColumns.add(sourceMessageIdCol.toLowerCase());
            }

            const createdAtCol = this.resolveCreatedAtColumn(map, rows, usedColumns);
            const chainColumn = this.resolveChainColumn(map, rows, usedColumns, conversationCol);
            const confirmedCol = this.resolveConfirmedColumn(map);
            const qnaStatusCol = this.resolveQnaStatusColumn(map);
            const statusNameCol = this.resolveStatusNameColumn(map);
            const tagNamesCol = this.resolveTagNamesColumn(map);
            const actorMatchesConversation = this.columnsMostlyMatchValues(actorCol, conversationCol, rows);

            this.detectedColumns = {
                messageTextColumn: messageTextCol,
                responseTextColumn: responseTextCol,
                actorColumn: actorCol,
                roleColumn: roleCol,
                conversationColumn: conversationCol,
                subjectColumn: subjectCol,
                sourceMessageIdColumn: sourceMessageIdCol,
                createdAtColumn: createdAtCol,
                chainColumn,
                confirmedColumn: confirmedCol,
                qnaStatusColumn: qnaStatusCol,
                statusNameColumn: statusNameCol,
                tagNamesColumn: tagNamesCol
            };
            this.activeChainColumn = chainColumn || '';

            if (!messageTextCol) {
                return [];
            }

            const out = [];
            for (let rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                const row = rows[rowIndex];
                const rawMessageText = this.readCell(row, messageTextCol);
                if (!rawMessageText) {
                    continue;
                }
                if (this.shouldExcludeMessage(rawMessageText)) {
                    continue;
                }

                const messageText = this.cleanMessageText(rawMessageText);
                if (!messageText) {
                    continue;
                }

                const actor = actorMatchesConversation ? '' : this.readCell(row, actorCol);
                const role = this.readCell(row, roleCol);
                if (!this.isLikelyHuman(role, actor)) {
                    continue;
                }

                const conversationId = this.readCell(row, conversationCol);
                const subject = this.readCell(row, subjectCol);
                const sourceMessageId = this.readCell(row, sourceMessageIdCol);
                const chainValue = this.readCell(row, chainColumn) || conversationId || subject;
                const qnaStatus = this.readCell(row, qnaStatusCol);
                const statusName = this.readCell(row, statusNameCol);
                const tagNames = this.readCell(row, tagNamesCol);
                const isConfirmed = this.isConfirmedDiscussion(row, confirmedCol, qnaStatus, statusName, tagNames);

                out.push({
                    createdAt: this.readCell(row, createdAtCol),
                    conversationId,
                    subject,
                    conversationDisplay: this.composeConversationDisplay(conversationId, subject),
                    sourceMessageId,
                    chainValue,
                    rowIndex,
                    actor,
                    messageText,
                    qnaStatus,
                    statusName,
                    tagNames,
                    isConfirmed,
                    confirmedLabel: isConfirmed ? 'Confirmed' : 'Not confirmed'
                });
            }

            return out;
        },

        shouldExcludeMessage(rawMessageText) {
            if (!rawMessageText) {
                return false;
            }
            const comparable = this.toComparableText(rawMessageText);
            for (const pattern of EXCLUDED_MESSAGE_PATTERNS) {
                if (pattern.test(comparable)) {
                    return true;
                }
            }
            return false;
        },

        toComparableText(text) {
            let value = String(text || '');
            value = value.replace(/<[^>]+>/g, ' ');
            value = this.decodeHtmlEntities(value);
            value = value.replace(/\s+/g, ' ').trim().toLowerCase();
            return value;
        },

        cleanMessageText(rawText) {
            if (!rawText) {
                return '';
            }

            let text = String(rawText);
            text = text.replace(/<script[\s\S]*?<\/script>/gi, ' ');
            text = text.replace(/<style[\s\S]*?<\/style>/gi, ' ');
            text = text.replace(/<\s*br\s*\/?>/gi, '\n');
            text = text.replace(/<\s*\/p\s*>/gi, '\n\n');
            text = text.replace(/<\s*li[^>]*>/gi, '\n- ');
            text = text.replace(/<\s*\/li\s*>/gi, '');
            text = text.replace(/<[^>]+>/g, ' ');
            text = this.decodeHtmlEntities(text);
            text = text.replace(/\r/g, '');

            const lines = text
                .split('\n')
                .map((line) => line.replace(/\s+/g, ' ').trim())
                .filter(Boolean);

            return lines.join('\n').trim();
        },

        decodeHtmlEntities(text) {
            if (!text) {
                return '';
            }
            try {
                const area = document.createElement('textarea');
                area.innerHTML = text;
                return area.value;
            } catch {
                return String(text);
            }
        },

        buildColumnMap(columns) {
            const map = new Map();
            for (const col of columns) {
                const original = String(col || '').trim();
                if (!original) {
                    continue;
                }
                map.set(original.toLowerCase(), original);
            }
            return map;
        },

        resolveColumnName(columnMap, hintedName, candidates) {
            if (!(columnMap instanceof Map) || !columnMap.size) {
                return '';
            }

            const hintRaw = String(hintedName || '').trim();
            const hint = hintRaw.toLowerCase();
            if (hint && columnMap.has(hint)) {
                return columnMap.get(hint);
            }

            const normalizedHint = this.normalizeColumnName(hintRaw);
            if (normalizedHint) {
                for (const original of columnMap.values()) {
                    if (this.normalizeColumnName(original) === normalizedHint) {
                        return original;
                    }
                }
            }

            for (const candidate of candidates) {
                const lowerCandidate = String(candidate || '').toLowerCase();
                if (lowerCandidate && columnMap.has(lowerCandidate)) {
                    return columnMap.get(lowerCandidate);
                }
            }

            for (const candidate of candidates) {
                const normalizedCandidate = this.normalizeColumnName(candidate);
                if (!normalizedCandidate) {
                    continue;
                }
                for (const original of columnMap.values()) {
                    if (this.normalizeColumnName(original) === normalizedCandidate) {
                        return original;
                    }
                }
            }

            for (const [lower, original] of columnMap.entries()) {
                for (const candidate of candidates) {
                    const candidateLower = String(candidate || '').toLowerCase();
                    if (candidateLower && lower.includes(candidateLower)) {
                        return original;
                    }
                }
            }

            for (const candidate of candidates) {
                const normalizedCandidate = this.normalizeColumnName(candidate);
                if (!normalizedCandidate) {
                    continue;
                }
                for (const original of columnMap.values()) {
                    const normalizedColumn = this.normalizeColumnName(original);
                    if (normalizedColumn.includes(normalizedCandidate)) {
                        return original;
                    }
                }
            }

            return '';
        },

        resolveMessageTextColumn(columnMap, rows) {
            const hints = this.loadedHints || {};

            const preferredHint = this.resolveColumnName(columnMap, hints.messageTextColumn,
                ['prompt', 'message', 'body', 'content', 'text', 'question', 'user_message', 'user_text', 'post_text', 'input', 'query']);
            if (this.isUsableMessageColumn(preferredHint, rows)) {
                return preferredHint;
            }

            const preferredDetected = this.resolveColumnName(columnMap, '',
                ['prompt', 'message', 'body', 'content', 'text', 'question', 'user_message', 'user_text', 'post_text', 'input', 'query']);
            if (this.isUsableMessageColumn(preferredDetected, rows)) {
                return preferredDetected;
            }

            const responseHint = this.resolveColumnName(columnMap, hints.responseTextColumn,
                ['response_text', 'response', 'assistant_response', 'reply', 'answer', 'bot_response']);
            if (this.isUsableMessageColumn(responseHint, rows)) {
                return responseHint;
            }

            const responseDetected = this.resolveColumnName(columnMap, '',
                ['response_text', 'response', 'assistant_response', 'reply', 'answer', 'bot_response']);
            if (this.isUsableMessageColumn(responseDetected, rows)) {
                return responseDetected;
            }

            return this.findBestLongTextColumn(columnMap, rows);
        },

        resolveResponseTextColumn(columnMap, rows, messageTextCol, usedColumns) {
            const hints = this.loadedHints || {};
            const responseCandidates = ['response_text', 'response', 'assistant_response', 'reply', 'answer', 'bot_response'];

            const fromHint = this.resolveColumnName(columnMap, hints.responseTextColumn, responseCandidates);
            if (this.isUsableResponseColumn(fromHint, rows, messageTextCol, usedColumns)) {
                return fromHint;
            }

            const bestDetected = this.findBestCandidateColumn(
                columnMap,
                rows,
                usedColumns,
                responseCandidates,
                (name) => this.isUsableResponseColumn(name, rows, messageTextCol, usedColumns),
                (name) => this.scoreSubjectColumn(name, rows)
            );
            if (bestDetected) {
                return bestDetected;
            }

            const detected = this.resolveColumnName(columnMap, '', responseCandidates);
            if (this.isUsableResponseColumn(detected, rows, messageTextCol, usedColumns)) {
                return detected;
            }

            return '';
        },

        resolveActorColumn(columnMap, rows, usedColumns) {
            const hints = this.loadedHints || {};
            const actorCandidates = [
                'username',
                'user_name',
                'display_name',
                'user_display_name',
                'author_name',
                'sender_name',
                'insert_user_name',
                'full_name',
                'insert_user_id',
                'insertuserid',
                'user_id',
                'author_id',
                'sender_id',
                'created_by',
                'created_by_user',
                'owner_id',
                'user',
                'author',
                'sender'
            ];

            const fromHint = this.resolveColumnName(columnMap, hints.actorColumn, actorCandidates);
            const hintedScore = this.isUsableActorColumn(fromHint, rows, usedColumns)
                ? this.scoreActorColumn(fromHint, rows)
                : Number.NEGATIVE_INFINITY;

            const bestDetected = this.findBestCandidateColumn(
                columnMap,
                rows,
                usedColumns,
                actorCandidates,
                (name) => this.isUsableActorColumn(name, rows, usedColumns),
                (name) => this.scoreActorColumn(name, rows)
            );
            if (bestDetected) {
                if (!fromHint || this.scoreActorColumn(bestDetected, rows) >= hintedScore) {
                    return bestDetected;
                }
            }

            const detected = this.resolveColumnName(columnMap, '', actorCandidates);
            if (this.isUsableActorColumn(detected, rows, usedColumns)) {
                return detected;
            }

            if (this.isUsableActorColumn(fromHint, rows, usedColumns)) {
                return fromHint;
            }

            return '';
        },

        resolveRoleColumn(columnMap, usedColumns) {
            const hints = this.loadedHints || {};

            const fromHint = this.resolveColumnName(columnMap, hints.roleColumn,
                ['role', 'sender_type', 'author_type', 'message_type', 'origin', 'source', 'direction']);
            if (this.isUsableRoleColumn(fromHint, usedColumns)) {
                return fromHint;
            }

            const detected = this.resolveColumnName(columnMap, '',
                ['role', 'sender_type', 'author_type', 'message_type', 'origin', 'source', 'direction']);
            if (this.isUsableRoleColumn(detected, usedColumns)) {
                return detected;
            }

            return '';
        },

        resolveConversationColumn(columnMap, rows, usedColumns) {
            const hints = this.loadedHints || {};
            const conversationCandidates = [
                'session_id',
                'sessionid',
                'session',
                'conversation_id',
                'conversationid',
                'conversation',
                'ai_conversation_id',
                'aiconversationid',
                'thread_id',
                'threadid',
                'thread',
                'dialog_id',
                'dialogid',
                'dialog',
                'discussion_id',
                'discussionid',
                'discussion',
                'topic_id',
                'topicid',
                'topic',
                'chat_id',
                'chatid',
                'chat',
                'widget_chat_id',
                'widgetchatid',
                'conversation_key',
                'session_key'
            ];

            const fromHint = this.resolveColumnName(columnMap, hints.conversationColumn, conversationCandidates);
            if (this.isUsableConversationColumn(fromHint, rows, usedColumns)) {
                const bestForHint = this.findBestCandidateColumn(
                    columnMap,
                    rows,
                    usedColumns,
                    conversationCandidates,
                    (name) => this.isUsableConversationColumn(name, rows, usedColumns),
                    (name) => this.scoreConversationColumn(name, rows)
                );
                if (bestForHint) {
                    const hintedScore = this.scoreConversationColumn(fromHint, rows);
                    const bestScore = this.scoreConversationColumn(bestForHint, rows);
                    return bestScore > hintedScore ? bestForHint : fromHint;
                }
                return fromHint;
            }

            const bestDetected = this.findBestCandidateColumn(
                columnMap,
                rows,
                usedColumns,
                conversationCandidates,
                (name) => this.isUsableConversationColumn(name, rows, usedColumns),
                (name) => this.scoreConversationColumn(name, rows)
            );
            if (bestDetected) {
                return bestDetected;
            }

            const detected = this.resolveColumnName(columnMap, '', conversationCandidates);
            if (this.isUsableConversationColumn(detected, rows, usedColumns)) {
                return detected;
            }

            return '';
        },

        resolveSubjectColumn(columnMap, rows, usedColumns) {
            const subjectCandidates = [
                'subject',
                'topic',
                'title',
                'thread_subject',
                'conversation_subject',
                'discussion_subject',
                'post_subject',
                'post_title',
                'name'
            ];

            const bestDetected = this.findBestCandidateColumn(
                columnMap,
                rows,
                usedColumns,
                subjectCandidates,
                (name) => this.isUsableSubjectColumn(name, rows, usedColumns),
                (name) => this.scoreSubjectColumn(name, rows)
            );
            if (bestDetected) {
                return bestDetected;
            }

            const detected = this.resolveColumnName(columnMap, '', subjectCandidates);
            if (this.isUsableSubjectColumn(detected, rows, usedColumns)) {
                return detected;
            }

            return '';
        },

        resolveSourceMessageIdColumn(columnMap, rows, usedColumns) {
            const idCandidates = [
                'message_id',
                'messageid',
                'conversation_message_id',
                'conversationmessageid'
            ];

            const bestDetected = this.findBestCandidateColumn(
                columnMap,
                rows,
                usedColumns,
                idCandidates,
                (name) => this.isUsableSourceMessageIdColumn(name, rows, usedColumns),
                (name) => this.scoreChainColumn(name, rows)
            );
            if (bestDetected) {
                return bestDetected;
            }

            const detected = this.resolveColumnName(columnMap, '', idCandidates);
            if (this.isUsableSourceMessageIdColumn(detected, rows, usedColumns)) {
                return detected;
            }

            return '';
        },

        resolveChainColumn(columnMap, rows, usedColumns, preferredConversationCol) {
            const chainCandidates = [
                'chain_key',
                'ai_conversation_id',
                'conversation_id',
                'session_id',
                'thread_id',
                'discussion_id',
                'dialog_id',
                'topic_id',
                'chat_id',
                'parent_id',
                'parent_message_id',
                'reply_to_id',
                'root_id',
                'conversation_key',
                'session_key'
            ];

            const bestDetected = this.findBestCandidateColumn(
                columnMap,
                rows,
                usedColumns,
                chainCandidates,
                (name) => this.isUsableChainColumn(name, rows, usedColumns),
                (name) => this.scoreChainColumn(name, rows)
            );
            if (bestDetected) {
                return bestDetected;
            }

            if (this.isUsableChainColumn(preferredConversationCol, rows, usedColumns)) {
                return preferredConversationCol;
            }

            return '';
        },

        findBestCandidateColumn(columnMap, rows, usedColumns, candidates, isUsable, scoreColumn) {
            if (!(columnMap instanceof Map) || !columnMap.size) {
                return '';
            }

            const matchedColumns = this.findMatchingColumns(columnMap, candidates);
            if (!matchedColumns.length) {
                return '';
            }

            let best = '';
            let bestScore = Number.NEGATIVE_INFINITY;

            for (const columnName of matchedColumns) {
                if (typeof isUsable === 'function' && !isUsable(columnName, rows, usedColumns)) {
                    continue;
                }
                const score = typeof scoreColumn === 'function' ? scoreColumn(columnName, rows) : 0;
                if (score > bestScore) {
                    bestScore = score;
                    best = columnName;
                }
            }

            return best;
        },

        findMatchingColumns(columnMap, candidates) {
            if (!(columnMap instanceof Map) || !columnMap.size || !Array.isArray(candidates) || !candidates.length) {
                return [];
            }

            const out = [];
            const seen = new Set();
            const pushUnique = (name) => {
                const key = String(name || '').toLowerCase();
                if (!key || seen.has(key)) {
                    return;
                }
                seen.add(key);
                out.push(name);
            };

            for (const candidate of candidates) {
                const lowerCandidate = String(candidate || '').toLowerCase();
                if (lowerCandidate && columnMap.has(lowerCandidate)) {
                    pushUnique(columnMap.get(lowerCandidate));
                }
            }

            for (const candidate of candidates) {
                const normalizedCandidate = this.normalizeColumnName(candidate);
                if (!normalizedCandidate) {
                    continue;
                }

                for (const original of columnMap.values()) {
                    const normalizedColumn = this.normalizeColumnName(original);
                    if (normalizedColumn === normalizedCandidate || normalizedColumn.includes(normalizedCandidate)) {
                        pushUnique(original);
                    }
                }
            }

            return out;
        },

        resolveCreatedAtColumn(columnMap, rows, usedColumns) {
            const hints = this.loadedHints || {};

            const fromHint = this.resolveColumnName(columnMap, hints.createdAtColumn,
                [
                    'date_inserted',
                    'dateinserted',
                    'created_at',
                    'created_on',
                    'created',
                    'createdon',
                    'createdat',
                    'timestamp',
                    'sent_at',
                    'message_time',
                    'event_time',
                    'posted_at',
                    'inserted_at',
                    'inserted',
                    'time',
                    'date'
                ]);
            if (this.isUsableCreatedAtColumn(fromHint, rows, usedColumns)) {
                return fromHint;
            }

            const detected = this.resolveColumnName(columnMap, '',
                [
                    'date_inserted',
                    'dateinserted',
                    'created_at',
                    'created_on',
                    'created',
                    'createdon',
                    'createdat',
                    'timestamp',
                    'sent_at',
                    'message_time',
                    'event_time',
                    'posted_at',
                    'inserted_at',
                    'inserted',
                    'time',
                    'date'
                ]);
            if (this.isUsableCreatedAtColumn(detected, rows, usedColumns)) {
                return detected;
            }

            return this.findBestCreatedAtColumn(columnMap, rows, usedColumns);
        },

        isUsableMessageColumn(columnName, rows) {
            if (!columnName) {
                return false;
            }
            if (this.isIdLikeColumn(columnName)) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            if (normalized.includes('created') || normalized.includes('updated') || normalized.includes('timestamp') || normalized.includes('date')) {
                return false;
            }

            return this.hasMeaningfulTextValues(rows, columnName, 12);
        },

        isUsableResponseColumn(columnName, rows, messageTextCol, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (messageTextCol && columnName.toLowerCase() === String(messageTextCol).toLowerCase()) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }
            if (this.isIdLikeColumn(columnName)) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            if (!(normalized.includes('response') || normalized.includes('reply') || normalized.includes('answer') || normalized.includes('bot'))) {
                return false;
            }

            return this.hasMeaningfulTextValues(rows, columnName, 10);
        },

        isUsableChainColumn(columnName, rows, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender') || normalized.includes('name')) {
                return false;
            }
            if (normalized.includes('created') || normalized.includes('updated') || normalized.includes('time') || normalized.includes('date')) {
                return false;
            }
            if (this.isMessageLikeColumn(columnName)) {
                return false;
            }

            const metrics = this.getColumnRepeatMetrics(rows, columnName, 120);
            return metrics.nonEmpty >= 4;
        },

        isUsableActorColumn(columnName, rows, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }
            const normalized = this.normalizeColumnName(columnName);
            if (this.isIdLikeColumn(columnName) && !this.isUserIdentityColumn(normalized)) {
                return false;
            }
            if (this.isMessageLikeColumn(columnName)) {
                return false;
            }

            const values = this.sampleColumnValues(rows, columnName, 40);
            if (!values.length) {
                return false;
            }

            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
            return avgLen > 0 && avgLen <= 64;
        },

        isUsableRoleColumn(columnName, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }
            return !this.isIdLikeColumn(columnName);
        },

        isUsableConversationColumn(columnName, rows, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender')) {
                return false;
            }
            if (normalized.includes('prompt') || normalized.includes('message') || normalized.includes('content') || normalized.includes('text') || normalized.includes('response')) {
                return false;
            }

            const values = this.sampleColumnValues(rows, columnName, 40);
            if (!values.length) {
                return false;
            }

            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
            return avgLen > 0 && avgLen <= 128;
        },

        scoreActorColumn(columnName, rows) {
            const normalized = this.normalizeColumnName(columnName);
            const values = this.sampleColumnValues(rows, columnName, 60);
            if (!values.length) {
                return Number.NEGATIVE_INFINITY;
            }

            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
            const numericLikeCount = values.filter((value) => /^\d+$/.test(value)).length;
            const numericRatio = numericLikeCount / values.length;

            let score = values.length * 2;

            if (normalized.includes('username')
                || normalized.includes('user_name')
                || normalized.includes('display_name')
                || normalized.includes('full_name')
                || normalized.includes('author_name')
                || normalized.includes('sender_name')
                || normalized.includes('insert_user_name')) {
                score += 70;
            }

            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender')) {
                score += 24;
            }
            if (normalized.includes('name')) {
                score += 12;
            }

            if (normalized.endsWith('id') || normalized.includes('_id')) {
                score -= 18;
            }

            if (normalized.includes('title') || normalized.includes('subject') || normalized.includes('topic')) {
                score -= 80;
            }

            score -= Math.round(numericRatio * 35);

            if (avgLen >= 2 && avgLen <= 48) {
                score += 10;
            }
            if (avgLen > 96) {
                score -= 20;
            }

            return score;
        },

        scoreConversationColumn(columnName, rows) {
            const normalized = this.normalizeColumnName(columnName);
            const values = this.sampleColumnValues(rows, columnName, 60);
            if (!values.length) {
                return Number.NEGATIVE_INFINITY;
            }

            const uniqueCount = new Set(values).size;
            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;

            let score = values.length * 2 + Math.min(uniqueCount, 40);

            if (normalized.includes('conversation') || normalized.includes('session')) {
                score += 36;
            }
            if (normalized.includes('thread') || normalized.includes('chat') || normalized.includes('dialog')) {
                score += 26;
            }
            if (normalized.includes('topic') || normalized.includes('discussion')) {
                score += 18;
            }
            if (normalized.includes('id')) {
                score += 6;
            }

            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender') || normalized.includes('name')) {
                score -= 35;
            }
            if (this.isMessageLikeColumn(columnName)) {
                score -= 80;
            }

            if (avgLen > 180) {
                score -= 20;
            }

            return score;
        },

        scoreSubjectColumn(columnName, rows) {
            const normalized = this.normalizeColumnName(columnName);
            const values = this.sampleColumnValues(rows, columnName, 60);
            if (!values.length) {
                return Number.NEGATIVE_INFINITY;
            }

            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
            const uniqueCount = new Set(values).size;

            let score = values.length + Math.min(uniqueCount, 30);

            if (normalized.includes('subject') || normalized.includes('title')) {
                score += 55;
            }
            if (normalized.includes('topic') || normalized.includes('thread') || normalized.includes('discussion')) {
                score += 35;
            }
            if (normalized === 'name') {
                score += 10;
            }

            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender')) {
                score -= 80;
            }
            if (normalized.endsWith('id') || normalized.includes('_id')) {
                score -= 60;
            }

            if (avgLen >= 12 && avgLen <= 180) {
                score += 12;
            }

            return score;
        },

        scoreChainColumn(columnName, rows) {
            const normalized = this.normalizeColumnName(columnName);
            const metrics = this.getColumnRepeatMetrics(rows, columnName, 150);
            if (metrics.nonEmpty < 4) {
                return Number.NEGATIVE_INFINITY;
            }

            let score = Math.min(metrics.nonEmpty, 40) * 0.5;

            if (normalized.includes('conversation') || normalized.includes('session')) {
                score += 35;
            }
            if (normalized.includes('thread') || normalized.includes('discussion') || normalized.includes('dialog') || normalized.includes('topic') || normalized.includes('chat')) {
                score += 25;
            }
            if (normalized.includes('parent') || normalized.includes('reply') || normalized.includes('root')) {
                score += 38;
            }
            if (normalized.includes('id') || normalized.includes('key')) {
                score += 8;
            }

            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender') || normalized.includes('name')) {
                score -= 65;
            }
            if (normalized.includes('subject') || normalized.includes('title')) {
                score -= 25;
            }

            if (metrics.maxGroup <= 1) {
                score -= 45;
            } else {
                score += 28 + Math.min(40, metrics.maxGroup * 3);
            }

            score += (1 - metrics.uniqueRatio) * 35;
            if (metrics.uniqueRatio > 0.98) {
                score -= 22;
            }
            if (metrics.uniqueRatio < 0.05) {
                score -= 12;
            }

            return score;
        },

        isUsableCreatedAtColumn(columnName, rows, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            const nameLooksTemporal = normalized.includes('created')
                || normalized.includes('updated')
                || normalized.includes('inserted')
                || normalized.includes('posted')
                || normalized.includes('time')
                || normalized.includes('date')
                || normalized.includes('timestamp');

            if (!nameLooksTemporal) {
                return false;
            }

            const values = this.sampleColumnValues(rows, columnName, 25);
            if (!values.length) {
                return true;
            }

            const temporalHits = values.filter((value) => this.looksTemporal(value)).length;
            return temporalHits >= Math.max(1, Math.floor(values.length / 3));
        },

        isUsableSubjectColumn(columnName, rows, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            const nameLooksSubject = normalized.includes('subject')
                || normalized.includes('topic')
                || normalized.includes('title')
                || normalized.includes('thread')
                || normalized.includes('discussion')
                || normalized === 'name';
            if (!nameLooksSubject) {
                return false;
            }

            if (normalized.includes('user') || normalized.includes('author') || normalized.includes('sender')) {
                return false;
            }

            const values = this.sampleColumnValues(rows, columnName, 40);
            if (!values.length) {
                return true;
            }

            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
            if (avgLen < 8) {
                return false;
            }

            const longLike = values.filter((value) => value.length >= 12 || /\s/.test(value)).length;
            return longLike >= Math.max(1, Math.floor(values.length / 3));
        },

        isUsableSourceMessageIdColumn(columnName, rows, usedColumns) {
            if (!columnName) {
                return false;
            }
            if (usedColumns && usedColumns.has(columnName.toLowerCase())) {
                return false;
            }

            const normalized = this.normalizeColumnName(columnName);
            const nameLooksMessageId = normalized.includes('message')
                || normalized.includes('conversationmessage');
            if (!nameLooksMessageId) {
                return false;
            }

            const values = this.sampleColumnValues(rows, columnName, 40);
            return values.length > 0;
        },

        findBestCreatedAtColumn(columnMap, rows, usedColumns) {
            if (!(columnMap instanceof Map) || !columnMap.size) {
                return '';
            }

            let bestColumn = '';
            let bestScore = -1;

            for (const original of columnMap.values()) {
                if (!original) {
                    continue;
                }
                if (usedColumns && usedColumns.has(original.toLowerCase())) {
                    continue;
                }

                const normalized = this.normalizeColumnName(original);
                const nameLooksTemporal = normalized.includes('created')
                    || normalized.includes('updated')
                    || normalized.includes('inserted')
                    || normalized.includes('posted')
                    || normalized.includes('time')
                    || normalized.includes('date')
                    || normalized.includes('timestamp');

                if (!nameLooksTemporal) {
                    continue;
                }

                if (this.isMessageLikeColumn(original)) {
                    continue;
                }

                const values = this.sampleColumnValues(rows, original, 40);
                const temporalHits = values.filter((value) => this.looksTemporal(value)).length;

                let score = 0;
                if (normalized.includes('created') || normalized.includes('inserted')) {
                    score += 35;
                }
                if (normalized.includes('posted')) {
                    score += 20;
                }
                if (normalized.includes('updated')) {
                    score -= 10;
                }
                if (normalized.includes('date') || normalized.includes('time') || normalized.includes('timestamp')) {
                    score += 12;
                }
                score += Math.min(values.length, 20);
                score += temporalHits * 5;

                if (values.length > 0 && temporalHits === 0) {
                    score -= 8;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestColumn = original;
                }
            }

            return bestScore > 0 ? bestColumn : '';
        },

        findBestLongTextColumn(columnMap, rows) {
            if (!(columnMap instanceof Map) || !columnMap.size) {
                return '';
            }

            let bestColumn = '';
            let bestScore = -1;

            for (const original of columnMap.values()) {
                if (!this.isUsableMessageColumn(original, rows)) {
                    continue;
                }

                const values = this.sampleColumnValues(rows, original, 40);
                if (!values.length) {
                    continue;
                }

                const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
                const whitespaceScore = values.some((value) => /\s/.test(value)) ? 20 : 0;
                const score = avgLen + whitespaceScore;

                if (score > bestScore) {
                    bestScore = score;
                    bestColumn = original;
                }
            }

            return bestColumn;
        },

        sampleColumnValues(rows, columnName, maxValues) {
            if (!Array.isArray(rows) || !columnName) {
                return [];
            }

            const out = [];
            const limit = Number.isInteger(maxValues) && maxValues > 0 ? maxValues : 25;
            for (let i = 0; i < rows.length && out.length < limit; i++) {
                const value = this.readCell(rows[i], columnName);
                if (value) {
                    out.push(value);
                }
            }
            return out;
        },

        getColumnRepeatMetrics(rows, columnName, maxValues) {
            const values = this.sampleColumnValues(rows, columnName, maxValues);
            if (!values.length) {
                return {
                    nonEmpty: 0,
                    unique: 0,
                    uniqueRatio: 1,
                    maxGroup: 0
                };
            }

            const counts = new Map();
            for (const rawValue of values) {
                const key = String(rawValue || '').trim().toLowerCase();
                if (!key) {
                    continue;
                }
                counts.set(key, (counts.get(key) || 0) + 1);
            }

            let maxGroup = 0;
            for (const count of counts.values()) {
                if (count > maxGroup) {
                    maxGroup = count;
                }
            }

            const nonEmpty = values.length;
            const unique = counts.size;
            return {
                nonEmpty,
                unique,
                uniqueRatio: nonEmpty > 0 ? (unique / nonEmpty) : 1,
                maxGroup
            };
        },

        columnsMostlyMatchValues(columnA, columnB, rows) {
            if (!columnA || !columnB || columnA === columnB || !Array.isArray(rows) || !rows.length) {
                return false;
            }

            let compared = 0;
            let matches = 0;
            for (let i = 0; i < rows.length && compared < 50; i++) {
                const left = this.readCell(rows[i], columnA);
                const right = this.readCell(rows[i], columnB);
                if (!left || !right) {
                    continue;
                }

                compared++;
                if (left.toLowerCase() === right.toLowerCase()) {
                    matches++;
                }
            }

            if (compared < 6) {
                return false;
            }

            return (matches / compared) >= 0.7;
        },

        hasMeaningfulTextValues(rows, columnName, minAvgLength) {
            const values = this.sampleColumnValues(rows, columnName, 30);
            if (!values.length) {
                return false;
            }

            const avgLen = values.reduce((sum, value) => sum + value.length, 0) / values.length;
            if (avgLen < (Number.isFinite(minAvgLength) ? minAvgLength : 10)) {
                return false;
            }

            const numericLike = values.filter((value) => /^[0-9_-]+$/.test(value)).length;
            return numericLike < values.length;
        },

        looksTemporal(value) {
            const text = String(value || '').trim();
            if (!text) {
                return false;
            }

            if (/^\d{4}-\d{2}-\d{2}/.test(text)) {
                return true;
            }
            if (/^\d{4}\/\d{2}\/\d{2}/.test(text)) {
                return true;
            }
            if (/^\d{10,13}$/.test(text)) {
                return true;
            }

            const parsed = Date.parse(text);
            if (!Number.isNaN(parsed)) {
                return true;
            }

            return false;
        },

        isUserIdentityColumn(normalizedName) {
            const normalized = String(normalizedName || '');
            return normalized.includes('user')
                || normalized.includes('author')
                || normalized.includes('sender')
                || normalized.includes('owner')
                || normalized.includes('created_by')
                || normalized.includes('insert_user');
        },

        isIdLikeColumn(columnName) {
            const normalized = this.normalizeColumnName(columnName);
            return normalized === 'id'
                || normalized.endsWith('_id')
                || normalized.endsWith('id')
                || normalized.includes('uuid')
                || normalized.includes('guid');
        },

        isMessageLikeColumn(columnName) {
            const normalized = this.normalizeColumnName(columnName);
            return normalized.includes('prompt')
                || normalized.includes('message')
                || normalized.includes('content')
                || normalized.includes('response')
                || normalized.includes('body')
                || normalized.includes('text')
                || normalized.includes('question')
                || normalized.includes('query')
                || normalized.includes('subject')
                || normalized.includes('title');
        },

        normalizeColumnName(columnName) {
            return String(columnName || '')
                .trim()
                .toLowerCase()
                .replace(/[^a-z0-9]+/g, '_');
        },

        composeConversationDisplay(conversationId, subject) {
            const conversation = String(conversationId || '').trim();
            const topic = String(subject || '').trim();
            if (conversation && topic) {
                return `${conversation} | ${topic}`;
            }
            return conversation || topic || '';
        },

        readCell(row, columnName) {
            if (!row || !columnName) {
                return '';
            }
            if (!Object.prototype.hasOwnProperty.call(row, columnName)) {
                return '';
            }
            const raw = row[columnName];
            if (raw === null || raw === undefined) {
                return '';
            }
            return String(raw).trim();
        },

        isLikelyHuman(roleValue, actorValue) {
            const role = String(roleValue || '').toLowerCase();
            if (role) {
                if (/(assistant|bot|system|ai|machine|auto|automation)/.test(role)) {
                    return false;
                }
                if (/(user|human|customer|member|client|person|author)/.test(role)) {
                    return true;
                }
            }

            const actor = String(actorValue || '').toLowerCase();
            if (actor) {
                if (/(assistant|bot|system|ai|automation)/.test(actor)) {
                    return false;
                }
                return true;
            }

            return true;
        },

        isHumanReadableText(text) {
            if (!text) {
                return false;
            }
            const normalized = String(text).replace(/\s+/g, ' ').trim();
            if (normalized.length < 6) {
                return false;
            }

            const letterCount = (normalized.match(/[A-Za-z]/g) || []).length;
            if (letterCount < 3 && normalized.length < 16) {
                return false;
            }

            return true;
        },

        renderHumanSummary(totalExtracted, totalFiltered) {
            const summary = byId('forumDbHumanSummary');
            if (!summary) {
                return;
            }

            if (!this.loadedColumns.length) {
                summary.textContent = 'No data loaded yet.';
                return;
            }

            if (!totalExtracted) {
                summary.textContent = 'No human message text was detected in the loaded rows.';
                return;
            }

            const confirmedCount = this.humanRowsAll.filter((entry) => entry && entry.isConfirmed).length;

            if (totalFiltered === totalExtracted) {
                summary.textContent = `Showing ${totalFiltered} human message row(s). Confirmed: ${confirmedCount}.`;
                return;
            }

            summary.textContent = `Showing ${totalFiltered} of ${totalExtracted} human message row(s). Confirmed: ${confirmedCount}.`;
        },

        getHumanPageCount() {
            return Math.max(1, Math.ceil(this.humanRowsFiltered.length / this.humanPageSize));
        },

        renderHumanPagination() {
            const prev = byId('forumDbHumanPrevBtn');
            const next = byId('forumDbHumanNextBtn');
            const info = byId('forumDbHumanPageInfo');
            const jumpInput = byId('forumDbHumanPageJumpInput');
            const jumpBtn = byId('forumDbHumanPageJumpBtn');

            const totalRows = this.humanRowsFiltered.length;
            const pageCount = this.getHumanPageCount();
            if (this.humanCurrentPage > pageCount) {
                this.humanCurrentPage = pageCount;
            }
            if (this.humanCurrentPage < 1) {
                this.humanCurrentPage = 1;
            }

            if (prev) {
                prev.disabled = this.humanCurrentPage <= 1;
            }
            if (next) {
                next.disabled = this.humanCurrentPage >= pageCount;
            }
            if (info) {
                if (!totalRows) {
                    info.textContent = 'Page 1 of 1 (0 messages)';
                } else {
                    info.textContent = `Page ${this.humanCurrentPage} of ${pageCount} (${totalRows} messages)`;
                }
            }
            if (jumpInput) {
                jumpInput.max = String(pageCount);
                jumpInput.min = '1';
                jumpInput.value = String(this.humanCurrentPage);
                jumpInput.disabled = totalRows === 0;
            }
            if (jumpBtn) {
                jumpBtn.disabled = totalRows === 0;
            }
        },

        jumpToHumanPageFromInput() {
            const input = byId('forumDbHumanPageJumpInput');
            if (!input) {
                return;
            }

            const requested = Number.parseInt(String(input.value || '').trim(), 10);
            if (!Number.isInteger(requested)) {
                input.value = String(this.humanCurrentPage || 1);
                return;
            }

            this.goToHumanPage(requested);
        },

        goToHumanPage(page) {
            const pageCount = this.getHumanPageCount();
            const target = Math.max(1, Math.min(pageCount, Number(page) || 1));
            this.humanCurrentPage = target;
            this.selectedHumanAbsoluteIndex = -1;
            this.resetReplyState(true);
            this.renderHumanPagination();
            this.renderCurrentHumanPage();
            this.renderHumanDetail(null);
            this.updateOverlayActionState();
        },

        renderCurrentHumanPage() {
            const rows = Array.isArray(this.humanRowsFiltered) ? this.humanRowsFiltered : [];
            if (!rows.length) {
                this.renderHumanTable([], 0);
                return;
            }

            const start = (this.humanCurrentPage - 1) * this.humanPageSize;
            const pageRows = rows.slice(start, start + this.humanPageSize);
            this.renderHumanTable(pageRows, start);
        },

        renderHumanTable(rows, absoluteStartIndex) {
            const body = byId('forumDbHumanBody');
            if (!body) {
                return;
            }

            const safeRows = Array.isArray(rows) ? rows : [];
            if (!safeRows.length) {
                body.innerHTML = '<tr><td class="empty-row" colspan="5">No human messages matched the current filters.</td></tr>';
                body._rowsCache = [];
                return;
            }

            const html = safeRows.map((entry, idx) => {
                const createdAt = entry.createdAt || '';
                const conversation = entry.conversationDisplay || entry.conversationId || entry.subject || '';
                const actor = entry.actor || '';
                const confirmedLabel = entry.isConfirmed ? 'Yes' : 'No';
                const confirmedClass = entry.isConfirmed ? 'forum-confirmed-yes' : 'forum-confirmed-no';
                const messageText = this.truncateForTable(entry.messageText, 260);
                const absoluteIndex = absoluteStartIndex + idx;
                return `<tr data-absolute-index="${absoluteIndex}">`
                    + `<td>${escapeHtml(createdAt)}</td>`
                    + `<td>${escapeHtml(conversation)}</td>`
                    + `<td>${escapeHtml(actor)}</td>`
                    + `<td class="${confirmedClass}">${escapeHtml(confirmedLabel)}</td>`
                    + `<td class="forum-human-message-cell">${escapeHtml(messageText)}</td>`
                    + '</tr>';
            }).join('');

            body.innerHTML = html;
            body._rowsCache = this.humanRowsFiltered;
        },

        truncateForTable(text, maxLen) {
            const value = String(text || '');
            if (value.length <= maxLen) {
                return value;
            }
            return value.slice(0, maxLen) + '...';
        },

        onHumanRowClick(event) {
            const tr = event && event.target && event.target.closest
                ? event.target.closest('tr[data-absolute-index]')
                : null;
            if (!tr) {
                return;
            }

            const body = byId('forumDbHumanBody');
            const rows = body && Array.isArray(body._rowsCache) ? body._rowsCache : [];
            const absoluteIndex = Number.parseInt(tr.getAttribute('data-absolute-index') || '-1', 10);
            if (!Number.isInteger(absoluteIndex) || absoluteIndex < 0 || absoluteIndex >= rows.length) {
                return;
            }

            this.selectedHumanAbsoluteIndex = absoluteIndex;
            Array.from(body.querySelectorAll('tr[data-absolute-index]')).forEach((row) => {
                row.classList.toggle('selected', row === tr);
            });
            this.clearTranslationUi();
            this.resetReplyState(true);
            this.repliesLoading = true;
            const selectedEntry = rows[absoluteIndex];
            this.renderHumanDetail(selectedEntry);
            this.openOverlay();
            this.updateOverlayActionState();
            this.loadRepliesForEntry(selectedEntry, absoluteIndex);
        },

        async loadRepliesForEntry(entry, absoluteIndex) {
            const token = ++this.repliesRequestToken;
            if (!entry) {
                this.selectedReplyRows = [];
                this.repliesLoading = false;
                this.repliesError = '';
                this.renderHumanDetail(null);
                return;
            }

            const params = this.buildParams('threadReplies');
            const discussionId = String(entry.conversationId || '').trim();
            const messageId = String(entry.sourceMessageId || '').trim();
            if (!discussionId && !messageId) {
                this.selectedReplyRows = [];
                this.repliesLoading = false;
                this.repliesError = 'This row is missing conversation and message identifiers.';
                if (this.selectedHumanAbsoluteIndex === absoluteIndex) {
                    this.renderHumanDetail(entry);
                }
                return;
            }
            if (discussionId) {
                params.set('discussionId', discussionId);
                params.set('conversationId', discussionId);
            }
            if (messageId) {
                params.set('messageId', messageId);
            }
            params.set('limit', '500');

            try {
                const result = await Api.postUrlEncoded(`${this.contextPath}/admin/forum-db/explorer`, params);
                if (token !== this.repliesRequestToken) {
                    return;
                }

                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    const message = result.payload && result.payload.message
                        ? result.payload.message
                        : `Replies query failed (${result.status}).`;
                    this.selectedReplyRows = [];
                    this.repliesError = message;
                    this.repliesLoading = false;
                    if (this.selectedHumanAbsoluteIndex === absoluteIndex) {
                        this.renderHumanDetail(entry);
                    }
                    return;
                }

                const replies = Array.isArray(result.payload.replies) ? result.payload.replies : [];
                this.selectedReplyRows = replies.map((row) => ({
                    createdAt: row && row.created_at ? String(row.created_at) : '',
                    actor: row && row.actor ? String(row.actor) : '',
                    role: row && row.role ? String(row.role) : 'reply',
                    messageText: row && row.message_text ? this.cleanMessageText(row.message_text) : ''
                })).filter((row) => row.messageText);
                this.repliesError = '';
                this.repliesLoading = false;
                if (this.selectedHumanAbsoluteIndex === absoluteIndex) {
                    this.renderHumanDetail(entry);
                }
            } catch (error) {
                if (token !== this.repliesRequestToken) {
                    return;
                }
                this.selectedReplyRows = [];
                this.repliesError = error && error.message ? String(error.message) : 'Unable to load replies.';
                this.repliesLoading = false;
                if (this.selectedHumanAbsoluteIndex === absoluteIndex) {
                    this.renderHumanDetail(entry);
                }
            }
        },

        renderHumanDetail(entry) {
            const box = byId('forumDbHumanDetail');
            if (!box) {
                return;
            }

            if (!entry) {
                box.innerHTML = '<div class="small-note">Select a human message row to view full text.</div>';
                this.clearTranslationUi();
                this.closeOverlay();
                return;
            }

            box.innerHTML = ''
                + `<div class="forum-human-detail-meta"><strong>Conversation/Session:</strong> ${escapeHtml(entry.conversationId || '(none)')}</div>`
                + `<div class="forum-human-detail-meta"><strong>Subject:</strong> ${escapeHtml(entry.subject || '(none)')}</div>`
                + `<div class="forum-human-detail-meta"><strong>User:</strong> ${escapeHtml(entry.actor || '(unknown)')}</div>`
                + `<div class="forum-human-detail-meta"><strong>Created:</strong> ${escapeHtml(entry.createdAt || '(unknown)')}</div>`
                + `<div class="forum-human-detail-meta"><strong>Confirmed:</strong> ${escapeHtml(entry.isConfirmed ? 'Yes' : 'No')}</div>`
                + `<div class="forum-human-detail-meta"><strong>Q&A Status:</strong> ${escapeHtml(entry.qnaStatus || '(none)')}</div>`
                + `<div class="forum-human-detail-meta"><strong>Status Name:</strong> ${escapeHtml(entry.statusName || '(none)')}</div>`
                + `<div class="forum-human-detail-meta"><strong>Tags:</strong> ${escapeHtml(entry.tagNames || '(none)')}</div>`
                + `<div class="forum-human-detail-block"><strong>Human Message</strong><pre>${escapeHtml(entry.messageText || '')}</pre></div>`
                + this.buildRelatedConversationChainHtml(entry);
        },

        resolveConfirmedColumn(columnMap) {
            return this.resolveColumnName(columnMap, 'community_confirmed', ['community_confirmed', 'is_confirmed', 'confirmed']);
        },

        resolveQnaStatusColumn(columnMap) {
            return this.resolveColumnName(columnMap, 'qna_status', ['qna_status', 'qna', 'question_status']);
        },

        resolveStatusNameColumn(columnMap) {
            return this.resolveColumnName(columnMap, 'status_name', ['status_name', 'status', 'record_status_name']);
        },

        resolveTagNamesColumn(columnMap) {
            return this.resolveColumnName(columnMap, 'tag_names', ['tag_names', 'tags', 'tag_list']);
        },

        isConfirmedDiscussion(row, confirmedCol, qnaStatus, statusName, tagNames) {
            const confirmedRaw = this.readCell(row, confirmedCol).toLowerCase();
            if (confirmedRaw) {
                if (confirmedRaw === '1' || confirmedRaw === 'true' || confirmedRaw === 'yes' || confirmedRaw === 'y') {
                    return true;
                }
                if (confirmedRaw === '0' || confirmedRaw === 'false' || confirmedRaw === 'no' || confirmedRaw === 'n') {
                    return false;
                }
            }

            const hasConfirmedSignal = (valueText) => {
                const text = String(valueText || '').toLowerCase();
                return text.includes('accepted') || text.includes('answered');
            };

            return hasConfirmedSignal(qnaStatus)
                || hasConfirmedSignal(statusName)
                || hasConfirmedSignal(tagNames);
        },

        buildRelatedConversationChainHtml(entry) {
            if (this.repliesLoading) {
                return '<div class="forum-human-detail-block"><strong>Replies</strong><div class="small-note">Loading replies...</div></div>';
            }

            if (this.repliesError) {
                return '<div class="forum-human-detail-block"><strong>Replies</strong><div class="small-note">Unable to load replies: ' + escapeHtml(this.repliesError) + '</div></div>';
            }

            const replies = Array.isArray(this.selectedReplyRows) ? this.selectedReplyRows : [];
            if (!replies.length) {
                return '<div class="forum-human-detail-block"><strong>Replies</strong><div class="small-note">No reply messages detected for this message yet.</div></div>';
            }

            const preview = replies.slice(0, 12);
            const items = preview.map((row) => {
                const createdAt = escapeHtml(row.createdAt || '(unknown)');
                const actor = escapeHtml(row.actor || '(unknown)');
                const snippet = escapeHtml(this.truncateForTable(row.messageText || '', 180));
                return `<li><strong>${createdAt}</strong> - ${actor}<br>${snippet}</li>`;
            }).join('');

            const remainder = replies.length - preview.length;
            const more = remainder > 0
                ? `<div class="small-note">${remainder} more reply message(s) are not shown here.</div>`
                : '';

            return ''
                + `<div class="forum-human-detail-block">`
                + `<strong>Replies In Same Conversation (${replies.length})</strong>`
                + '<ul class="forum-human-chain-list">'
                + items
                + '</ul>'
                + more
                + '</div>';
        },

        getReplyRowsForEntry(entry) {
            const chain = this.getConversationChainRows(entry);
            if (!chain.length) {
                return this.getDirectReplyRowsForEntry(entry);
            }

            const selectedTime = this.safeCreatedAtSortValue(entry && entry.createdAt);
            return chain.filter((row) => {
                if (!row || row.isReply !== true) {
                    return false;
                }

                const rowTime = this.safeCreatedAtSortValue(row.createdAt);
                if (selectedTime === Number.MAX_SAFE_INTEGER || rowTime === Number.MAX_SAFE_INTEGER) {
                    return true;
                }
                return rowTime >= selectedTime;
            });
        },

        getDirectReplyRowsForEntry(entry) {
            if (!entry || typeof entry !== 'object') {
                return [];
            }

            const columns = this.detectedColumns || {};
            const responseColumn = columns.responseTextColumn || '';
            if (!responseColumn || !Array.isArray(this.loadedRows) || !this.loadedRows.length) {
                return [];
            }

            const rowIndex = Number.isInteger(entry.rowIndex) ? entry.rowIndex : -1;
            if (rowIndex < 0 || rowIndex >= this.loadedRows.length) {
                return [];
            }

            const sourceRow = this.loadedRows[rowIndex];
            const responseRaw = this.readCell(sourceRow, responseColumn);
            if (!responseRaw) {
                return [];
            }

            const cleaned = this.cleanMessageText(responseRaw);
            if (!cleaned || this.shouldExcludeMessage(cleaned)) {
                return [];
            }

            const originalMessage = String(entry.messageText || '').trim();
            if (originalMessage && cleaned === originalMessage) {
                return [];
            }

            const actor = this.resolveTimelineActor('', 'assistant', true);
            return [{
                createdAt: String(entry.createdAt || ''),
                conversationId: String(entry.conversationId || ''),
                subject: String(entry.subject || ''),
                conversationDisplay: this.composeConversationDisplay(entry.conversationId, entry.subject),
                actor,
                messageText: cleaned,
                isReply: true
            }];
        },

        getConversationChainRows(entry) {
            const fromRawRows = this.buildRawConversationChainRows(entry);
            if (fromRawRows.length) {
                return fromRawRows;
            }

            if (!entry || !Array.isArray(this.humanRowsAll) || !this.humanRowsAll.length) {
                return [];
            }

            const key = this.getConversationChainKey(entry);
            if (!key) {
                return [];
            }

            const matches = this.humanRowsAll.filter((row) => this.getConversationChainKey(row) === key);
            matches.sort((a, b) => this.compareByCreatedAt(a, b));
            return matches;
        },

        getConversationChainKey(entry) {
            if (!entry || typeof entry !== 'object') {
                return '';
            }

            const chainValue = this.resolveEntryChainValue(entry);
            if (chainValue) {
                return `k:${chainValue.toLowerCase()}`;
            }

            const conversation = String(entry.conversationId || '').trim();
            if (conversation) {
                return `c:${conversation.toLowerCase()}`;
            }

            const subject = String(entry.subject || '').trim();
            if (subject) {
                return `s:${subject.toLowerCase()}`;
            }

            return '';
        },

        buildRawConversationChainRows(entry) {
            if (!entry || !Array.isArray(this.loadedRows) || !this.loadedRows.length) {
                return [];
            }

            const columns = this.detectedColumns || {};
            const chainColumn = this.activeChainColumn || columns.chainColumn || columns.conversationColumn || '';
            const targetChainValue = this.resolveEntryChainValue(entry);
            let matchingRows = [];

            if (chainColumn && targetChainValue) {
                const normalizedTarget = targetChainValue.toLowerCase();
                matchingRows = this.loadedRows.filter((row) => {
                    const value = this.readCell(row, chainColumn);
                    return value && value.toLowerCase() === normalizedTarget;
                });
            }

            if (!matchingRows.length) {
                const subjectColumn = columns.subjectColumn || '';
                const subjectValue = String(entry.subject || '').trim();
                if (subjectColumn && subjectValue) {
                    const normalizedSubject = subjectValue.toLowerCase();
                    matchingRows = this.loadedRows.filter((row) => {
                        const value = this.readCell(row, subjectColumn);
                        return value && value.toLowerCase() === normalizedSubject;
                    });
                }
            }

            if (!matchingRows.length && Number.isInteger(entry.rowIndex) && entry.rowIndex >= 0 && entry.rowIndex < this.loadedRows.length) {
                matchingRows = [this.loadedRows[entry.rowIndex]];
            }

            if (!matchingRows.length) {
                return [];
            }

            const messageColumn = columns.messageTextColumn || '';
            const responseColumn = columns.responseTextColumn || '';
            const actorColumn = columns.actorColumn || '';
            const roleColumn = columns.roleColumn || '';
            const createdAtColumn = columns.createdAtColumn || '';
            const conversationColumn = columns.conversationColumn || '';
            const subjectColumn = columns.subjectColumn || '';

            const timeline = [];
            const dedupe = new Set();

            for (const row of matchingRows) {
                const createdAt = this.readCell(row, createdAtColumn);
                const conversationId = this.readCell(row, conversationColumn);
                const subject = this.readCell(row, subjectColumn);
                const actor = this.readCell(row, actorColumn);
                const role = this.readCell(row, roleColumn);

                const pushTimelineItem = (rawText, isResponse) => {
                    const cleaned = this.cleanMessageText(rawText);
                    if (!cleaned || this.shouldExcludeMessage(cleaned)) {
                        return;
                    }

                    const actorLabel = this.resolveTimelineActor(actor, role, isResponse);
                    const replyFlag = isResponse || this.isLikelyReplyByRoleOrActor(role, actorLabel);
                    const dedupeKey = [createdAt, actorLabel, cleaned.slice(0, 300), replyFlag ? 'r' : 'm'].join('|').toLowerCase();
                    if (dedupe.has(dedupeKey)) {
                        return;
                    }
                    dedupe.add(dedupeKey);

                    timeline.push({
                        createdAt,
                        conversationId,
                        subject,
                        conversationDisplay: this.composeConversationDisplay(conversationId, subject),
                        actor: actorLabel,
                        messageText: cleaned,
                        isReply: replyFlag
                    });
                };

                if (messageColumn) {
                    pushTimelineItem(this.readCell(row, messageColumn), false);
                }
                if (responseColumn) {
                    const responseText = this.readCell(row, responseColumn);
                    const messageText = messageColumn ? this.readCell(row, messageColumn) : '';
                    if (responseText && responseText !== messageText) {
                        pushTimelineItem(responseText, true);
                    }
                }
            }

            timeline.sort((a, b) => this.compareByCreatedAt(a, b));
            return timeline;
        },

        isLikelyReplyByRoleOrActor(role, actor) {
            const roleText = String(role || '').toLowerCase();
            const actorText = String(actor || '').toLowerCase();
            return /(assistant|bot|system|ai|agent|automation|responder|reply)/.test(roleText)
                || /(assistant|bot|system|ai|agent|automation|responder|reply)/.test(actorText);
        },

        resolveTimelineActor(actor, role, isResponse) {
            const actorText = String(actor || '').trim();
            const roleText = String(role || '').trim();
            if (actorText) {
                return actorText;
            }
            if (roleText) {
                return roleText;
            }
            return isResponse ? 'assistant' : '(unknown)';
        },

        resolveEntryChainValue(entry) {
            if (!entry || typeof entry !== 'object') {
                return '';
            }

            const explicit = String(entry.chainValue || '').trim();
            if (explicit) {
                return explicit;
            }

            const conversation = String(entry.conversationId || '').trim();
            if (conversation) {
                return conversation;
            }

            const subject = String(entry.subject || '').trim();
            if (subject) {
                return subject;
            }

            return '';
        },

        compareByCreatedAt(a, b) {
            const aValue = this.safeCreatedAtSortValue(a && a.createdAt);
            const bValue = this.safeCreatedAtSortValue(b && b.createdAt);
            if (aValue !== bValue) {
                return aValue - bValue;
            }

            const aText = String((a && a.messageText) || '');
            const bText = String((b && b.messageText) || '');
            return aText.localeCompare(bText);
        },

        safeCreatedAtSortValue(value) {
            const text = String(value || '').trim();
            if (!text) {
                return Number.MAX_SAFE_INTEGER;
            }
            const parsed = Date.parse(text);
            if (!Number.isNaN(parsed)) {
                return parsed;
            }
            return Number.MAX_SAFE_INTEGER;
        },

        openOverlay() {
            const overlay = byId('forumDbHumanOverlay');
            if (!overlay) {
                return;
            }
            overlay.style.display = 'block';
        },

        closeOverlay() {
            const overlay = byId('forumDbHumanOverlay');
            if (!overlay) {
                return;
            }
            overlay.style.display = 'none';
        },

        updateOverlayActionState() {
            const hasRows = this.humanRowsAll.length > 0;
            const hasSelection = this.getSelectedEntry() != null;

            const exportBtn = byId('forumDbExportThreadsBtn');
            if (exportBtn) {
                exportBtn.disabled = !hasRows;
            }

            const emailBtn = byId('forumDbOverlayEmailBtn');
            if (emailBtn) {
                emailBtn.disabled = !hasSelection;
            }

            const translateBtn = byId('forumDbOverlayTranslateBtn');
            if (translateBtn) {
                translateBtn.disabled = !hasSelection;
            }
        },

        async translateSelectedMessage() {
            const selected = this.getSelectedEntry();
            const text = selected ? String(selected.messageText || '') : '';
            if (!text.trim()) {
                this.setTranslateMeta('Select a human message row first, then click Translate Message.', true);
                this.setTranslateOutput('');
                return;
            }

            const targetLang = value('forumDbOverlayTranslateLang') || 'en';
            this.setTranslateMeta('Translating...', false);
            this.setTranslateOutput('');

            try {
                const result = await Api.postJson(`${this.contextPath}/dashboard/widgets/drilldown/review/translate`, {
                    text,
                    targetLang
                });

                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    const message = result.payload && result.payload.message
                        ? result.payload.message
                        : `Translate failed (${result.status}).`;
                    this.setTranslateMeta(`Translation failed: ${message}`, true);
                    this.setTranslateOutput('');
                    return;
                }

                const sourceLang = String(result.payload.sourceLang || 'auto');
                const finalTarget = String(result.payload.targetLang || targetLang);
                const translatedText = String(result.payload.translatedText || '(empty translation)');

                this.setTranslateMeta(`Translated: ${sourceLang} -> ${finalTarget}`, false);
                this.setTranslateOutput(translatedText);
            } catch (e) {
                this.setTranslateMeta(`Translation error: ${e.message}`, true);
                this.setTranslateOutput('');
            }
        },

        getSelectedEntry() {
            if (!Number.isInteger(this.selectedHumanAbsoluteIndex)) {
                return null;
            }
            if (this.selectedHumanAbsoluteIndex < 0 || this.selectedHumanAbsoluteIndex >= this.humanRowsFiltered.length) {
                return null;
            }
            return this.humanRowsFiltered[this.selectedHumanAbsoluteIndex] || null;
        },

        async exportMessagesAndRepliesCsv() {
            const exportBtn = byId('forumDbExportThreadsBtn');
            if (exportBtn) {
                exportBtn.disabled = true;
            }

            try {
                const exportScope = this.getSelectedExportScope();
                const targets = this.getExportThreadTargets(exportScope);
                if (!targets.length) {
                    this.setShareMessage(
                        exportScope === 'confirmed'
                            ? 'No confirmed chat posts are available to export.'
                            : 'No loaded messages are available to export.',
                        true
                    );
                    return;
                }

                const flattened = [];
                let totalReplies = 0;
                const scopeLabel = exportScope === 'confirmed' ? 'confirmed message thread(s)' : 'message thread(s)';
                this.setShareMessage(`Building CSV from ${targets.length} ${scopeLabel}...`, false);

                for (let i = 0; i < targets.length; i++) {
                    const entry = targets[i];
                    if ((i + 1) % 5 === 0 || i === targets.length - 1) {
                        this.setShareMessage(`Collecting replies for thread ${i + 1} of ${targets.length}...`, false);
                    }

                    const confirmedText = entry.isConfirmed ? 'Yes' : 'No';

                    flattened.push({
                        discussionId: String(entry.conversationId || ''),
                        subject: String(entry.subject || ''),
                        rowType: 'message',
                        rowOrder: 0,
                        createdAt: String(entry.createdAt || ''),
                        actor: String(entry.actor || ''),
                        messageText: String(entry.messageText || ''),
                        confirmed: confirmedText,
                        qnaStatus: String(entry.qnaStatus || ''),
                        statusName: String(entry.statusName || ''),
                        tagNames: String(entry.tagNames || '')
                    });

                    const replies = await this.fetchRepliesForExport(entry);
                    for (let r = 0; r < replies.length; r++) {
                        const reply = replies[r];
                        flattened.push({
                            discussionId: String(entry.conversationId || ''),
                            subject: String(entry.subject || ''),
                            rowType: 'reply',
                            rowOrder: r + 1,
                            createdAt: String(reply.createdAt || ''),
                            actor: String(reply.actor || ''),
                            messageText: String(reply.messageText || ''),
                            confirmed: confirmedText,
                            qnaStatus: String(entry.qnaStatus || ''),
                            statusName: String(entry.statusName || ''),
                            tagNames: String(entry.tagNames || '')
                        });
                    }
                    totalReplies += replies.length;
                }

                const csv = this.toThreadCsv(flattened);
                const filename = exportScope === 'confirmed'
                    ? `forum-confirmed-messages-with-replies-${this.timestampForFilename()}.csv`
                    : `forum-messages-with-replies-${this.timestampForFilename()}.csv`;
                const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(url);

                this.setShareMessage(
                    `Exported ${targets.length} ${scopeLabel} with ${totalReplies} reply row(s) to ${filename}.`,
                    false
                );
            } catch (error) {
                const msg = error && error.message ? String(error.message) : 'Unable to export messages and replies.';
                this.setShareMessage(msg, true);
            } finally {
                if (exportBtn) {
                    exportBtn.disabled = false;
                }
                this.updateOverlayActionState();
            }
        },

        getSelectedExportScope() {
            const raw = String(byId('forumDbExportScope')?.value || 'all').trim().toLowerCase();
            return raw === 'confirmed' ? 'confirmed' : 'all';
        },

        getExportThreadTargets(scope) {
            const sourceRows = Array.isArray(this.humanRowsAll) ? this.humanRowsAll : [];
            const targets = [];
            const seen = new Set();
            const confirmedOnly = scope === 'confirmed';

            for (const row of sourceRows) {
                if (confirmedOnly && !(row && row.isConfirmed)) {
                    continue;
                }
                const discussionId = String((row && row.conversationId) || '').trim();
                const subject = String((row && row.subject) || '').trim();
                const fallbackId = String((row && row.sourceMessageId) || '').trim();
                const key = discussionId || (fallbackId ? `m:${fallbackId}` : `s:${subject.toLowerCase()}`);
                if (!key || seen.has(key)) {
                    continue;
                }
                seen.add(key);
                targets.push(row);
            }

            return targets;
        },

        async fetchRepliesForExport(entry) {
            const params = this.buildParams('threadReplies');
            const discussionId = String((entry && entry.conversationId) || '').trim();
            const messageId = String((entry && entry.sourceMessageId) || '').trim();

            if (discussionId) {
                params.set('discussionId', discussionId);
                params.set('conversationId', discussionId);
            }
            if (messageId) {
                params.set('messageId', messageId);
            }
            params.set('limit', '500');

            const result = await Api.postUrlEncoded(`${this.contextPath}/admin/forum-db/explorer`, params);
            if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                const detail = result.payload && result.payload.message
                    ? String(result.payload.message)
                    : `Replies query failed (${result.status}).`;
                throw new Error(`Failed to export replies for discussion ${discussionId || '(unknown)'}: ${detail}`);
            }

            const replies = Array.isArray(result.payload.replies) ? result.payload.replies : [];
            return replies.map((row) => ({
                createdAt: row && row.created_at ? String(row.created_at) : '',
                actor: row && row.actor ? String(row.actor) : '',
                messageText: row && row.message_text ? this.cleanMessageText(row.message_text) : ''
            })).filter((row) => row.messageText);
        },

        toThreadCsv(rows) {
            const header = [
                'discussion_id',
                'subject',
                'row_type',
                'row_order',
                'created_at',
                'user',
                'community_confirmed',
                'qna_status',
                'status_name',
                'tag_names',
                'message_text'
            ];
            const lines = [header.join(',')];

            for (const row of rows) {
                lines.push([
                    this.csvCell(row.discussionId || row.conversationId || ''),
                    this.csvCell(row.subject || ''),
                    this.csvCell(row.rowType || ''),
                    this.csvCell(row.rowOrder || ''),
                    this.csvCell(row.createdAt || ''),
                    this.csvCell(row.actor || ''),
                    this.csvCell(row.confirmed || ''),
                    this.csvCell(row.qnaStatus || ''),
                    this.csvCell(row.statusName || ''),
                    this.csvCell(row.tagNames || ''),
                    this.csvCell(row.messageText || '')
                ].join(','));
            }

            return lines.join('\n');
        },

        csvCell(value) {
            const text = String(value == null ? '' : value);
            return '"' + text.replace(/"/g, '""') + '"';
        },

        timestampForFilename() {
            const now = new Date();
            const pad = (n) => String(n).padStart(2, '0');
            return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}-${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
        },

        async emailCleanedMessages() {
            const selected = this.getSelectedEntry();
            const rows = selected
                ? [selected]
                : [];
            if (!rows.length) {
                this.setShareMessage('Select a message row first, then use Email in the overlay.', true);
                return;
            }

            const recipients = this.parseRecipients(value('forumDbOverlayShareTo'));
            if (!recipients.length) {
                this.setShareMessage('Add at least one recipient email address before sending.', true);
                return;
            }

            const subject = value('forumDbOverlayShareSubject') || 'Cleaned Forum Human Message';
            const body = this.buildEmailBody(rows);

            this.setShareMessage('Sending cleaned messages email...', false);

            try {
                const payload = {
                    to: recipients,
                    subject,
                    textBody: body
                };

                const result = await Api.postJson(`${this.contextPath}/admin/email/send`, payload);
                if (!result.ok || !result.payload || result.payload.status !== 'ok') {
                    const message = result.payload && result.payload.message
                        ? result.payload.message
                        : `Email send failed (${result.status}).`;
                    this.setShareMessage(message, true);
                    return;
                }

                this.setShareMessage(`Emailed ${rows.length} cleaned message row(s) to ${recipients.length} recipient(s).`, false);
            } catch (e) {
                this.setShareMessage(`Email send error: ${e.message}`, true);
            }
        },

        parseRecipients(raw) {
            return String(raw || '')
                .split(/[\n,;]+/)
                .map((s) => s.trim())
                .filter(Boolean);
        },

        buildEmailBody(rows) {
            const lines = [];
            lines.push('Cleaned Forum Human Messages');
            lines.push(`Generated: ${new Date().toISOString()}`);
            lines.push(`Total rows included: ${rows.length}`);
            lines.push('');

            let consumed = lines.join('\n').length;
            let included = 0;
            for (let i = 0; i < rows.length; i++) {
                const row = rows[i];
                const block = [
                    `Message ${i + 1}`,
                    `Created: ${row.createdAt || ''}`,
                    `Conversation/Session: ${row.conversationDisplay || row.conversationId || row.subject || ''}`,
                    `User: ${row.actor || ''}`,
                    'Text:',
                    row.messageText || '',
                    ''
                ].join('\n');

                if (consumed + block.length > MAX_EMAIL_BODY_CHARS) {
                    lines.push(`... truncated. Included ${included} of ${rows.length} message row(s) due to email size limits.`);
                    break;
                }

                lines.push(block);
                consumed += block.length;
                included++;
            }

            return lines.join('\n');
        },

        clearLoadedRowsOnly() {
            this.loadedColumns = [];
            this.loadedRows = [];
            this.loadedTable = '';
            this.loadedEntity = '';
            this.loadedHints = null;
            this.detectedColumns = null;
            this.activeChainColumn = '';
            this.humanRowsAll = [];
            this.humanRowsFiltered = [];
            this.humanCurrentPage = 1;
            this.selectedHumanAbsoluteIndex = -1;
            this.resetReplyState(true);
            this.renderHumanSummary(0, 0);
            this.renderHumanPagination();
            this.renderHumanTable([], 0);
            this.renderHumanDetail(null);
            this.clearTranslationUi();
            this.updateOverlayActionState();
        },

        clearResults() {
            this.setMessage('', false);
            this.setShareMessage('', false);
            this.clearLoadedRowsOnly();

            const humanSearchEl = byId('forumDbHumanSearch');
            if (humanSearchEl) {
                humanSearchEl.value = '';
            }

            const shareTo = byId('forumDbOverlayShareTo');
            if (shareTo) {
                shareTo.value = '';
            }
            const shareSubject = byId('forumDbOverlayShareSubject');
            if (shareSubject) {
                shareSubject.value = 'Cleaned Forum Human Message';
            }

            const translateLang = byId('forumDbOverlayTranslateLang');
            if (translateLang) {
                translateLang.value = 'en';
            }
            this.clearTranslationUi();
        }
    };

    window.AdminPage.ForumDbExplorer = module;
})();