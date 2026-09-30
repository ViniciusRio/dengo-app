# Design System — Direção Aprovada

## Personalidade

- cuidadoso;
- afetivo;
- amigável;
- leve;
- íntimo sem ser infantil demais;
- limpo e com baixa densidade visual.

## Tema

Claro no MVP.

## Paleta

A paleta abaixo é a direção inicial aprovada. Pequenos ajustes de contraste são permitidos durante a implementação para atender acessibilidade.

### Base

- `background`: `#FFF9F7` — creme/off-white
- `surface`: `#FFFFFF`
- `surfaceSoft`: `#FFF2F4`
- `textPrimary`: `#292426`
- `textSecondary`: `#766B6E`
- `outline`: `#E9DEE1`

### Lidianne / rosa

- `lidiannePrimary`: `#D96C83`
- `lidianneSoft`: `#F7DCE3`
- `lidianneAccent`: `#EAA0AE`

### Vinícius / azul

- `viniciusPrimary`: `#5E8FC7`
- `viniciusSoft`: `#DDEAF7`
- `viniciusAccent`: `#8CB3DD`

### Semânticas

Definir durante implementação respeitando contraste:
- `success`
- `warning`
- `error`

Mood nunca deve depender apenas de cor: sempre combinar emoji/ícone + texto.

## Uso das cores por pessoa

Rosa e azul são **acentos contextuais**, não temas completamente separados. A base creme/branca e a tipografia são compartilhadas.

- Home de Lidianne: rosa domina CTAs, seleções e detalhes.
- Home de Vinícius: azul domina identidade própria; cards relacionados a Lidianne podem usar rosa discretamente para indicar origem/contexto.
- Evitar o clichê visual de dividir toda a interface em “rosa versus azul”.

## Espaçamento

Escala: `4 / 8 / 12 / 16 / 24 / 32 / 48 dp`.

- margem horizontal padrão: 20–24 dp;
- entre elementos do mesmo grupo: 8–12 dp;
- entre grupos/seções: 24–32 dp.

## Bordas

- small: 8 dp
- medium: 12 dp
- large: 20 dp
- extra-large/hero: 24 dp
- pill: somente para chips/CTAs que realmente pedirem esse formato

## Tipografia

Usar fonte Android legível e amigável. Material 3 typography é a base inicial; não usar fonte decorativa em conteúdo funcional.

Hierarquia:
- `displaySmall` — mensagens curtas/hero quando necessário;
- `headlineMedium` — título principal;
- `titleLarge` — seção;
- `titleMedium` — card;
- `bodyLarge` — conteúdo principal;
- `bodyMedium` — secundário;
- `labelLarge` — botões;
- `labelMedium` — metadados.

## Elevação

Baixa. Preferir diferença de superfície e borda suave a sombras pesadas.

## Ícones

Simples e consistentes. Usar texto quando o significado não for universal.

## Personagens

Os personagens são elementos emocionais importantes, mas não devem substituir informação funcional. Eles ocupam a região hero das Homes e podem aparecer em estados/contextos específicos.

## Movimento

Mínimo:
- confirmação de pedido;
- seleção de mood;
- mudança de estado;
- pequenas reações futuras do personagem.

## Acessibilidade

- contraste adequado;
- não comunicar estado somente por cor;
- alvos de toque confortáveis;
- suportar font scaling;
- respeitar safe areas;
- considerar diferentes tamanhos Android;
- manter labels acessíveis para ícones e imagens importantes.
