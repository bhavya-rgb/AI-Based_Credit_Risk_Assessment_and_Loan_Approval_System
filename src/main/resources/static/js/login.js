import { setTokens } from './api.js';

const form = document.getElementById('loginForm');
const errorEl = document.getElementById('loginError');

form?.addEventListener('submit', async (e) => {
  e.preventDefault();
  errorEl.textContent = '';
  const data = new FormData(form);
  const payload = Object.fromEntries(data.entries());
  try {
    const res = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.message || 'Login failed');
    }
    const json = await res.json();
    setTokens(json);
    window.location.href = '/dashboard.html';
  } catch (err) {
    errorEl.textContent = err.message || String(err);
  }
});
