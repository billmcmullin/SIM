(() => {
const form = document.getElementById("loginForm");
const result = document.getElementById("result");

function resolveContextPath() {
    const script = document.currentScript || document.querySelector('script[src$="/assets/js/login.js"]');
    if (script && script.src) {
        const url = new URL(script.src, window.location.href);
        const fromScript = url.pathname.replace(/\/assets\/js\/login\.js$/, "");
        if (fromScript) {
            return fromScript;
        }
    }

    const p = window.location.pathname || "";
    if (p.endsWith("/login")) {
        return p.slice(0, -"/login".length);
    }

    if (p.endsWith("/dashboard")) {
        return p.slice(0, -"/dashboard".length);
    }

    return "";
}

const contextPath = resolveContextPath();
const loginUrl = `${window.location.origin}${contextPath}/api/auth/login`;

if (form && result) {
    form.addEventListener("submit", async (evt) => {
        evt.preventDefault();
        result.textContent = "Authenticating…";
        const payload = {
            username: form.username.value,
            password: form.password.value
        };
        try {
            const response = await fetch(loginUrl, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const text = await response.text();
            if (response.ok) {
                window.location.href = `${window.location.origin}${contextPath}/dashboard`;
            } else {
                result.textContent = `Login failed (${response.status}): ${text}`;
            }
        } catch (err) {
            result.textContent = "Network error: " + err.message;
        }
    });
}
})();
