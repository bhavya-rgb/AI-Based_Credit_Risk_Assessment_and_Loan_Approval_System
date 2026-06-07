const ACCESS_KEY = 'crs_access_token';
const REFRESH_KEY = 'crs_refresh_token';

export function setTokens({ accessToken, refreshToken }) {
  if (accessToken) sessionStorage.setItem(ACCESS_KEY, accessToken);
  if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken);
}

export function clearTokens() {
  sessionStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
}

export function getAccessToken() {
  return sessionStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_KEY);
}

export async function apiFetch(path, options = {}, retry = true) {
  const headers = new Headers(options.headers || {});
  if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  const token = getAccessToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(path, { ...options, headers });
  if (response.status === 401 && retry && getRefreshToken()) {
    const ok = await refreshTokens();
    if (ok) return apiFetch(path, options, false);
  }
  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const data = await response.json();
      message = data.message || message;
      if (data.details?.length) message += `: ${data.details.join('; ')}`;
    } catch (_) {}
    throw new Error(message);
  }
  if (response.status === 204) return null;
  const contentType = response.headers.get('content-type') || '';
  return contentType.includes('application/json') ? response.json() : response.text();
}

export async function refreshTokens() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;
  try {
    const res = await fetch('/api/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    if (!res.ok) {
      clearTokens();
      return false;
    }
    const data = await res.json();
    setTokens(data);
    return true;
  } catch {
    clearTokens();
    return false;
  }
}

export async function logout() {
  const refreshToken = getRefreshToken();
  try {
    if (refreshToken) {
      await fetch('/api/v1/auth/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });
    }
  } finally {
    clearTokens();
    window.location.href = '/login.html';
  }
}

export async function requireAuth() {
  if (!getAccessToken() && !(await refreshTokens())) {
    window.location.href = '/login.html';
    return false;
  }
  return true;
}

export function bindLogoutButton() {
  document.getElementById('logoutBtn')?.addEventListener('click', logout);
}
