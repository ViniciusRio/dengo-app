# Componentes de UI

## Estrutura

### AppScaffold
Safe areas, conteúdo e bottom navigation.

### GreetingHeader
Saudação personalizada (`Olá, Lidianne` / `Olá, Vinícius`) e contexto curto.

### PartnerCharacter
Exibe personagem, expressão e descrição acessível.

### PrimaryCareAction
CTA afetivo principal, como `Pedir dengo ♡`.

### QuickRequestCard
Card reutilizável com ícone, título e feedback de toque.

### IncomingRequestCard
Pedido recebido com tipo, horário, estado e ações (`Estou indo ❤️`, `Não consigo agora`).

### MoodSelector
Moods com emoji + rótulo; estado selecionado evidente sem depender só de cor.

### MoodSummary
Resumo do mood atual de Lidianne na Home de Vinícius.

### PersonalSpaceCard
Estado especial para `Preciso ficar sozinha`, com linguagem calma e sem pressão.

### SuggestionCard
Sugestão contextual como desenhar, mandar recado ou oferecer companhia.

### HistoryItem
Evento cronológico com horário, descrição e estado.

### MuralPreview
Preview discreto do recado mais recente, com autoria e trecho, ou estado vazio; abre a aba Mural. Desenhos permanecem possibilidade futura.

### BottomNavigation
Destinos iniciais:
- Início
- Mural
- Histórico
- Perfil

## Componentes de apoio

- `SectionHeader`
- `EmptyState`
- `StatusChip`
- `CharacterHero`
- `SoftCard`
- `PrimaryButton`
- `SecondaryButton`

## Regra

Não criar componentes diferentes apenas porque aparecem em telas diferentes. Se função e hierarquia forem equivalentes, reutilizar o mesmo componente e variar conteúdo/estado.
