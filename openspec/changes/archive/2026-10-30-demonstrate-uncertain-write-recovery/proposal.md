# Proposal

## Why

O ADR-020 e a spec `local-couple-state` exigem que `Uncertain` não provoque reenvio automático. Os testes Room atuais demonstram o caso em que a transação falha e não grava dados; ainda não demonstram o caso em que a gravação termina e apenas a resposta ao chamador se perde. Esse segundo caso é o menor exercício útil de SDD e confiabilidade após o Incremento 2, sem escolher uma nova etapa de produto.

## What Changes

- Acrescentar à spec vigente cenários verificáveis para os dois resultados possíveis de uma escrita com retorno `Uncertain`, usando a criação de um pedido predefinido como caso representativo.
- Demonstrar, com dados sintéticos e banco Room real, que um retorno perdido após a gravação não produz reenvio automático, pedido duplicado ou evento duplicado; o estado publicado permite conferir o resultado.
- Preservar o teste existente de falha antes da gravação e a possibilidade de nova tentativa **manual** após conferir o estado.
- Registrar o limite da demonstração: a falha após o commit será simulada na resposta do repository ao chamador, sem alegar que um defeito físico do SQLite foi reproduzido.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `local-couple-state`: explicitar o comportamento observável de uma escrita incerta nos dois desfechos de durabilidade.

## Impact

- Spec: `openspec/specs/local-couple-state/spec.md`, por delta desta mudança quando ela for concluída e arquivada.
- Verificação prevista: testes instrumentados de repository e teste da camada que recebe o `WriteResult`, preferindo um decorador somente de teste para simular perda da resposta.
- Sem mudança planejada em telas, esquema Room, interface pública, regras de pedidos ou fluxo de produto. Corrigir código de produção somente se a verificação revelar descumprimento da spec e após avaliar o alcance.

Esta proposta não registra implementação, testes, validação no Samsung ou aprovação de nova etapa como concluídos.
