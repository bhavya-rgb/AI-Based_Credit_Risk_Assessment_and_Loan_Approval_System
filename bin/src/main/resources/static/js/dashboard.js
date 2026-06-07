import { apiFetch, bindLogoutButton, requireAuth } from './api.js';

let appsChart;
let decisionsChart;

const STATUS_COLOR_MAP = {
  DRAFT: '#8b7b67',
  SUBMITTED: '#244f8f',
  MANUAL_REVIEW: '#9b6a05',
  DECIDED: '#b23a24',
  APPROVED: '#1f7a4f',
  DECLINED: '#9c2d2d',
  AUTO_APPROVE: '#1f7a4f',
  AUTO_DECLINE: '#9c2d2d'
};

(async function init() {
  if (!(await requireAuth())) return;
  bindLogoutButton();
  await load();
})();

async function load() {
  const summary = await apiFetch('/api/v1/admin/metrics/summary');
  renderCards(summary);
  renderCharts(summary);
}

function renderCards(summary) {
  const cards = [
    ['Applicants', summary.applicants],
    ['Applications', summary.loanApplications],
    ['Risk Assessments', summary.riskAssessments],
    ['Decisions', summary.loanDecisions],
    ['Models', summary.models],
    ['Active Model', summary.activeModelVersion || 'None']
  ];
  const root = document.getElementById('metricsCards');
  root.innerHTML = cards.map(([label, value]) => `
    <article class="metric-card">
      <div class="label">${label}</div>
      <div class="value">${value}</div>
    </article>`).join('');
}

function renderCharts(summary) {
  const appCtx = document.getElementById('applicationsChart');
  const decCtx = document.getElementById('decisionsChart');
  const appEntries = normalizeEntries(summary.applicationsByStatus || {});
  const decEntries = normalizeEntries(summary.decisionsByStatus || {});

  appsChart?.destroy();
  decisionsChart?.destroy();

  appsChart = renderStatusChart(appCtx, appEntries, 'Applications');
  decisionsChart = renderStatusChart(decCtx, decEntries, 'Decisions');
  renderStatusBreakdown('applicationsStatusList', 'applicationsChartMeta', appEntries, 'Applications');
  renderStatusBreakdown('decisionsStatusList', 'decisionsChartMeta', decEntries, 'Decisions');
}

function normalizeEntries(input) {
  return Object.entries(input)
    .filter(([, v]) => Number.isFinite(Number(v)))
    .map(([k, v]) => [k, Number(v)])
    .sort((a, b) => b[1] - a[1] || String(a[0]).localeCompare(String(b[0])));
}

function renderStatusChart(canvas, entries, datasetLabel) {
  const hasData = entries.length > 0;
  const labels = hasData ? entries.map(([k]) => humanizeStatus(k)) : ['No Data'];
  const values = hasData ? entries.map(([, v]) => v) : [0];
  const max = values.reduce((m, v) => Math.max(m, v), 0);
  const headroom = max <= 1 ? 2 : Math.max(1, Math.ceil(max * 0.2));

  return new Chart(canvas, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: datasetLabel,
        data: values,
        borderRadius: 8,
        borderSkipped: false,
        backgroundColor: hasData
          ? entries.map(([k]) => getStatusColor(k))
          : ['rgba(139, 123, 103, 0.25)'],
        maxBarThickness: 32
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: 'y',
      animation: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title(items) {
              return items[0]?.label || '';
            },
            label(ctx) {
              return `${datasetLabel}: ${ctx.parsed.x ?? 0}`;
            }
          }
        }
      },
      layout: { padding: { right: 8, left: 4 } },
      scales: {
        x: {
          beginAtZero: true,
          suggestedMax: max + headroom,
          ticks: {
            precision: 0,
            color: '#6b6156'
          },
          grid: {
            color: 'rgba(107, 97, 86, 0.12)'
          }
        },
        y: {
          ticks: {
            color: '#4d4338',
            font: { weight: 600 }
          },
          grid: { display: false }
        }
      }
    },
    plugins: [{
      id: `value-labels-${datasetLabel.toLowerCase()}`,
      afterDatasetsDraw(chart) {
        if (!hasData) return;
        const { ctx } = chart;
        const meta = chart.getDatasetMeta(0);
        ctx.save();
        ctx.font = '600 12px "IBM Plex Sans", sans-serif';
        ctx.fillStyle = '#2d261f';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'middle';
        meta.data.forEach((bar, idx) => {
          const value = values[idx];
          ctx.fillText(String(value), bar.x + 8, bar.y);
        });
        ctx.restore();
      }
    }]
  });
}

function renderStatusBreakdown(containerId, metaId, entries, label) {
  const root = document.getElementById(containerId);
  const meta = document.getElementById(metaId);
  if (!root || !meta) return;

  const total = entries.reduce((sum, [, v]) => sum + v, 0);
  meta.textContent = `${total} total • ${entries.length} status${entries.length === 1 ? '' : 'es'}`;

  if (!entries.length) {
    root.innerHTML = `<div class="status-empty">No ${label.toLowerCase()} data yet.</div>`;
    return;
  }

  root.innerHTML = entries.map(([status, count]) => {
    const pct = total > 0 ? ((count / total) * 100).toFixed(1) : '0.0';
    return `
      <div class="status-item">
        <span class="status-dot" style="--dot:${getStatusColor(status)}"></span>
        <span class="status-name">${humanizeStatus(status)}</span>
        <span class="status-percent">${pct}%</span>
        <span class="status-count">${count}</span>
      </div>
    `;
  }).join('');
}

function humanizeStatus(value) {
  return String(value || '')
    .toLowerCase()
    .split('_')
    .map(part => part ? part[0].toUpperCase() + part.slice(1) : '')
    .join(' ');
}

function getStatusColor(status) {
  return STATUS_COLOR_MAP[status] || '#6b6156';
}
