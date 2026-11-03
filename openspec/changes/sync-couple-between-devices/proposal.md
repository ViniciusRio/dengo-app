# Proposal

## Why

O objetivo do MVP é validar o ciclo `comunicar → receber → responder → registrar`, mas hoje tudo roda em um aparelho, com troca de perspectiva: o casal não consegue se comunicar pelo app. Em 2026-11-03, o responsável escolheu levar o Dengo aos dois celulares com backend próprio (ADR-025).

## What Changes

- Criar o backend de produto em `backend/` (Kotlin, Spring Boot, PostgreSQL), separado do laboratório, como fonte da verdade e dono das regras.
- Vincular cada celular a uma pessoa com código de uso único, sem cadastro público. A identidade passa a vir da sessão e substitui a troca de perspectiva.
- Sincronizar pedidos, Histórico, mood, espaço pessoal e Mural com texto e desenho. O Room passa a ser a cópia local.
- Notificar pelo FCM, sem conteúdo no aviso: pedido novo para Vinícius e resposta ao pedido para Lidianne.
- Publicar o servidor em uma VPS, com HTTPS e backup externo testado.
- **BREAKING:** a troca de perspectiva sai do app, e `CoupleRepository` deixa de receber quem está agindo.

O uso diário só começa depois de todas as etapas, incluindo o Mural. O servidor começa vazio: nada é importado do Samsung, e o banco atual fica intacto no aparelho.

## Capabilities

### New Capabilities

- `device-identity`: vínculo de cada aparelho a uma pessoa, sessão, revogação e autorização por pessoa.
- `couple-sync`: servidor como fonte da verdade, regras, escrita idempotente, sincronização e funcionamento sem internet.
- `couple-notifications`: quando notificar, privacidade do aviso e sinal silencioso para atualizar.

### Modified Capabilities

- `local-couple-state`: o estado local passa a ser cópia do servidor, o resultado de escrita depende da confirmação remota, e a identidade sai da lista de dados fora do escopo.

## Fora do escopo

Edição, exclusão, cancelamento e reações; novos tipos de pedido; outros casais ou iOS; envio offline em fila; Redis, RabbitMQ, Kafka e SQS no produto; importação dos dados atuais; acesso de um mesmo perfil por mais de um aparelho ao mesmo tempo.

## Impact

- **Novo:** `backend/`, infraestrutura de implantação, projeto Firebase usado só para o FCM.
- **App:** ponto de composição, `CoupleRepository`, os quatro ViewModels, `DengoApp`, Perfil, nova tela de vínculo, Manifest e dependências. A composição visual das Homes, do Histórico e do Mural não muda de propósito.
- **Documentação ao implementar:** `AGENTS.md` (comandos e regra de dados locais), `docs/features/`, `docs/MVP.md`, `docs/STATUS.md`, specs consolidadas e vault.
- **Testes:** `ProfileNavigationTest` e outros que usam a troca de perspectiva precisam passar a receber a identidade.
- **Privacidade:** dados afetivos reais passam a sair do aparelho; Lidianne precisa saber e concordar antes do uso.

## Autorização

Aprovar esta spec autoriza commit, push e abertura de PR somente nas branches nomeadas em `design.md`, seção "Branches e entregas". Não autoriza push na `main`, force-push, merge, contratação de serviços pagos nem uso de dados afetivos reais antes da etapa 7.

Esta proposta não registra implementação, testes ou aprovação humana como concluídos.
