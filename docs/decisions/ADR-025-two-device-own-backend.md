# ADR-025 — Dois aparelhos com backend próprio

## Status

Escolhas de produto e de direção tomadas pelo responsável em 2026-11-03. O desenho técnico detalhado está em `openspec/changes/sync-couple-between-devices/`, com spec aprovada pelo responsável em 2026-11-03. Nada foi implementado. Quando a implementação for concluída, este ADR recebe a evidência e substitui em parte o ADR-021 e o ADR-002.

## Contexto

O objetivo do MVP é validar o ciclo `comunicar → receber → responder → registrar`, mas no aparelho único esse ciclo é simulado. Comparadas às alternativas de reação a recados, carinho durante o espaço pessoal, sugestões por mood e pausa em funcionalidades, os dois aparelhos eram a única opção que permite o casal usar o produto de verdade. O ADR-022 deixou em aberto identidade, privacidade e sincronização, e o laboratório não aprova backend de produto.

## Decisão

- O próximo trabalho de produto é levar o Dengo a dois aparelhos (MVP 2).
- O backend é próprio, porque estudar backend também é objetivo do responsável.
- O uso diário só começa com pedidos, Histórico, mood, espaço pessoal e Mural com texto e desenho sincronizados, além de notificações de pedido novo e resposta.
- O servidor começa vazio; os dados atuais do Samsung não são importados.
- A hospedagem é uma VPS paga pequena, e não um plano gratuito que pode recuperar a instância.
- O FCM entrega os avisos, sem conteúdo afetivo.

## Alternativas e limites

**Firebase completo:** chegaria mais rápido ao uso e reduziria a manutenção, mas deixaria os dados afetivos em um serviço de terceiros e não atenderia ao objetivo de estudo. **Mural local temporário:** reduziria a primeira entrega, mas faria cada pessoa ver uma lista diferente em um espaço cuja função é ser compartilhado. **Importar dados atuais:** daria mais trabalho, e eles vêm de um só aparelho, podendo incluir recados de teste. **Uso diário antes do Mural:** foi descartado pelo mesmo motivo.

**Consequências:** o prazo até o uso é maior, o responsável passa a operar um servidor com dados íntimos e tem acesso técnico a eles, e surge um custo mensal e dependência do Google para as notificações. Lidianne precisa saber disso antes de usar. Redis, mensageria e outras ideias do plano de estudos continuam fora do produto.

## Evidência

Nenhuma de implementação. Esta decisão registra só a escolha. Testes, revisão nos dois aparelhos e aprovação humana são fases posteriores.
