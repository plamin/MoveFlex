Estou desenvolvendo o MoveFlex para a Global Solution da FIAP. Quero concluir a integração do endpoint GET /viagens com o Oracle, seguindo o padrão DAO e BO da aula.

Leia o AGENTS.md, o pom.xml, o guia do banco e o SQL de criação das tabelas. Inspecione também Viagem.java, ViagemController.java, ViagemBO.java, ConexaoOracle.java e ViagemDAO.java, se já existir.

1. Confira como o GET /viagens funciona hoje. Se alguma parte da integração já estiver pronta, revise e complete só o que faltar.
2. Implemente a consulta em ViagemDAO usando JDBC, a classe ConexaoOracle existente e try-with-resources. Confira no SQL do grupo os nomes das tabelas, colunas, tipos e relacionamentos.
3. Preserve a rota e os campos atuais da resposta. Verifique no guia como montar a data e hora de saída, obter a chegada prevista e identificar o bairro de origem. Se houver divergência entre o guia e o SQL, indique exatamente qual é antes de assumir um mapeamento.
4. Faça o ViagemBO chamar o DAO. A consulta deve devolver uma lista vazia quando houver zero viagens. Uma falha de conexão ou de consulta deve seguir o tratamento de exceções do projeto.
5. Limite a mudança à listagem de viagens. Use os scripts do banco apenas como referência. Não execute DDL nem carga de dados e não altere as credenciais.
6. Compile com o Maven Wrapper e rode os testes aplicáveis. Ao final, diga quais arquivos mudou, explique o caminho da requisição do controller até o Oracle e informe separadamente se compilou e se o GET foi testado com o banco real.

O registro em prompts.md será feito por mim. Deixe esse arquivo sob meu controle e deixe as alterações para eu revisar antes do commit.