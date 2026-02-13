// apis.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    const Api = {
        async fetchJson(url, options = {}) {
            options.credentials = options.credentials || 'same-origin';
            options.headers = options.headers || { 'Accept': 'application/json' };
            const resp = await fetch(url, options);
            const payload = await resp.json().catch(() => null);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        postUrlEncoded(url, params, extraHeaders = {}) {
            const headers = Object.assign({ 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' }, extraHeaders);
            return this.fetchJson(url, { method: 'POST', headers, body: params.toString() });
        },

        async postJson(url, obj) {
            const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
            const resp = await fetch(url, { method: 'POST', headers, credentials: 'same-origin', body: JSON.stringify(obj) });
            const payload = await resp.json().catch(() => null);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        async putJson(url, obj) {
            const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
            const resp = await fetch(url, { method: 'PUT', headers, credentials: 'same-origin', body: JSON.stringify(obj) });
            const payload = await resp.json().catch(() => null);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        async delete(url) {
            const resp = await fetch(url, { method: 'DELETE', credentials: 'same-origin', headers: { 'Accept': 'application/json' } });
            const payload = await resp.json().catch(() => null);
            return { status: resp.status, ok: resp.ok, payload, response: resp };
        },

        async postFormData(url, formData) {
            const resp = await fetch(url, { method: 'POST', credentials: 'same-origin', body: formData, redirect: 'follow' });
            return resp;
        }
    };

    window.AdminPage.Api = Api;
})();
