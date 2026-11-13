# AGENTS.md

## Contexto do projeto

MoveFlex é a solução de fretamento corporativo compartilhado da equipe para a Global Solution CitizenTech 2026 da FIAP. O piloto proposto atende o polo da Faria Lima, em São Paulo.

O problema central é a concentração de carros nos horários de entrada e saída do trabalho. A proposta reúne funcionários de empresas do mesmo polo em vans compartilhadas, operadas por fretadoras parceiras.

A aplicação Java implementa a consulta de viagens e o ciclo de vida das reservas. A ocupação informa quantos lugares estão em uso. Para estimar quantos carros deixaram de circular, o projeto também precisaria saber como cada passageiro se deslocava antes.

## Antes de implementar

- Leia o README, o `pom.xml` e o código relacionado à tarefa.
- Confira as rotas nos controllers, os campos dos modelos e as respostas de erro.
- Preserve as alterações existentes e os padrões utilizados em aula.
- Faça mudanças pequenas que o aluno consiga verificar e explicar.
- Restrinja a implementação ao pedido atual. Registre separadamente sugestões de evolução.
- Priorize recursos já disponíveis no projeto antes de adicionar dependências.

## Stack e arquitetura

- Java 21, Spring Boot, Maven Wrapper e Oracle da FIAP.
- Fluxo principal: Controller → BO → DAO → Oracle.
- `model` representa os dados do domínio. DTOs e records podem representar entradas e saídas específicas.
- `controller` recebe as requisições e define as respostas HTTP.
- `bo` coordena as operações e aplica as regras de negócio.
- `dao` concentra o SQL, executa consultas e alterações e mapeia os resultados.
- `exception` concentra o tratamento de exceções da aplicação.
- `config` contém a configuração de conexão com o Oracle.

Siga o padrão JDBC existente: `ConexaoOracle.getConnection()`, `PreparedStatement` com parâmetros e `try-with-resources`. Quem abre uma conexão é responsável por fechá-la. Preserve o driver Oracle e as versões definidos no `pom.xml`.

## API implementada

| Operação | Endpoint | Entrada | Sucesso |
|---|---|---|---|
| Listar viagens | `GET /viagens` | Filtro opcional `data` em `yyyy-MM-dd` | `200` com lista, inclusive `[]` |
| Consultar ocupação | `GET /viagens/{idViagem}/ocupacao` | ID da viagem na URL | `200` com capacidade, lugares ocupados e vagas disponíveis |
| Criar reserva | `POST /reservas` | JSON com `idColaborador` e `idViagem` | `201` com a reserva criada |
| Trocar de viagem | `PUT /reservas/{idReserva}` | ID da reserva na URL e JSON com o novo `idViagem` | `200` com a reserva |
| Cancelar reserva | `DELETE /reservas/{idReserva}` | ID da reserva na URL, sem corpo | `204` sem corpo |

No PUT, o colaborador é identificado a partir da reserva existente. A resposta do POST fornece o `id` usado para trocar ou cancelar essa reserva.

## Regras de negócio

- A API valida os IDs recebidos e a existência dos registros necessários.
- A disponibilidade vem do Oracle. Reservas canceladas ficam fora da contagem de lugares ocupados.
- O banco admite um registro por par de colaborador e viagem, inclusive quando esse registro já foi cancelado.
- Para viagens diferentes, a API impede outra reserva do mesmo colaborador no mesmo dia e sentido. Reservas canceladas ficam fora dessa verificação.
- A troca se aplica a reservas confirmadas. A verificação de conflito ignora a própria reserva em alteração.
- O cancelamento muda o status de `CONFIRMADA` para `CANCELADA`, preserva o histórico e libera a vaga. Repetir o cancelamento de uma reserva já cancelada retorna `204`.
- A API verifica a disponibilidade antes da gravação. A proteção contra duas solicitações simultâneas para a última vaga está pendente no protótipo.

Os nomes de tabelas e relacionamentos devem acompanhar o DER da equipe. Cadastros adicionais, filtros por destino ou ponto de embarque, consulta de passageiros e histórico por colaborador ficam fora do escopo atual do frontend.

## Frontend do colaborador

- Se o repositório já tiver frontend, siga sua estrutura. Caso contrário, use `src/main/resources/static` para servir HTML, CSS e JavaScript pelo Spring Boot.
- Use caminhos relativos nas chamadas à API para funcionar localmente e, depois, na URL pública.
- Use os campos que os endpoints devolvem. Consulte a ocupação quando o usuário selecionar uma viagem.
- Informe carregamento, lista vazia, sucesso e falha. Aproveite o campo `mensagem` das respostas de erro quando estiver disponível.
- Trate respostas `204` sem tentar ler JSON.
- Evite requisições duplicadas enquanto uma ação estiver em andamento.
- Use português, formulários com rótulos, navegação por teclado, foco visível e layout responsivo.
- A identidade visual usa branco, verde-claro e verde-escuro com contraste legível. Priorize horários, disponibilidade e ações. Evite emojis e slogans.
- O ID da reserva serve para localizar o registro. Informá-lo na tela não comprova a identidade do usuário.

## Oracle e configuração

- Use `DB_URL`, `DB_USER` e `DB_PASSWORD` na configuração do backend.
- Mantenha credenciais, arquivos locais com segredos e dados de conexão fora do Git, do frontend e da imagem de deploy.
- Use dados fictícios ou públicos na demonstração.
- Preserve o esquema Oracle. Mudanças em tabelas e cargas de dados dependem de uma tarefa específica.

## Testes e validação

- Execute os testes e o build pertinentes à mudança usando o Maven Wrapper.
- Para o frontend, confira consulta de viagens, filtro por data, ocupação, criação, troca e cancelamento.
- Verifique os estados de erro relevantes: `400` para entrada inválida, `404` para recurso inexistente e `409` para conflito de negócio.
- Execute operações de escrita no Oracle apenas com registros destinados a teste.
- Informe quais verificações foram executadas e quais ficaram pendentes. Compilação, teste de interface e teste com Oracle são verificações distintas.

## Documentação e entrega

- Mantenha o README coerente com a implementação: objetivo, tecnologias, versões, configuração, execução e exemplos de requisição.
- O PDF da entrega deve conter capa com nomes e RMs, índice, descrição, justificativa, objetivos, diagrama de classes, protótipos de tela, tabela de endpoints, DER, instruções de execução e links do GitHub e do deploy.
- Use nomes, links, capturas e resultados reais. Identifique os itens pendentes.
- O aluno registra manualmente em `prompts.md` os prompts que utilizou. Deixe esse arquivo sob edição do aluno.

## Deploy

O destino previsto é o Render. A publicação aguarda orientação do professor. A tarefa atual de frontend termina com a validação local.

Quando o deploy for solicitado, respeite a variável `PORT`, configure as credenciais no ambiente do servidor e valide pela URL pública uma rota que consulte o Oracle antes de documentar a publicação como concluída.