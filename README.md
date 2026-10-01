# Dengo

Projeto Android pessoal para criar uma forma divertida, útil e afetiva de interação entre **Lidianne e Vinícius**.

> Dengo é um app de comunicação afetiva entre duas pessoas, sem reproduzir o brinquedo/jogo Tamagotchi.

## Estado atual

As Etapas 1–6 foram concluídas e aprovadas no dispositivo. A Etapa 7 — Mural de recados — está implementada e aguarda revisão visual no Samsung. Histórico e Mural funcionam em memória. O planejamento e o estado atuais ficam no vault versionado `dengo/`; decisões técnicas ficam em `docs/decisions/`.

## Stack aprovada para o protótipo

- Android
- Kotlin
- Jetpack Compose
- Material 3 como base, customizado pelo design system
- Dados fake/local durante a validação
- Backend futuro, somente quando necessário: Java + Spring Boot + PostgreSQL

## Direção visual aprovada

- tema claro;
- fundo creme/off-white;
- Lidianne associada a rosa;
- Vinícius associado a azul;
- personagens originais em linguagem chibi/pixel-art suave;
- cards suaves, bordas levemente arredondadas e baixa elevação;
- interface carinhosa, limpa e sem excesso de elementos.

A referência aprovada está em `docs/design/references/approved/01-approved-design-direction.png`.

## Documentação principal

- `docs/PRODUCT.md` — visão e princípios do produto
- `docs/MVP.md` — escopo inicial e roadmap
- `docs/USER-FLOWS.md` — fluxos principais
- `docs/design/SCREENS.md` — estrutura e comportamento das telas
- `docs/design/REFERENCES.md` — uso das referências visuais
- `docs/design/DESIGN-SYSTEM.md` — tokens e direção visual
- `docs/design/CHARACTERS.md` — especificação de Lidianne e Vinícius
- `docs/design/COMPONENTS.md` — componentes previstos
- `docs/features/` — especificações por funcionalidade
- `docs/decisions/` — decisões registradas
- `dengo/` — produto, UX, regras de negócio, planejamento e backlog versionados

## Regra para agentes

Leia `AGENTS.md` e a documentação antes de alterar o projeto. Não invente requisitos silenciosamente e não introduza backend, autenticação ou infraestrutura remota antes de validar o protótipo local.
