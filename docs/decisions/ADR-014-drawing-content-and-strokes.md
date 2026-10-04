# ADR-014 — Conteúdo e traços dos recados desenhados

## Status

Direções técnicas aceitas após revisão humana da auditoria da implementação. O protótipo passou pela revisão contra a spec e pelos testes automatizados; após ajustes do compositor e da paleta, a evolução recebeu aprovação humana na validação final no Galaxy A26. O relato confirma abertura no tamanho esperado, rótulos das três cores em uma linha, troca Rosa → Azul → Escuro → Rosa sem recuo visual, desenho, cores, Desfazer, Limpar, Cancelar e publicação no Mural. A aprovação geral não comprova testes individuais de TalkBack, fonte ampliada ou previews nas duas Homes.

## Contexto

O [ADR-013](ADR-013-hand-drawn-mural-notes.md) aprovou o experimento de recados desenhados, mantendo texto e desenho como formatos alternativos na mesma coleção. A representação técnica ficou aberta. O protótipo continua em memória e precisa mostrar o traço real no Mural e nas duas Homes, com desfazer por traço.

## Decisão

`MuralNote` mantém ID, autoria e horário e contém um único `MuralContent`: texto ou desenho. O desenho guarda traços independentes da UI, com cor ARGB e pontos normalizados entre zero e um. A lógica de renderização é reutilizada no editor, no Mural e nas Homes. Um toque isolado também forma um traço visível.

O `FakeCoupleRepository` valida o desenho antes de atribuir ID e o publica na mesma lista dos recados textuais. Os traços concluídos do rascunho ficam no `MuralViewModel`; a captura enquanto o dedo toca a tela é transitória na UI. A publicação copia os traços concluídos e fecha a composição, impedindo acionamento duplicado do mesmo rascunho. Sair com traços concluídos exige continuar ou descartar.

## Consequências

Texto e desenho compartilham ordenação, autoria e previews. O conteúdo tipado exclui a combinação dos dois formatos; conteúdo vazio ainda pode ser construído no modelo, mas o repository impede sua publicação. Os dados não dependem de `Path`, `Canvas` ou classes de cor do Compose.

Na implementação atual, a captura registra a proporção da superfície por traço. `DrawingStroke.aspectRatio` admite `null`; nesse caso, a renderização usa a proporção de `Drawing`. O ajuste proporcional com centralização por traço é uma escolha de implementação em avaliação, não uma decisão congelada por este ADR: o relato da aprovação geral não discrimina avaliação individual de sua fidelidade visual.

O protótipo ainda perde recados e rascunhos ao morrer o processo. Persistência, sincronização, exportação e limite de pontos permanecem fora do experimento. A validação geral no Galaxy A26 não discrimina avaliação de conforto ao escrever, legibilidade de diferentes desenhos ou peso visual de vários recados.
