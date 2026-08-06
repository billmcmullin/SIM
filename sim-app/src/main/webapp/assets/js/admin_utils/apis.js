// apis.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    function withDefaultHeaders(options = {}) {
        const opts = { ...options };
        opts.credentials = opts.credentials || 'same-origin';
        opts.headers = opts.headers || { 'Accept': 'application/json' };
        return opts;
    }

    async function parseJsonSafe(resp) {
        return resp.json().catch(() => null);
    }

    const Api = {
        async fetchJson(url, options = {}) {
            const resp = await fetch(url, withDefaultHeaders(options));
            const payload = await parseJsonSafe(resp);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        async requestJson(url, method, obj, extraHeaders = {}) {
            const headers = Object.assign({
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }, extraHeaders);
            const resp = await fetch(url, withDefaultHeaders({
                method,
                headers,
                body: JSON.stringify(obj)
            }));
            const payload = await parseJsonSafe(resp);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        postUrlEncoded(url, params, extraHeaders = {}) {
            const headers = Object.assign({ 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' }, extraHeaders);
            return this.fetchJson(url, { method: 'POST', headers, body: params.toString() });
        },

        postJson(url, obj, extraHeaders = {}) {
            return this.requestJson(url, 'POST', obj, extraHeaders);
        },

        putJson(url, obj, extraHeaders = {}) {
            return this.requestJson(url, 'PUT', obj, extraHeaders);
        },

        async delete(url) {
            const resp = await fetch(url, withDefaultHeaders({ method: 'DELETE' }));
            const payload = await parseJsonSafe(resp);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        async postFormData(url, formData) {
            const resp = await fetch(url, withDefaultHeaders({ method: 'POST', body: formData, redirect: 'follow' }));
            return resp;
        },

        async postFormDataJson(url, formData) {
            const resp = await this.postFormData(url, formData);
            const payload = await parseJsonSafe(resp);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        }
    };

    window.AdminPage.Api = Api;
})();
