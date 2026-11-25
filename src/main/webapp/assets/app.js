(function () {
  'use strict';

  var DEMO_ACCOUNT = 'admin';
  var DEMO_PASSWORD = '123456';

  function byId(id) {
    return document.getElementById(id);
  }

  function initLogin() {
    var form = byId('login-form');
    if (!form) {
      return;
    }

    var account = byId('account');
    var password = byId('password');
    var toggle = byId('toggle-password');
    var fill = byId('fill-demo');
    var hint = byId('hint');

    toggle.addEventListener('click', function () {
      var show = password.type === 'password';
      password.type = show ? 'text' : 'password';
      toggle.setAttribute('aria-label', show ? '隐藏口令' : '显示口令');
      toggle.innerHTML = show
        ? '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M3 3l18 18"/><path d="M10.6 10.6a2 2 0 003 3"/><path d="M9.9 5.1A9.8 9.8 0 0112 5c5 0 9 4.5 9 7 0 .9-.5 2.1-1.4 3.2M6.2 6.2C4.3 7.5 3 9.3 3 12c0 2.5 4 7 9 7 1.4 0 2.7-.3 3.8-.9"/></svg>'
        : '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></svg>';
    });

    fill.addEventListener('click', function () {
      account.value = DEMO_ACCOUNT;
      password.value = DEMO_PASSWORD;
      hint.textContent = '';
      password.focus();
    });

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      if (!account.value.trim()) {
        hint.textContent = '请输入账号';
        account.focus();
        return;
      }
      if (!password.value) {
        hint.textContent = '请输入口令';
        password.focus();
        return;
      }
      if (account.value.trim() !== DEMO_ACCOUNT || password.value !== DEMO_PASSWORD) {
        hint.textContent = '账号或口令不正确，请使用演示账号';
        return;
      }
      sessionStorage.setItem('photoLedgerLogin', '1');
      window.location.href = 'dashboard.html';
    });
  }

  function initDashboard() {
    var logout = byId('logout');

    // 只在 dashboard.html 上执行登录守卫，否则 index.html 会无限自我重定向
    if (!logout) {
      return;
    }

    if (sessionStorage.getItem('photoLedgerLogin') !== '1') {
      window.location.replace('index.html');
      return;
    }

    logout.addEventListener('click', function () {
      sessionStorage.removeItem('photoLedgerLogin');
      window.location.href = 'index.html';
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initLogin();
    initDashboard();
  });
})();
