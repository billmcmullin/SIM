const profileConfig = window.profileConfig || {};
const contextPath = profileConfig.contextPath || '';
const role = (profileConfig.role || '').toUpperCase();

(function () {
    if (role !== 'ADMIN') {
        document.querySelectorAll('[data-admin-link]').forEach(el => el.style.display = 'none');
    }
})();

const profileForm = document.getElementById('profileForm');
const resultEl = document.getElementById('profileResult');

profileForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const data = new URLSearchParams(new FormData(profileForm));
    resultEl.textContent = 'Saving…';
    resultEl.style.color = '#0f172a';
    fetch(`${contextPath}/profile`, {
        method: 'POST',
        body: data.toString(),
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
        .then(async (response) => {
            if (response.status === 405 && !contextPath) {
                throw new Error('Backend endpoint not configured; verify contextPath variable.');
            }
            const payload = await response.json();
            if (payload.status === 'ok') {
                resultEl.textContent = 'Profile updated successfully.';
                resultEl.style.color = '#047857';
            } else {
                throw new Error(payload.message || 'Update failed.');
            }
        })
        .catch((error) => {
            resultEl.textContent = `Error: ${error.message}`;
            resultEl.style.color = '#b91c1c';
        });
});
