# MoveFlex

## Ficha da CitizenTech

| Campo | Descrição |
|---|---|
| Nome da solução | MoveFlex |
| Projeto | Global Solution 2026 · Engenharia de Software · FIAP |
| Cidadão beneficiado | Trabalhador que precisa chegar ao escritório em horário definido e busca uma opção de transporte nos dias presenciais. |
| Problema | Funcionários de empresas vizinhas fazem trajetos parecidos, mas cada empresa, sozinha, pode ter poucos interessados para viabilizar um fretado. |
| Como funciona | A plataforma reúne a demanda de várias empresas em uma mesma viagem. O funcionário escolhe o dia, o horário e o ponto de embarque. Uma operadora parceira realiza o transporte. |
| Partes da solução | O backend Java gerencia viagens e reservas, o Oracle guarda os dados, os módulos em Python apoiam o planejamento das viagens e a análise de atrasos, e o projeto de redes representa a base de operação. |
| Indicador de impacto | Percentual de passageiros que chegam ao trabalho até o horário de entrada, comparado ao período anterior ao piloto. |
| ODS relacionado | ODS 11 — Cidades e Comunidades Sustentáveis. A proposta busca ampliar as opções de transporte coletivo no deslocamento ao trabalho. |

## Como o MoveFlex funciona

Imagine que três empresas da Faria Lima tenham, cada uma, quatro funcionários que saem da mesma região e precisam chegar ao escritório às 9h de quinta-feira. Ao reunir esses grupos, o MoveFlex permite planejar uma única viagem para os doze passageiros. Na sexta-feira, quem trabalha de casa deixa de reservar, e a operadora pode planejar a saída com base na demanda daquele dia.

O funcionário informa seu bairro, consulta os pontos de embarque disponíveis e reserva um lugar. A empresa acompanha a adesão de sua equipe. A operadora parceira disponibiliza o veículo e realiza o trajeto. Os números desse exemplo são fictícios e ilustram o funcionamento proposto.

## A reserva de uma viagem

Uma **linha** representa o trajeto entre os pontos de embarque e o destino. Uma **viagem** é uma saída dessa linha em uma data e um horário específicos. Essa diferença permite que a saída de quinta-feira esteja lotada enquanto a de sexta ainda tem vagas.

O fluxo previsto para o protótipo é o seguinte:

1. O funcionário consulta as viagens do seu dia presencial e seleciona um ponto de embarque.
2. Ao receber o pedido, a API verifica se a viagem ainda aceita reservas, se há vaga e se esse funcionário já possui uma reserva ativa para a mesma saída.
3. Se houver disponibilidade, a API confirma a reserva e atualiza a ocupação. Funcionários de empresas diferentes podem reservar a mesma viagem.
4. Se duas pessoas solicitarem a última vaga ao mesmo tempo, apenas uma reserva deve ser confirmada.
5. Se o funcionário cancelar, o sistema registra o cancelamento e libera a vaga para outra pessoa.

## Backend Java

O backend proposto é uma API REST em **Java com Spring Boot**, com persistência no **Oracle**.

| Camada | Responsabilidade no MoveFlex |
|---|---|
| Model | Representa conceitos do projeto, como empresa, funcionário, veículo, linha, viagem e reserva. |
| Controller | Recebe a requisição HTTP e devolve uma resposta, como a confirmação da reserva ou um aviso de viagem lotada. |
| BO | Aplica as regras de negócio. Na reserva, verifica a disponibilidade e coordena a operação para que a última vaga seja ocupada uma única vez. |
| DAO | Consulta e grava os dados no Oracle usando JDBC e `PreparedStatement`. |
| Tratamento de exceções | Converte situações como viagem inexistente, dados inválidos e reserva duplicada em respostas HTTP adequadas. |

## Informações da entrega

| Item | Informação |

| Integrantes | Clara Barboza Costa, Pedro Henrique Lamin Rodrigues, Tomás Braga Cantuária |

## Listagem de viagens no Oracle

`GET /viagens` segue o fluxo `ViagemController -> ViagemBO -> ViagemDAO -> ConexaoOracle.getConnection() -> Oracle`. O DAO usa JDBC, `PreparedStatement.executeQuery()` e try-with-resources para fechar conexão, statement e resultado. Não executa DDL nem carga de dados.

O contrato mantém apenas `id`, `dataHoraSaida`, `dataHoraChegada` e `bairro`:

| Campo JSON | Origem no banco do grupo |
|---|---|
| `id` | `VIAGEM.ID_VIAGEM` (`NUMBER(9)`) |
| `dataHoraSaida` | Data de `DATA_VIAGEM` (`DATE`) combinada com `HORARIO_PARTIDA` (`VARCHAR2(5)`, `HH:MM`) |
| `dataHoraChegada` | `CHEGADA_PREVISTA` (`TIMESTAMP`), sem substituir por `CHEGADA_REAL` |
| `bairro` | `REGIAO.NOME`, via `VIAGEM.ID_LINHA -> LINHA.ID_PONTO_ORIGEM -> PONTO_EMBARQUE.ID_REGIAO` |

O bairro é o da origem da linha inclusive nas viagens de volta, conforme o guia. Sem o parâmetro `data`, a consulta lista todas as viagens, ordenadas por data, hora e ID. `GET /viagens?data=2025-11-03` restringe a listagem ao dia informado, mantendo os campos, joins e ordenação. O DAO usa `WHERE v.DATA_VIAGEM >= ? AND v.DATA_VIAGEM < ?`, com início do dia e início do dia seguinte vinculados por `PreparedStatement.setDate`. Assim, inclui também valores Oracle DATE que contenham horário. Uma data inválida (inclusive vazia ou inexistente no calendário) retorna HTTP 400; use o formato `yyyy-MM-dd`. Uma data válida sem viagens retorna HTTP 200 com `[]`. Sem registros, retorna HTTP 200 com `[]`. Falhas de conexão, consulta ou conversão do horário retornam HTTP 500 com `{"status":500,"mensagem":"Falha ao consultar as viagens."}`, sem detalhes internos.

Os nomes, tipos e relacionamentos utilizados conferem com `banco/01_ddl.sql` do arquivo `gs_banco.zip`. Há uma diferença de validação: o guia descreve horários `HH:MM`, mas a expressão `^[0-2][0-9]:[0-5][0-9]$` de `CK_VIAGEM_HORARIO` também permite 24:00 a 29:59. A API rejeita esses valores como falha de consulta, sem convertê-los em outra data. O SQL foi apenas consultado.

Tecnologias: Java 21, Spring Boot 4.1.2, Maven Wrapper 3.3.4 com Maven 3.10.0 e driver `com.oracle.database.jdbc:ojdbc11` na versão gerenciada pelo Spring Boot. A conexão continua usando a configuração existente e o arquivo local opcional; não versionar credenciais.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\mvnw.cmd verify
# Testes de integração explícitos: somente SELECT, usando a configuração Oracle local.
.\mvnw.cmd '-Dtest=ConexaoOracleIT,ViagemOracleIT' test
curl.exe http://localhost:8080/viagens
```

Se o Maven resolver o cache como `C:\.m2\repository`, informar `-Dmaven.repo.local=C:\Users\PEDRO\.m2\repository` ao Wrapper neste ambiente. O Wrapper foi ajustado para aceitar a ausência da propriedade `Target` no PowerShell.

Os testes `ViagemListagemTests` usam JDBC simulado para verificar contrato HTTP, lista vazia, falhas e fechamento dos recursos; não comprovam compatibilidade Oracle. `ViagemOracleIT` inicia a API em porta aleatória e chama `GET /viagens` por HTTP com o DAO real. Testes com sufixo `IT` só executam quando selecionados explicitamente. A validação pública no Render permanece pendente.

Validação realizada em 09/11/2026: `mvnw.cmd verify` concluiu com BUILD SUCCESS no JDK 21.0.6 (seis testes locais, sem falhas). O teste explícito `ConexaoOracleIT` passou com `SELECT 1 FROM DUAL`. O GET real foi chamado por HTTP, mas retornou 500; a execução direta do DAO identificou código Oracle 942 (`ORA-00942`, SQLState `42000`: tabela/view inexistente ou sem acesso). Portanto, a listagem com dados reais ainda não foi validada com sucesso. É necessário disponibilizar ao usuário configurado as tabelas `VIAGEM`, `LINHA`, `PONTO_EMBARQUE` e `REGIAO` do SQL do grupo e repetir os testes de integração. Não foram alterados schema, dados ou credenciais.

Validação do filtro: os testes locais cobrem listagem com e sem data, vínculo dos parâmetros JDBC antes da consulta, preservação dos joins e ordenação, lista vazia e datas inválidas sem acessar o DAO. A consulta filtrada no Oracle real e a validação pública no Render permanecem pendentes; nenhum teste com sufixo IT foi executado nesta alteração.
