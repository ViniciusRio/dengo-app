## ADDED Requirements

### Requirement: Notificações visíveis limitadas
O sistema SHALL mostrar notificação visível somente para pedido novo a Vinícius, sem espaço pessoal ativo, e para resposta ao pedido a Lidianne. Agradecimento, mood, espaço pessoal, recado e pedido criado com espaço pessoal ativo SHALL atualizar a outra pessoa sem notificação visível. Quem fez a ação SHALL NOT ser notificado por ela.

#### Scenario: Pedido novo com o app fechado
- **WHEN** Lidianne cria um pedido, sem espaço pessoal ativo, e o app de Vinícius está fechado
- **THEN** Vinícius recebe uma notificação e, ao tocar, abre a Home com o pedido

#### Scenario: Resposta com o app fechado
- **WHEN** Vinícius responde e o app de Lidianne está fechado
- **THEN** Lidianne recebe uma notificação que indica a resposta

#### Scenario: Pedido com espaço pessoal ativo
- **WHEN** Lidianne cria um pedido com o espaço pessoal ativo
- **THEN** Vinícius não recebe notificação visível e vê o pedido na Home ao abrir o app

#### Scenario: Acontecimento sem notificação
- **WHEN** ocorre agradecimento, mudança de mood, mudança de espaço pessoal ou novo recado
- **THEN** não há notificação visível, e a outra pessoa vê a mudança ao sincronizar

### Requirement: Aviso sem conteúdo afetivo
O aviso enviado pelo FCM SHALL conter somente o tipo do aviso e a revisão. O app SHALL buscar os dados no servidor para montar a notificação, SHALL usar texto genérico se a busca falhar e SHALL NOT incluir o texto de um pedido `OTHER` na notificação.

#### Scenario: Busca concluída
- **WHEN** o aviso chega e a busca termina a tempo
- **THEN** a notificação é montada com os dados vindos do servidor

#### Scenario: Busca falha
- **WHEN** o aviso chega e a busca falha
- **THEN** o app mostra a notificação genérica sem conteúdo do pedido

#### Scenario: Pedido com texto livre
- **WHEN** a notificação se refere a um pedido `OTHER`
- **THEN** o texto do pedido aparece só dentro do app

### Requirement: Entrega não bloqueia a escrita
O servidor SHALL registrar o aviso na mesma transação da escrita, SHALL enviá-lo com tentativas limitadas e SHALL NOT desfazer a escrita por falha de entrega. O app SHALL NOT mostrar duas notificações para o mesmo acontecimento.

#### Scenario: FCM indisponível
- **WHEN** o FCM está indisponível no envio
- **THEN** a escrita continua confirmada e novos envios são tentados até o limite, e a alteração aparece na próxima sincronização

#### Scenario: Reinício do servidor
- **WHEN** o servidor reinicia depois de confirmar a escrita e antes de enviar o aviso
- **THEN** o aviso pendente é enviado ao voltar, se ainda estiver dentro do prazo

#### Scenario: Aviso repetido
- **WHEN** o mesmo aviso é entregue mais de uma vez
- **THEN** o app mostra uma única notificação

### Requirement: Permissão recusada ou token ausente
O app SHALL funcionar sem notificações quando a permissão for negada ou a configuração do Firebase não existir, e SHALL atualizar o token do FCM no servidor quando ele mudar.

#### Scenario: Permissão negada
- **WHEN** a pessoa nega a permissão de notificação
- **THEN** o app continua funcionando, e as mudanças aparecem ao abrir o app

#### Scenario: Build sem configuração
- **WHEN** o app é compilado sem configuração do Firebase
- **THEN** o build e os testes passam, com notificações desligadas

#### Scenario: Token renovado
- **WHEN** o FCM renova o token do aparelho
- **THEN** o servidor passa a usar o novo token da sessão daquela pessoa
