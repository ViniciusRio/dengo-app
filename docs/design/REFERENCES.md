# Referências de UI/UX

## Regra central

As imagens originais são **inspiração**, não especificação para cópia. A arte aprovada criada para este projeto define a direção visual atual, mas também não é pixel-perfect.

## Referências aprovadas

`references/approved/01-approved-design-direction.png`

Direção inicial de interface validada:
- Lidianne em rosa;
- Vinícius em azul;
- fundo claro/creme;
- cards suaves;
- bordas arredondadas;
- linguagem afetiva;
- Homes com prioridades diferentes para cada pessoa.

Esta prancha inicial é referência de interface, não a identidade individual vigente dos personagens. Ela traz o nome “Patricia” para a personagem feminina e um traço chibi diferente da prancha de personagens posterior. O produto e a identidade aprovada usam **Lidianne**. O Perfil 1.1 usou a identidade posterior no checkpoint do ADR-017; a composição B.1 vigente também a preserva, no alcance do ADR-018, sem descartar a direção de interface anterior.

`references/approved/02-approved-lidianne-vinicius-characters.png`

Prancha aprovada para a **identidade e direção visual** de Lidianne, Vinícius e do casal, criada com base em sua aparência real. Reúne representações principais e conjunta, vistas de frente/lado/costas, expressões, propostas de avatares circulares e pequenos ícones. Os avatares circulares foram recortados para o Perfil e permanecem na composição B.1, aprovada visualmente no Galaxy A26 no alcance do ADR-018, sem autorizar uso transversal. As fotografias reais usadas como referência não fazem parte dos assets do projeto e não devem ser adicionadas ao repositório.

A ilustração `app/src/main/res/drawable/dengo_couple_hero.png` continua na Home de Lidianne, conforme o ADR-008, e foi reutilizada nos checkpoints anteriores do Perfil. O Perfil B.1 usa a ilustração transparente `app/src/main/res/drawable-nodpi/dengo_profile_couple_cutout.png` e o lettering `app/src/main/res/drawable-nodpi/dengo_profile_couple_lettering.png`, aprovados nesta composição específica; ver ADR-018. A nova arte não substitui automaticamente o hero da Home. Outros usos dos assets e avatares exigem decisão própria.

A aprovação da prancha não torna obrigatórios poses, expressões, roupas específicas, moods de personagem, personalização, seleção de avatar, fotos reais no app ou avatares nas Homes, Mural e Histórico. Ver [ADR-015](../decisions/ADR-015-approved-character-reference.md) e [Personagens](CHARACTERS.md).

## Originais

Em `references/original/` ficam os prints usados para inspiração inicial.

### Manter como inspiração

- Home personalizada pelo nome;
- personagem como elemento emocional central;
- ação afetiva principal;
- pedidos rápidos em cards fáceis de tocar;
- mood visual e imediato;
- bottom navigation simples;
- bastante respiro;
- linguagem curta e carinhosa;
- feedback de pedidos pendentes;
- mural como elemento secundário.

### Adaptar

- substituir a forte identidade laranja/coral por rosa + azul sobre base creme;
- manter `Pedir dengo` como linguagem possível, mas permitir refinamento do vocabulário;
- compartilhar componentes entre as duas Homes, mudando prioridade/conteúdo;
- emojis complementam clareza textual; personagens não substituem labels.

### Evitar

- copiar personagens, ilustrações ou composição exata;
- excesso de decoração;
- cards demais competindo pela atenção;
- tratar `quero ficar sozinha` como tarefa que exige resposta;
- comunicar estado somente por cor.

## Mapeamento dos originais

- `01-request-received.jpeg` — pedido recebido e ações.
- `02-home-reference.jpeg` — Home, CTA e pedidos rápidos.
- `03-home-mood-reference.jpeg` — Home, personagem e navegação.
- `04-character-male-reference.jpeg` — linguagem de personagem masculino.
- `05-character-female-reference.jpeg` — linguagem de personagem feminino.
- `06-mood-mural-reference.jpeg` — mood, mural e bottom navigation.
