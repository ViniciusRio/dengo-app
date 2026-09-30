# ADR-006 — Esqueleto Android e navegação inicial

## Status

Aceito para a Etapa 1 do protótipo.

## Decisão

Usar um único módulo Android (`app`), Kotlin, Jetpack Compose e Material 3. A interface começa com tema claro, tokens compartilhados e quatro destinos inferiores. A seleção da aba fica em estado Compose salvo localmente durante recriações da Activity; não há navegação entre fluxos nem camada de dados nesta etapa.

Reservar os packages `model` e `data` para a próxima etapa, sem criar classes antecipadamente. Usar o namespace `com.lidiannevinicius.tamagotchi`.

## Motivo

Esta estrutura permite validar o projeto e a navegação com pouca infraestrutura, mantendo espaço para as duas Homes e os fluxos locais aprovados posteriormente.
