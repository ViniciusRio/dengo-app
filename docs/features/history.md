# Feature — Histórico

## Objetivo

Dar contexto para pedidos e respostas anteriores sem transformar o app em ferramenta de monitoramento.

## Primeira versão — Etapa 6

A aba Histórico apresenta a mesma linha do tempo somente de leitura a Lidianne e Vinícius. Usa os seis eventos já produzidos: criação, aceitação e recusa de pedido; mudança de mood; início e fim de espaço pessoal. Cada alteração de mood aparece separadamente. Os textos usam nomes fixos e preservam o sentido original das respostas: “Estou indo ❤️” não significa conclusão; “Não consigo agora” não é punitivo.

Eventos de pedido consultam o pedido por ID para recuperar tipo e texto de `OTHER`. A ausência do pedido não remove o evento: usa-se texto humano genérico, sem ID técnico. O status atual do pedido não altera o significado do evento passado.

Mais recentes aparecem primeiro. Em horários iguais, aparece primeiro o evento incluído mais tarde na lista. A tela agrupa por Hoje, Ontem ou `dd/MM/yyyy` no fuso local e mostra `HH:mm` em cada item.

O estado é exclusivamente em memória e se perde ao reiniciar o processo. Não há `SEEN`, conclusão de pedidos ou persistência nesta etapa. A timeline e o estado vazio foram aprovados no Samsung.

## Princípio

Mostrar apenas informações úteis para memória/contexto da interação. Evitar métricas, rankings ou cobranças sobre frequência de cuidado.

## Evolução do ciclo afetivo — aprovada no Samsung

O reconhecimento de Lidianne adiciona `RequestAcknowledged`, com o texto “Lidianne agradeceu pelo carinho ❤️”. Criação, resposta e agradecimento são acontecimentos distintos. Como nos eventos anteriores, o contexto do pedido é resolvido por ID; na ausência do pedido, o evento continua com texto humano, sem ID técnico. O Histórico permanece somente leitura e em memória. Ver ADR-012.
