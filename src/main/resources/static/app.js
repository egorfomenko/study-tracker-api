'use strict';
const $ = (id) => document.getElementById(id);
const state = {users: [], tasks: [], tags: [], sessions: [], leaders: [], userId: null, view: 'tasks', loading: false};
const statuses = {CREATED: 'Новая', IN_PROGRESS: 'В работе', DONE: 'Завершена'};
const priorities = {Low: 'Низкий', Medium: 'Средний', High: 'Высокий'};
let submitForm = null, loadVersion = 0;
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const options = (items, selected) => items.map(([value, label]) => `<option value="${escapeHtml(value)}" ${String(value) === String(selected) ? 'selected' : ''}>${escapeHtml(label)}</option>`).join('');
const tagOptions = (selected) => options(state.tags.map(t => [t.id, t.name]), selected);
const activeSession = () => state.sessions.find(s => s.status === 'ACTIVE');
const duration = (value) => {const s = Math.max(0, Math.floor(Number(value) || 0)); return [Math.floor(s/3600), Math.floor(s%3600/60), s%60].map(n => String(n).padStart(2,'0')).join(':');};
const date = (value) => value ? new Intl.DateTimeFormat('ru', {day:'numeric',month:'short'}).format(new Date(value)) : 'Без срока';
function notice(message = '') { $('notice').textContent = message; $('notice').hidden = !message; }
async function api(path, method = 'GET', body) {
  let response;
  try {response = await fetch(path, {method, headers: body ? {'Content-Type':'application/json'} : {}, body: body ? JSON.stringify(body) : undefined});}
  catch {throw new Error('Не удалось связаться с сервером. Проверь, что Spring Boot запущен.');}
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.detail || data?.message || `Не удалось выполнить запрос (HTTP ${response.status}).`);
  return data;
}
async function load() {
  const version = ++loadVersion;
  state.loading = true; $('refresh').disabled = true; notice();
  try {
    const [users, tasks, leaders] = await Promise.all([api('/users'), api('/tasks'), api('/leaderboard')]);
    if (version !== loadVersion) return;
    const userId = users.some(u => u.id === state.userId) ? state.userId : users[0]?.id ?? null;
    const [tags, sessions] = userId ? await Promise.all([api(`/users/${userId}/tags`), api(`/users/${userId}/sessions`)]) : [[], []];
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
  $('stat-time').textContent = `${(state.sessions.filter(s => s.status === 'COMPLETED').reduce((sum,s) => sum + (s.durationSeconds || 0),0)/3600).toFixed(1)} ч`;
  const titles = {tasks:['Мои задачи','Список задач','＋ Новая задача'],sessions:['Учебные сессии','История сессий','▶ Новая сессия'],tags:['Мои теги','Направления учёбы','＋ Новый тег'],leaders:['Рейтинг','Время, которое стало знаниями',null]};
  const [title, section, create] = titles[state.view];
  $('page-title').textContent = title; $('section-title').textContent = section;
  $('create-button').hidden = !create; $('create-button').textContent = create || '';
  $('filters').hidden = state.view !== 'tasks';
  document.querySelectorAll('[data-view]').forEach(b => b.classList.toggle('active', b.dataset.view === state.view));
  const session = activeSession();
  $('session-action').textContent = session ? '■ Завершить' : '▶ Начать сессию';
  $('focus-title').textContent = session ? 'Ты в потоке.' : 'Одна задача за раз.';
  $('focus-description').textContent = session ? (state.tags.find(t => t.id === session.tagId)?.name || 'Учебная сессия') + (session.taskId ? ' · ' + (state.tasks.find(t => t.id === session.taskId)?.title || 'Задача') : '') : 'Выбери направление и начни учебную сессию.';
  tick();
  if (!state.userId && state.view !== 'leaders') { $('content').innerHTML = '<div class="empty"><strong>Начни свой ритм</strong>Создай пользователя с помощью кнопки слева.<br>Затем добавь первый тег и задачу.</div>'; return; }
  if (state.view === 'tasks') renderTasks();
  if (state.view === 'tags') $('content').innerHTML = state.tags.map(t => `<article class="tag-row"><span class="swatch" style="--tag-color:${color(t.colorHex)}"></span><div class="task-info"><strong>${escapeHtml(t.name)}</strong><div class="meta">Задач: ${state.tasks.filter(task => task.tagId === t.id).length}</div></div><button class="icon" data-action="edit-tag" data-id="${t.id}" aria-label="Изменить тег">✎</button><button class="icon" data-action="delete-tag" data-id="${t.id}" aria-label="Удалить тег">×</button></article>`).join('') || empty('У каждого интереса — свой тег','Добавь, например, «Java», «Английский» или «Чтение».');
  if (state.view === 'sessions') $('content').innerHTML = state.sessions.map(s => `<article class="history-row"><span class="rank">◷</span><div class="task-info"><strong>${escapeHtml(state.tags.find(t => t.id === s.tagId)?.name || 'Учебная сессия')}</strong><div class="meta">${escapeHtml(date(s.startTime))} · ${s.status === 'ACTIVE' ? 'Сейчас идёт' : s.status === 'COMPLETED' ? 'Завершена' : 'Отменена'}${s.taskId ? ' · ' + escapeHtml(state.tasks.find(t => t.id === s.taskId)?.title || 'Задача') : ''}</div></div><span class="duration">${s.status === 'ACTIVE' ? 'В процессе' : duration(s.durationSeconds)}</span></article>`).join('') || empty('Здесь появится твой прогресс','Начни первую сессию — время сохранится после завершения.');
  if (state.view === 'leaders') $('content').innerHTML = state.leaders.map((u,i) => `<article class="leader-row"><span class="rank">${String(i+1).padStart(2,'0')}</span><div class="task-info"><strong>${escapeHtml(u.name)}${u.userId === state.userId ? ' · ты' : ''}</strong></div><span class="duration">${duration(u.totalDurationSeconds)}</span></article>`).join('') || empty('Первый шаг к вершине','Рейтинг появится после завершённых учебных сессий.');
}
function color(value) {return /^#[0-9a-f]{6}$/i.test(value || '') ? value : '#8b9e7b';}
function empty(title, text) {return `<div class="empty"><strong>${title}</strong>${text}</div>`;}
function renderTasks() {
 const today = new Date(); const todayString = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;
 const tasks = state.tasks.filter(t => t.title.toLocaleLowerCase().includes($('search').value.toLocaleLowerCase()) && (!$('status-filter').value || t.status === $('status-filter').value) && (!$('tag-filter').value || String(t.tagId) === $('tag-filter').value));
 $('content').innerHTML = tasks.map(t => `<article class="task-row ${t.status === 'DONE' ? 'done' : ''}"><button class="check" data-action="toggle-task" data-id="${t.id}" aria-label="${t.status === 'DONE' ? 'Вернуть в новые' : 'Завершить задачу'}">${t.status === 'DONE' ? '✓' : ''}</button><div class="task-info"><div class="task-title">${escapeHtml(t.title)}</div><div class="meta"><span class="pill">${escapeHtml(t.tagName)}</span><span>${statuses[t.status] || escapeHtml(t.status)}</span><span class="${t.deadlineDate && t.deadlineDate < todayString && t.status !== 'DONE' ? 'overdue' : ''}">${date(t.deadlineDate)}</span></div></div><span class="priority ${escapeHtml(t.priority)}">${priorities[t.priority] || 'Без приоритета'}</span><div class="row-actions"><button class="icon" data-action="edit-task" data-id="${t.id}" aria-label="Изменить задачу">✎</button><button class="icon" data-action="delete-task" data-id="${t.id}" aria-label="Удалить задачу">×</button></div></article>`).join('') || empty(state.tasks.length ? 'Ничего не найдено' : 'Место для твоих планов', state.tasks.length ? 'Попробуй изменить фильтры или поисковый запрос.' : 'Создай первую задачу. Даже большое дело начинается с маленького шага.');
}
function tick() {const session = activeSession(); $('timer').textContent = duration(session ? (Date.now() - new Date(session.startTime).getTime())/1000 : 0);}
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
function taskEditor(task) {
 if (!requireTags()) return;
 openModal(task ? 'Изменить задачу' : 'Новая задача',field('Что нужно сделать?',`<input name="title" required maxlength="255" value="${escapeHtml(task?.title)}" placeholder="Например, разобраться с коллекциями">`)+field('Направление',`<select name="tagId" required>${tagOptions(task?.tagId)}</select>`)+field('Приоритет',`<select name="priority">${options(Object.entries(priorities),task?.priority || 'Medium')}</select>`)+field('Срок',`<input type="date" name="deadlineDate" value="${escapeHtml(task?.deadlineDate)}">`)+(task ? field('Статус',`<select name="status">${options(Object.entries(statuses),task.status)}</select>`) : ''), data => api(task ? `/tasks/${task.id}` : '/tasks',task ? 'PUT':'POST',{...data,tagId:Number(data.tagId),deadlineDate:data.deadlineDate || null,...(!task ? {userId:state.userId} : {})}));
}
function sessionEditor() {
 if (activeSession()) {notice('Сначала заверши текущую учебную сессию.'); return;}
 if (!requireTags()) return;
 openModal('Начать фокус-сессию',field('Направление',`<select id="session-tag" name="tagId" required>${tagOptions()}</select>`)+field('Задача (необязательно)','<select id="session-task" name="taskId"></select>'), data => api('/sessions/start','POST',{userId:state.userId,tagId:Number(data.tagId),taskId:data.taskId ? Number(data.taskId) : null}));
 const updateTasks = () => {$('session-task').innerHTML = '<option value="">Без привязки к задаче</option>' + options(state.tasks.filter(t => String(t.tagId) === $('session-tag').value && t.status !== 'DONE').map(t => [t.id,t.title]));};
 $('session-tag').addEventListener('change',updateTasks); updateTasks();
}
async function mutate(action) {if (state.loading) return; state.loading = true; try {await action(); await load();} catch(error) {notice(error.message);} finally {state.loading = false;}}
$('editor').addEventListener('submit',async event => {event.preventDefault(); const handler = submitForm; $('save-button').disabled = true; $('form-error').textContent = ''; try {await handler(Object.fromEntries(new FormData(event.target))); $('modal').close(); await load();} catch(error) {$('form-error').textContent = error.message;} finally {$('save-button').disabled = false;}});
$('close-modal').onclick = $('cancel-modal').onclick = () => $('modal').close();
$('add-user').onclick = userEditor;
$('refresh').onclick = load;
$('user-select').onchange = event => {state.userId = Number(event.target.value); load();};
$('create-button').onclick = () => {if (state.view === 'tasks') taskEditor(); if (state.view === 'tags') tagEditor(); if (state.view === 'sessions') sessionEditor();};
$('session-action').onclick = () => {const session = activeSession(); if (session) mutate(() => api(`/sessions/${session.id}/stop`,'POST')); else sessionEditor();};
document.querySelectorAll('[data-view]').forEach(button => button.onclick = () => {state.view = button.dataset.view; render();});
['search','status-filter','tag-filter'].forEach(id => $(id).addEventListener(id === 'search' ? 'input' : 'change', renderTasks));
$('content').addEventListener('click',event => {
 const button = event.target.closest('[data-action]'); if (!button || state.loading) return;
 const id = Number(button.dataset.id), action = button.dataset.action;
 const task = state.tasks.find(t => t.id === id), tag = state.tags.find(t => t.id === id);
 if (action === 'edit-task') taskEditor(task);
 if (action === 'edit-tag') tagEditor(tag);
 if (action === 'toggle-task') mutate(() => api(`/tasks/${id}`,'PUT',{title:task.title,tagId:task.tagId,deadlineDate:task.deadlineDate,priority:task.priority,status:task.status === 'DONE' ? 'CREATED' : 'DONE'}));
 if (action === 'delete-task' || action === 'delete-tag') {const isTask = action === 'delete-task'; openModal('Удалить ' + (isTask ? 'задачу?' : 'тег?'), `<p>«${escapeHtml(isTask ? task.title : tag.name)}» будет удалён${isTask ? 'а' : ''}. Это действие нельзя отменить.</p>`, () => api(`/${isTask ? 'tasks' : 'tags'}/${id}`,'DELETE')); $('save-button').textContent = 'Удалить';}
});
$('modal').addEventListener('close',() => {$('save-button').textContent = 'Сохранить';});
$('date-label').textContent = new Intl.DateTimeFormat('ru',{weekday:'long',day:'numeric',month:'long'}).format(new Date()).toLocaleUpperCase();
try {state.userId = Number(localStorage.getItem('rhythm-user')) || null;} catch {}
setInterval(tick,1000); load();
