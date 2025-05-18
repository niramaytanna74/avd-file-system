// Handles registration and redirects to login.html on success
const form = document.getElementById('registerForm');
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const user = {
        username: form.username.value,
        email: form.email.value,
        role: form.role.value
    };
    const password = form.password.value;
    const res = await fetch(`/auth/register?password=${encodeURIComponent(password)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    });
    if (res.ok) {
        window.location.href = 'login.html';
    } else {
        document.getElementById('registerError').innerText = 'Registration failed';
    }
});
