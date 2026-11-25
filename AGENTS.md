# AGENTS.md

## Contexto

Este repositório contém o app Android **Dengo**, de interação afetiva entre **Lidianne e Vinícius**.

## Antes de programar

Leia sob demanda:
- A spec da mudança em `openspec/changes/<change>/` ou, sem change ativa, em `openspec/specs/`; e a feature relevante em `docs/features/`.
- `docs/design/*` e imagens de referência somente se a tarefa mexer em UI.
- Um ADR somente quando a tarefa tocar a decisão dele. Índice: `docs/decisions/README.md`.
- `docs/PRODUCT.md`, `docs/MVP.md` e `docs/USER-FLOWS.md` somente em tarefas de produto, fluxo ou planejamento.

## Estrutura

`app/`: Android (`:app`). `labs/backend/`: laboratório isolado, sem ligação com o app. `openspec/`: specs e changes. `docs/`: documentação consolidada. `dengo/`: vault de produto.

## Comandos

Rode com `set -o pipefail; <comando> 2>&1 | tail -50`:
- Teste focado: `./gradlew :app:testDebugUnitTest --tests "*HomeViewModelTest*" -q`
- Lab focado: `./gradlew -p labs/backend test --tests "*CareRequestCatalog*" -q`
- Antes do handoff: `./gradlew -q :app:testDebugUnitTest :app:testReleaseUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`
- Se tocar o lab: `./gradlew -p labs/backend test -q`
- No aparelho, só se mexer em UI ou Room: `adb shell am instrument -w com.viniciusrio.dengo.test/androidx.test.runner.AndroidJUnitRunner`

Se falhar, procure o erro (`grep -n "FAILED\|error:"`) antes de abrir o relatório. Nunca imprima o relatório HTML nem o logcat completo.

## Fluxo

Claude Code implementa, Codex revisa somente leitura, Vinícius aprova e faz o merge. O quadro do Kanzo só mostra o andamento; não é fonte de verdade. Detalhes: [docs/AI-WORKFLOW.md](docs/AI-WORKFLOW.md).

## Correções recorrentes

- Teste verde, build ou screenshot não é aprovação humana.
- Se a instalação do Gradle falhar no A26: `adb install --no-streaming`.

## Regras

- Não copiar literalmente as interfaces nem personagens das referências originais.
- A arte em `docs/design/references/approved/` define a **direção**, não uma especificação pixel-perfect.
- Não transformar o produto em um Tamagotchi tradicional; a essência é comunicação afetiva entre duas pessoas.
- Preferir soluções simples e adequadas a um projeto pessoal.
- No app, os dados publicados ficam locais (Room); sem decisão explícita, nada de Spring Boot, PostgreSQL, Retrofit, autenticação remota ou sincronização. `labs/backend/` segue suas specs e ADRs.
- Componentes equivalentes devem reutilizar o mesmo componente/design token.
- Manter acessibilidade, legibilidade, contraste e tamanhos de toque adequados.
- Animações devem ser mínimas e funcionais.
- Não adicionar funcionalidades ao MVP 1 sem justificar e registrar a decisão.
- Decisões relevantes devem ser registradas em `docs/decisions/`.
- Quando houver ambiguidade de produto, apresentar alternativas em vez de escolher silenciosamente.

## Skills externas

- Regras deste repositório, documentação consolidada e skills específicas do Dengo prevalecem sobre skills externas. Elas complementam a disciplina de engenharia, sem redefinir requisitos, decisões de produto, fontes de verdade, terminologia consolidada, critérios de aprovação, fluxo documental ou autorização para commit/push.
- Use `codebase-design` em decisões estruturais, `tdd` em comportamento testável e `diagnosing-bugs` ao investigar bugs.
- Não aplique mecanicamente exemplos de outras stacks ao Android. Pedidos de confirmação dessas skills não reabrem decisões autorizadas.

## Fontes e fases

- Antes de afirmar o estado do projeto, confira código e documentação atuais. [docs/STATUS.md](docs/STATUS.md) é a fonte única do estado atual; documentos iniciais podem descrever planos já superados. `docs/` e ADRs registram especificações e decisões consolidadas; o vault `dengo/` registra planejamento e contexto de produto. Notas exploratórias não substituem decisões vigentes; exponha divergências entre fontes.
- Planejamento, implementação, testes automatizados, revisão no Samsung, ajustes, aprovação humana e fechamento documental são fases distintas. Registre aprovação e conclusão somente após ocorrerem; adapte os artefatos à etapa.
- Não faça commit nem push sem autorização explícita. Aprovar um plano ou spec que nomeia a branch e declara que autoriza commit, push e PR autoriza essas operações só nela. Nunca na `main`, sem force-push e sem merge.
