# Índice dos ADRs

Abra um ADR só quando a tarefa tocar a decisão dele. ADRs antigos ficam como estão e registram o checkpoint em que foram escritos; a coluna "Situação" diz o que ainda vale.

| ADR | Decisão | Situação |
|---|---|---|
| [001](ADR-001-android-first.md) | Android nativo primeiro | Vigente |
| [002](ADR-002-local-first-prototype.md) | Protótipo local antes de backend | Vigente; armazenamento local concretizado no 021 |
| [003](ADR-003-jetpack-compose.md) | Kotlin, Jetpack Compose e Material 3 | Vigente |
| [004](ADR-004-design-direction.md) | Direção visual: base creme, rosa e azul | Vigente; identidade dos personagens substituída pelo 015 |
| [005](ADR-005-two-user-mvp.md) | MVP fechado em Lidianne e Vinícius | Vigente |
| [006](ADR-006-android-shell.md) | Módulo único, tokens e navegação inferior | Parcial; estrutura vigente, ausência de camada de dados superada |
| [007](ADR-007-in-memory-state.md) | Estado em memória e status de pedido | Histórico; substituído por 012 (status), 020 e 021 (estado) |
| [008](ADR-008-lidianne-home.md) | Home de Lidianne | Vigente na composição; ciclo de pedidos no 012 |
| [009](ADR-009-vinicius-home.md) | Home de Vinícius e respostas | Vigente no papel da Home; ciclo no 012 e persistência no 021 |
| [010](ADR-010-shared-history.md) | Histórico compartilhado somente leitura | Parcial; apresentação no 019, eventos no 012, persistência no 021 |
| [011](ADR-011-shared-mural.md) | Mural compartilhado de recados de texto | Parcial; regras do texto vigentes, desenho no 013 e 014, persistência no 021 |
| [012](ADR-012-affective-request-cycle.md) | Ciclo afetivo dos pedidos (`ACKNOWLEDGED`) | Vigente; persistência no 021 |
| [013](ADR-013-hand-drawn-mural-notes.md) | Recados desenhados como alternativa ao texto | Vigente; persistência no 021 |
| [014](ADR-014-drawing-content-and-strokes.md) | Conteúdo e traços dos desenhos | Vigente; persistência no 021 |
| [015](ADR-015-approved-character-reference.md) | Prancha oficial dos personagens | Vigente; uso no Perfil definido depois, no 018 |
| [016](ADR-016-profile-visual-experiment-checkpoint.md) | Checkpoint funcional do Perfil | Histórico; aparência superada pelo 018 |
| [017](ADR-017-profile-visual-refinement-1-1.md) | Refinamento visual 1.1 do Perfil | Histórico; aparência superada pelo 018 |
| [018](ADR-018-option-1-profile-composition.md) | Composição B.1 do Perfil | Vigente |
| [019](ADR-019-affectionate-history-stories.md) | Histórico agrupado por pedido | Vigente; persistência no 021 |
| [020](ADR-020-async-repository-increment-1.md) | Contrato assíncrono do repository | Vigente no contrato; o Fake como fonte de estado foi superado pelo 021 |
| [021](ADR-021-room-local-persistence.md) | Room para os dados publicados | Vigente |
| [022](ADR-022-isolated-backend-lab.md) | Laboratório backend isolado | Vigente, só como laboratório |
| [023](ADR-023-rabbitmq-delivery-lab.md) | Laboratório isolado com RabbitMQ | Vigente, só como laboratório |
| [024](ADR-024-ai-agent-workflow.md) | Fluxo com agentes, leitura sob demanda e fonte única de estado | Vigente a partir do merge do PR #1 |
| [025](ADR-025-two-device-own-backend.md) | Dois aparelhos com backend próprio | Direção escolhida e spec aprovada; nada implementado |
