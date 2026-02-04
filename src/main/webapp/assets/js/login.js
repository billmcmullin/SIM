const form = document.getElementById("loginForm");
const result = document.getElementById("result");

const context = window.location.pathname.replace(/\/login$/, "/");
const loginUrl = `${window.location.origin}${context}api/auth/login`;

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
            window.location.href = `${context}dashboard`;
        } else {
            result.textContent = `Login failed (${response.status}): ${text}`;
        }
    } catch (err) {
        result.textContent = "Network error: " + err.message;
    }
});
