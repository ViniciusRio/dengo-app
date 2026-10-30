# Laboratório backend isolado

Este laboratório Kotlin/Spring Boot usa **apenas dados sintéticos** e memória do processo. Ele não é o backend do Dengo, não acessa Room e não se comunica com o app Android. O servidor escuta somente em `127.0.0.1:8081`; não use dados afetivos reais.

Requer JDK 21. Usa o wrapper Gradle 8.14.5 da raiz; o build Android continua separado.

## Reproduzir

Na raiz do repositório:

```bash
./gradlew -p labs/backend test bootJar
./gradlew -p labs/backend bootRun
```

Em outro terminal:

```bash
curl -i http://127.0.0.1:8081/lab/requests
curl -i -H 'Content-Type: application/json' -d '{"type":"DENGO"}' http://127.0.0.1:8081/lab/requests
curl -i -H 'Content-Type: application/json' -d '{"type":"DENGO"}' http://127.0.0.1:8081/lab/requests
curl -i http://127.0.0.1:8081/lab/requests
```

A primeira criação retorna `201`, a repetição retorna `200` com o mesmo ID, e a consulta não altera a coleção. Tipos admitidos: `DENGO`, `HOT_WATER_BAG`, `MEDICINE` e `SPEND_TIME_TOGETHER`. `OTHER`, tipos desconhecidos e entradas sem `type` retornam `400`.

Pare o processo e inicie novamente: a consulta volta a `[]`. Não há persistência, autenticação ou sincronização. O comando e a consulta usam a mesma coleção em memória; separar projeções ou bancos não traria ganho para este volume e esta finalidade. Este código é um exercício de semântica e verificação, sem aprovação para integrar o MVP 2.

## RabbitMQ: laboratório de entrega isolado

O perfil `rabbit-lab` acrescenta somente sinais sintéticos independentes de `/lab/requests`. Requer Docker e libera apenas a porta AMQP em `127.0.0.1:5672`; o HTTP permanece em `127.0.0.1:8081`. A imagem está fixada em `rabbitmq:4.2.9-alpine`. Use estes comandos na raiz:

```bash
docker compose -f labs/backend/compose.yaml up -d --wait
./gradlew -p labs/backend test rabbitIntegrationTest bootJar
./gradlew -p labs/backend bootRun --args='--spring.profiles.active=rabbit-lab'
```

Em outro terminal, faça o smoke test com UUID sintético:

```bash
curl -i -H 'Content-Type: application/json' -d '{"eventId":"123e4567-e89b-12d3-a456-426614174099"}' http://127.0.0.1:8081/lab/signals
curl -i -H 'Content-Type: application/json' -d '{"eventId":"bad"}' http://127.0.0.1:8081/lab/signals
```

O primeiro comando retorna `202` com o mesmo `eventId` **após** publisher confirm positivo e sem mandatory return; o segundo retorna `400`. Logs distinguem publicação, tentativa de entrega, reentrega e ACK. O teste `rabbitIntegrationTest` provoca falha antes do ACK na primeira entrega e observa duas tentativas e uma conclusão. A confirmação do publicador atesta aceitação e roteamento pelo broker, não o processamento pelo worker. Falha ou timeout de publicação retorna `503` sem reenvio automático; como a publicação pode ter ocorrido antes da perda da confirmação, repetir manualmente pode duplicar a entrega.

Pare o servidor e remova o broker e seu estado local com `docker compose -f labs/backend/compose.yaml down -v`. Sem Docker, `./gradlew -p labs/backend test` executa somente os testes independentes do broker; o perfil padrão inicia `/lab/requests` e `/lab/signals` responde `404`.

Este recorte não une criação de pedidos à fila: não existe escrita atômica nem outbox. O processamento é simulado e não oferece deduplicação durável; se o worker falhar após o efeito e antes do ACK, o efeito pode ocorrer de novo. Não há retry com limite, DLQ, notificação real, integração Android, sincronização ou backend de produto.
