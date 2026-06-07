export function qs(name) {
  return new URLSearchParams(window.location.search).get(name);
}

export function toJson(value) {
  return JSON.stringify(value, null, 2);
}

export function formToObject(form) {
  const data = new FormData(form);
  const obj = {};
  for (const [k, v] of data.entries()) {
    obj[k] = typeof v === 'string' ? v.trim() : v;
  }
  return obj;
}

export function numberOrNull(v) {
  if (v === '' || v == null) return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

export function setText(id, text, isError = false) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = text || '';
  el.classList.toggle('error', !!isError);
}

export function badgeClassForRiskBand(band) {
  if (!band) return 'tag';
  return `tag ${String(band).toLowerCase()}`;
}
