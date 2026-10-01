# ADR-008 — Primeira Home da Lidianne

## Status

Aceito após validação funcional e visual da versão 3.1 em dispositivo físico. Etapa 3 concluída. Ilustração do casal integrada na Etapa 4 e aprovada visualmente em dispositivo físico.

## Decisão

A Home da Lidianne usa o `HomeViewModel` da Etapa 2 e apresenta, nesta ordem, saudação, hero do casal, ação principal de dengo, pedidos rápidos, mood, espaço pessoal e mural vazio. Os outros destinos mantêm seus placeholders.

Na Etapa 3.1, como os personagens ainda não tinham assets isolados, o hero mantinha uma área aberta com um placeholder discreto. Na Etapa 4, a ilustração aprovada do casal substitui esse placeholder, preservando sua proporção original com `ContentScale.Fit` e o dimensionamento atual do hero. A composição final foi aprovada visualmente em dispositivo físico. A saudação é o único texto de abertura.

`Pedir dengo` é a única ação principal. Bolsa quente, remédio, passar tempo juntos e `OTHER` formam um grid compacto de atalhos com ícones da mesma família. `OTHER` abre um diálogo curto e só permite envio com texto não vazio. O último pedido registrado aparece como feedback discreto, sem container próprio. O mood usa uma faixa horizontal rolável com emoji, rótulo e marca de seleção. O espaço pessoal mantém uma superfície suave; o mural mostra seu estado vazio sem card. Mood e espaço pessoal refletem o estado em memória; apenas a abertura do diálogo e o texto em edição são estado local de Compose.

O feedback compacto mantém como principal o pedido da Lidianne mais recente pela criação (ID desempata horários iguais). Se outros pedidos dela já tiverem recebido resposta, mostra apenas a quantidade desses pedidos além do principal; pedidos ainda pendentes não entram nessa contagem. A indicação não representa leitura ou notificação.

Essa projeção foi validada manualmente no Samsung na Etapa 5: com dois pedidos respondidos de formas diferentes, o mais recente apareceu como principal e o outro foi contabilizado; após criar um terceiro pedido pendente, ele se tornou o principal e os dois anteriores permaneceram contabilizados.

## Consequências

Esta Home é a referência visual aprovada da Etapa 3. A expansão para outras telas depende de decisões próprias para cada etapa.
