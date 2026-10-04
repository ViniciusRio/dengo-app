# ADR-013 — Recados desenhados à mão no Mural

## Status

Aceito como decisão de produto. A implementação local passou pela revisão contra a spec e recebeu aprovação humana na validação final no Galaxy A26 após os ajustes do compositor e da paleta. O estado técnico e a evidência da validação posterior constam do ADR-014.

## Contexto

O Mural textual da Etapa 7, registrado no [ADR-011](ADR-011-shared-mural.md), funciona como coleção compartilhada de recados individuais, mas o uso real indicou que texto e emoji ainda transmitem sensação de listagem. A hipótese é que o traço feito pela própria mão torne o recado mais pessoal e afetivo.

## Decisão

Manter o recado textual com emoji e acrescentar o recado desenhado como alternativa sob “Deixar um recado”: **Escrever** ou **Desenhar**. O novo formato reúne traços livres feitos com o dedo, inclusive escrita manual, sem teclado. A pessoa pode escolher a cor dos traços.

O Mural continua sendo uma coleção compartilhada de recados individuais, com autoria e horário, sem canvas colaborativo. O desenho real aparece no recado publicado e, quando ele for o mais recente, em preview visual nas duas Homes. A publicação não cria `HistoryEvent`. O experimento continua em memória, sem persistência ou sincronização.

## Consequências

Esta decisão não altera retroativamente a versão entregue no ADR-011; sua aceitação como decisão de produto, por si só, não constituía aprovação no Samsung. A aprovação posterior está registrada no [ADR-014](ADR-014-drawing-content-and-strokes.md), que também define a representação do conteúdo e dos traços. Limites de memória e armazenamento durável continuam abertos. A [spec do Mural](../features/mural.md) é a fonte dos fluxos, limites comportamentais e critérios de aceitação; nenhuma nova etapa de produto foi definida.
