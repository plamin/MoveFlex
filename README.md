# MoveFlex

## Ficha da CitizenTech

| Campo | Descrição |
|---|---|
| Nome da solução | MoveFlex |
| Projeto | Global Solution 2026 · Engenharia de Software · FIAP |
| Cidadão beneficiado | Trabalhador que precisa chegar ao escritório em horário definido e busca uma opção de transporte nos dias presenciais. |
| Problema | O trânsito dificulta a chegada ao trabalho no polo da Faria Lima. Funcionários de empresas vizinhas fazem trajetos parecidos, mas uma empresa sozinha pode ter poucos interessados para viabilizar um fretado. |
| Como funciona | A plataforma reúne a demanda de várias empresas em uma mesma viagem. O funcionário consulta as saídas por data, verifica as vagas e reserva um lugar. Uma operadora parceira realiza o transporte. |
| Partes da solução | O backend Java gerencia viagens e reservas, o Oracle guarda os dados, os módulos em Python apoiam o planejamento das viagens e a análise de atrasos, e o projeto de redes representa a base de operação. |
| Indicador de impacto | Percentual de passageiros que chegam ao trabalho até o horário de entrada, comparado ao período anterior ao piloto. |
| ODS relacionado | ODS 11 — Cidades e Comunidades Sustentáveis. A proposta busca ampliar as opções de transporte coletivo no deslocamento ao trabalho. |

**[Abrir o MoveFlex no Render](https://moveflex.onrender.com/)**

## Como o MoveFlex funciona

Imagine que três empresas da Faria Lima tenham, cada uma, quatro funcionários que saem da mesma região e precisam chegar ao escritório às 9h de quinta-feira. Ao reunir esses grupos, o MoveFlex permite planejar uma única viagem para os doze passageiros. Na sexta-feira, quem trabalha de casa deixa de reservar, e a operadora pode planejar a saída com base na demanda daquele dia.

Uma linha define o trajeto. Uma viagem é uma saída dessa linha em determinada data e horário, com sua própria ocupação. O exemplo acima ilustra o piloto proposto para a Faria Lima, em São Paulo.

## Interface do colaborador

1. Ao abrir, a tela chama `GET /viagens` sem filtro. Selecione uma data e clique em **Filtrar**; **Limpar filtro** volta a todas as datas.
2. Clique em **Ver vagas**. A tela chama `GET /viagens/{idViagem}/ocupacao` e exibe capacidade, ocupados e vagas. Uma viagem lotada fica com a reserva desabilitada; falhas permitem repetir a consulta.
3. Com um colaborador e viagens destinados a teste, informe o ID do colaborador e clique em **Reservar lugar**. O POST envia `idColaborador` e `idViagem`. Guarde o número da reserva apresentado com a viagem e o status retornados.
4. Clique em **Trocar viagem**, escolha outra saída e confira a viagem atual e a nova. **Confirmar troca** envia `PUT /reservas/{idReserva}` com apenas `idViagem` no corpo. O resultado substitui os dados exibidos.
5. Clique em **Cancelar reserva** e confira o número no diálogo antes de confirmar. O DELETE não tem corpo; HTTP 204 mostra **Reserva cancelada**. A ocupação visível é consultada novamente após criação, troca e cancelamento.
6. Para uma reserva anterior, abra **Já tenho o número da reserva** e informe o número. Não há GET de reserva na API: a interface não simula uma consulta, nem informa status ou viagem atual desconhecidos. Permite enviar troca ou cancelamento; a API valida a operação.

## API REST

| Método | Recurso | Entrada | Sucesso |
|---|---|---|---|
| GET | `/viagens` | Filtro opcional `?data=2025-11-03` | 200 com lista de viagens |
| GET | `/viagens/{idViagem}/ocupacao` | ID da viagem na URL | 200 com capacidade, lugares ocupados e vagas disponíveis |
| POST | `/reservas` | JSON `{"idColaborador": 6, "idViagem": 12}` | 201 com a reserva |
| PUT | `/reservas/{idReserva}` | JSON `{"idViagem": 15}` com a viagem de destino | 200 com a reserva atualizada |
| DELETE | `/reservas/{idReserva}` | ID da reserva na URL | 204, sem corpo |

Os IDs são ilustrativos. A listagem devolve `id`, `dataHoraSaida`, `dataHoraChegada` e `bairro`. O bairro identifica a região da linha, inclusive no sentido de volta. Uma data inválida retorna 400. Uma data válida sem viagens retorna 200 com `[]`.

## Implementação e limites do protótipo

O Spring Boot 4.1.2 serve a API e o frontend em HTML, CSS e JavaScript. Os arquivos da interface ficam em `src/main/resources/static`. O backend usa Java 21, JDBC e o driver Oracle `ojdbc17`.

| Camada | Responsabilidade |
|---|---|
| Controller | Recebe as requisições de viagens e reservas. |
| BO | Verifica disponibilidade, colaborador, duplicidade e conflitos de dia e sentido. Coordena a criação, a troca e o cancelamento de reservas. |
| DAO | Executa consultas e alterações no Oracle com JDBC e parâmetros em `PreparedStatement`. |
| Model | Representa `Viagem`, `OcupacaoViagem` e `Reserva`. |

A consulta de vagas ocorre antes da gravação. Duas solicitações simultâneas podem disputar a última vaga. O protótipo também funciona sem autenticação. Quem conhece um número de reserva pode solicitar sua troca ou cancelamento.

## Execução local no Windows

Use PowerShell, Git e JDK 21. A conta Oracle precisa ter acesso às tabelas do projeto e permissão de escrita para operar reservas.

```powershell
git clone https://github.com/plamin/MoveFlex.git
cd MoveFlex
$env:DB_URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
$env:DB_USER = "SEU_USUARIO"
$env:DB_PASSWORD = "SUA_SENHA"
.\mvnw.cmd spring-boot:run
```

Substitua os valores de exemplo na sua sessão local. Abra [localhost:8080](http://localhost:8080/) e selecione **Ver vagas**. Use Ctrl+C no terminal para encerrar.

## Deploy no Render

O frontend e a API compartilham um Web Service. Para reproduzir o deploy, conecte o repositório, selecione a branch `main`, escolha Docker e use o `Dockerfile` da raiz. Configure `DB_URL`, `DB_USER` e `DB_PASSWORD` no painel do serviço. A aplicação lê a porta de `PORT`. Para conferir a integração publicada, abra o site, escolha uma viagem e consulte a ocupação.

## Testes e validação

No teste local com o Oracle, a consulta da viagem 12 retornou capacidade de 15 lugares, 7 ocupados e 8 disponíveis. Esses valores registram o momento do teste e variam conforme as reservas.

Execute na raiz do repositório:

```powershell
# Build e testes locais do backend
.\mvnw.cmd verify

# Testes da interface com Node.js
node --check src/main/resources/static/app.js
node --test src/test/frontend/app.test.cjs

# Integração com o Oracle configurado na sessão
.\mvnw.cmd '-Dtest=ConexaoOracleIT,ViagemOracleIT' test
```

## Integrantes

| Projeto | Integrantes |
|---|---|
| MoveFlex · FIAP | Clara Barboza Costa, Pedro Henrique Lamin Rodrigues e Tomás Braga Cantuária |
