'use strict';

const form = document.getElementById('auth-form');
const loginTab = document.getElementById('login-tab');
const registerTab = document.getElementById('register-tab');
const submit = document.getElementById('auth-submit');
const message = document.getElementById('auth-message');
let registering = false;

function setMode(register) {
  registering = register;
  loginTab.setAttribute('aria-pressed', String(!register));
  registerTab.setAttribute('aria-pressed', String(register));
  document.getElementById('auth-title').textContent = register ? 'Создать аккаунт' : 'С возвращением';
  document.getElementById('name-field').hidden = !register;
  form.elements.name.disabled = !register;
  form.elements.name.required = register;
  form.elements.password.autocomplete = register ? 'new-password' : 'current-password';
  form.elements.password.value = '';
  submit.textContent = register ? 'Зарегистрироваться' : 'Войти';
  message.textContent = '';
}

loginTab.onclick = () => setMode(false);
registerTab.onclick = () => setMode(true);
form.addEventListener('submit', async event => {
  event.preventDefault();
  if (submit.disabled) return;
  const email = form.elements.email.value.trim();
  const password = form.elements.password.value;
  submit.disabled = loginTab.disabled = registerTab.disabled = true;
  message.textContent = '';
  try {
    if (registering) {
      await api('/users', 'POST', {name: form.elements.name.value.trim(), email, password});
      setMode(false);
      message.textContent = 'Аккаунт создан. Введи пароль, чтобы войти.';
      form.elements.password.focus();
    } else {
      await api('/login', 'POST', new URLSearchParams({email, password}));
      location.replace('/');
    }
  } catch (error) {
    message.textContent = error.message;
  } finally {
    submit.disabled = loginTab.disabled = registerTab.disabled = false;
  }
});
