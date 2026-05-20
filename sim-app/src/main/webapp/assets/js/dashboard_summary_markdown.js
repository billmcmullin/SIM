(() => {
    'use strict';

    function setStatus(msg) {
        const status = document.getElementById('copyStatus');
        if (status) status.textContent = msg || '';
    }

    async function copyToClipboard(text) {
        const value = text || '';
        try {
            await navigator.clipboard.writeText(value);
            return true;
        } catch {
            return false;
        }
    }

    function fallbackCopyFromHiddenInput(inputEl) {
        if (!inputEl) return false;

        // create a temporary textarea so legacy copy works even when source is hidden
        const ta = document.createElement('textarea');
        ta.value = inputEl.value || '';
        ta.setAttribute('readonly', 'readonly');
        ta.style.position = 'fixed';
        ta.style.left = '-9999px';
        ta.style.top = '0';
        document.body.appendChild(ta);

        try {
            ta.focus();
            ta.select();
            return document.execCommand('copy');
        } catch {
            return false;
        } finally {
            ta.remove();
        }
    }

    function wireCopyActions() {
        const source = document.getElementById('markdownSource'); // hidden input now
        const copyBtn = document.getElementById('copyMarkdownBtn');

        if (!copyBtn) return;

        copyBtn.addEventListener('click', async () => {
            if (!source) {
                setStatus('Summary text is unavailable.');
                setTimeout(() => setStatus(''), 2200);
                return;
            }

            const text = source.value || '';
            const ok = await copyToClipboard(text) || fallbackCopyFromHiddenInput(source);

            if (ok) {
                setStatus('Summary text copied to clipboard.');
            } else {
                setStatus('Unable to copy automatically. Press Ctrl/Cmd+C.');
            }

            setTimeout(() => setStatus(''), 2200);
        });
    }

    wireCopyActions();
})();
