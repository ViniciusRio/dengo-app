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

## Histórias por pedido — direção visual aprovada e validação final registrada

Eventos de um mesmo pedido aparecem em um cartão, agrupados por `requestId` na ordem registrada: criação, resposta e eventual agradecimento. O cartão ocupa o dia do acontecimento mais recente e mostra a data local de cada passo quando atravessa dias. A resposta “Estou indo ❤️” continua sendo apenas aceitação; o agradecimento aparece somente quando há evento próprio. Pedidos com um ou dois acontecimentos mostram apenas o que foi registrado, sem rótulo de cobrança. Na ausência dos dados do pedido, os eventos permanecem com texto genérico. Mood e espaço pessoal continuam como acontecimentos independentes, sem relação causal com pedidos. O Mural não entra no Histórico.

A composição visual vigente usa cartões de superfície muito suave, pontos rosa e azul conforme a autoria dos passos e conectores discretos. Na perspectiva Lidianne, a superfície é `surfaceVariant` (`#FFF2F4`); na de Vinícius, é um azul claro derivado de `surface` e `ViniciusBlueSoft`. A borda, os espaçamentos e a tipografia reutilizam os tokens existentes. A perspectiva muda somente a superfície do cartão: conteúdo, ordenação e pontos por autoria são iguais para os dois. O responsável aprovou **visualmente** essa composição em ambas as perspectivas no Samsung Galaxy A26. Ver ADR-019.

A validação final no Galaxy A26 incluiu 11 testes instrumentados (`OK (11 tests)`), executados via ADB após instalação dos APKs com `adb install --no-streaming`; os testes cobrem, entre outros casos, as duas perspectivas, recusa com texto longo de `OTHER`, mood e espaço pessoal independentes. O responsável confirmou manualmente TalkBack e fonte ampliada em aproximadamente `1,5×` como satisfatórios. A aprovação visual nas duas perspectivas e essas verificações não constituem relato individual de todos os estados em uso manual nem auditoria exaustiva de acessibilidade. O estado continua em memória; esta evolução não adiciona persistência, sincronização ou novos eventos.
