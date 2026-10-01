# AGENTS.md

## Contexto

Este repositório contém o app Android **Dengo**, de interação afetiva entre **Lidianne e Vinícius**.

## Antes de programar

Leia, nesta ordem:
1. `docs/PRODUCT.md`
2. `docs/MVP.md`
3. `docs/USER-FLOWS.md`
4. `docs/design/SCREENS.md`
5. `docs/design/REFERENCES.md`
6. `docs/design/DESIGN-SYSTEM.md`
7. `docs/design/CHARACTERS.md`
8. `docs/design/COMPONENTS.md`
9. a especificação relevante em `docs/features/`
10. os ADRs em `docs/decisions/`

## Regras

- Não copiar literalmente as interfaces nem personagens das referências originais.
- A arte em `docs/design/references/approved/` define a **direção**, não uma especificação pixel-perfect.
- Não transformar o produto em um Tamagotchi tradicional; a essência é comunicação afetiva entre duas pessoas.
- Android: Kotlin + Jetpack Compose + Material 3.
- Preferir soluções simples e adequadas a um projeto pessoal.
- No protótipo inicial, usar dados fake/local. Não introduzir Spring Boot, PostgreSQL, Retrofit, autenticação remota ou sincronização sem uma decisão explícita posterior.
- Componentes equivalentes devem reutilizar o mesmo componente/design token.
- Manter acessibilidade, legibilidade, contraste e tamanhos de toque adequados.
- Animações devem ser mínimas e funcionais.
- Não adicionar funcionalidades ao MVP 1 sem justificar e registrar a decisão.
- Decisões relevantes devem ser registradas em `docs/decisions/`.
- Quando houver ambiguidade de produto, apresentar alternativas em vez de escolher silenciosamente.

## Fase atual

As Etapas 1–7 foram concluídas e aprovadas no dispositivo. Histórico e Mural permanecem em memória. A próxima etapa de produto ainda não foi definida. Consulte `dengo/06 - Desenvolvimento/Etapa Atual.md` e os ADRs mais recentes para o estado vigente; documentos iniciais podem descrever planos já superados.
