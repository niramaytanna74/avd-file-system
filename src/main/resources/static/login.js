// Handles login and redirects to dashboard.html on success
const form = document.getElementById('loginForm');
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = form.username.value;
    const password = form.password.value;
    const res = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
    });
    if (res.ok) {
        const data = await res.json();
        localStorage.setItem('jwt', data.token);
        window.location.href = 'dashboard.html';
    } else {
        document.getElementById('loginError').innerText = 'Invalid credentials';
    }
});
