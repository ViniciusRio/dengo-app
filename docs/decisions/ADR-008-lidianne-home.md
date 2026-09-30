# ADR-008 — Primeira Home da Lidianne

## Status

Aceito após validação funcional e visual da versão 3.1 em dispositivo físico. Etapa 3 concluída.

## Decisão

A Home da Lidianne usa o `HomeViewModel` da Etapa 2 e apresenta, nesta ordem, saudação, hero do casal, ação principal de dengo, pedidos rápidos, mood, espaço pessoal e mural vazio. Os outros destinos mantêm seus placeholders.

Como os personagens ainda não têm assets isolados, o hero mantém uma área aberta com um fundo suave e um placeholder discreto para a futura ilustração. A composição usa os tokens existentes de creme, rosa e azul, sem representar o placeholder como arte final. A saudação é o único texto de abertura.

`Pedir dengo` é a única ação principal. Bolsa quente, remédio, passar tempo juntos e `OTHER` formam um grid compacto de atalhos com ícones da mesma família. `OTHER` abre um diálogo curto e só permite envio com texto não vazio. O último pedido registrado aparece como feedback discreto, sem container próprio. O mood usa uma faixa horizontal rolável com emoji, rótulo e marca de seleção. O espaço pessoal mantém uma superfície suave; o mural mostra seu estado vazio sem card. Mood e espaço pessoal refletem o estado em memória; apenas a abertura do diálogo e o texto em edição são estado local de Compose.

## Consequências

Esta Home é a referência visual aprovada da Etapa 3. A expansão para outras telas depende de decisões próprias para cada etapa.
