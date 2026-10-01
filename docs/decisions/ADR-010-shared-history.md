# ADR-010 — Histórico compartilhado em memória

## Status

Aceito após aprovação funcional e visual da Etapa 6 no dispositivo.

## Decisão

A aba Histórico apresenta uma linha do tempo somente de leitura, com os mesmos acontecimentos nas perspectivas de Lidianne e Vinícius. Os textos usam nomes absolutos. São apresentados apenas os seis tipos de `HistoryEvent` existentes: pedido criado, aceito ou recusado; mood alterado; espaço pessoal ativado ou encerrado. Cada mudança de mood é um acontecimento independente. Não há `SEEN` nem conclusão de pedido nesta etapa.

Um `HistoryViewModel` observa o mesmo `FakeCoupleRepository` das Homes e projeta o `CoupleState` em grupos por dia. Eventos de pedido procuram seu `CareRequest` pelo ID apenas para obter tipo e texto de `OTHER`; o status atual não reinterpreta o acontecimento. Se o pedido não existir, o evento continua visível com texto humano genérico, sem ID técnico.

A ordenação é por `occurredAt` decrescente e, em empate, pelo índice original da lista `history` decrescente. As datas são agrupadas no fuso local e apresentadas como Hoje, Ontem ou `dd/MM/yyyy`; cada item mostra `HH:mm`. A UI usa os tokens existentes e recursos de texto, sem colocar linguagem de apresentação nos models.

## Consequências

O Histórico não tem persistência: seus eventos reiniciam com o processo, como o restante do estado em memória. A tela não modifica pedidos nem registra eventos ao ser consultada. A timeline preenchida e a apresentação compartilhada foram validadas no Samsung; o estado vazio foi aprovado após o ajuste de espaçamento antes do coração.
