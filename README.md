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
