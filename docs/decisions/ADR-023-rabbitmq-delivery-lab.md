# ADR-023 — Laboratório isolado de entrega com RabbitMQ

## Status

Aceito para laboratório técnico no alcance da mudança OpenSpec `study-rabbitmq-delivery-basics`, aprovada pelo responsável. Não aprova backend de produto, integração com Android ou próxima etapa de produto.

## Contexto e decisão

O primeiro laboratório backend mantém pedidos sintéticos apenas em memória. Publicar um evento após criar pedido nesse estado não demonstraria atomicidade, pois não há banco e outbox. O responsável escolheu estudar primeiro as duas confirmações distintas do RabbitMQ: publisher confirm e ACK do consumidor.

O perfil opcional `rabbit-lab` do mesmo build usa broker local via Docker Compose, exchange e fila duráveis e mensagens persistentes. `POST /lab/signals` recebe somente `eventId` UUID sintético e devolve `202` apenas após confirmação positiva e sem retorno por falta de rota. O worker envia ACK manual após processamento simulado; um teste injeta falha antes do ACK, reenfileira e observa a reentrega. O perfil padrão e `/lab/requests` permanecem independentes do broker.

## Garantias e limites

O `202` comprova aceitação e roteamento no broker, não processamento concluído. A reentrega pode repetir um efeito se houver falha entre efeito e ACK. Timeout ou perda da confirmação pode deixar o resultado incerto; não há reenvio automático e uma tentativa manual pode duplicar. O laboratório não fornece exatamente uma vez, deduplicação durável, atomicidade entre pedido e mensagem, retries limitados, DLQ ou outbox. Esses tópicos exigem outro recorte e hipótese própria. A fila não carrega dados afetivos e não participa do app.

## Verificação

Em 2026-10-30, `./gradlew -p labs/backend test rabbitIntegrationTest bootJar` passou com broker local; o teste de integração observou duas tentativas e uma conclusão após falha injetada, além do mandatory return de rota inexistente. Smoke test no perfil Rabbit retornou `202` para UUID válido e `400` para inválido. Com broker removido, o perfil padrão iniciou e retornou `200` em `/lab/requests` e `404` em `/lab/signals`. Veja `labs/backend/README.md` para reprodução e limpeza. Esta evidência não é validação de produto no Samsung.
