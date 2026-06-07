import { apiFetch, bindLogoutButton, requireAuth } from './api.js';
import { setText } from './utils.js';

let outcomesChart;
const PROJECT_DATASET_PATH = './data/lending_club_synthetic.csv';

(async function init() {
  if (!(await requireAuth())) return;
  bindLogoutButton();
  bindForms();
  bindButtons();
  await refreshAll();
})();

function bindForms() {
  document.getElementById('datasetForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = { sourcePath: String(fd.get('sourcePath') || ''), sourceName: String(fd.get('sourceName') || '') };
    try {
      const res = await apiFetch('/api/v1/admin/datasets/import', { method: 'POST', body: JSON.stringify(payload) });
      setText('datasetMsg', `Imported ${res.datasetVersion} (${res.rowsLoaded}/${res.rowsRead} rows loaded)`);
      const dsInput = document.querySelector('#trainingForm input[name="datasetVersion"]');
      if (dsInput) dsInput.value = res.datasetVersion;
      await refreshAll();
    } catch (err) {
      setText('datasetMsg', err.message, true);
    }
  });

  document.getElementById('trainingForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = {
      datasetVersion: String(fd.get('datasetVersion') || ''),
      mode: String(fd.get('mode') || 'BASELINE'),
      featurePolicy: String(fd.get('featurePolicy') || 'application_time_only'),
      xgboostSearchSpace: {
        learning_rate: [0.01, 0.30],
        max_depth: [3, 10],
        n_estimators: [100, 600],
        gamma: [0.0, 5.0]
      },
      evaluationStrategy: { cv: 'RepeatedStratified5Foldx3', holdout: 'leakage-safe' }
    };
    try {
      const res = await apiFetch('/api/v1/admin/training-runs', { method: 'POST', body: JSON.stringify(payload) });
      setText('trainingMsg', `Run #${res.runId} completed (${res.runType}) - AUC ${res.metrics?.auc ?? '-'}`);
      await refreshAll();
    } catch (err) {
      setText('trainingMsg', err.message, true);
    }
  });
}

function bindButtons() {
  document.getElementById('refreshModelsBtn')?.addEventListener('click', refreshAll);
  document.getElementById('useProjectDatasetPathBtn')?.addEventListener('click', () => {
    const input = document.querySelector('#datasetForm input[name="sourcePath"]');
    if (input) input.value = PROJECT_DATASET_PATH;
  });
}

async function refreshAll() {
  await Promise.all([refreshModels(), refreshOutcomes(), refreshAudit(), refreshLatestDataset()]);
}

async function refreshModels() {
  const models = await apiFetch('/api/v1/admin/models');
  const tbody = document.getElementById('modelsTbody');
  tbody.innerHTML = models.map(m => `
    <tr>
      <td>${m.id}</td>
      <td><code>${m.modelVersion}</code></td>
      <td>${m.status}</td>
      <td>${m.modelType}</td>
      <td>${m.trainedOnDatasetVersion || '-'}</td>
      <td>
        <div class="inline-controls">
          <button class="btn btn-secondary" data-action="promote" data-id="${m.id}">Promote</button>
          <button class="btn" data-action="retire" data-id="${m.id}">Retire</button>
        </div>
      </td>
    </tr>
  `).join('') || '<tr><td colspan="6" class="muted">No models</td></tr>';

  tbody.querySelectorAll('button[data-action]').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.id;
      const action = btn.dataset.action;
      try {
        await apiFetch(`/api/v1/admin/models/${id}/${action}`, { method: 'POST' });
        await refreshModels();
      } catch (e) {
        alert(e.message);
      }
    });
  });
}

async function refreshOutcomes() {
  const report = await apiFetch('/api/v1/admin/reports/decision-outcomes');
  const entries = Object.entries(report.decisionsByStatus || {});
  const ctx = document.getElementById('outcomesChart');
  outcomesChart?.destroy();
  outcomesChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: entries.map(([k]) => k),
      datasets: [{ label: 'Decisions', data: entries.map(([, v]) => v), backgroundColor: '#244f8f' }]
    },
    options: { plugins: { legend: { display: false } } }
  });
}

async function refreshAudit() {
  const logs = await apiFetch('/api/v1/admin/audit-logs?page=0&size=20');
  const tbody = document.getElementById('auditTbody');
  tbody.innerHTML = (logs.items || []).map(a => `
    <tr>
      <td>${a.id}</td>
      <td>${a.actionType}</td>
      <td>${a.entityType}</td>
      <td>${a.entityId}</td>
      <td>${a.ipAddress || '-'}</td>
      <td>${a.createdAt}</td>
    </tr>
  `).join('') || '<tr><td colspan="6" class="muted">No audit logs</td></tr>';
}

async function refreshLatestDataset() {
  try {
    const latest = await apiFetch('/api/v1/admin/datasets/latest');
    if (!latest) {
      setText('latestDatasetInfo', 'No dataset import recorded yet.');
      return;
    }
    if (latest.datasetVersion && latest.status === 'COMPLETED') {
      const dsInput = document.querySelector('#trainingForm input[name="datasetVersion"]');
      if (dsInput && !dsInput.value) dsInput.value = latest.datasetVersion;
    }
    const triggerMode = latest.triggerMode || 'UNKNOWN';
    const sourcePath = latest.sourcePath || '-';
    const rows = `${latest.rowsLoaded ?? 0}/${latest.rowsRead ?? 0}`;
    setText('latestDatasetInfo', `Latest dataset: ${latest.datasetVersion || '-'} [${latest.status || '-'}] (${rows} rows) via ${triggerMode} from ${sourcePath}`);
  } catch (err) {
    setText('latestDatasetInfo', err.message, true);
  }
}
