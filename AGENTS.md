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

## Skills externas

- Regras deste repositório, documentação consolidada e skills específicas do Dengo prevalecem sobre skills externas. Elas complementam a disciplina de engenharia, sem redefinir requisitos, decisões de produto, fontes de verdade, terminologia consolidada, critérios de aprovação, fluxo documental ou autorização para commit/push.
- Use `codebase-design` ao projetar ou reconsiderar interfaces, seams, módulos ou decisões estruturais relevantes; `tdd` para orientar implementação test-first quando houver comportamento implementável e testável; e `diagnosing-bugs` para investigar bugs sistematicamente antes de propor correções.
- Não aplique mecanicamente instruções ou exemplos de outras stacks e ecossistemas ao Android/Kotlin/Compose. Pedidos de confirmação das skills externas não reabrem decisões já autorizadas; diante de ambiguidade real, apresente alternativas. Em caso de conflito com `AGENTS.md`, documentação consolidada, código atual ou skill específica do Dengo, prevalece o contexto do Dengo.
- Mantenha as skills upstream sem modificações locais enquanto forem tratadas como upstream. Revise as mudanças antes de aceitar uma atualização.

## Fontes e fases

- Antes de afirmar o estado do projeto, confira código e documentação atuais. `docs/` e ADRs registram especificações e decisões consolidadas; o vault `dengo/` registra planejamento e contexto de produto. Notas exploratórias não substituem decisões vigentes; exponha divergências entre fontes.
- Planejamento, implementação, testes automatizados, revisão no Samsung, ajustes, aprovação humana e fechamento documental são fases distintas. Registre aprovação e conclusão somente após ocorrerem; adapte os artefatos à etapa.
- Não faça commit nem push sem autorização explícita.

## Fase atual

As Etapas 1–7 foram concluídas e aprovadas no dispositivo. Histórico e Mural permanecem em memória. A próxima etapa de produto ainda não foi definida. Consulte `dengo/06 - Desenvolvimento/Etapa Atual.md` e os ADRs mais recentes para o estado vigente; documentos iniciais podem descrever planos já superados.
