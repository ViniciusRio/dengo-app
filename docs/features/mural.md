# Feature — Mural de recados (Etapa 7)

## Objetivo

Cantinho compartilhado para Lidianne e Vinícius deixarem pequenos recados/carinho. A primeira versão está implementada em memória e aguarda revisão visual no Samsung.

## Primeira versão

- texto com emojis digitados normalmente;
- conteúdo aparado nas extremidades, de 1 a 160 pontos de código Unicode, validado também no repository;
- ID, autoria explícita e horário por recado;
- mesma lista para as duas perspectivas, mais recentes primeiro e desempate por maior ID;
- composição em bottom sheet com contador e publicação desabilitada fora do limite;
- previews das duas Homes mostram o último recado e abrem a aba Mural;
- leitura e publicação permitidas durante espaço pessoal ativo, sem incentivo a contato insistente;
- nenhum `HistoryEvent` é criado por publicação.

## Limites e futuro

Não há edição, exclusão, comentários, reações, fotos, anexos, desenhos, filtros ou busca. O estado reinicia com o processo; sincronização entre dispositivos depende de uma etapa futura. Desenhar no Mural, inclusive como gesto para a outra pessoa, permanece uma possibilidade futura.
