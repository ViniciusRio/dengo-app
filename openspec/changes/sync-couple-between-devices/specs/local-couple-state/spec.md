## MODIFIED Requirements

### Requirement: Recuperação do estado publicado
O aplicativo SHALL recuperar após encerramento do processo e reabertura, na mesma instalação e com sessão válida, a última cópia local dos pedidos e seus estados, eventos factuais do Histórico, mood atual de Lidianne, espaço pessoal e recados publicados de texto ou desenho, e SHALL atualizá-la a partir do servidor quando houver conexão.

#### Scenario: Reabertura com dados publicados
- **WHEN** o casal publica interações, o processo termina e o app é reaberto
- **THEN** o aparelho mostra o estado sincronizado anteriormente, com IDs, autoria, conteúdo e horários preservados, e depois o atualiza a partir do servidor

#### Scenario: Primeira abertura sem dados
- **WHEN** o app é aberto logo após o vínculo e o servidor não tem interações
- **THEN** ele apresenta o estado inicial vazio com os dois parceiros conhecidos e sem interações demonstrativas criadas automaticamente

#### Scenario: Banco anterior preservado
- **WHEN** o app atualizado encontra um `dengo-local.db` de versão anterior
- **THEN** o arquivo permanece intacto, não é lido como fonte de dados e não é enviado ao servidor

### Requirement: Resultado de escrita confiável
O aplicativo SHALL respeitar o contrato de `WriteResult` e cancelamento: `Confirmed` somente depois da confirmação durável do servidor, `Reverted` para recusa conhecida ou falha conhecida antes do envio, e `Uncertain` quando não for possível saber se o servidor confirmou, sem reenvio automático. Após `Uncertain`, o estado de leitura SHALL permitir conferir o resultado por sincronização, sem presumir que a operação foi gravada ou revertida.

#### Scenario: Escrita confirmada
- **WHEN** uma escrita retorna confirmação
- **THEN** seu resultado está guardado no servidor e chega à cópia local pela sincronização

#### Scenario: Falha conhecida antes do envio
- **WHEN** não foi possível conectar ao servidor
- **THEN** o app recebe `Reverted`, informa a falta de conexão e não repete a operação

#### Scenario: Resultado incerto
- **WHEN** a conexão se perde depois do envio, o tempo esgota ou o servidor responde com erro interno
- **THEN** o app informa resultado incerto e não repete a operação automaticamente

#### Scenario: Pedido predefinido gravado com resposta perdida
- **WHEN** a criação de um pedido predefinido é confirmada no servidor, mas a resposta se perde e o resultado é `Uncertain`
- **THEN** o app não envia o comando de novo, a sincronização permite conferir o pedido, e há um pedido e um único acontecimento de criação

#### Scenario: Pedido predefinido não gravado com resposta incerta
- **WHEN** a criação de um pedido predefinido retorna `Uncertain` e o servidor não concluiu a transação
- **THEN** o app não envia o comando de novo, a sincronização mostra a ausência do pedido e do acontecimento, e uma nova tentativa depende de ação explícita após conferência

#### Scenario: Cancelamento
- **WHEN** a operação é cancelada no aparelho
- **THEN** o cancelamento é propagado, não é apresentado como confirmação, e a sincronização seguinte mostra o resultado real

### Requirement: Dados fora do escopo
O aplicativo SHALL manter rascunhos fora do armazenamento durável, exceto o conteúdo de uma tentativa de publicação ainda sem resultado definitivo, conforme a spec `couple-sync`, e SHALL manter desativado o backup automático do app. A identidade da sessão SHALL ser guardada conforme a spec `device-identity`.

#### Scenario: Reabertura com rascunho e perspectiva
- **WHEN** um rascunho não publicado é deixado no app antes de encerrar o processo
- **THEN** apenas as interações publicadas são recuperadas na reabertura, e a perspectiva segue a pessoa da sessão

#### Scenario: Desinstalação
- **WHEN** o app é desinstalado, instalado novamente e vinculado com um novo código
- **THEN** as interações voltam pela sincronização com o servidor, e rascunhos não são recuperados
