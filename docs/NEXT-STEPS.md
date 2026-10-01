# Próximos passos para o agente de código

> Registro do planejamento inicial. As Etapas 1–7 foram concluídas e aprovadas no dispositivo. A próxima etapa de produto ainda não foi definida. Para o estado vigente, consulte `dengo/06 - Desenvolvimento/Etapa Atual.md`.

## Estado

Produto, MVP, personagens e direção visual inicial estão aprovados. Não redesenhar o produto do zero.

## Primeira sessão com o agente

**Não alterar arquivos ainda.**

1. Ler `AGENTS.md` e toda a documentação indicada nele.
2. Propor arquitetura Android para Kotlin + Jetpack Compose + Material 3.
3. Propor estrutura de packages.
4. Identificar models mínimos para `Partner`, `CareRequest`, `Mood`, `PersonalSpace` e `HistoryEvent`.
5. Propor uma fonte de dados fake/local simples.
6. Mapear os componentes Compose reutilizáveis de `docs/design/COMPONENTS.md`.
7. Escolher a ordem de implementação das telas.
8. Indicar explicitamente quais arquivos pretende criar.
9. Não adicionar backend, banco remoto, Retrofit ou autenticação.
10. Apresentar o plano e aguardar aprovação.

## Após aprovação do plano

Ordem recomendada:
1. criar esqueleto Android/Compose;
2. implementar theme/tokens;
3. implementar componentes básicos;
4. implementar Home de Lidianne com dados fake;
5. implementar Home de Vinícius com dados fake;
6. conectar estados locais de pedido/mood;
7. implementar Histórico;
8. implementar Mural local simples;
9. validar UX antes de discutir backend.

## Prompt sugerido

```text
Leia integralmente o AGENTS.md e a documentação indicada nele.

Este é um novo projeto Android. Ainda não implemente nada.

Quero que você:
1. entenda o produto e o MVP;
2. analise o design aprovado e as telas documentadas;
3. proponha a arquitetura inicial usando Kotlin, Jetpack Compose e Material 3;
4. proponha a estrutura de packages;
5. identifique os componentes Compose reutilizáveis;
6. proponha os models necessários para o protótipo;
7. proponha como usar dados fake/localmente;
8. apresente a ordem de implementação;
9. liste os arquivos que pretende criar.

Não adicione backend, banco remoto, Retrofit ou autenticação nesta etapa.
Não altere arquivos ainda.

Ao final, apresente o plano para minha aprovação e destaque decisões que ainda precisam de escolha humana.
```
