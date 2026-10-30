# Proposal

## Why

Hoje as interações publicadas desaparecem quando o processo do app termina. O Incremento 1 preparou o contrato assíncrono e os estados de falha; o Incremento 2 deve tornar os dados publicados duráveis no mesmo aparelho, conforme o escopo de produto já aprovado para especificação em `docs/STATUS.md`.

## What Changes

- Adicionar persistência local com Room para pedidos e seus estados, eventos factuais do Histórico, mood atual de Lidianne, espaço pessoal e recados publicados de texto ou desenho completo.
- Recuperar esses dados após encerramento do processo e reabertura do app, preservando IDs, autoria, horários, conteúdo e ordenação.
- Aplicar cada alteração de estado e seus eventos relacionados como uma unidade atômica; uma falha não pode deixar pedido, Histórico ou desenho parcialmente gravados.
- Conectar o armazenamento durável ao `CoupleRepository` existente, preservando os estados assíncronos de leitura/escrita e os fluxos de UI do Incremento 1.
- Em falha de leitura, preservar o banco, apresentar erro e permitir nova tentativa; nunca zerar ou recriar o banco automaticamente.
- Manter fora do escopo rascunhos, perspectiva/identidade de sessão, backup, recuperação após desinstalação, sincronização, backend e novas funcionalidades de produto.

## Capabilities

### New Capabilities

- `local-couple-state`: durabilidade e recuperação dos dados publicados do casal no mesmo aparelho, com consistência entre dados relacionados e tratamento de falhas.

### Modified Capabilities

Nenhuma. Este repositório ainda não possui specs OpenSpec consolidadas; as regras existentes permanecem em `docs/features/` e nos ADRs.

## Impact

- Dados e inicialização: `app/src/main/java/com/viniciusrio/dengo/data/`, `PrototypeStateViewModel.kt` e dependências Gradle.
- Contrato e comportamento existentes: `CoupleRepository`, modelos de domínio e os quatro ViewModels, sem mudança de escopo de UX prevista.
- Testes: contrato do repository, persistência/reabertura e testes instrumentados em banco real, além de regressão dos fluxos atuais.
- Documentação consolidada posterior à aprovação e implementação: spec em `docs/features/`, ADR técnico, `docs/STATUS.md` e checkpoint no vault.

Esta proposta OpenSpec não registra implementação, testes ou aprovação humana como concluídos.
