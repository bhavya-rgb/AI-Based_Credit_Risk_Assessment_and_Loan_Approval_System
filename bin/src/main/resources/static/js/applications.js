import { apiFetch, bindLogoutButton, requireAuth } from './api.js';
import { formToObject, numberOrNull, setText } from './utils.js';

(async function init() {
  if (!(await requireAuth())) return;
  bindLogoutButton();
  bindForms();
  bindFilters();
  await loadApplications();
})();

function bindForms() {
  document.getElementById('applicantForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    const payload = formToObject(form);
    try {
      const res = await apiFetch('/api/v1/applicants', { method: 'POST', body: JSON.stringify(payload) });
      setText('applicantFormMsg', `Created applicant #${res.id}`);
      form.reset();
    } catch (err) {
      setText('applicantFormMsg', err.message, true);
    }
  });

  document.getElementById('loanForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    const raw = formToObject(form);
    const payload = {
      applicantId: numberOrNull(raw.applicantId),
      loanAmount: numberOrNull(raw.loanAmount),
      termMonths: numberOrNull(raw.termMonths),
      purpose: raw.purpose,
      annualIncome: numberOrNull(raw.annualIncome),
      employmentLengthYears: numberOrNull(raw.employmentLengthYears),
      homeOwnership: raw.homeOwnership,
      verificationStatus: raw.verificationStatus,
      dti: numberOrNull(raw.dti),
      existingDebt: numberOrNull(raw.existingDebt),
      creditProfile: {
        ficoLow: numberOrNull(raw.ficoLow),
        ficoHigh: numberOrNull(raw.ficoHigh),
        inqLast6Months: numberOrNull(raw.inqLast6Months),
        delinq2Yrs: numberOrNull(raw.delinq2Yrs),
        openAccounts: numberOrNull(raw.openAccounts),
        publicRecords: numberOrNull(raw.publicRecords),
        revolvingBalance: numberOrNull(raw.revolvingBalance),
        revolvingUtilization: numberOrNull(raw.revolvingUtilization),
        totalAccounts: numberOrNull(raw.totalAccounts),
        mortgageAccounts: numberOrNull(raw.mortgageAccounts),
        bankruptcies: numberOrNull(raw.bankruptcies)
      }
    };
    try {
      const res = await apiFetch('/api/v1/loan-applications', { method: 'POST', body: JSON.stringify(payload) });
      setText('loanFormMsg', `Created application #${res.id}`);
      await loadApplications();
    } catch (err) {
      setText('loanFormMsg', err.message, true);
    }
  });
}

function bindFilters() {
  document.getElementById('refreshListBtn')?.addEventListener('click', loadApplications);
  document.getElementById('statusFilter')?.addEventListener('change', loadApplications);
}

async function loadApplications() {
  const status = document.getElementById('statusFilter')?.value || '';
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  const res = await apiFetch(`/api/v1/loan-applications${query}`);
  const tbody = document.getElementById('applicationsTbody');
  tbody.innerHTML = (res.items || []).map(row => `
    <tr>
      <td>${row.id}</td>
      <td>${row.applicantId}</td>
      <td>${row.loanAmount}</td>
      <td>${row.termMonths}</td>
      <td>${row.dti}</td>
      <td>${row.applicationStatus}</td>
      <td>${row.submittedAt || '-'}</td>
      <td><a class="btn btn-ghost" href="/application-detail.html?id=${row.id}">Open</a></td>
    </tr>
  `).join('') || '<tr><td colspan="8" class="muted">No applications found</td></tr>';
}
