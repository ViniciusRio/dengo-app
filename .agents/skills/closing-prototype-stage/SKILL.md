---
name: closing-prototype-stage
description: Use when a Dengo prototype implementation has been reviewed on a device and the user asks to close the stage, record Samsung approval, consolidate post-review documentation, or decide whether it is ready for commit. Also use for a final pre-commit stage check after human device review.
---

# Fechar uma etapa aprovada do protótipo

## Fronteira

Esta skill começa após implementação e revisão humana no dispositivo. Planejar a próxima entrega cabe a `planning-product-feature`; comparar código com o contrato aprovado antes da revisão no dispositivo cabe a `reviewing-implementation-against-spec`. Fechamento não corrige código nem executa a revisão no Samsung pelo usuário.

## Apuração

1. Separe os estados **planejado/aprovado para implementação**, **implementado e verificado automaticamente**, **revisado/aprovado manualmente no dispositivo** e **fechado documentalmente e pronto para commit**. Um não prova o seguinte. Delimite o alcance do relato humano: aprovação visual e funcional explícita da etapa sustenta esse registro geral, mas não comprova testes individuais de cada fluxo ou estado. Pedido para *registrar* TalkBack, fonte ampliada, rotação ou aprovação funcional não é evidência de que foram *testados*. Sem relato explícito de aprovação humana no dispositivo, não feche; indique a confirmação factual necessária.
2. Antes de editar, confira `git status`, diff completo e arquivos novos/untracked. Examine implementação e testes relevantes, contrato vigente em spec/ADR, `docs/STATUS.md` e contexto operacional do vault; procure afirmações de “aguardando revisão” e divergências. Um relatório anterior não representa necessariamente o working tree atual. Se houver revisão contra spec explicitamente pendente, encaminhe para ela; ausência de relatório formal, isoladamente, não prova que a revisão não ocorreu.
3. Classifique achados como bloqueador funcional, divergência obrigatória da spec, documentação incorreta, escopo extra, evidência ausente ou melhoria futura. **Pare o fechamento** diante de violação do contrato, mesmo que o Samsung tenha agradado e mesmo que o reparo pareça trivial. Reporte a correção necessária para a fase de implementação/revisão; não a faça para conseguir fechar. Melhoria futura aceita só bloqueia se for requisito aprovado ou condição explícita da aprovação.

## Fechamento documental

Com evidência suficiente e sem bloqueadores, atualize apenas as fontes que precisam refletir o novo estado: decisão vigente, specs afetadas e status operacional em `docs/` ou `dengo/`, conforme o caso. Preserve ADRs históricos; registre na decisão mais recente a precedência sobre regras antigas quando necessário. Remova pendências obsoletas sem ampliar o que foi validado: uma checklist de cenários não vira evidência individual só porque a etapa recebeu aprovação geral. Se o contrato exigir validação específica ainda sem evidência, trate-a como pendência; caso contrário, registre a aprovação geral e a limitação do relato. Não transforme ideia futura em requisito, não escolha a próxima etapa e não aproveite o fechamento para melhorar a UI ou refatorar documentação.

## Verificação e entrega

Após editar, revise novamente diff completo, `git status`, untracked e as afirmações de aprovação. Execute `git diff --check`. Se há código Android pendente na etapa, a verificação padrão inclui `./gradlew test` e `./gradlew assembleDebug`; use `--rerun-tasks` quando houver razão concreta para desconfiar de `UP-TO-DATE`. Em fechamento só documental com verificação recente suficiente do código, justifique a dispensa de builds.

Termine com **PRONTO PARA COMMIT** ou **NÃO PRONTO PARA COMMIT**, incluindo evidência, verificações e bloqueadores restantes. Se pronto, pode sugerir mensagem de commit. Fechar documentação ou declarar prontidão não autoriza commit; autorização explícita para commit não autoriza push. Aplique as regras de autorização do `AGENTS.md` a cada operação.
