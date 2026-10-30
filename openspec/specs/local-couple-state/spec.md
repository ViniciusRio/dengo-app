# local-couple-state Specification

## Purpose
Preservar no mesmo aparelho as interações publicadas do casal para que Home, Histórico e Mural recuperem o mesmo estado após o encerramento do processo, sem perder dados diante de falhas de leitura ou escrita.

## Requirements

### Requirement: Recuperação do estado publicado
O aplicativo SHALL recuperar após encerramento do processo e reabertura, na mesma instalação, os pedidos e seus estados, eventos factuais do Histórico, mood atual de Lidianne, espaço pessoal e recados publicados de texto ou desenho.

#### Scenario: Reabertura com dados publicados
- **WHEN** o casal publica interações, o processo termina e o app é reaberto
- **THEN** as duas perspectivas recebem o mesmo estado publicado anteriormente, com IDs, autoria, conteúdo e horários preservados

#### Scenario: Primeira abertura sem dados
- **WHEN** o app é aberto em uma instalação sem dados publicados
- **THEN** ele apresenta o estado inicial vazio com os dois parceiros conhecidos e sem interações demonstrativas criadas automaticamente

### Requirement: Pedidos e acontecimentos consistentes
O aplicativo SHALL persistir cada criação, resposta ou agradecimento de pedido junto com seu acontecimento factual de Histórico, mantendo as transições e restrições vigentes em `docs/features/requests.md`.

#### Scenario: Pedido respondido e reaberto
- **WHEN** um pedido é criado, aceito ou recusado e depois o app é reaberto
- **THEN** o pedido mantém seu estado e o Histórico contém os acontecimentos correspondentes uma única vez

#### Scenario: Operação de pedido falha
- **WHEN** a gravação de uma criação, resposta ou agradecimento falha antes de confirmar
- **THEN** nem a mudança do pedido nem seu acontecimento relacionado ficam parcialmente persistidos

#### Scenario: Novo pedido após reabertura
- **WHEN** o app é reaberto e uma nova interação é publicada
- **THEN** seus IDs não colidem com os já persistidos e as restrições de duplicidade de pedidos predefinidos continuam válidas

### Requirement: Mood e espaço pessoal duráveis
O aplicativo SHALL recuperar o mood atual e o estado de espaço pessoal após reabertura, preservando os acontecimentos factuais correspondentes e a regra de mudança explícita.

#### Scenario: Mood e espaço pessoal após reabertura
- **WHEN** Lidianne altera o mood, ativa o espaço pessoal e o app é reaberto
- **THEN** o mood, o espaço pessoal ativo e seus acontecimentos permanecem visíveis nas perspectivas pertinentes

#### Scenario: Ação sem mudança efetiva
- **WHEN** uma ação de mood ou espaço pessoal não altera o estado segundo as regras vigentes
- **THEN** ela não acrescenta acontecimento duplicado ao Histórico, inclusive depois de reabrir o app

### Requirement: Recados publicados duráveis
O aplicativo SHALL recuperar recados publicados de texto e desenho completo com autoria, horário, ordem e conteúdo suficientes para mostrar o recado no Mural e o preview nas duas Homes.

#### Scenario: Recado de texto recuperado
- **WHEN** um recado textual é publicado e o app é reaberto
- **THEN** o texto, a autoria e o horário aparecem no Mural e no preview quando ele é o mais recente

#### Scenario: Desenho recuperado
- **WHEN** um recado desenhado é publicado e o app é reaberto
- **THEN** todos os traços, suas cores, pontos e proporções são recuperados para renderizar o desenho inteiro no Mural e no preview quando ele é o mais recente

#### Scenario: Publicação de desenho falha
- **WHEN** a gravação de um recado desenhado falha antes da confirmação
- **THEN** nenhum recado incompleto ou conjunto parcial de traços aparece após nova leitura ou reabertura

#### Scenario: Ordem de recados preservada
- **WHEN** recados compartilham o mesmo horário de criação e o app é reaberto
- **THEN** o recado com maior ID continua aparecendo primeiro

### Requirement: Leitura falha sem apagar dados
O aplicativo SHALL preservar o armazenamento existente diante de falha de leitura, apresentar estado de erro e permitir nova tentativa sem reset automático.

#### Scenario: Falha e nova tentativa
- **WHEN** uma leitura falha e a pessoa usa a ação de tentar novamente
- **THEN** o app tenta ler os mesmos dados preservados e mostra o estado recuperado caso a leitura tenha êxito

#### Scenario: Falha persistente
- **WHEN** a leitura continua falhando
- **THEN** o app permanece em estado de erro sem apagar, recriar ou substituir o banco por um estado vazio

### Requirement: Resultado de escrita confiável
O aplicativo SHALL respeitar o contrato de `WriteResult` e cancelamento do Incremento 1: confirmar apenas escrita durável, reverter rejeição conhecida e tratar resultado de durabilidade desconhecida como incerto sem reenvio automático.

#### Scenario: Escrita confirmada
- **WHEN** uma escrita retorna confirmação
- **THEN** seu resultado está recuperável após reabertura

#### Scenario: Resultado incerto
- **WHEN** uma falha impede determinar se a escrita foi confirmada
- **THEN** o app informa resultado incerto e não repete a operação automaticamente

#### Scenario: Cancelamento
- **WHEN** a operação é cancelada
- **THEN** o cancelamento é propagado e não é apresentado como confirmação

### Requirement: Dados fora do escopo
O aplicativo SHALL manter rascunhos, perspectiva ativa e identidade de sessão fora do armazenamento durável deste incremento; SHALL manter desativado o backup automático do app.

#### Scenario: Reabertura com rascunho e perspectiva
- **WHEN** um rascunho não publicado e uma perspectiva local são deixados no app antes de encerrar o processo
- **THEN** apenas as interações publicadas são recuperadas do banco na reabertura

#### Scenario: Desinstalação
- **WHEN** o app é desinstalado e instalado novamente
- **THEN** este incremento não oferece recuperação das interações anteriores
