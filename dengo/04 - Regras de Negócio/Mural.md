# Mural
Status da primeira versão textual: 🟢 Implementada em memória e aprovada no Samsung. A evolução posterior de desenhos está em `docs/features/mural.md` e no ADR-014.

- As duas perspectivas consultam a mesma lista e podem deixar recados.
- A perspectiva atual define o autor; cada recado registra ID próprio e horário pelo relógio do repository.
- O texto é aparado nas extremidades e deve conter de 1 a 160 pontos de código Unicode. Exatamente 160 são válidos; 161 ou mais são recusados sem truncamento. Emojis compostos podem consumir mais de um ponto.
- A lista exibe criação mais recente primeiro e desempata pelo maior ID.
- Publicar não altera pedidos, espaço pessoal nem Histórico e não cria `HistoryEvent`.
- Espaço pessoal ativo não impede a leitura nem a publicação; a interface evita convites insistentes para contato.
- Não há edição ou exclusão nesta versão. O estado é apenas em memória.
