# ADR-011 — Mural compartilhado de recados em memória

## Status

Implementado na Etapa 7; aguardando revisão visual no Samsung.

## Decisão

O Mural é um cantinho compartilhado para pequenos recados de Lidianne e Vinícius, não um feed social. Ambos publicam e veem a mesma lista de `MuralNote` no `CoupleState`, pela instância existente de `FakeCoupleRepository`. A perspectiva local identifica o autor ao publicar, sem criar listas separadas. O estado se perde quando o processo reinicia.

Cada recado contém ID sequencial próprio, autor, texto e instante de criação pelo `Clock` injetável. O texto aceita emojis comuns, é aparado nas extremidades e precisa conter de 1 a 160 pontos de código Unicode. Exatamente 160 são aceitos; acima disso a composição continua editável, mas a publicação é recusada, inclusive pelo repository. Emojis compostos podem ocupar mais de um ponto de código. Não há truncamento automático.

Recados aparecem por `createdAt` decrescente, com ID decrescente em empate. A apresentação usa o fuso local e indica Hoje, Ontem ou data, mais `HH:mm`; mostra sempre o nome do autor. Publicar não cria `HistoryEvent`. Espaço pessoal ativo não bloqueia ler ou publicar recados: o Mural é assíncrono e não exige resposta. A interface não sugere contato insistente nesse estado.

Os previews discretos das duas Homes mantêm o texto vazio anterior ou mostram somente o recado mais recente, com autor e trecho de até duas linhas. Tocá-los abre a aba Mural pela navegação existente. O Histórico e o ciclo de pedidos não mudam.

## Consequências

Não há edição, exclusão, comentários, reações, fotos, anexos, desenhos, filtros, busca, notificações, persistência ou sincronização. Desenhar no Mural permanece possibilidade futura do conceito original. A implementação funcional e os testes não significam aprovação visual; a Etapa 7 aguarda revisão no Samsung.
