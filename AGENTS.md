# AGENTS.md

## Contexto do projeto

MoveFlex é a solução de fretamento corporativo compartilhado da equipe
para a Global Solution CitizenTech 2026 da FIAP, com piloto proposto
no polo da Faria Lima, em São Paulo.

Nesta entrega de Java, funcionários de empresas diferentes podem
reservar lugares na mesma viagem. A plataforma organiza os cadastros,
consulta a disponibilidade e registra reservas e cancelamentos.
As operadoras parceiras executam as viagens.

## Antes de implementar

- Leia o README, o pom.xml e o código existente.
- Preserve funcionalidades e alterações existentes.
- Identifique os padrões utilizados nos exemplos de aula, quando disponíveis.
- Siga os padrões do professor antes de adotar uma arquitetura diferente.
- Se faltar uma informação que realmente impeça a implementação,
  explique a pendência. Não invente credenciais ou requisitos.
- Evite dependências e funcionalidades sem necessidade.

## Requisitos obrigatórios da avaliação

- Java com Spring Boot.
- API RESTful.
- Persistência no servidor Oracle da FIAP.
- Classes de modelo coerentes com o banco de dados.
- Camadas DAO e BO explícitas.
- Controllers com operações GET, POST, PUT e DELETE.
- Tratamento centralizado de exceções.
- Publicar a API no Render e incluir a URL real na documentação.
  Validar o deploy chamando, pela URL pública, um endpoint que consulte
  o Oracle da FIAP. Se essa validação estiver pendente, registrá-la.
- Código e documentação coerentes entre si.

Não substituir DAO e BO apenas por classes chamadas Repository
e Service sem verificar os padrões exigidos pelo professor.

Utilize a versão de Java definida no projeto e compatível com
as orientações de aula. Documente as versões utilizadas.

## Arquitetura

Fluxo principal:

Controller -> BO -> DAO -> Oracle

Responsabilidades:

- model: entidades do domínio.
- dto: objetos de entrada e saída da API, quando necessários.
- controller: endpoints, validação de entrada e respostas HTTP.
- bo: regras de negócio e coordenação das operações.
- dao: acesso ao banco e operações de persistência.
- exception: exceções específicas e tratamento centralizado.
- config: configurações necessárias à aplicação.

Não colocar SQL nos controllers.
Não colocar regras de negócio nos controllers.
Não concentrar toda a aplicação em uma única classe.

- Implementar DAOs concretos com JDBC (`java.sql`) e o driver Oracle
  `com.oracle.database.jdbc:ojdbc11`, seguindo o exemplo da aula.
- Centralizar a abertura da conexão em `ConexaoOracle.getConnection()`,
  usando `DriverManager` e as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD`.
- Manter o SQL nos DAOs. Usar `PreparedStatement` com parâmetros,
  `executeQuery()` para consultas e `executeUpdate()` para alterações.
  Mapear as linhas do `ResultSet` para os objetos do domínio.
- Nos cadastros com CRUD, seguir os nomes da aula: `inserir`, `listar`,
  `atualizar` e `deletar`. Adicionar `buscarPorId` quando necessário.
- Na reserva, o BO coordena a transação. Os DAOs envolvidos recebem
  a mesma `Connection`. Bloquear a linha da viagem antes de verificar
  a disponibilidade e registrar a reserva. Efetuar commit em caso de
  sucesso e rollback em caso de falha.
- Usar try-with-resources para fechar os recursos JDBC. Quem abriu
  a conexão é responsável por fechá-la ao término da operação.

## Domínio

Entidades previstas, ajustáveis ao modelo acordado com o grupo:

- Empresa
- Funcionario
- Operadora
- Veiculo
- PontoEmbarque
- Linha
- Viagem
- Reserva

Distinções importantes:

- Linha representa um trajeto planejado.
- Viagem representa uma execução em determinada data e horário.
- Reserva associa um funcionário a uma viagem.
- Uma viagem pode receber funcionários de várias empresas.
- A capacidade utilizada na reserva deve estar claramente definida.

Mantenha nomes e relacionamentos alinhados com a entrega de Database.
Não crie tabelas incompatíveis com o DER acordado com o grupo.

## Escopo principal

Implementar:

1. Cadastros necessários para operar as viagens.
2. Consulta de viagens por data, destino e ponto de embarque,
   conforme os relacionamentos definidos.
3. Consulta de vagas disponíveis.
4. Reserva de lugar.
5. Cancelamento de reserva.
6. Consulta de passageiros e ocupação por viagem.

Priorize um fluxo completo funcionando antes de criar extras.

Não implementar por padrão:
- Pagamentos.
- Rastreamento GPS em tempo real.
- Emissão real de autorizações de transporte.
- Chatbot.
- Microserviços.
- Integrações externas não solicitadas.

## Regras de negócio

- Impedir reserva acima da capacidade da viagem.
- Impedir reserva ativa duplicada do mesmo funcionário
  na mesma viagem.
- Impedir reserva em viagem cancelada ou já iniciada.
- Validar a existência dos registros relacionados.
- Cancelar uma reserva deve liberar sua vaga.
- Preservar o histórico de reservas canceladas.
- Impedir exclusões que deixem registros relacionados inconsistentes.
- Não confiar em valores enviados pelo cliente para calcular
  ocupação ou quantidade de vagas.
- Permitir compartilhamento de uma viagem entre empresas.

As verificações de capacidade e a gravação da reserva devem ocorrer
de maneira transacional, com proteção contra reservas simultâneas
para a última vaga. Uma simples consulta seguida de insert,
sem controle de concorrência, não é suficiente.

Não inventar regras de cobrança ou prazos de cancelamento.
Quando necessário, documentar a decisão adotada.

## API REST

- Usar nomes de recursos consistentes no plural.
- Implementar GET, POST, PUT e DELETE em recursos adequados.
- Cancelamento pode ser uma alteração de status; não precisa
  apagar fisicamente a reserva.
- Utilizar DTOs quando ajudarem a controlar os dados expostos.
- Validar campos obrigatórios, formatos e valores permitidos.

Utilizar códigos HTTP coerentes, por exemplo:
- 200: consulta ou atualização com resposta.
- 201: criação.
- 204: operação bem-sucedida sem corpo.
- 400: entrada inválida.
- 404: recurso inexistente.
- 409: conflito de regra de negócio.

Padronizar respostas de erro.
Não retornar stack traces, SQL, senhas ou detalhes internos ao cliente.

Não apresentar um filtro por empresa como mecanismo de autorização.
Se houver autenticação, verificar o vínculo e as permissões do usuário.
Se não houver, documentar essa limitação do protótipo.

## Oracle e segurança

- Utilizar variáveis de ambiente para credenciais e conexão.
- Nunca versionar senhas, tokens ou arquivos .env com segredos.
- Fornecer exemplos de configuração apenas com placeholders.
- Usar consultas parametrizadas.
- Garantir fechamento adequado dos recursos de banco.
- Manter scripts SQL compatíveis com Oracle.
- Não executar alterações destrutivas no banco sem autorização.
- Não apagar ou recriar automaticamente tabelas ao iniciar a aplicação.
- Usar dados fictícios ou públicos, identificando sua origem.

## Deploy no Render

Preparar o deploy desde o início do desenvolvimento.

- Respeitar a porta fornecida pela variável PORT.
- Configurar conexão Oracle por variáveis de ambiente.
- Documentar build, inicialização e variáveis necessárias.
- Criar Dockerfile se for necessário para a estratégia de deploy.
- Não incluir credenciais na imagem.
- Disponibilizar uma verificação simples de saúde da aplicação.
- Distinguir aplicação iniciada de conexão com banco validada.
- Não afirmar que o deploy funciona sem testar a URL publicada.

Se o ambiente não permitir validar Render ou Oracle,
informar exatamente o que ficou pendente.

## Testes e validação

Priorizar testes para:

- Reserva válida.
- Viagem lotada.
- Reserva duplicada.
- Cancelamento e liberação de vaga.
- Recurso inexistente.
- Dados inválidos.
- Reservas concorrentes para a última vaga.

Executar os testes e o build disponíveis antes de concluir mudanças.

Fornecer exemplos de requisições ou coleção do Postman
para demonstrar o fluxo completo.

Não afirmar que testes passaram se não foram executados.
Não usar um banco alternativo como prova de compatibilidade com Oracle.

## Documentação

Manter o README com:

- Ficha da CitizenTech no início.
- Descrição, justificativa e objetivos.
- Escopo implementado.
- Tecnologias e versões.
- Configuração e execução local.
- Variáveis de ambiente.
- Instruções de deploy.
- Exemplos de uso.
- Limitações conhecidas.

Preparar conteúdo coerente para o PDF único exigido:

- Capa com projeto, nomes completos e RMs.
- Índice.
- Descrição, justificativa e objetivos.
- Diagrama de classes refletindo o código.
- Protótipos das telas.
- Tabela de endpoints GET, POST, PUT e DELETE.
- Diagrama Entidade-Relacionamento.
- Ferramentas e versões.
- Links reais do GitHub e do deploy.
- Passo a passo para executar.

Não inventar nomes, RMs, links ou resultados de testes.
Sinalizar informações ainda não fornecidas.

## Registro de prompts

O registro em prompts.md será feito manualmente pelo aluno.

- Não criar nem alterar prompts.md por enquanto.
- Não inventar ou reconstruir prompts como se fossem registros reais.
- Não afirmar que esse requisito da entrega já foi cumprido.

## Forma de trabalhar

- Implementar em etapas pequenas e verificáveis.
- Priorizar clareza e código que o aluno consiga explicar.
- Usar orientação a objetos com propósito.
- Não criar herança apenas para aumentar a quantidade de classes.
- Explicar decisões relevantes e regras de negócio.
- Atualizar a documentação quando o comportamento mudar.
- Ao terminar, informar o que foi implementado, como foi validado
  e quais pendências permanecem.