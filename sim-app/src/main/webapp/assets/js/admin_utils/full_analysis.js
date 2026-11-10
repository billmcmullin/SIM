(() => {
    'use strict';
    window.AdminPage = window.AdminPage || {};
    const element = id => document.getElementById(id);
    const analysis = {
        contextPath: '',
        jobId: '',
        job: null,
        offset: 0,
        busy: false,
        timer: null,

        init(contextPath) {
            const form = element('fullAnalysisForm');
            if (!form || form.dataset.wired === '1') {
                return;
            }
            form.dataset.wired = '1';
            this.contextPath = contextPath || '';
            element('faStart').addEventListener('click', () => this.start());
            element('faRefresh').addEventListener('click', () => this.loadJobs().catch(error => this.error(error)));
            element('faJobs').addEventListener('change', () => {
                this.jobId = element('faJobs').value;
                this.offset = 0;
                this.refresh().catch(error => this.error(error));
            });
            ['pause', 'resume', 'cancel'].forEach(action => {
                element(`fa${action.charAt(0).toUpperCase()}${action.slice(1)}`).addEventListener('click', () => this.control(action));
            });
            element('faConfirmedIdle').addEventListener('change', () => this.buttons());
            element('faConfirmIdle').addEventListener('click', () => this.control('confirmIdle'));
            element('faPrevious').addEventListener('click', () => {
                this.offset = Math.max(0, this.offset - 5);
                this.details().catch(error => this.error(error));
            });
            element('faNext').addEventListener('click', () => {
                this.offset += 5;
                this.details().catch(error => this.error(error));
            });
            this.loadJobs().catch(error => this.error(error));
        },

        endpoint(query = '') {
            return `${this.contextPath}/admin/full-analysis${query ? `?${query}` : ''}`;
        },

        async get(query) {
            const result = await window.AdminPage.Api.fetchJson(this.endpoint(query));
            if (!result.ok || !result.payload || result.payload.status === 'error') {
                throw new Error(result.payload?.message || `Analysis read failed (HTTP ${result.status}).`);
            }
            return result.payload;
        },

        async post(payload) {
            const result = await window.AdminPage.Api.postJson(this.endpoint(), payload);
            if (!result.ok || !result.payload || result.payload.status === 'error') {
                throw new Error(result.payload?.message || `Analysis action failed (HTTP ${result.status}). No automatic resubmission was made.`);
            }
            return result.payload;
        },

        async loadJobs() {
            const payload = await this.get('action=list');
            const select = element('faJobs');
            select.replaceChildren();
            (payload.jobs || []).sort((first, second) => second.createdAt - first.createdAt).forEach(job => {
                const option = document.createElement('option');
                option.value = job.id;
                option.textContent = `${job.day} | ${job.state} | ${job.id.slice(0, 8)}`;
                select.appendChild(option);
            });
            if (!element('faDay').value && payload.today) {
                element('faDay').value = payload.today;
                element('faDay').max = payload.today;
            }
            const ids = (payload.jobs || []).map(job => job.id);
            this.jobId = ids.includes(this.jobId) ? this.jobId : (select.options[0]?.value || '');
            select.value = this.jobId;
            if (!this.jobId) {
                this.job = null;
                element('faStatus').textContent = 'No Full Analysis jobs.';
                this.buttons();
                return;
            }
            await this.refresh();
        },

        async start() {
            if (this.busy || !element('fullAnalysisForm').reportValidity()) {
                return;
            }
            this.busy = true;
            this.buttons();
            try {
                const payload = await this.post({action:'start', day:element('faDay').value,
                    requestBytes:Number(element('faRequestBytes').value), inputByteBudget:Number(element('faInputBytes').value),
                    paceSeconds:Number(element('faPace').value), requestsPerRun:Number(element('faRequests').value),
                    minutesPerRun:Number(element('faMinutes').value)});
                this.jobId = payload.jobId;
                this.offset = 0;
                await this.loadJobs();
            } catch (error) {
                this.error(new Error(`Start outcome may be unknown. Refresh Jobs before starting another analysis. ${error.message}`));
            } finally {
                this.busy = false;
                this.buttons();
            }
        },

        async control(action) {
            if (this.busy) {
                return;
            }
            const confirmed = element('faConfirmedIdle').checked;
            if ((action === 'confirmIdle' || (action === 'resume' && this.job?.uncertain)) && !confirmed) {
                this.error(new Error('Review upstream logs and explicitly confirm the server is idle before authorizing this action.'));
                return;
            }
            this.busy = true;
            this.buttons();
            try {
                const payload = await this.post({action, jobId:this.jobId, confirmedIdle:confirmed});
                element('faConfirmedIdle').checked = false;
                if (payload.message) {
                    element('faStatus').textContent = payload.message;
                }
                if (this.jobId) {
                    await this.refresh();
                }
            } catch (error) {
                this.error(error);
            } finally {
                this.busy = false;
                this.buttons();
            }
        },

        async refresh() {
            window.clearTimeout(this.timer);
            if (!this.jobId) {
                return;
            }
            const id = this.jobId;
            const payload = await this.get(`action=status&jobId=${encodeURIComponent(id)}`);
            if (id !== this.jobId) {
                return;
            }
            this.job = payload.job;
            const job = this.job;
            element('faStatus').textContent = `${job.state}: ${job.message}`;
            element('faCoverage').textContent = `Complete chats: ${job.completedChats}/${job.totalChats} | Pending chats: ${Math.max(0, job.totalChats - job.completedChats)} | Complete source segments: ${job.mapCompleted}/${job.mapSteps} | Uncertain requests: ${job.uncertain ? 1 : 0} | Requests attempted: ${job.requests}${job.retainedUntil ? ` | Retained until: ${new Date(job.retainedUntil).toLocaleDateString()}` : ''}`;
            element('faExport').hidden = false;
            element('faExport').href = this.endpoint(`action=export&jobId=${encodeURIComponent(id)}`);
            const overview = element('faOverview');
            overview.replaceChildren();
            if (job.overview) {
                this.text(overview, 'h3', 'Analysis Overview');
                this.text(overview, 'p', job.overview.overview || '');
                this.text(overview, 'p', 'Completion tracks source segments processed; factual accuracy and knowledge-base completeness require independent review.');
                [['themes', 'Topics Asked About'], ['candidateGaps', 'Candidate Knowledge Gaps'], ['improvements', 'Recommended Improvements']].forEach(([key, title]) => {
                    this.text(overview, 'h4', title);
                    const list = document.createElement('ul');
                    (job.overview[key] || []).forEach(item => this.text(list, 'li', typeof item === 'string' ? item : JSON.stringify(item)));
                    overview.appendChild(list);
                });
            }
            this.buttons();
            await this.details();
            if (['PREPARING', 'RUNNING', 'SENDING'].includes(job.state)) {
                this.timer = window.setTimeout(() => this.refresh().catch(error => this.error(error)), 5000);
            }
        },

        async details() {
            if (!this.jobId) {
                return;
            }
            const id = this.jobId;
            const payload = await this.get(`action=details&jobId=${encodeURIComponent(id)}&offset=${this.offset}`);
            if (id !== this.jobId) {
                return;
            }
            const container = element('faDetails');
            container.replaceChildren();
            (payload.items || []).forEach(item => {
                const details = document.createElement('details');
                this.text(details, 'summary', `${item.kind === 'map' ? item.chatId : 'Synthesis'} | Segment ${item.part}/${item.parts} | ${item.state}`);
                this.text(details, 'h4', 'Complete Source Segment');
                this.text(details, 'pre', item.text || '');
                if (item.result) {
                    this.text(details, 'h4', 'Saved Findings');
                    if (Array.isArray(item.result.questions)) {
                        item.result.questions.forEach(question => {
                            this.text(details, 'h4', question.asked || 'Question');
                            [['Response provided', question.answerSummary], ['Coverage', question.answerCoverage],
                                ['Gap type', question.gapKind], ['Gap / limitation', question.gapDescription],
                                ['Proposed improvement', question.improvement], ['Evidence confidence', question.confidence]]
                                .forEach(([label, value]) => this.text(details, 'p', `${label}: ${value || 'Not provided'}`));
                            this.text(details, 'blockquote', question.sourceQuote || 'No source quote');
                        });
                    } else {
                        this.text(details, 'pre', JSON.stringify(item.result, null, 2));
                    }
                }
                if (item.receivedResponse) {
                    this.text(details, 'h4', 'Received Response - Not Validated');
                    this.text(details, 'pre', item.receivedResponse);
                }
                container.appendChild(details);
            });
            element('faPage').textContent = payload.total ? `${this.offset + 1}-${Math.min(payload.total, this.offset + payload.items.length)} of ${payload.total} saved work items` : 'No work items yet';
            element('faPrevious').disabled = this.offset === 0;
            element('faNext').disabled = this.offset + payload.items.length >= payload.total;
        },

        text(parent, tag, value) {
            const node = document.createElement(tag);
            node.textContent = value;
            parent.appendChild(node);
        },

        buttons() {
            const state = this.job?.state || '';
            const active = ['PREPARING', 'RUNNING', 'SENDING'].includes(state);
            element('faStart').disabled = this.busy || active || Boolean(this.job?.uncertain && !this.job?.upstreamIdleConfirmed);
            element('faRefresh').disabled = this.busy;
            element('faPause').disabled = this.busy || !active || Boolean(this.job?.pauseRequested);
            element('faResume').disabled = this.busy || state !== 'PAUSED' || this.job?.resultBytes > 16 * 1024 * 1024 || (this.job?.uncertain && !element('faConfirmedIdle').checked);
            element('faResume').textContent = this.job?.uncertain ? 'Retry Uncertain Request'
                : (this.job?.validationFailed ? 'Retry Invalid Result' : 'Resume Pending Work');
            element('faCancel').disabled = this.busy || !this.job || ['COMPLETED', 'CANCELLED', 'FAILED'].includes(state) || Boolean(this.job?.cancelRequested);
            element('faConfirmIdle').disabled = this.busy || !element('faConfirmedIdle').checked;
        },

        error(error) {
            element('faStatus').textContent = error.message || 'Full Analysis action failed. No automatic resubmission was made.';
        }
    };
    window.AdminPage.FullAnalysis = analysis;
})();