const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const source = fs.readFileSync('src/main/resources/static/app.js', 'utf8');
class Element {
  constructor() { this.children = []; this.events = {}; this.value = ''; this.hidden = false; this.disabled = false; this.textContent = ''; }
  append(...items) { this.children.push(...items); }
  replaceChildren(...items) { this.children = items; this.textContent = ''; }
  setAttribute() {} removeAttribute() {} focus() {} scrollIntoView() {}
  addEventListener(type, callback) { this.events[type] = callback; }
  showModal() { this.open = true; } close() { this.open = false; }
}
const trip = id => ({ id, bairro: 'Mooca', dataHoraSaida: '2025-11-03T06:30:00', dataHoraChegada: '2025-11-03T07:11:00' });
const occupancy = { capacidade: 15, lugaresOcupados: 4, vagasDisponiveis: 11 };
const settle = () => new Promise(resolve => setImmediate(resolve));
async function setup(handler) {
  const nodes = new Map(); const calls = [];
  const get = id => { if (!nodes.has(id)) nodes.set(id, new Element()); return nodes.get(id); };
  const sandbox = { document: { getElementById: get, createElement: () => new Element(), querySelectorAll: () => [...nodes.values()], body: new Element() }, fetch: async (path, options = {}) => {
    calls.push({ path, ...options });
    const reply = await handler(path, options);
    return { status: reply.status ?? 200, ok: (reply.status ?? 200) < 400, text: async () => reply.status === 204 ? '' : JSON.stringify(reply.body) };
  } };
  vm.createContext(sandbox); vm.runInContext(source, sandbox); await settle();
  return { get, calls, run: expression => vm.runInContext(expression, sandbox), fire: async (id, event = 'click') => { get(id).events[event]({ preventDefault() {} }); await settle(); } };
}
test('carrega sem filtro, filtra data e limpa; lista vazia', async () => {
  const ui = await setup(() => ({ body: [] }));
  assert.equal(ui.calls[0].path, '/viagens');
  assert.match(ui.get('list-status').textContent, /Nenhuma viagem/);
  ui.get('date').value = '2025-11-03'; await ui.fire('filters', 'submit');
  assert.equal(ui.calls.at(-1).path, '/viagens?data=2025-11-03');
  await ui.fire('clear'); assert.equal(ui.calls.at(-1).path, '/viagens');
});
test('seleção consulta ocupação; lotação impede reserva', async () => {
  const ui = await setup(path => ({ body: path === '/viagens' ? [trip(1)] : { ...occupancy, vagasDisponiveis: 0 } }));
  await ui.run('selectTrip(state.trips[0])');
  assert.equal(ui.calls.at(-1).path, '/viagens/1/ocupacao');
  assert.equal(ui.get('book').disabled, true);
});
test('cria, troca com ID da reserva na URL e cancela com 204 sem corpo', async () => {
  const ui = await setup((path, options) => {
    if (path === '/viagens') return { body: [trip(1), trip(2)] };
    if (path.endsWith('/ocupacao')) return { body: occupancy };
    if (options.method === 'DELETE') return { status: 204 };
    return { status: options.method === 'POST' ? 201 : 200, body: { id: 91, idColaborador: 7, idViagem: options.method === 'POST' ? 1 : 2, status: 'CONFIRMADA' } };
  });
  await ui.run('selectTrip(state.trips[0])'); ui.get('collaborator').value = '7';
  await ui.fire('booking', 'submit');
  const post = ui.calls.find(call => call.method === 'POST');
  assert.deepEqual(JSON.parse(post.body), { idColaborador: 7, idViagem: 1 });
  assert.equal(ui.run('state.reservation.id'), 91);
  await ui.fire('exchange-start'); await ui.run('selectTrip(state.trips[1])');
  assert.match(ui.get('exchange-current').textContent, /viagem 1/);
  assert.match(ui.get('exchange-new').textContent, /viagem 2/);
  await ui.fire('confirm-exchange');
  const put = ui.calls.find(call => call.method === 'PUT');
  assert.equal(put.path, '/reservas/91'); assert.deepEqual(JSON.parse(put.body), { idViagem: 2 });
  await ui.fire('cancel-start'); assert.match(ui.get('cancel-description').textContent, /91/);
  await ui.fire('cancel-confirm');
  const del = ui.calls.find(call => call.method === 'DELETE');
  assert.equal(del.path, '/reservas/91'); assert.equal(del.body, undefined);
  assert.equal(ui.get('notice').textContent, 'Reserva cancelada');
  assert.equal(ui.calls.at(-1).path, '/viagens/2/ocupacao');
});
test('reserva manual não inventa consulta ou status', async () => {
  const ui = await setup(() => ({ body: [] }));
  ui.get('reservation-id').value = '42'; await ui.fire('manual-reservation', 'submit');
  assert.equal(ui.calls.length, 1); assert.equal(ui.run('state.reservation.status'), undefined);
  await ui.fire('exchange-start'); assert.match(ui.get('exchange-current').textContent, /não consultada/);
});
for (const code of [400, 404, 409]) test(`exibe mensagem de negócio HTTP ${code}`, async () => {
  const ui = await setup(() => ({ status: code, body: { mensagem: `Mensagem da API ${code}` } }));
  assert.equal(ui.get('list-status').textContent, `Mensagem da API ${code}`);
});
test('falha de rede permite tentar novamente', async () => {
  const ui = await setup(() => { throw new Error('network'); });
  assert.match(ui.get('list-status').textContent, /Verifique sua conexão/);
  assert.equal(ui.get('reload').hidden, false);
});
test('bloqueia POST duplicado enquanto aguarda resposta', async () => {
  let release;
  const pending = new Promise(resolve => { release = resolve; });
  const ui = await setup((path, options) => options.method === 'POST' ? pending : { body: path === '/viagens' ? [trip(1)] : occupancy });
  await ui.run('selectTrip(state.trips[0])'); ui.get('collaborator').value = '7';
  await ui.fire('booking', 'submit'); await ui.fire('booking', 'submit');
  assert.equal(ui.calls.filter(call => call.method === 'POST').length, 1);
  release({ status: 201, body: { id: 1, idViagem: 1, status: 'CONFIRMADA' } }); await settle();
});
test('resposta atrasada de ocupação não substitui seleção nova', async () => {
  let release;
  const ui = await setup(path => path === '/viagens' ? { body: [trip(1), trip(2)] } : path.includes('/1/') ? new Promise(resolve => { release = resolve; }) : { body: occupancy });
  const first = ui.run('selectTrip(state.trips[0])'); await ui.run('selectTrip(state.trips[1])');
  release({ body: { ...occupancy, vagasDisponiveis: 0 } }); await first;
  assert.equal(ui.run('state.selected.id'), 2); assert.equal(ui.get('book').disabled, false);
});
