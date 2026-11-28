'use strict';

async function api(path, method = 'GET', body) {
  const headers = new Headers();
  let payload;
  if (body instanceof URLSearchParams) {
    payload = body;
  } else if (body !== undefined) {
    headers.set('Content-Type', 'application/json');
    payload = JSON.stringify(body);
  }

  if (method !== 'GET') {
    const csrfResponse = await fetch('/csrf', {cache: 'no-store'});
    if (!csrfResponse.ok) throw new Error('Не удалось получить токен. Обнови страницу.');
    const csrf = await csrfResponse.json();
    if (!csrf.headerName || !csrf.token) throw new Error('Не удалось подтвердить запрос.');
    headers.set(csrf.headerName, csrf.token);
  }

  const response = await fetch(path, {method, headers, body: payload, cache: 'no-store'});
  if (response.status === 401 && path !== '/login') {
    location.replace('/signin.html');
    throw new Error('Войди в аккаунт.');
  }
  if (response.status === 401) throw new Error('Неверный email или пароль.');
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error(data?.detail || data?.message || `Не удалось выполнить запрос (HTTP ${response.status}).`);
  }
  return data;
}

window.addEventListener('pageshow', event => {
  if (event.persisted) location.reload();
});
