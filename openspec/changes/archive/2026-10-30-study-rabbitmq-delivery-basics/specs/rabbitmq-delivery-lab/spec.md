## ADDED Requirements

### Requirement: Laboratório de mensageria isolado
O perfil padrão do backend SHALL iniciar e atender `/lab/requests` sem RabbitMQ. O perfil `rabbit-lab` SHALL usar apenas broker local com portas no loopback e SHALL manter os sinais independentes dos pedidos e do app Android.

#### Scenario: Perfil padrão sem broker
- **WHEN** o broker não está em execução e o backend inicia sem o perfil `rabbit-lab`
- **THEN** `/lab/requests` continua disponível, e o backend não tenta publicar ou consumir sinais

#### Scenario: Perfil de laboratório
- **WHEN** o broker local está disponível e o backend inicia com `rabbit-lab`
- **THEN** o endpoint de sinais e o worker são ativados sem criar pedidos ou alterar dados Room

### Requirement: Publicação confirmada de sinal sintético
`POST /lab/signals` no perfil `rabbit-lab` SHALL aceitar somente JSON com `eventId` UUID e publicar mensagem sintética persistente em fila durável. HTTP `202` SHALL ser retornado somente após confirmação positiva e roteamento ao broker; não significa processamento pelo consumidor. Entrada inválida SHALL retornar HTTP `400` sem publicação. Falha, retorno por falta de rota ou confirmação inconclusiva SHALL não provocar reenvio automático.

#### Scenario: Publicação aceita
- **WHEN** um UUID válido é enviado e o broker confirma a mensagem roteada
- **THEN** o endpoint retorna HTTP `202` com o mesmo `eventId`

#### Scenario: Entrada inválida
- **WHEN** o JSON não contém UUID válido ou contém dados além de `eventId`
- **THEN** o endpoint retorna HTTP `400` e nenhuma mensagem é publicada

#### Scenario: Publicação não confirmada
- **WHEN** o broker rejeita, devolve por falta de rota ou não confirma a publicação
- **THEN** o endpoint não retorna `202` e não repete automaticamente o comando

### Requirement: ACK e reentrega observáveis
O worker SHALL enviar ACK manual somente após processamento simulado bem-sucedido. Falha provocada antes do ACK SHALL deixar a mensagem elegível para reentrega, preservando o `eventId`. O laboratório SHALL distinguir tentativas de entrega de processamentos concluídos.

#### Scenario: Processamento normal
- **WHEN** o worker recebe um sinal e o processamento simulado termina com sucesso
- **THEN** ele registra uma conclusão e envia ACK; o broker não mantém a entrega pendente

#### Scenario: Falha antes do ACK
- **WHEN** o primeiro processamento de um sinal falha antes do ACK e a entrega é reenfileirada
- **THEN** o mesmo `eventId` é entregue novamente, com duas tentativas observáveis e uma conclusão bem-sucedida no teste controlado
