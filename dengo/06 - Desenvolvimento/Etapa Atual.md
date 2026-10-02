# Etapa Atual

## Etapa 7 — Mural de recados do casal
Status: 🟢 Concluída e aprovada no Samsung

## Escopo implementado
- recados de texto e emojis, com autoria explícita e limite de 160 pontos de código Unicode;
- mesma lista em memória nas duas perspectivas, ordenada por horário e ID decrescentes;
- composição curta em bottom sheet e estado vazio afetivo;
- previews discretos nas duas Homes, abrindo a aba Mural;
- publicação permitida durante espaço pessoal e sem `HistoryEvent`;
- testes unitários, sem persistência ou sincronização.

## Fora do escopo da Etapa 7
Desenhos, fotos, anexos, edição, exclusão, comentários, reações, filtros, busca, novos eventos de Histórico, backend, persistência e sincronização.

## Validação da Etapa 7
Revisão visual e funcional concluída no Samsung: estado vazio, CTA, bottom sheet e teclado, contador e limite, texto e emoji, publicação imediata, lista compartilhada e ordenada, autoria, previews das Homes e navegação para o Mural. A próxima etapa de produto ainda não foi definida e dependerá de novo planejamento.

## Evolução posterior — ciclo afetivo dos pedidos

Status: 🟢 Aprovada funcional e visualmente no Samsung Galaxy A26 5G. O contrato vigente está no ADR-012. Esta evolução não define uma nova etapa de produto; a próxima etapa permanece em aberto.

## Mural — evolução implementada, revisão contra a spec e no Samsung pendentes

Recados desenhados à mão foram aprovados como alternativa aos recados textuais da Etapa 7; o contrato de produto está em `docs/features/mural.md`. A implementação e os testes automatizados ocorreram; uma nova revisão contra a spec e a validação funcional e visual no Samsung ainda não ocorreram. O experimento permanece em memória, sem nova etapa definida. As direções técnicas aceitas estão no ADR-014. A aprovação da Etapa 7 continua se referindo à versão textual entregue naquela etapa.
