---
name: planning-product-feature
description: Use when planning a Dengo product feature or stage, choosing the next delivery, comparing backlog options, or deciding what brings the couple most value now.
---

# Planejar uma funcionalidade do Dengo

## Princípio

Recomende após reconstruir o estado vigente. Recomendação não aprova etapa: a escolha exige decisão humana explícita.

## Apuração antes da recomendação

1. Leia `AGENTS.md`. Confira código e testes relevantes para distinguir implementação de descrição.
2. Consulte `docs/` consolidado: visão, MVP, fluxos, feature pertinente e ADRs recentes. Confira no vault `dengo/` Roadmap, Backlog, Etapa Atual e Decisões de Produto; abra notas de UX, regras e histórico pertinentes.
3. Separe as evidências em: **decisão aprovada**, **comportamento implementado**, **ideia exploratória**, **dívida ou inconsistência** e **decisão pendente**. Use o código para afirmar implementação; use decisões registradas e o estado vigente para afirmar aprovação. Se as fontes divergirem, exponha a divergência e sua consequência.
4. Compare alternativas reais para a próxima entrega por valor para o casal, dependências, risco, tamanho e impacto arquitetural. Inclua a opção de não acrescentar funcionalidade quando ela for plausível. Indique evidências e incertezas; não transforme prioridade de backlog, proposta inicial ou ideia do vault em requisito fechado.

## Entrega e limite

Antes da escolha, entregue apenas um retrato curto do estado atual, alternativas comparadas, recomendação justificada e escolhas que exigem decisão humana. Faça perguntas apenas sobre escolhas que mudam o escopo e que o repositório não resolve. **Depois** da escolha explícita, produza uma proposta implementável com objetivo, comportamento e limites, critérios de aceitação, dependências e validação prevista.

Planejamento termina nessa proposta. Não altere código, não inicie implementação, não registre uma decisão como aprovada antes da escolha humana e não declare testes, revisão no Samsung ou fechamento documental concluídos sem que tenham ocorrido. Revisar a implementação contra a especificação e fechar uma etapa aprovada são trabalhos posteriores.

## Referência rápida

| Evidência encontrada | Tratamento |
| --- | --- |
| Código e testes atuais | Comportamento observado, com limites identificados |
| ADR ou decisão consolidada | Decisão vigente, conferida com registros mais recentes |
| Backlog, nota de UX ou ideia futura | Candidata a avaliar; não equivale a aprovação |
| Documentos contraditórios | Pendência explícita, sem escolher silenciosamente |

Exemplo: uma função chamada de possibilidade no Backlog é alternativa, não etapa aprovada.

## Erros comuns

| Atalho tentador | Correção |
| --- | --- |
| “O dono pediu rapidez; posso confiar só no Roadmap.” | Faça a leitura direcionada do código, dos ADRs e do estado atual antes de recomendar. |
| “Minha recomendação já define a próxima etapa.” | Marque-a como proposta e espere a escolha humana. |
| “Posso adiantar um plano e pedir aprovação depois.” | Antes da escolha, pare na comparação e nas decisões pendentes. |
| “Já posso preparar implementação e ADR de conclusão.” | Pare na proposta de etapa; essas fases dependem da decisão e da validação posteriores. |
