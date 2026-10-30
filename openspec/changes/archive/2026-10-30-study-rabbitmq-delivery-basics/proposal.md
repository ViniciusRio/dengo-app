# Proposal

## Why

O primeiro laboratório backend já demonstra comando e consulta com estado volátil, sem integração com o Android. O próximo aprendizado escolhido é RabbitMQ. A menor fatia útil é observar separadamente a confirmação do broker ao publicador e o ACK do consumidor, inclusive uma reentrega após falha provocada. O laboratório atual não tem armazenamento durável para sustentar uma outbox; ligar a fila à criação de pedidos agora sugeriria uma garantia de atomicidade que não existe.

## What Changes

- Acrescentar ao build `labs/backend/` um perfil `rabbit-lab`, desligado por padrão, para publicar e consumir sinais sintéticos por RabbitMQ local.
- Subir o broker por Docker Compose com portas limitadas ao loopback e versão de imagem fixada.
- Expor somente no perfil do laboratório `POST /lab/signals`, com identificador sintético fornecido pelo chamador. Responder com aceite apenas depois de publisher confirm positivo e roteamento confirmado.
- Consumir com ACK manual após o processamento simulado; demonstrar em teste uma falha antes do ACK seguida de reentrega.
- Registrar comandos, resultados e limites: entrega não é processamento exatamente uma vez; não há atomicidade entre a API de pedidos e a fila.

## Capabilities

### New Capabilities

- `rabbitmq-delivery-lab`: publicação confirmada e consumo com ACK/reentrega de sinais sintéticos em ambiente local.

### Modified Capabilities

Nenhuma capacidade do produto Android ou do laboratório `backend-care-request-lab`; a API de pedidos continua independente da fila.

## Impact

- Código, testes e reprodução sob `labs/backend/`, com perfil Spring separado e Docker Compose local.
- Sem mudança no app Android, em Room, em `/lab/requests` ou na spec de persistência local.
- Sem notificações reais, backend de produto, sincronização, outbox, DLQ, Kafka, SQS, Redis ou AWS neste incremento.

Esta proposta registra o escopo de estudo escolhido; não declara o broker implementado, testes executados ou nova etapa de produto aprovada.
