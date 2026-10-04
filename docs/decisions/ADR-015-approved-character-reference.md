# ADR-015 — Referência visual aprovada de Lidianne e Vinícius

## Status

Aceito como decisão de direção visual. O uso na tela de Perfil ainda não foi planejado nem implementado.

## Contexto

O Perfil atual serve principalmente para alternar localmente a perspectiva do protótipo. Já existiam nomes, acentos rosa/azul e uma ilustração afetiva do casal na Home de Lidianne, mas faltava uma referência consolidada para as identidades individuais. A investigação de Perfil apontou como direção a identidade individual dentro de um Perfil que também comunique o vínculo do casal.

## Decisão

A prancha `docs/design/references/approved/02-approved-lidianne-vinicius-characters.png`, criada a partir da aparência real de Lidianne e Vinícius e aprovada pelo responsável pelo produto, é a referência oficial da identidade e direção visual de cada personagem e do casal. Suas representações principais e propostas de avatares circulares tornam concreta a investigação visual do Perfil. As fotografias reais usadas na criação não fazem parte dos assets do projeto e não devem ser versionadas.

A aprovação é visual. Vistas, poses, expressões, roupas e ícones da prancha são exploração/referência, sem criar funcionalidades ou requisitos de uso. Não foram aprovados sistema de moods de personagens, personalização ou seleção de avatar, fotos reais no aplicativo, nem uso obrigatório dos avatares nas Homes, Mural ou Histórico.

## Relação com as referências anteriores

O ADR-004 e `01-approved-design-direction.png` registram a direção inicial da interface: base creme, acentos rosa/azul e composição afetiva. A prancha inicial usa um traço chibi e o nome “Patricia” em exemplos; a nova prancha prevalece para a identidade visual de **Lidianne** e Vinícius. A compatibilização do traço dos personagens com a interface antiga é questão de design para a próxima investigação, sem alterar agora os tokens ou telas.

O asset `app/src/main/res/drawable/dengo_couple_hero.png` continua em uso como composição afetiva da Home de Lidianne, aprovada no ADR-008. Esta decisão não o substitui.

## Consequências

O próximo trabalho de produto é planejar a evolução visual/UX do Perfil a partir dessa identidade individual e do vínculo do casal, preservando inicialmente o significado atual da troca local de perspectiva. O design final do Perfil, o recorte de assets para runtime e eventual presença dos avatares em outras telas continuam em aberto. Esta decisão não altera código nem comportamento do protótipo e não define uma nova etapa de implementação.
