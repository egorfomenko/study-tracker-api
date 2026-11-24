'use strict';
const $ = (id) => document.getElementById(id);
const state = {users: [], tasks: [], tags: [], sessions: [], leaders: [], userId: null, view: 'tasks', loading: false};
let calendarMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
let selectedDate = localDate(new Date());

const statuses = {CREATED: 'Новая', IN_PROGRESS: 'В работе', DONE: 'Завершена'};
const priorities = {Low: 'Низкий', Medium: 'Средний', High: 'Высокий'};
let submitForm = null, loadVersion = 0;
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const options = (items, selected) => items.map(([value, label]) => `<option value="${escapeHtml(value)}" ${String(value) === String(selected) ? 'selected' : ''}>${escapeHtml(label)}</option>`).join('');
const tagOptions = (selected) => options(state.tags.map(t => [t.id, t.name]), selected);
const activeSession = () => state.sessions.find(s => s.status === 'ACTIVE');
function duration(value) {
  const seconds = Math.max(0, Math.floor(Number(value) || 0));
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;

  return [hours, minutes, remainingSeconds]
    .map(part => String(part).padStart(2, '0'))
    .join(':');
}
const date = (value) => value ? new Intl.DateTimeFormat('ru', {day:'numeric',month:'short'}).format(new Date(value)) : 'Без срока';
function notice(message = '') { $('notice').textContent = message; $('notice').hidden = !message; }
async function api(path, method = 'GET', body) {
  const headers = body ? {'Content-Type': 'application/json'} : {};
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method.toUpperCase())) {
    try {
      const csrfResponse = await fetch('/csrf', {method: 'GET', cache: 'no-store'});
      if (!csrfResponse.ok) throw new Error();
      const csrf = await csrfResponse.json();
      if (typeof csrf?.token !== 'string' || !csrf.token.trim() ||
          typeof csrf?.headerName !== 'string' || !csrf.headerName.trim()) throw new Error();
      // Validate the server-provided header before sending the modifying request.
      new Headers({[csrf.headerName]: csrf.token});
      headers[csrf.headerName] = csrf.token;
    } catch {
      throw new Error('Не удалось получить CSRF-токен. Запрос не отправлен. Проверь подключение и войди в аккаунт заново.');
    }
  }
  let response;
  try {response = await fetch(path, {method, headers, body: body ? JSON.stringify(body) : undefined});}
  catch {throw new Error('Не удалось связаться с сервером. Проверь, что Spring Boot запущен.');}
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.detail || data?.message || `Не удалось выполнить запрос (HTTP ${response.status}).`);
  return data;
}
async function loadTagsWithDuration(userId, sessions) {
  const tags = await api(`/users/${userId}/tags`);

  return Promise.all(tags.map(async tag => {
    let totalDurationSeconds;

    try {
      totalDurationSeconds = await api(`/users/${userId}/tags/${tag.id}/stats`);
    } catch {
      totalDurationSeconds = sessions
        .filter(session => session.tagId === tag.id && session.status === 'COMPLETED')
        .reduce((total, session) => total + Number(session.durationSeconds || 0), 0);
    }

    return {
      ...tag,
      totalDurationSeconds
    };
  }));
}

async function load() {
  const version = ++loadVersion;
  state.loading = true; $('refresh').disabled = true; notice();
  try {
    const [users, tasks, leaders] = await Promise.all([api('/users'), api('/tasks'), api('/leaderboard')]);
    if (version !== loadVersion) return;
    const userId = users.some(u => u.id === state.userId) ? state.userId : users[0]?.id ?? null;
    const sessions = userId ? await api(`/users/${userId}/sessions`) : [];
    const tags = userId ? await loadTagsWithDuration(userId, sessions) : [];
    if (version !== loadVersion) return;
    Object.assign(state, {users, tasks: tasks.filter(t => t.userId === userId), leaders, userId, tags, sessions});
    try {localStorage.setItem('rhythm-user', userId ?? '');} catch {}
    $('user-select').innerHTML = users.length ? options(users.map(u => [u.id,u.name]), userId) : '<option>Создай пользователя</option>';
    $('tag-filter').innerHTML = '<option value="">Все теги</option>' + tagOptions();
    render();
  } catch (error) {if (version === loadVersion) {notice(error.message); $('content').innerHTML = '<div class="empty"><strong>Не удалось загрузить данные</strong>Проверь подключение к базе и нажми «Обновить».</div>';}}
  finally {if (version === loadVersion) {state.loading = false; $('refresh').disabled = false;}}
}
function render() {
  $('stat-total').textContent = state.tasks.length;
  $('stat-done').textContent = state.tasks.filter(t => t.status === 'DONE').length;
  const completedSeconds = state.sessions
    .filter(session => session.status === 'COMPLETED')
    .reduce((total, session) => total + Number(session.durationSeconds || 0), 0);

  $('stat-time').textContent = duration(completedSeconds);
  const titles = {tasks:['Мои задачи','Список задач','＋ Новая задача'],sessions:['Учебные сессии','История сессий','▶ Новая сессия'],tags:['Мои теги','Направления учёбы','＋ Новый тег'],leaders:['Рейтинг','Время, которое стало знаниями',null]};
  titles.calendar = ['Календарь', 'Задачи и дедлайны', '＋ Новая задача'];
  const [title, section, create] = titles[state.view];
  $('page-title').textContent = title; $('section-title').textContent = section;
  $('create-button').hidden = !create; $('create-button').textContent = create || '';
  $('filters').hidden = state.view !== 'tasks';
  document.querySelectorAll('[data-view]').forEach(b => b.classList.toggle('active', b.dataset.view === state.view));
  const session = activeSession();
  $('session-action').textContent = session ? '■ Завершить' : '▶ Начать сессию';
  $('focus-title').textContent = session ? 'Сессия идёт' : 'Учебная сессия';
  $('focus-description').textContent = session ? (state.tags.find(t => t.id === session.tagId)?.name || 'Учебная сессия') + (session.taskId ? ' · ' + (state.tasks.find(t => t.id === session.taskId)?.title || 'Задача') : '') : 'Выбери направление и начни учебную сессию.';
  tick();
  if (!state.userId && state.view !== 'leaders') { $('content').innerHTML = '<div class="empty"><strong>Начни свой ритм</strong>Создай пользователя с помощью кнопки слева.<br>Затем добавь первый тег и задачу.</div>'; return; }
  if (state.view === 'tasks') renderTasks();
  if (state.view === 'calendar') renderCalendar();
  if (state.view === 'tags') renderTags();
  if (state.view === 'sessions') $('content').innerHTML = state.sessions.map(s => `<article class="history-row"><span class="rank">◷</span><div class="task-info"><strong>${escapeHtml(state.tags.find(t => t.id === s.tagId)?.name || 'Учебная сессия')}</strong><div class="meta">${escapeHtml(date(s.startTime))} · ${s.status === 'ACTIVE' ? 'Сейчас идёт' : s.status === 'COMPLETED' ? 'Завершена' : 'Отменена'}${s.taskId ? ' · ' + escapeHtml(state.tasks.find(t => t.id === s.taskId)?.title || 'Задача') : ''}</div></div><span class="duration">${s.status === 'ACTIVE' ? 'В процессе' : duration(s.durationSeconds)}</span></article>`).join('') || empty('Здесь появится твой прогресс','Начни первую сессию — время сохранится после завершения.');
  if (state.view === 'leaders') $('content').innerHTML = state.leaders.map((u,i) => `<article class="leader-row"><span class="rank">${String(i+1).padStart(2,'0')}</span><div class="task-info"><strong>${escapeHtml(u.name)}${u.userId === state.userId ? ' · ты' : ''}</strong></div><span class="duration">${duration(u.totalDurationSeconds)}</span></article>`).join('') || empty('Первый шаг к вершине','Рейтинг появится после завершённых учебных сессий.');
}
function color(value) {return /^#[0-9a-f]{6}$/i.test(value || '') ? value : '#8b9e7b';}
function empty(title, text) {return `<div class="empty"><strong>${title}</strong>${text}</div>`;}
function renderTags() {
  const tagRows = state.tags.map(tag => {
    const taskCount = state.tasks.filter(task => task.tagId === tag.id).length;

    return `
      <article class="tag-row">
        <span class="swatch" style="--tag-color:${color(tag.colorHex)}"></span>
        <div class="task-info">
          <strong>${escapeHtml(tag.name)}</strong>
          <div class="meta">
            <span>Задач: ${taskCount}</span>
            <span>Время занятий: ${duration(tag.totalDurationSeconds)}</span>
          </div>
        </div>
        <button class="icon" data-action="edit-tag" data-id="${tag.id}" aria-label="Изменить тег">✎</button>
        <button class="icon" data-action="delete-tag" data-id="${tag.id}" aria-label="Удалить тег">×</button>
      </article>
    `;
  });

  $('content').innerHTML = tagRows.join('') || empty(
    'У каждого интереса — свой тег',
    'Добавь, например, «Java», «Английский» или «Чтение».'
  );
}

function localDate(value) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function isOverdue(task) {
  return task.deadlineDate && task.deadlineDate < localDate(new Date()) && task.status !== 'DONE';
}

function taskRow(task) {
  return `
    <article class="task-row ${task.status === 'DONE' ? 'done' : ''} ${isOverdue(task) ? 'task-overdue' : ''}">
      <input type="checkbox" class="complete-task" data-action="complete-task" data-id="${task.id}"
        aria-label="Выполнить и удалить задачу ${escapeHtml(task.title)}" title="Выполнить и удалить задачу">
      <div class="task-info">
        <div class="task-title">${escapeHtml(task.title)}</div>
        <div class="meta">
          <span class="pill">${escapeHtml(task.tagName)}</span>
          <span>${statuses[task.status] || escapeHtml(task.status)}</span>
          <span>Дедлайн: ${date(task.deadlineDate)}</span>
          ${isOverdue(task) ? '<span>Просрочена</span>' : ''}
        </div>
      </div>
      <span class="priority ${escapeHtml(task.priority)}">${priorities[task.priority] || 'Без приоритета'}</span>
      <div class="row-actions">
        <button class="icon" data-action="edit-task" data-id="${task.id}" aria-label="Изменить задачу">✎</button>
        <button class="icon" data-action="delete-task" data-id="${task.id}" aria-label="Удалить задачу">×</button>
      </div>
    </article>
  `;
}

function renderTasks() {
  const tasks = state.tasks.filter(task => {
    const matchesSearch = task.title.toLocaleLowerCase().includes($('search').value.toLocaleLowerCase());
    const matchesStatus = !$('status-filter').value || task.status === $('status-filter').value;
    const matchesTag = !$('tag-filter').value || String(task.tagId) === $('tag-filter').value;
    return matchesSearch && matchesStatus && matchesTag;
  });

  $('content').innerHTML = tasks.map(taskRow).join('') || empty(
    state.tasks.length ? 'Ничего не найдено' : 'Место для твоих планов',
    state.tasks.length ? 'Попробуй изменить фильтры.' : 'Создай первую задачу.'
  );
}

function renderCalendar() {
  const year = calendarMonth.getFullYear();
  const month = calendarMonth.getMonth();
  const offset = (calendarMonth.getDay() + 6) % 7;
  const dayCount = new Date(year, month + 1, 0).getDate();
  const cells = Array.from({length: offset}, () => '<div class="calendar-blank"></div>');

  for (let day = 1; day <= dayCount; day++) {
    const dayDate = localDate(new Date(year, month, day));
    const deadlines = state.tasks.filter(task => task.deadlineDate === dayDate);
    const created = state.tasks.filter(task => task.createDate === dayDate);
    const overdue = deadlines.some(isOverdue);
    cells.push(`
      <button class="calendar-day ${dayDate === selectedDate ? 'selected' : ''} ${overdue ? 'calendar-overdue' : ''}"
        data-action="select-day" data-date="${dayDate}" aria-pressed="${dayDate === selectedDate}"
        aria-label="${dayDate}, дедлайнов: ${deadlines.length}, создано: ${created.length}">
        <strong>${day}</strong>
        ${deadlines.length ? `<span>Дедлайны: ${deadlines.length}</span>` : ''}
        ${created.length ? `<span>Создано: ${created.length}</span>` : ''}
      </button>
    `);
  }

  const tasks = state.tasks.filter(task => task.deadlineDate === selectedDate || task.createDate === selectedDate);
  $('content').innerHTML = `
    <div class="calendar-toolbar">
      <button class="secondary" data-action="previous-month" aria-label="Предыдущий месяц">←</button>
      <h3>${new Intl.DateTimeFormat('ru', {month: 'long', year: 'numeric'}).format(calendarMonth)}</h3>
      <button class="secondary" data-action="next-month" aria-label="Следующий месяц">→</button>
    </div>
    <div class="calendar-grid">
      ${['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'].map(day => `<div class="calendar-weekday">${day}</div>`).join('')}
      ${cells.join('')}
    </div>
    <div class="section-heading calendar-selection">
      <h3>${date(selectedDate + 'T12:00:00')}</h3>
      <button class="primary" data-action="add-on-date">＋ Задача на эту дату</button>
    </div>
    <p class="muted">Задачи, созданные в этот день или с дедлайном на эту дату. Чекбокс выполняет и удаляет задачу.</p>
    <div class="calendar-tasks">${tasks.map(taskRow).join('') || empty('Нет задач на эту дату', 'Выбери другой день или добавь задачу.')}</div>
  `;
}

const pomodoroIntervals = [20, 30, 40, 50];
let pomodoroSettings = {};
try { pomodoroSettings = JSON.parse(localStorage.getItem('rhythm-pomodoro') || '{}') || {}; } catch {}
function savePomodoro() {
  try { localStorage.setItem('rhythm-pomodoro', JSON.stringify(pomodoroSettings)); } catch {}
}
const pomodoroKey = session => `${state.userId}:${session.id}:${session.startTime}`;
let alertContext;
function unlockAlert() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!alertContext) alertContext = new AudioContext();
    if (alertContext.state === 'suspended') alertContext.resume().catch(() => {});
  } catch {}
}
function playPomodoroAlert() {
  unlockAlert();
  if (!alertContext || alertContext.state !== 'running') {
    notice('Интервал помодоро завершён. Нажми на страницу, чтобы разрешить звуковые оповещения.');
    return;
  }
  [0, 0.35, 0.7].forEach((delay, index) => {
    const oscillator = alertContext.createOscillator();
    const gain = alertContext.createGain();
    const start = alertContext.currentTime + delay;
    oscillator.frequency.value = index === 2 ? 880 : 660;
    gain.gain.setValueAtTime(0, start);
    gain.gain.linearRampToValueAtTime(0.25, start + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.001, start + 0.3);
    oscillator.connect(gain);
    gain.connect(alertContext.destination);
    oscillator.start(start);
    oscillator.stop(start + 0.31);
    oscillator.onended = () => { oscillator.disconnect(); gain.disconnect(); };
  });
}
function tick() {
  const session = activeSession();
  const elapsed = session ? Math.max(0, (Date.now() - new Date(session.startTime).getTime()) / 1000) : 0;
  $('timer').textContent = duration(elapsed);
  const setting = session && pomodoroSettings[pomodoroKey(session)];
  $('pomodoro-status').textContent = setting && pomodoroIntervals.includes(setting.minutes)
    ? `Помодоро · ${setting.minutes} мин${setting.notified ? ' · интервал завершён' : ''}` : '';
  if (setting && pomodoroIntervals.includes(setting.minutes) && !setting.notified && elapsed >= setting.minutes * 60) {
    setting.notified = true;
    savePomodoro();
    playPomodoroAlert();
    $('pomodoro-status').textContent = `Помодоро · ${setting.minutes} мин · интервал завершён`;
  }
}
document.addEventListener('pointerdown', unlockAlert);
document.addEventListener('keydown', unlockAlert);
document.addEventListener('visibilitychange', () => { if (!document.hidden) tick(); });
function field(label, input) {return `<label>${label}${input}</label>`;}
function openModal(title, fields, handler) {
 $('modal-title').textContent = title; $('fields').innerHTML = fields; $('form-error').textContent = ''; submitForm = handler; $('save-button').disabled = false; $('modal').showModal();
}
function requireUser() {if (!state.userId) {userEditor(); return false;} return true;}
function requireTags() {if (!requireUser()) return false; if (!state.tags.length) {notice('Сначала создай тег — он нужен для задач и сессий.'); tagEditor(); return false;} return true;}
function userEditor() {openModal('Новое пространство',field('Имя','<input name="name" required maxlength="100" autocomplete="name" placeholder="Как тебя зовут?">') + field('Email','<input name="email" required type="email" maxlength="254" autocomplete="email" placeholder="you@example.com">'), async data => {const user = await api('/users','POST',data); state.userId = user.id;});}
function tagEditor(tag) {
 if (!requireUser()) return;
 openModal(tag ? 'Изменить тег' : 'Новый тег', field('Название',`<input name="name" required maxlength="100" value="${escapeHtml(tag?.name)}" placeholder="Например, Java">`) + field('Цвет',`<input name="colorHex" type="color" value="${color(tag?.colorHex || '#7e9768')}">`), data => api(tag ? `/tags/${tag.id}` : '/tags',tag ? 'PUT':'POST',tag ? data : {...data,userId:state.userId}));
}
function taskEditor(task, deadlineDate = '') {
 if (!requireTags()) return;
 openModal(task ? 'Изменить задачу' : 'Новая задача',field('Что нужно сделать?',`<input name="title" required maxlength="255" value="${escapeHtml(task?.title)}" placeholder="Например, разобраться с коллекциями">`)+field('Направление',`<select name="tagId" required>${tagOptions(task?.tagId)}</select>`)+field('Приоритет',`<select name="priority">${options(Object.entries(priorities),task?.priority || 'Medium')}</select>`)+field('Срок',`<input type="date" name="deadlineDate" value="${escapeHtml(task?.deadlineDate || deadlineDate)}">`)+(task ? field('Статус',`<select name="status">${options(Object.entries(statuses),task.status)}</select>`) : ''), data => api(task ? `/tasks/${task.id}` : '/tasks',task ? 'PUT':'POST',{...data,tagId:Number(data.tagId),deadlineDate:data.deadlineDate || null,...(!task ? {userId:state.userId} : {})}));
}
function sessionEditor() {
 if (activeSession()) {notice('Сначала заверши текущую учебную сессию.'); return;}
 if (!requireTags()) return;
 openModal('Начать фокус-сессию',field('Направление',`<select id="session-tag" name="tagId" required>${tagOptions()}</select>`)+field('Задача (необязательно)','<select id="session-task" name="taskId"></select>') + field('Интервал помодоро', `<select name="pomodoroMinutes">${options(pomodoroIntervals.map(minutes => [minutes, `${minutes} минут`]), 20)}</select>`) + '<p class="muted">В конце интервала прозвучит сигнал. Сессия продолжится до ручного завершения.</p>', async data => {
   unlockAlert();
   const session = await api('/sessions/start','POST',{userId:state.userId,tagId:Number(data.tagId),taskId:data.taskId ? Number(data.taskId) : null});
   pomodoroSettings[pomodoroKey(session)] = {minutes: Number(data.pomodoroMinutes), notified: false};
   savePomodoro();
 });
 const updateTasks = () => {$('session-task').innerHTML = '<option value="">Без привязки к задаче</option>' + options(state.tasks.filter(t => String(t.tagId) === $('session-tag').value && t.status !== 'DONE').map(t => [t.id,t.title]));};
 $('session-tag').addEventListener('change',updateTasks); updateTasks();
}
async function mutate(action) {if (state.loading) return; state.loading = true; try {await action(); await load();} catch(error) {notice(error.message);} finally {state.loading = false;}}
function setTheme(theme) {
  const isDark = theme === 'dark';
  document.documentElement.dataset.theme = theme;
  const themeToggle = $('theme-toggle');
  if (themeToggle) {
    themeToggle.textContent = isDark ? '☀ Светлая тема' : '☾ Тёмная тема';
    themeToggle.setAttribute('aria-pressed', String(isDark));
    themeToggle.setAttribute(
      'aria-label',
      isDark ? 'Включить светлую тему' : 'Включить тёмную тему'
    );
  }

  const themeColor = document.querySelector('meta[name="theme-color"]');
  if (themeColor) {
    themeColor.content = isDark ? '#171e1b' : '#f6f7f3';
  }
}

const timerStyles = ['forest', 'ocean', 'lavender', 'sunset', 'rose', 'graphite'];

function setTimerStyle(value) {
  const style = timerStyles.includes(value) ? value : 'forest';
  document.documentElement.dataset.timerStyle = style;

  const selector = $('timer-style');
  if (selector) {
    selector.value = style;
  }
}

let initialTimerStyle = 'forest';
try {
  initialTimerStyle = localStorage.getItem('rhythm-timer-style') || 'forest';
} catch {}
setTimerStyle(initialTimerStyle);

$('timer-style')?.addEventListener('change', event => {
  setTimerStyle(event.target.value);

  try {
    localStorage.setItem('rhythm-timer-style', document.documentElement.dataset.timerStyle);
  } catch {}
});

let initialTheme = 'light';
try {
  initialTheme = localStorage.getItem('rhythm-theme') || 'light';
} catch {}
setTheme(initialTheme === 'dark' ? 'dark' : 'light');

$('theme-toggle')?.addEventListener('click', () => {
  const theme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
  setTheme(theme);

  try {
    localStorage.setItem('rhythm-theme', theme);
  } catch {}
});

$('editor').addEventListener('submit',async event => {event.preventDefault(); const handler = submitForm; $('save-button').disabled = true; $('form-error').textContent = ''; try {await handler(Object.fromEntries(new FormData(event.target))); $('modal').close(); await load();} catch(error) {$('form-error').textContent = error.message;} finally {$('save-button').disabled = false;}});
$('close-modal').onclick = $('cancel-modal').onclick = () => $('modal').close();
$('add-user').onclick = userEditor;
$('refresh').onclick = load;
$('user-select').onchange = event => {state.userId = Number(event.target.value); load();};
$('create-button').onclick = () => {if (state.view === 'tasks' || state.view === 'calendar') {
  taskEditor(undefined, state.view === 'calendar' ? selectedDate : '');
} if (state.view === 'tags') tagEditor(); if (state.view === 'sessions') sessionEditor();};
$('session-action').onclick = () => {const session = activeSession(); if (session) mutate(() => api(`/sessions/${session.id}/stop`,'POST')); else sessionEditor();};
document.querySelectorAll('[data-view]').forEach(button => button.onclick = () => {state.view = button.dataset.view; render();});
['search','status-filter','tag-filter'].forEach(id => $(id).addEventListener(id === 'search' ? 'input' : 'change', renderTasks));
$('content').addEventListener('click',event => {
 const button = event.target.closest('[data-action]');
 if (!button) return;
 if (state.loading) {
   if (button.matches('input[type="checkbox"]')) button.checked = false;
   return;
 }
 const id = Number(button.dataset.id), action = button.dataset.action;
 const task = state.tasks.find(t => t.id === id), tag = state.tags.find(t => t.id === id);
 if (action === 'edit-task') taskEditor(task);
 if (action === 'edit-tag') tagEditor(tag);
 if (action === 'complete-task') {
   button.disabled = true;
   mutate(() => api(`/tasks/${id}`, 'DELETE')).finally(() => {
     button.checked = false;
     button.disabled = false;
   });
 }
 if (action === 'select-day') {
   selectedDate = button.dataset.date;
   renderCalendar();
 }
 if (action === 'previous-month' || action === 'next-month') {
   const step = action === 'next-month' ? 1 : -1;
   calendarMonth = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + step, 1);
   renderCalendar();
 }
 if (action === 'add-on-date') taskEditor(undefined, selectedDate);
 if (action === 'delete-task' || action === 'delete-tag') {const isTask = action === 'delete-task'; openModal('Удалить ' + (isTask ? 'задачу?' : 'тег?'), `<p>«${escapeHtml(isTask ? task.title : tag.name)}» будет удалён${isTask ? 'а' : ''}. Это действие нельзя отменить.</p>`, () => api(`/${isTask ? 'tasks' : 'tags'}/${id}`,'DELETE')); $('save-button').textContent = 'Удалить';}
});
$('modal').addEventListener('close',() => {$('save-button').textContent = 'Сохранить';});
$('date-label').textContent = new Intl.DateTimeFormat('ru',{weekday:'long',day:'numeric',month:'long'}).format(new Date()).toLocaleUpperCase();
try {state.userId = Number(localStorage.getItem('rhythm-user')) || null;} catch {}
setInterval(tick, 1000);
setInterval(() => {
  if (!state.loading && (state.view === 'tasks' || state.view === 'calendar')) render();
}, 60000);
load();

const lofiAudio = $('lofi-audio');
function updateLofiButton() {
  $('lofi-toggle').textContent = lofiAudio.paused ? '▶' : 'Ⅱ';
  $('lofi-toggle').setAttribute('aria-label', lofiAudio.paused ? 'Включить lofi' : 'Приостановить lofi');
  $('lofi-toggle').setAttribute('aria-pressed', String(!lofiAudio.paused));
}
function setLofiVolume(value) {
  const volume = Number(value);
  lofiAudio.volume = Number.isFinite(volume) ? Math.min(100, Math.max(0, volume)) / 100 : 0.4;
  $('lofi-volume').value = Math.round(lofiAudio.volume * 100);
  $('lofi-volume-value').textContent = `${$('lofi-volume').value}%`;
}
let savedLofiVolume = 40;
try { savedLofiVolume = localStorage.getItem('rhythm-lofi-volume') ?? 40; } catch {}
setLofiVolume(savedLofiVolume);
$('lofi-volume').addEventListener('input', event => {
  setLofiVolume(event.target.value);
  try { localStorage.setItem('rhythm-lofi-volume', event.target.value); } catch {}
});
$('lofi-toggle').addEventListener('click', async () => {
  if (!lofiAudio.paused) { lofiAudio.pause(); return; }
  try { await lofiAudio.play(); }
  catch { notice('Не удалось включить lofi. Попробуй запустить плеер ещё раз.'); }
  updateLofiButton();
});
lofiAudio.addEventListener('play', updateLofiButton);
lofiAudio.addEventListener('pause', updateLofiButton);
lofiAudio.addEventListener('error', () => { updateLofiButton(); notice('Не удалось загрузить lofi-трек.'); });
