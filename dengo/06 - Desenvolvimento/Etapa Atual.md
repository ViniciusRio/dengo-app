# Etapa Atual

## Etapa 6 — Histórico compartilhado
Status: 🟢 Concluída e aprovada no dispositivo

## Escopo implementado
- timeline somente de leitura, igual nas duas perspectivas;
- seis `HistoryEvent` atuais em linguagem humana;
- contexto de pedidos resolvido por ID, com fallback genérico;
- ordem por horário decrescente e inclusão mais recente em caso de empate;
- grupos Hoje, Ontem e data no fuso local;
- testes unitários e integração à aba Histórico;
- estado compartilhado em memória, sem persistência.

## Fora do escopo
`SEEN`, conclusão de pedidos, novos eventos, filtros, busca, edição, exclusão, métricas, Mural funcional, backend, persistência e sincronização.

## Validação
A timeline preenchida e a apresentação compartilhada foram validadas no Samsung. O estado vazio foi aprovado após reduzir o espaço entre o título e o bloco do coração. Próxima etapa ainda não definida.
