# PROMPT 1

Estou desenvolvendo o MoveFlex para a Global Solution da FIAP. Quero concluir a integração do endpoint GET /viagens com o Oracle, seguindo o padrão DAO e BO da aula.

Leia o AGENTS.md, o pom.xml, o guia do banco e o SQL de criação das tabelas. Inspecione também Viagem.java, ViagemController.java, ViagemBO.java, ConexaoOracle.java e ViagemDAO.java, se já existir.

1. Confira como o GET /viagens funciona hoje. Se alguma parte da integração já estiver pronta, revise e complete só o que faltar.
2. Implemente a consulta em ViagemDAO usando JDBC, a classe ConexaoOracle existente e try-with-resources. Confira no SQL do grupo os nomes das tabelas, colunas, tipos e relacionamentos.
3. Preserve a rota e os campos atuais da resposta. Verifique no guia como montar a data e hora de saída, obter a chegada prevista e identificar o bairro de origem. Se houver divergência entre o guia e o SQL, indique exatamente qual é antes de assumir um mapeamento.
4. Faça o ViagemBO chamar o DAO. A consulta deve devolver uma lista vazia quando houver zero viagens. Uma falha de conexão ou de consulta deve seguir o tratamento de exceções do projeto.
5. Limite a mudança à listagem de viagens. Use os scripts do banco apenas como referência. Não execute DDL nem carga de dados e não altere as credenciais.
6. Compile com o Maven Wrapper e rode os testes aplicáveis. Ao final, diga quais arquivos mudou, explique o caminho da requisição do controller até o Oracle e informe separadamente se compilou e se o GET foi testado com o banco real.

# PROMPT 2


Leia o AGENTS.md e o código atual do GET /viagens. Implemente um filtro opcional por data, seguindo o fluxo ViagemController → ViagemBO → ViagemDAO.

GET /viagens deve continuar retornando a listagem atual, com os mesmos campos e a mesma ordenação.
GET /viagens?data=2025-11-03 deve retornar apenas as viagens daquele dia, filtrando por VIAGEM.DATA_VIAGEM no Oracle. Use PreparedStatement com parâmetro e preserve os joins existentes.
Uma data inválida deve retornar HTTP 400. Uma data válida sem viagens deve retornar HTTP 200 com [].

Atualize os testes para cobrir esses casos e execute os que puder sem acesso ao Oracle. Ao terminar, explique o que mudou em cada camada e quais verificações ficaram pendentes. 


# PROMPT 3

volte para a main corrigindo o bug que ta dando

# PROMPT 4

Leia o `AGENTS.md`, o README, o `pom.xml`, os controllers e os modelos do MoveFlex. Implemente diretamente no repositório um frontend funcional para o colaborador consultar viagens e gerenciar uma reserva. Trabalhe com a API Spring Boot que já existe.

## Como quero a interface

Quero abrir a aplicação e já ver as viagens. A tela principal deve ajudar alguém a responder rapidamente a três perguntas: de onde sai, a que horas sai e se tem vaga.

Use branco como fundo, verde-claro nas seleções e verde-escuro no texto e nas ações principais. Pode partir de `#FFFFFF`, `#E7F3E9`, `#C8E4CF` e `#214C38`. Use uma tipografia legível e dê mais destaque ao horário de saída do que aos IDs técnicos.

No desktop, apresente as viagens em linhas alinhadas para facilitar a comparação. Coloque o filtro de data perto da lista e mostre os detalhes da viagem selecionada ao lado ou abaixo. No celular, adapte a disposição para uma coluna, com horários, bairro e ação acessíveis sem rolagem horizontal.

Quero uma interface de transporte sóbria e com personalidade. Evite aparência de landing page genérica: sem emojis, degradês, efeitos de vidro, sombras fortes, títulos gigantes, slogans, métricas inventadas ou excesso de cards. Use rótulos diretos como “Viagens”, “Ver vagas”, “Reservar lugar”, “Trocar viagem” e “Cancelar reserva”.

## Fluxos que precisam funcionar

1. Ao abrir a tela, carregue `GET /viagens` sem filtro para mostrar as viagens disponíveis na base. Permita escolher uma data, enviá-la em `yyyy-MM-dd` e limpar o filtro. Mostre um estado vazio quando a consulta retornar `[]`.

2. Ao selecionar uma viagem, chame `GET /viagens/{idViagem}/ocupacao`. Mostre capacidade, lugares ocupados e vagas disponíveis. Se estiver lotada, indique isso junto à ação de reservar.

3. Para reservar, peça o ID do colaborador e aproveite o ID da viagem selecionada. Envie `POST /reservas` com `idColaborador` e `idViagem`. Ao receber `201`, mostre claramente o número da reserva, a viagem e o status. Deixe as ações de troca e cancelamento acessíveis nessa confirmação.

4. Para trocar, permita selecionar outra viagem da lista. Mostre a viagem atual e a nova antes de confirmar. Envie `PUT /reservas/{idReserva}` com `{"idViagem": ...}`. Use na URL o ID da reserva e no corpo o ID da viagem de destino. Atualize a tela com a resposta da API.

5. Para cancelar, peça confirmação indicando o número da reserva. Envie `DELETE /reservas/{idReserva}` sem corpo. Trate `204 No Content` como sucesso e mostre “Reserva cancelada”. Atualize a ocupação das viagens afetadas quando ela estiver visível.

6. Se o usuário já souber o número da reserva, ofereça uma forma de informá-lo para trocar ou cancelar. Confira antes quais rotas existem. Se a API não oferecer consulta da reserva por ID, peça apenas os dados necessários para a ação, sem simular uma busca nem inventar o status ou a viagem atual.

## Implementação e verificação

- Aproveite o frontend existente, se houver. Caso contrário, crie `index.html`, CSS e JavaScript em `src/main/resources/static`, acessíveis pela raiz da aplicação com o mesmo comando que inicia o Spring Boot.
- Faça chamadas reais com `fetch` e caminhos relativos. O navegador conversa com a API; as credenciais do Oracle ficam no backend.
- Apresente as mensagens de negócio devolvidas pela API. Trate também falha de rede e bloqueie cliques repetidos durante o envio.
- Use labels visíveis, foco de teclado, botões com nomes claros e mensagens que não dependam apenas da cor.
- Preserve o comportamento do backend e as alterações locais existentes. Se encontrar uma limitação da API, explique o impacto antes de ampliar o escopo.
- Valide o que estiver disponível. Se conseguir abrir a interface, confira desktop e celular. Para testes que gravem no Oracle, use apenas dados identificados para teste. Registre o que exigir validação manual no meu ambiente.
- Ao terminar, informe os arquivos modificados, o comando para iniciar, a URL local e um roteiro curto para eu testar o fluxo completo. Deixe o deploy, o commit e o registro em `prompts.md` comigo.