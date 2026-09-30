# ADR-009 — Home Vinícius e respostas locais

## Status

Implementado na Etapa 5; aguardando revisão visual em dispositivo físico.

## Decisão

A Home da Lidianne expressa mood, pedidos e espaço pessoal. A Home do Vinícius prioriza compreender esses sinais e responder aos pedidos recebidos, com layout compacto próprio e os mesmos tokens visuais.

Um `PrototypeStateViewModel` preserva uma única instância de `FakeCoupleRepository` na Activity. `HomeViewModel` e `ViniciusHomeViewModel` recebem essa instância pela mesma fábrica e observam seu `StateFlow`. A perspectiva é estado local de UI, alternado em Perfil para o protótipo, sem autenticação e sem campo de sessão nos models.

Somente pedidos `PENDING` dirigidos a Vinícius admitem resposta. “Estou indo ❤️” muda para `ACCEPTED`: acolhimento, sem conclusão. “Não consigo agora” muda para `DECLINED`: indisponibilidade no momento, sem julgamento da pessoa. Cada resposta válida atualiza pedido e histórico no mesmo snapshot. Transições inválidas não mudam estado nem histórico. Visualizar não muda status; não existe `SEEN`. `COMPLETED` permanece representável, sem ação de conclusão nesta etapa.

Com espaço pessoal ativo, o aviso precede o mood. Pedidos anteriores continuam visíveis, mas ações de resposta imediata ficam indisponíveis na UI e no repository até Lidianne encerrar o espaço. Nenhum status muda automaticamente. Mood registrado na data atual pode ser descrito como atual; mood de outra data aparece como “Último mood” com a data, e ausência de mood tem estado vazio.

## Consequências

O estado ainda é local e se perde ao encerrar o processo. A alternância facilita a revisão das duas perspectivas no mesmo aparelho. A aprovação visual da Home Vinícius depende de revisão no dispositivo antes do commit final da Etapa 5.
