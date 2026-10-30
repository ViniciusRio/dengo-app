## MODIFIED Requirements

### Requirement: Resultado de escrita confiável
O aplicativo SHALL respeitar o contrato de `WriteResult` e cancelamento do Incremento 1: confirmar apenas escrita durável, reverter rejeição conhecida e tratar resultado de durabilidade desconhecida como incerto sem reenvio automático. Após `Uncertain`, o estado de leitura SHALL permitir conferir o resultado quando a leitura estiver disponível, sem presumir que a operação foi gravada ou revertida.

#### Scenario: Escrita confirmada
- **WHEN** uma escrita retorna confirmação
- **THEN** seu resultado está recuperável após reabertura

#### Scenario: Resultado incerto
- **WHEN** uma falha impede determinar se a escrita foi confirmada
- **THEN** o app informa resultado incerto e não repete a operação automaticamente

#### Scenario: Pedido predefinido gravado com resposta perdida
- **WHEN** a criação de um pedido predefinido é confirmada no banco, mas a resposta ao chamador é substituída por `Uncertain`
- **THEN** o app não envia o comando de novo, o estado legível permite conferir o pedido e, após reabertura, há um pedido e um único acontecimento de criação no Histórico

#### Scenario: Pedido predefinido não gravado com resposta incerta
- **WHEN** a criação de um pedido predefinido retorna `Uncertain` após falha que impede a transação de concluir
- **THEN** o app não envia o comando de novo, e a leitura ou reabertura mostra ausência tanto do pedido quanto do acontecimento relacionado; uma nova tentativa depende de ação explícita após conferência

#### Scenario: Cancelamento
- **WHEN** a operação é cancelada
- **THEN** o cancelamento é propagado e não é apresentado como confirmação
