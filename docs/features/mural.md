# Feature — Mural de recados

## Objetivo

Cantinho compartilhado para Lidianne e Vinícius deixarem pequenos recados/carinho. O Mural é uma coleção de recados individuais, não um canvas compartilhado. A primeira versão em memória, somente com texto e emojis, foi aprovada visual e funcionalmente no Samsung na Etapa 7.

## Primeira versão — implementada e aprovada no Samsung

- texto com emojis digitados normalmente;
- conteúdo aparado nas extremidades, de 1 a 160 pontos de código Unicode, validado também no repository;
- ID, autoria explícita e horário por recado;
- mesma lista para as duas perspectivas, mais recentes primeiro e desempate por maior ID;
- composição em bottom sheet com contador e publicação desabilitada fora do limite;
- previews das duas Homes mostram o último recado e abrem a aba Mural;
- leitura e publicação permitidas durante espaço pessoal ativo, sem incentivo a contato insistente;
- nenhum `HistoryEvent` é criado por publicação.

Esta versão não oferece desenho. Não há edição, exclusão, comentários, reações, fotos, anexos, filtros ou busca. O estado reinicia com o processo; não há comunicação entre aparelhos.

## Evolução implementada no protótipo — aprovada no Galaxy A26

A hipótese é que o traço, a caligrafia e o gesto feitos pela própria mão tornem um recado mais íntimo e pessoal. A evolução abaixo foi implementada para um experimento local, passou pela revisão contra a spec e pelos testes automatizados e recebeu aprovação humana na validação manual final no Galaxy A26. Após os ajustes, o compositor abriu no tamanho esperado, manteve Rosa, Azul e Escuro em uma linha e não recuou na sequência Rosa → Azul → Escuro → Rosa. O relato também confirma desenho, troca de cores, Desfazer, Limpar, Cancelar e publicação no Mural. A aprovação é da evolução posterior à Etapa 7 e não define uma nova etapa de produto. Os critérios abaixo continuam sendo o contrato; este registro não altera seus requisitos.

### Dois formatos e início da criação

O recado textual com emoji continua disponível e conserva sua composição e regras atuais. Sob a ação existente “Deixar um recado”, a pessoa escolhe de forma simples entre **Escrever**, que abre a composição textual existente, e **Desenhar**, que abre a composição manual sem teclado. Os formatos são alternativas: não há texto digitado e desenho no mesmo recado.

O recado desenhado reúne traços livres feitos com o dedo. Desenho e escrita à mão são a mesma modalidade: um coração, uma palavra manuscrita ou ambos são conteúdo válido. Não há reconhecimento nem transcrição da escrita.

### Composição manual

- A superfície deve ser confortável para desenhar e escrever com o dedo e coerente com a linguagem visual do Dengo. Dentro dela, arrastar produz traços, sem rolar a composição; fora dela, a rolagem normal permanece possível quando aplicável. Dimensões e proporção serão refinadas e validadas no Samsung.
- A pessoa escolhe entre poucas cores de traço coerentes com o design system e com contraste adequado. A cor ativa deve estar perceptível sem depender só da cor. Cada novo traço mantém a cor selecionada ao começar; mudar a seleção afeta somente novos traços, sem recolorir os anteriores. Quantidade, tons e apresentação exatos serão refinados no design e validados no aparelho.
- **Desfazer** remove somente o último traço. **Limpar** remove todos os traços e deixa o rascunho vazio, sem confirmação. Não há borracha, refazer nem escolha de espessura.
- Um rascunho vazio não pode ser publicado. Isso também vale após limpar ou desfazer todos os traços. Um rascunho vazio pode ser cancelado diretamente.
- Se houver traços não publicados, toda forma relevante de sair da composição deve permitir escolher entre continuar desenhando e descartar. Descartar não publica nada. A proteção é para abandono do rascunho, não para a ação **Limpar**.

### Publicação e leitura

Publicar um desenho válido cria exatamente um recado na mesma coleção do Mural, com autoria da perspectiva ativa e horário. Ele segue a ordenação vigente por criação, com desempate pelo ID mais recente. Publicar não cria `HistoryEvent` nem altera pedidos ou espaço pessoal.

O recado publicado mostra o traço real, autoria e horário na lista. Vários desenhos devem continuar legíveis sem tornar a lista um conjunto de cards pesados. Tocar em um desenho publicado não abre edição: não há edição posterior neste experimento. Uma visualização ampliada somente de leitura pode ser reconsiderada se a validação no Samsung mostrar necessidade, mas não integra o mínimo.

Se o recado mais recente for textual, as duas Homes mantêm o preview textual atual. Se for desenhado, ambas mostram um preview visual do desenho real, com autoria, preservando a ação de abrir o Mural. O desenho inteiro deve aparecer reduzido, evitando corte quando possível. O preview mantém o papel discreto do Mural e a hierarquia já aprovada de cada Home; tamanho e peso visual serão validados no Samsung.

Lidianne e Vinícius veem a mesma coleção local e cada um publica com sua própria autoria. A troca de perspectiva existente é suficiente para validar este protótipo; ela não representa comunicação real entre dois aparelhos. O estado vazio do Mural e os demais recados textuais permanecem disponíveis.

### Acessibilidade e duração do estado

Os controles de escolha do formato, criação, cores, desfazer, limpar, cancelar e publicar precisam de rótulos/semântica compreensíveis, alvos de toque adequados e estados perceptíveis sem depender somente de cor. O conteúdo desenhado comunica minimamente que é um desenho, quem o fez e quando foi publicado, inclusive no preview. Não se exige descrição manual do significado nem se presume leitura automática da caligrafia.

O experimento continua em memória. Ao reiniciar ou matar o processo, recados e rascunhos desenhados podem desaparecer, como o estado atual. Isso permite avaliar o valor afetivo imediato do gesto, mas não o valor de guardar e revisitar desenhos ao longo do tempo. Persistência e recuperação após morte do processo não integram esta evolução.

## Critérios de aceitação da evolução

### Verificáveis automaticamente

- A escolha **Escrever/Desenhar** abre a composição correspondente; o fluxo textual e suas validações atuais continuam funcionando, e a composição desenhada não abre teclado.
- Cada traço preserva a cor ativa quando foi criado; mudar a cor só afeta novos traços. Desfazer remove apenas o último; limpar remove todos, sem confirmação.
- Desenho vazio, inclusive após limpar ou desfazer até esvaziar, não publica. Cancelar rascunho vazio fecha diretamente; saída com traços oferece continuar ou descartar; descartar não cria recado.
- Publicar um desenho não vazio cria exatamente um recado na coleção compartilhada, com autoria, horário e ordenação corretos, sem `HistoryEvent` nem mudança em pedidos ou espaço pessoal.
- Último recado textual mantém o preview textual; último recado desenhado oferece o desenho real nas duas Homes e abre o Mural. O desenho publicado não entra em edição ao toque.
- As duas perspectivas consultam a mesma coleção local e publicam com autoria própria. Reiniciar o estado do protótipo perde o conteúdo em memória.

### Pontos de validação humana no Samsung

- Conforto e prazer ao desenhar ou escrever com o dedo, tamanho da superfície e ausência de rolagem acidental durante o traço.
- Legibilidade de coração, carinha, flor, palavra ou frase manuscrita e conteúdo misto; utilidade da troca de cor, de desfazer e de limpar.
- Contraste e indicação da cor ativa; clareza do descarte de rascunho; leitura de vários desenhos sem peso visual excessivo no Mural.
- Fidelidade e legibilidade do preview real nas duas Homes, preservando suas prioridades; uso com fonte de interface ampliada e semântica dos controles no TalkBack.
- Se ver o traço da outra pessoa produz o valor afetivo que motivou a evolução. Testes automatizados não substituem esta aprovação.

A aprovação humana geral no Galaxy A26 foi registrada acima. O relato não discrimina testes de todos estes pontos: não há evidência individual de TalkBack, fonte ampliada, previews nas duas Homes, leitura de vários desenhos ou avaliação do valor afetivo após uso continuado. Esses pontos não devem ser apresentados como testes concluídos individualmente.

## Fora deste experimento

Fotos, câmera, galeria, anexos, stickers, canvas colaborativo, desenho simultâneo, comentários, reações, teclado dentro do recado desenhado, texto digitado e desenho no mesmo recado, edição após publicação, borracha, refazer, escolha de espessura, editor gráfico complexo, reconhecimento ou transcrição de caligrafia, ampliação/zoom como requisito inicial, exportação, backend, sincronização, dois aparelhos, persistência, recuperação após morte do processo e novos eventos de Histórico.

## Decisões técnicas posteriores

A representação técnica escolhida está no [ADR-014](../decisions/ADR-014-drawing-content-and-strokes.md). Este documento continua sendo a fonte do comportamento e dos critérios de aceitação. Limites de memória, serialização e armazenamento continuam sem decisão.
