## ADDED Requirements

### Requirement: Isolamento do laboratório
O laboratório SHALL iniciar como processo local independente do app Android, com armazenamento somente em memória e sem acesso a dados reais do Dengo. O servidor SHALL escutar somente em `127.0.0.1:8081`.

#### Scenario: Primeira execução
- **WHEN** o laboratório inicia e `GET /lab/requests` é chamado antes de qualquer comando
- **THEN** a resposta é HTTP 200 com uma lista vazia

#### Scenario: Reinício do processo
- **WHEN** um pedido sintético é criado e o processo do laboratório é reiniciado
- **THEN** `GET /lab/requests` retorna uma lista vazia, sem afetar os dados Room do app Android

### Requirement: Criação de pedido predefinido
O laboratório SHALL aceitar em `POST /lab/requests` um JSON com `type` igual a `DENGO`, `HOT_WATER_BAG`, `MEDICINE` ou `SPEND_TIME_TOGETHER`. Uma criação nova SHALL retornar HTTP 201 com ID positivo, tipo, estado `PENDING` e horário UTC. Entrada ausente, malformada, `OTHER` ou tipo desconhecido SHALL retornar HTTP 400 sem criar pedido.

#### Scenario: Pedido novo
- **WHEN** `POST /lab/requests` recebe `{"type":"DENGO"}` em estado vazio
- **THEN** responde HTTP 201 com o pedido `DENGO` em `PENDING` e ID `1`

#### Scenario: Entrada inválida
- **WHEN** o comando recebe um tipo não admitido ou JSON sem `type`
- **THEN** responde HTTP 400 e a consulta permanece vazia

### Requirement: Duplicidade e concorrência
O laboratório SHALL manter no máximo um pedido ativo por tipo predefinido no processo. Repetir a criação do mesmo tipo SHALL devolver o pedido existente com HTTP 200. Duas criações concorrentes do mesmo tipo SHALL produzir um único pedido.

#### Scenario: Mesmo tipo repetido
- **WHEN** dois comandos `DENGO` são recebidos em sequência
- **THEN** o primeiro responde HTTP 201, o segundo HTTP 200 com o mesmo ID e a consulta contém um único pedido

#### Scenario: Mesmo tipo simultâneo
- **WHEN** dois comandos `MEDICINE` são processados simultaneamente
- **THEN** exatamente um cria o pedido e ambos observam o mesmo ID, sem duplicação na consulta

### Requirement: Consulta de pedidos
`GET /lab/requests` SHALL retornar HTTP 200 com uma cópia dos pedidos do processo ordenados por ID crescente. A consulta SHALL não alterar o estado nem criar acontecimentos de Histórico do produto.

#### Scenario: Tipos distintos
- **WHEN** são criados pedidos `DENGO` e `MEDICINE`, nessa ordem, e a lista é consultada duas vezes
- **THEN** ambas as consultas retornam os dois pedidos na mesma ordem, com IDs `1` e `2`
