'use strict';
const $ = id => document.getElementById(id);
const state = { trips: [], selected: null, occupancy: null, reservation: null, exchange: false, busy: false, listRequest: 0, occupancyRequest: 0 };
const dateLabel = value => value ? value.slice(0, 10).split('-').reverse().join('/') : '—';
const timeLabel = value => value ? value.slice(11, 16) : '—';
function element(tag, text, className) {
  const node = document.createElement(tag);
  if (text !== undefined) node.textContent = text;
  if (className) node.className = className;
  return node;
}
function notice(message, error = false) {
  $('notice').textContent = message;
  $('notice').className = error ? 'notice error' : 'notice';
  $('notice').hidden = !message;
}
async function request(path, options = {}) {
  let response;
  try { response = await fetch(path, options); }
  catch { throw new Error('Não foi possível conectar. Verifique sua conexão e tente novamente.'); }
  if (response.status === 204) return null;
  const text = await response.text();
  let body;
  try { body = text ? JSON.parse(text) : null; } catch { body = null; }
  if (!response.ok) throw new Error(body?.mensagem || `Não foi possível concluir a operação (HTTP ${response.status}). Tente novamente.`);
  if (body === null) throw new Error('A API retornou uma resposta inesperada. Tente novamente.');
  return body;
}
function numericId(input) {
  const value = Number(input.value);
  if (!Number.isSafeInteger(value) || value <= 0) throw new Error('Informe um número de identificação inteiro e positivo.');
  return value;
}
function tripDescription(id) {
  const trip = state.trips.find(item => item.id === id) || (state.selected?.id === id ? state.selected : null);
  return trip ? `${timeLabel(trip.dataHoraSaida)} · ${dateLabel(trip.dataHoraSaida)} · ${trip.bairro} (viagem ${id})` : `Viagem ${id}`;
}
function renderList() {
  $('trip-list').replaceChildren();
  for (const trip of state.trips) {
    const row = element('article', undefined, `trip-row${state.selected?.id === trip.id ? ' selected' : ''}`);
    const departure = element('div');
    departure.append(element('span', timeLabel(trip.dataHoraSaida), 'trip-time'), element('span', dateLabel(trip.dataHoraSaida), 'trip-date'));
    const origin = element('div', trip.bairro);
    origin.append(element('span', `Viagem ${trip.id}`, 'trip-id'));
    const arrival = element('div', timeLabel(trip.dataHoraChegada), 'trip-arrival');
    arrival.append(element('span', dateLabel(trip.dataHoraChegada), 'trip-date'));
    const button = element('button', state.selected?.id === trip.id ? 'Selecionada' : 'Ver vagas', 'secondary');
    button.type = 'button';
    button.disabled = state.busy;
    button.setAttribute('aria-label', `Ver vagas: ${trip.bairro}, ${dateLabel(trip.dataHoraSaida)} às ${timeLabel(trip.dataHoraSaida)}, viagem ${trip.id}`);
    button.setAttribute('aria-pressed', String(state.selected?.id === trip.id));
    button.addEventListener('click', () => selectTrip(trip));
    row.append(departure, origin, arrival, button);
    $('trip-list').append(row);
  }
}
function renderActions() {
  const available = state.occupancy && state.occupancy.vagasDisponiveis > 0;
  $('booking').hidden = !state.selected || state.exchange;
  $('book').disabled = state.busy || !available;
  $('exchange').hidden = !state.exchange;
  $('confirm-exchange').disabled = state.busy || !available || !state.selected || state.selected.id === state.reservation?.idViagem;
  if (state.exchange) {
    $('exchange-current').textContent = state.reservation.idViagem ? `Atual: ${tripDescription(state.reservation.idViagem)}` : 'Atual: não consultada. Confira o número da reserva antes de confirmar.';
    $('exchange-new').textContent = state.selected ? `Nova: ${tripDescription(state.selected.id)}` : 'Nova: selecione outra viagem na lista.';
  }
}
async function selectTrip(trip, focus = true) {
  if (state.busy) return;
  state.selected = trip;
  renderList();
  $('detail-title').textContent = `${timeLabel(trip.dataHoraSaida)} · ${dateLabel(trip.dataHoraSaida)}`;
  $('trip-summary').textContent = `${trip.bairro} · Chegada prevista às ${timeLabel(trip.dataHoraChegada)} em ${dateLabel(trip.dataHoraChegada)}. Viagem ${trip.id}.`;
  if (focus) { $('detail-title').tabIndex = -1; $('detail-title').focus({ preventScroll: true }); $('detail-title').scrollIntoView({ block: 'nearest', behavior: 'smooth' }); }
  await loadOccupancy();
}
async function loadOccupancy() {
  if (!state.selected) return;
  const version = ++state.occupancyRequest;
  const id = state.selected.id;
  state.occupancy = null;
  $('occupancy').textContent = 'Consultando vagas…';
  $('retry-occupancy').hidden = true;
  renderActions();
  try {
    const data = await request(`/viagens/${id}/ocupacao`);
    if (version !== state.occupancyRequest) return;
    state.occupancy = data;
    const grid = element('div', undefined, 'occupancy-grid');
    for (const [label, value] of [['Capacidade', data.capacidade], ['Ocupados', data.lugaresOcupados], ['Vagas', data.vagasDisponiveis]]) {
      const metric = element('div'); metric.append(element('strong', String(value)), element('span', label)); grid.append(metric);
    }
    $('occupancy').replaceChildren(grid, element('p', data.vagasDisponiveis > 0 ? 'Há lugar disponível.' : 'Viagem lotada. Escolha outra saída.', 'availability'));
  } catch (error) {
    if (version !== state.occupancyRequest) return;
    $('occupancy').textContent = error.message;
    $('retry-occupancy').hidden = false;
  }
  renderActions();
}
async function loadTrips() {
  if (state.busy) return;
  const version = ++state.listRequest;
  const date = $('date').value;
  $('list-status').hidden = false;
  $('list-status').textContent = 'Carregando viagens…';
  $('reload').hidden = true;
  $('trip-list').replaceChildren();
  $('list-caption').textContent = date ? dateLabel(date) : 'Todas as datas';
  try {
    const trips = await request(`/viagens${date ? `?data=${encodeURIComponent(date)}` : ''}`);
    if (version !== state.listRequest) return;
    state.trips = trips;
    $('list-status').textContent = trips.length ? '' : 'Nenhuma viagem encontrada. Escolha outra data ou limpe o filtro.';
    $('list-status').hidden = trips.length > 0;
    renderList();
  } catch (error) {
    if (version !== state.listRequest) return;
    state.trips = [];
    $('list-status').textContent = error.message;
    $('reload').hidden = false;
  }
}
function renderReservation() {
  const reservation = state.reservation;
  $('reservation-summary').replaceChildren();
  $('reservation-summary').append(element('p', `Reserva nº ${reservation.id}`, 'reservation-number'));
  if (reservation.idViagem) $('reservation-summary').append(element('p', tripDescription(reservation.idViagem)));
  $('reservation-summary').append(element('p', reservation.status ? `Status: ${reservation.status}` : 'Viagem atual e status não consultados.', reservation.status ? 'badge' : 'muted'));
  $('reservation-actions').hidden = reservation.status === 'CANCELADA';
  $('exchange-start').disabled = !!reservation.status && reservation.status !== 'CONFIRMADA';
  $('cancel-start').disabled = !!reservation.status && reservation.status !== 'CONFIRMADA';
  renderActions();
}
async function mutation(work) {
  if (state.busy) return;
  state.busy = true;
  const controls = Array.from(document.querySelectorAll('button,input,summary'));
  const disabled = controls.map(control => control.disabled);
  controls.forEach(control => { control.disabled = true; });
  document.body.setAttribute('aria-busy', 'true');
  notice('Enviando solicitação…');
  try { await work(); }
  catch (error) { notice(error.message, true); }
  finally {
    state.busy = false;
    controls.forEach((control, i) => { control.disabled = disabled[i]; });
    document.body.removeAttribute('aria-busy');
    if (state.reservation) renderReservation();
    renderList(); renderActions();
  }
}
$('filters').addEventListener('submit', event => { event.preventDefault(); loadTrips(); });
$('clear').addEventListener('click', () => { $('date').value = ''; loadTrips(); });
$('reload').addEventListener('click', loadTrips);
$('retry-occupancy').addEventListener('click', loadOccupancy);
$('booking').addEventListener('submit', event => {
  event.preventDefault();
  if (!state.selected || !state.occupancy || state.occupancy.vagasDisponiveis <= 0) return;
  let idColaborador;
  try { idColaborador = numericId($('collaborator')); } catch (error) { notice(error.message, true); return; }
  const idViagem = state.selected.id;
  mutation(async () => {
    state.reservation = await request('/reservas', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ idColaborador, idViagem }) });
    state.exchange = false;
    renderReservation();
    notice(`Reserva nº ${state.reservation.id} criada. Guarde este número.`);
    $('reserva').scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    await loadOccupancy();
  });
});
$('manual-reservation').addEventListener('submit', event => {
  event.preventDefault();
  if (state.busy) return;
  try { state.reservation = { id: numericId($('reservation-id')) }; }
  catch (error) { notice(error.message, true); return; }
  state.exchange = false;
  renderReservation();
  notice('Número informado. Nenhuma consulta de reserva foi realizada.');
  $('known-reservation').open = false;
});
$('exchange-start').addEventListener('click', () => {
  if (state.busy) return;
  state.exchange = true;
  renderActions();
  notice(`Selecione outra viagem para a reserva nº ${state.reservation.id}.`);
  $('viagens').scrollIntoView({ behavior: 'smooth' });
});
$('stop-exchange').addEventListener('click', () => { state.exchange = false; renderActions(); notice('Troca não enviada.'); });
$('confirm-exchange').addEventListener('click', () => {
  if (!state.selected || !state.reservation || !state.occupancy || state.occupancy.vagasDisponiveis <= 0) return;
  const idReserva = state.reservation.id;
  const idViagem = state.selected.id;
  mutation(async () => {
    state.reservation = await request(`/reservas/${idReserva}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ idViagem }) });
    state.exchange = false;
    renderReservation(); notice(`Reserva nº ${state.reservation.id} atualizada.`);
    await loadOccupancy();
  });
});
$('cancel-start').addEventListener('click', () => {
  if (state.busy) return;
  $('cancel-description').textContent = `Confirma o cancelamento da reserva nº ${state.reservation.id}?`;
  $('cancel-dialog').showModal();
});
$('cancel-back').addEventListener('click', () => $('cancel-dialog').close());
$('cancel-confirm').addEventListener('click', () => {
  const id = state.reservation.id;
  $('cancel-dialog').close();
  mutation(async () => {
    await request(`/reservas/${id}`, { method: 'DELETE' });
    state.reservation = { ...state.reservation, status: 'CANCELADA' };
    state.exchange = false;
    renderReservation(); notice('Reserva cancelada');
    await loadOccupancy();
  });
});
loadTrips();
