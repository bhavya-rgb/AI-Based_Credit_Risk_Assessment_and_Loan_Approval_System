import { apiFetch, bindLogoutButton, requireAuth } from './api.js';
import { badgeClassForRiskBand, qs, setText, toJson } from './utils.js';

const appId = qs('id');

(async function init() {
  if (!(await requireAuth())) return;
  bindLogoutButton();
  if (!appId) {
    alert('Missing ?id query parameter');
    window.location.href = '/applications.html';
    return;
  }
  bindActions();
  await refreshAll();
})();

function bindActions() {
  document.getElementById('submitBtn')?.addEventListener('click', async () => {
    try {
      await apiFetch(`/api/v1/loan-applications/${appId}/submit`, { method: 'PATCH' });
      await refreshAll();
    } catch (e) {
      alert(e.message);
    }
  });

  document.getElementById('scoreBtn')?.addEventListener('click', async () => {
    try {
      await apiFetch(`/api/v1/loan-applications/${appId}/score`, { method: 'POST' });
      await refreshAll();
    } catch (e) {
      alert(e.message);
    }
  });

  document.getElementById('decisionForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = {
      decisionStatus: fd.get('decisionStatus'),
      approvedAmount: num(fd.get('approvedAmount')),
      approvedTermMonths: intOrNull(fd.get('approvedTermMonths')),
      interestRateOffer: num(fd.get('interestRateOffer')),
      overrideReason: emptyToNull(fd.get('overrideReason'))
    };
    try {
      const res = await apiFetch(`/api/v1/loan-applications/${appId}/decision`, { method: 'POST', body: JSON.stringify(payload) });
      setText('decisionMsg', `Decision saved #${res.id}`);
      await refreshAll();
    } catch (err) {
      setText('decisionMsg', err.message, true);
    }
  });
}

async function refreshAll() {
  const [app, assessments] = await Promise.all([
    apiFetch(`/api/v1/loan-applications/${appId}`),
    apiFetch(`/api/v1/loan-applications/${appId}/risk-assessments`).catch(() => [])
  ]);
  document.getElementById('pageTitle').textContent = `Loan Application #${app.id}`;
  document.getElementById('applicationJson').textContent = toJson(app);
  renderAssessments(assessments);
}

function renderAssessments(assessments) {
  const latest = assessments[0];
  const latestRoot = document.getElementById('latestAssessment');
  const histRoot = document.getElementById('assessmentHistory');
  if (!latest) {
    latestRoot.innerHTML = '<p class="muted">No assessment yet. Submit and run AI score.</p>';
    histRoot.innerHTML = '<p class="muted">No history.</p>';
    return;
  }
  latestRoot.innerHTML = `
    <div class="card-grid">
      <div class="panel subtle"><div class="hint">Model</div><strong>${latest.modelVersion}</strong></div>
      <div class="panel subtle"><div class="hint">Default Probability</div><strong>${latest.defaultProbability}</strong></div>
      <div class="panel subtle"><div class="hint">Risk Score</div><strong>${latest.riskScore}</strong></div>
      <div class="panel subtle"><div class="hint">Band</div><span class="${badgeClassForRiskBand(latest.riskBand)}">${latest.riskBand}</span></div>
      <div class="panel subtle"><div class="hint">Recommendation</div><strong>${latest.recommendation}</strong></div>
      <div class="panel subtle"><div class="hint">Scored At</div><strong>${latest.scoredAt}</strong></div>
    </div>
    <div><div class="hint">Top Reasons</div><code>${(latest.topReasons || []).join(', ')}</code></div>
  `;
  histRoot.innerHTML = assessments.map(a => `
    <article class="panel subtle">
      <div class="row-between"><strong>#${a.assessmentId}</strong><span>${a.scoredAt}</span></div>
      <div class="hint">PD ${a.defaultProbability} | Score ${a.riskScore} | ${a.recommendation}</div>
      <div><span class="${badgeClassForRiskBand(a.riskBand)}">${a.riskBand}</span></div>
      <div class="hint">Reasons: ${(a.topReasons || []).join(', ') || '-'}</div>
    </article>
  `).join('');
}

function num(v) { return v === '' ? null : Number(v); }
function intOrNull(v) { return v === '' ? null : parseInt(v, 10); }
function emptyToNull(v) { v = String(v || '').trim(); return v ? v : null; }
