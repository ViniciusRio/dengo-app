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

Pare o processo e inicie novamente: a consulta volta a `[]`. Não há persistência, autenticação, sincronização ou fila. O comando e a consulta usam a mesma coleção em memória; separar projeções ou bancos não traria ganho para este volume e esta finalidade. Este código é um exercício de semântica e verificação, sem aprovação para integrar o MVP 2.
