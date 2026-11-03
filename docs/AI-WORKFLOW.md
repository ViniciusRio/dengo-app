# Fluxo de trabalho com agentes

Como Claude Code e Codex trabalham neste repositório: papéis separados, revisão cruzada e aprovação humana.

## Papéis

| Papel | Quem | Responsabilidade |
|---|---|---|
| Implementador | Claude Code | Implementa, corrige os problemas apontados e escreve o handoff |
| Revisor adversarial | Codex | Revisa a spec e o diff **somente leitura**; aponta problemas e não corrige |
| Aprovador | Vinícius | Aprova a spec, revisa no aparelho, aprova o PR e faz o merge |

De tempos em tempos, inverta os papéis em uma tarefa para evitar pontos cegos.

## Fluxo

### 1. Planejar
- Mudança relevante começa com uma change no OpenSpec (`proposal.md`, `design.md`, `tasks.md`).
- O Codex revisa a spec antes do código, e Vinícius a aprova.
- **Exceções, sem change:**
  - Tarefa pequena (typo, uma linha, ajuste trivial): um agente resolve, sem o fluxo completo.
  - Mudança de processo ou documentação: o plano aprovado vale como spec.

### 2. Implementar
- O trabalho acontece em branch própria (`feat/...`, `fix/...`, `chore/...`, `docs/...`), nunca na `main`.
- O progresso fica no `tasks.md` da change.
- Aprovar um plano ou spec que nomeia a branch autoriza commit e push só nela e a abertura do PR. Nunca autoriza push na `main`, force-push ou merge.
- **Pronto para o handoff:** lint, compilação e testes rodados pelo próprio implementador (comandos no `AGENTS.md`). Falha anterior à mudança é relatada, não escondida.

### 3. Handoff
O implementador abre o PR e escreve o handoff na descrição, com o formato de `.github/pull_request_template.md`.

### 4. Revisar
- O Codex revisa o diff com `/codex:adversarial-review` ou com o CLI em `--sandbox read-only`. A configuração padrão do Codex permite escrita, então o modo somente leitura precisa ser explícito.
- Ele lê só a spec da mudança e os ADRs que ela cita, e devolve saída curta com `arquivo:linha`:
  - **Bloqueante:** volta para o implementador corrigir.
  - **Sugestão:** fica registrada e não bloqueia o merge.
- A rodada seguinte confere cada problema original contra o diff corrigido, não só o código novo.
- **Máximo de 2 rodadas.** Se ainda houver bloqueante, Vinícius decide.
- Comentários de revisão ficam no PR, não em arquivos do repositório.
- A skill `reviewing-implementation-against-spec` orienta a comparação com a spec.

### 5. Aprovar
- Só Vinícius faz merge. O `gh` usa a conta dele e o GitHub não permite aprovar o próprio PR, então **o merge é a aprovação**.
- A proteção da `main` exige PR e 0 aprovações, vale para administradores e bloqueia force-push. Ela é a garantia real, porque instrução no `AGENTS.md` é só contexto.
- **O merge não prova aprovação no aparelho.** Em mudança de UI, a revisão no Samsung acontece antes do merge. Planejamento, testes, revisão no aparelho, aprovação humana e fechamento documental continuam sendo fases distintas.

### 6. Modo reserva (limite do Claude Code)
- O gatilho é ter **10% da cota restante**. O agente não lê a cota com segurança: ele para quando Vinícius avisa ou quando o Claude Code alerta.
- Ele para num ponto limpo, faz commit WIP na branch e escreve o handoff.
- O Codex continua sozinho a partir do handoff e do `git diff`.
- PRs desse período recebem o label `sem-revisao-cruzada`. Quando o Claude Code voltar, revisa esses PRs com os papéis invertidos. Nada desse período vai para a `main` antes disso.
- Confira que não há `ANTHROPIC_API_KEY` no ambiente, para não trocar a cobrança da assinatura pela da API.

## Isolamento

Branch não isola a pasta de trabalho. Quando dois agentes editam ao mesmo tempo, use worktrees:

```bash
git worktree add ../dengo-app-claude feat/minha-tarefa
git worktree add ../dengo-app-codex feat/outra-tarefa
```

No fluxo sequencial, o revisor somente leitura pode revisar o commit sem worktree próprio.

## Onde cada coisa fica

| Arquivo | Uso |
|---|---|
| `AGENTS.md` | Fonte única de regras, comandos, estrutura e correções recorrentes |
| `CLAUDE.md` | Importa o `AGENTS.md`; só notas específicas do Claude Code |
| `docs/STATUS.md` | Estado atual, ~15 linhas; fonte única. Atualizado quando um agente para |
| `openspec/changes/*/tasks.md` | Progresso das tarefas |
| `docs/decisions/` e `design.md` do OpenSpec | Decisões com data e motivo. Índice em `docs/decisions/README.md` |
| Descrição e comentários do PR | Handoff e revisão |
| `dengo/` | Planejamento e contexto de produto; não registra o estado atual |

Quando um agente repetir um erro já corrigido, acrescente-o às correções recorrentes do `AGENTS.md`. Deixe de fora o que o agente consegue ler no código.

## Commits

Conventional Commits (`feat(backend): ...`, `fix: ...`, `docs: ...`). O agente é identificado pelo trailer, não pelo prefixo:

```
Co-Authored-By: Claude <noreply@anthropic.com>
```

## Economia de tokens

O que gasta é o que o agente lê: cada arquivo, log ou saída é reenviado a cada turno.

- **Leitura sob demanda:** siga "Antes de programar" no `AGENTS.md`, inclusive nas revisões.
- **Uma sessão por tarefa:** `/clear` no Claude Code ou `/new` no Codex ao trocar de tarefa. O handoff carrega o contexto.
- **Compactar no meio de uma tarefa:** `/compact mantenha: objetivo, arquivos alterados, teste que falha, próximo passo`.
- **Testes focados** durante o trabalho; suíte completa uma vez, antes do handoff.
- **Revisão isolada:** `/codex:review` e `/codex:adversarial-review` retornam só o resultado. `codex` por script roda em subagente.
- **Trocar de ferramenta só entre etapas** (spec, implementação, revisão), nunca no meio de uma edição.

| Tarefa | Modelo | Esforço |
|---|---|---|
| Spec, decisões de produto, arquitetura | Opus / modelo forte do Codex | Alto |
| Implementação a partir de spec aprovada | Sonnet / modelo padrão | Médio |
| Busca, renomeações, docs, ADR de decisão já tomada | Haiku / mini | Baixo |

## Revisão periódica do processo

A cada alguns ciclos, verifique:
- A revisão do Codex acha problemas reais ou só detalhes? Se for só detalhe, ajuste o foco do prompt ou inverta os papéis.
- O `docs/STATUS.md` continua curto?
- O `AGENTS.md` está crescendo com coisas que o código já mostra?
