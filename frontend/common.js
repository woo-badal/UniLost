/* Bennett University Lost & Found - Shared Styles and Nav helpers */

/* Call this on every student page to check auth and render navbar */
function initStudentPage(activePage) {
  fetch('/api/session').then(r => r.json()).then(data => {
    if (!data.role || data.role !== 'student') {
      window.location.href = 'login.html'; return;
    }
    renderStudentNav(data.name, activePage);
    loadUnreadCount();
    if (typeof onPageLoad === 'function') onPageLoad(data);
  }).catch(() => window.location.href = 'login.html');
}

/* Call this on every staff page to check auth and render navbar */
function initStaffPage(activePage) {
  fetch('/api/session').then(r => r.json()).then(data => {
    if (!data.role || data.role !== 'staff') {
      window.location.href = 'staff-login.html'; return;
    }
    renderStaffNav(data.name, activePage);
    if (typeof onPageLoad === 'function') onPageLoad(data);
  }).catch(() => window.location.href = 'staff-login.html');
}

/* Render student navigation */
function renderStudentNav(name, activePage) {
  const nav = document.getElementById('mainNav');
  if (!nav) return;
  nav.innerHTML = `
    <div class="nav-brand">
      <a href="index.html" class="brand-text">
        <span class="brand-main">Bennett University</span>
        <span class="brand-sub">Lost &amp; Found</span>
      </a>
    </div>
    <div class="nav-links">
      <a href="index.html" class="${activePage==='home'?'active':''}">Browse Items</a>
      <a href="report-lost.html" class="${activePage==='report'?'active':''}">Report Lost</a>
      <a href="my-claims.html" class="${activePage==='claims'?'active':''}">My Claims</a>
    </div>
    <div class="nav-right">
      <a href="notifications.html" class="notif-btn" id="notifBtn">
        <span>🔔</span>
        <span class="notif-badge" id="notifBadge" style="display:none">0</span>
      </a>
      <span class="nav-user">👋 ${name}</span>
      <button class="btn-logout" onclick="doLogout()">Logout</button>
    </div>
  `;
}

/* Render staff navigation */
function renderStaffNav(name, activePage) {
  const nav = document.getElementById('mainNav');
  if (!nav) return;
  nav.innerHTML = `
    <div class="nav-brand">
      <a href="staff-dashboard.html" class="brand-text">
        <span class="brand-main">Bennett University</span>
        <span class="brand-sub">Staff Portal</span>
      </a>
    </div>
    <div class="nav-links">
      <a href="staff-dashboard.html" class="${activePage==='dashboard'?'active':''}">Dashboard</a>
      <a href="staff-upload.html" class="${activePage==='upload'?'active':''}">Upload Item</a>
      <a href="staff-claims.html" class="${activePage==='claims'?'active':''}">Claims</a>
      <a href="staff-lost-reports.html" class="${activePage==='lostReports'?'active':''}">Lost Reports</a>
      <a href="staff-items.html" class="${activePage==='items'?'active':''}">Manage Items</a>
    </div>
    <div class="nav-right">
      <span class="nav-user">🏢 ${name}</span>
      <button class="btn-logout" onclick="doLogout()">Logout</button>
    </div>
  `;
}

/* Load and display unread notification count */
function loadUnreadCount() {
  fetch('/api/notifications/unread-count').then(r => r.json()).then(data => {
    const badge = document.getElementById('notifBadge');
    if (badge && data.count > 0) {
      badge.textContent = data.count;
      badge.style.display = 'inline-block';
    }
  }).catch(() => {});
}

/* Logout and redirect */
function doLogout() {
  fetch('/api/logout', {method:'POST'}).then(() => {
    window.location.href = 'login.html';
  });
}

/* Format date nicely */
function formatDate(dateStr) {
  if (!dateStr) return 'Unknown';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-IN', {day:'2-digit', month:'short', year:'numeric'});
}

/* Get status badge HTML */
function statusBadge(status) {
  const colors = { 'Available': '#22c55e', 'Claim Pending': '#f59e0b', 'Claimed': '#9ca3af' };
  const color = colors[status] || '#9ca3af';
  return `<span style="background:${color};color:white;padding:3px 10px;border-radius:20px;font-size:0.75rem;font-weight:600;">${status}</span>`;
}

/* Category emoji map */
function catEmoji(cat) {
  const m = {Watch:'⌚',Phone:'📱',Chain:'📿',Wallet:'👛',Keys:'🔑',Electronics:'💻',Bag:'🎒','ID Card':'🪪',Other:'📦'};
  return m[cat] || '📦';
}
