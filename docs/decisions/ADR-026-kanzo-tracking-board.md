# ADR-026 — Kanzo como quadro de acompanhamento

## Status

Aceito pelo responsável em 2026-11-06. É uma decisão de processo: não altera produto, código do app nem o fluxo do [ADR-024](ADR-024-ai-agent-workflow.md).

## Contexto

O andamento fica espalhado entre os `tasks.md` das changes, os PRs e o `docs/STATUS.md`. Não há uma visão rápida do backlog e do que está em cada etapa, nem para o responsável nem para os agentes.

## Decisão

Usar o [Kanzo](https://github.com/emanuele-em/kanzo), um kanban local que os agentes acessam por MCP, **só para acompanhar e visualizar** o backlog e o fluxo. O OpenSpec (SDD), os `tasks.md`, os ADRs, o `docs/STATUS.md` e o `docs/AI-WORKFLOW.md` mantêm suas funções e continuam sendo as fontes de verdade. As regras de uso ficam no `docs/AI-WORKFLOW.md`.

## Alternativas e limites

Trocar os `tasks.md` pelo Kanzo tiraria o progresso do Git e da spec. Não usar quadro algum mantém a visão fragmentada.

O quadro pode ficar desatualizado em relação ao repositório; nesse caso vale o repositório. Mover um cartão não é aprovação. Os dados ficam em `~/kanzo`, em cada máquina, fora do repositório. O Kanzo é AGPL-3.0, o que não afeta o app porque ele é só usado, não modificado nem distribuído.
