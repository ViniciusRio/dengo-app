# Design

## Context

`CoupleRepository` fornece `StateFlow<RepositoryReadState>` e comandos suspensos com `WriteResult`. `RoomCoupleRepository` grava cada pedido e seu evento factual na mesma transação e republica o estado após uma escrita. O teste `failedHistoryInsertRollsBackRequestAndKeepsReadableState` cobre uma falha anterior ao commit. Os testes de `QuickWriteCoordinator` cobrem feedback incerto, mas não combinam resposta perdida com um commit Room recuperável.

## Goals / Non-Goals

**Goals:** demonstrar, por OpenSpec e teste reproduzível, a conferência do estado após retorno `Uncertain` nas duas possibilidades de durabilidade; impedir reenvio automático e duplicação de pedido/evento.

**Non-Goals:** criar recuperação automática, chave de idempotência, nova UX, CQRS, outro banco, backend, sincronização, mensageria ou requisito novo para pedidos `OTHER`. O exercício não certifica falhas físicas de armazenamento nem todos os comandos.

## Decisions

### 1. Pedido predefinido como caso representativo

Usar um pedido predefinido ainda não ativo. Sua regra vigente de no máximo um pedido ativo por tipo permite observar precisamente a duplicidade e o acontecimento factual correspondente. Não extrapolar o resultado para `OTHER`, que admite pedidos independentes mesmo com texto igual.

### 2. Simular perda da resposta depois da gravação

Um decorador somente de teste chama o `RoomCoupleRepository`, aguarda `Confirmed` e entrega `Uncertain` ao chamador. O banco real permanece responsável pela transação e pela reabertura. A simulação representa a fronteira entre commit e resposta; não altera o adapter de produção nem afirma ter provocado falha interna do Room. Se o teste revelar que o chamador repete a operação ou que o estado não permite conferência, parar e especificar a correção mínima antes de implementá-la.

### 3. Evidência separada para o resultado sem gravação

Reaproveitar ou ampliar o teste de falha de inserção do evento, que retorna `Uncertain` após rollback e mantém o estado legível. Verificar que pedido e evento permanecem ausentes após releitura ou reabertura. Uma segunda tentativa pode ser iniciada explicitamente no teste depois da conferência; ela não deve ser disparada pelo app.

## Risks / Trade-offs

- O teste com decorador comprova o contrato na fronteira do repository, mas não reproduz uma interrupção física entre commit SQLite e entrega da resposta.
- A regra de duplicidade dos pedidos predefinidos mascara algumas repetições no estado final; além das linhas, verificar a contagem de chamadas ao comando no decorador.
- Uma ação independente anterior pode perder o aviso diante de intenção mais recente, conforme ADR-020; este exercício usa uma única intenção para não reabrir essa decisão.

## Validation Plan

Executar os testes instrumentados focados em banco Room real, os testes unitários do coordenador de escrita pertinentes e `openspec validate --strict`. Registrar comandos, resultados e limites efetivos somente após a execução. Revisão humana no Samsung só será exigida se houver alteração observável de UI; não a declarar por antecipação.
