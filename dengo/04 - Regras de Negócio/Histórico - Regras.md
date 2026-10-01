# Histórico - Regras
Status: 🟢 Domínio e primeira UI implementados e validados no dispositivo

Eventos atuais: pedido criado, pedido aceito, pedido recusado, mood alterado, espaço pessoal ativado e espaço pessoal encerrado.

Cada resposta válida a um pedido gera um evento com o mesmo ID. O Histórico usa o tipo e o eventual texto do pedido para dar contexto, sem usar seu status atual para reinterpretar o evento passado. Se o pedido não for encontrado, o evento permanece com texto genérico.

Uma ação que não altera estado não deve gerar novo evento.

Os eventos são exibidos do mais recente ao mais antigo; empates de horário usam a inclusão mais recente na lista. Cada mudança de mood permanece independente. Visualizar o Histórico não altera o estado. Não existem eventos de `SEEN` ou conclusão nesta etapa.
