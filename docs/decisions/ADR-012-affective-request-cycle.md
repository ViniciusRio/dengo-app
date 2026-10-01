# ADR-012 — Ciclo afetivo dos pedidos

## Status

Implementado e aprovado funcional e visualmente no Samsung Galaxy A26 5G após reteste do ciclo interativo.

## Contexto

No uso real, pedidos predefinidos equivalentes se acumulavam e “Estou indo ❤️” não oferecia um retorno afetivo após o carinho acontecer. Os ADR-007, ADR-009 e ADR-010 registram as decisões anteriores de suas etapas, inclusive `COMPLETED` sem ação e os cards de pedidos recusados na Home de Vinícius.

## Decisão

O ciclo em memória é `PENDING → ACCEPTED → ACKNOWLEDGED` ou `PENDING → DECLINED`. Somente Lidianne reconhece um pedido aceito por ID com “Obrigada, meu amor ❤️”; a resposta aceita não conclui o carinho. Um aceito pode permanecer aberto indefinidamente. `COMPLETED` foi substituído, sem migração porque não há estado persistido.

Os quatro tipos predefinidos bloqueiam novo pedido equivalente enquanto houver `PENDING` ou `ACCEPTED`. Recusa ou agradecimento libera o tipo. `OTHER` não participa da equivalência, mesmo com texto idêntico. A duplicata mantém estado, histórico e sequência de IDs. O repository protege transições e publica pedido e evento no mesmo `CoupleState`. Espaço pessoal continua bloqueando respostas de Vinícius, sem alterar pedidos.

A Home de Lidianne comunica o estado ativo junto às ações e oferece agradecimento individual a todos os aceitos. A Home de Vinícius lista apenas pendentes e aceitos; mostra o agradecimento mais recente como um retorno afetivo breve, derivado do Histórico compartilhado, sem lista de encerrados. Outros acontecimentos posteriores não apagam esse retorno. O Histórico continua somente leitura e associa o novo acontecimento ao pedido por ID com fallback humano.

## Consequências

O estado continua local e compartilhado pelas duas perspectivas na mesma instância do repository. Sem expiração, cancelamento, conclusão por Vinícius ou agradecimento automático. Motion foi adiado para preservar a composição existente; há confirmação textual. Testes automatizados e build não substituem a revisão no Samsung.

No reteste no aparelho, dois pedidos aceitos foram agradecidos individualmente; cada card saiu da Home de Lidianne sem remover o outro. A Home de Vinícius mostrou apenas o agradecimento mais recente, com contexto do carinho, e o manteve após uma mudança de humor para “Cansada”. O Histórico preservou criação, resposta e os dois agradecimentos como acontecimentos distintos. A composição e a interação observadas foram aprovadas. Não há evidência específica de teste com fonte ampliada ou TalkBack nesta aprovação.
