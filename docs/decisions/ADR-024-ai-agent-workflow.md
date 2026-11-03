# ADR-024 — Fluxo com agentes de IA e leitura sob demanda

## Status

Aceito pelo responsável em 2026-11-02, com vigência a partir do merge do PR #1. É uma decisão de processo: não altera produto, código do app nem as decisões anteriores.

## Contexto

O `AGENTS.md` exigia ler cerca de 74 KB antes de programar (~18,6 mil tokens por sessão), inclusive nas revisões. O estado atual estava dividido entre `docs/STATUS.md` e o vault, com fontes apontando para um ou para outro. O repositório não registrava o trabalho com Claude Code e Codex, e a regra de autorização explícita para commit e push não dizia como se aplica a um fluxo em que o implementador abre o PR.

## Decisão

Claude Code implementa, Codex revisa somente leitura e o responsável aprova e faz o merge. O fluxo detalhado fica em [docs/AI-WORKFLOW.md](../AI-WORKFLOW.md), e o `AGENTS.md` mantém só um resumo e o link.

A leitura passa a ser sob demanda: spec da mudança e feature relevante; design só em tarefa de UI; ADR só quando a tarefa tocar a decisão, com o [índice](README.md) como ponto de entrada; produto e MVP só em tarefa de produto, fluxo ou planejamento.

`docs/STATUS.md` é a fonte única do estado atual e fica curto. Evidências e limites de cada aprovação ficam nos ADRs e nas features. No vault, `Etapa Atual.md` diz apenas qual etapa de produto está ativa.

A autorização explícita continua valendo. Aprovar um plano ou spec que nomeia a branch autoriza commit e push só nela e a abertura do PR, nunca push na `main`, force-push ou merge. Como o `gh` usa a conta do responsável e o GitHub não permite aprovar o próprio PR, o merge é a aprovação.

Mudança de processo ou documentação dispensa change no OpenSpec: o plano aprovado vale como spec.

## Alternativas e limites

Pedir autorização a cada commit seria mais lento e incompatível com o commit WIP do modo reserva. Manter a nota do vault como fonte de estado deixaria o estado fora de `docs/`, onde já estão as specs e decisões. CI ficou de fora por exigir Android SDK no runner; as checagens são locais e vão no handoff.

Sem leitura obrigatória, um agente pode agir sem conhecer o produto. As regras de produto continuam no `AGENTS.md`, que é sempre carregado, para reduzir esse risco. Merge não prova aprovação no aparelho: revisão no Samsung, aprovação humana e fechamento documental continuam sendo fases distintas. O gatilho de 10% do modo reserva depende de aviso do responsável ou de alerta do Claude Code, porque o agente não lê a cota com segurança.

## Evidência

As medições, as checagens e a revisão do Codex estão no PR #1. A proteção da `main` e o label `sem-revisao-cruzada` são posteriores ao merge e dependem de confirmação do responsável; enquanto não forem aplicados, a regra de merge depende só de instrução.
