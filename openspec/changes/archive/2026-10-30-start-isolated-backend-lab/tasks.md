# Tasks

## 1. Build e módulo de domínio

- [x] 1.1 Atualizar o wrapper Gradle da raiz para 8.14.5 sem mudar AGP/Kotlin Android; confirmar `./gradlew --version` e a regressão Android de base.
- [x] 1.2 Criar `labs/backend/` como build independente com Kotlin 2.2.20, Spring Boot 4.1.2 e Java 21; escrever primeiro teste de domínio para estado vazio/criação e observar falha antes de implementar.
- [x] 1.3 Implementar o módulo em memória com criação e consulta; testar duplicidade, tipos distintos, cópia ordenada e concorrência com relógio controlado.

## 2. HTTP local e reprodução

- [x] 2.1 Escrever primeiro testes HTTP para criação 201, repetição 200, entrada inválida 400 e consulta 200; implementar o controlador mínimo e executar os testes.
- [x] 2.2 Configurar escuta em `127.0.0.1:8081`; documentar execução, `curl` sintético, perda de dados no reinício e ausência de integração com Android.
- [x] 2.3 Executar smoke test local e conferir o endereço de escuta; não publicar o serviço nem usar dados afetivos reais.

## 3. Verificação e fechamento do laboratório

- [x] 3.1 Executar testes e build do laboratório; executar testes unitários debug/release e builds debug/androidTest do app após a atualização do wrapper. Registrar comandos e resultados reais.
- [x] 3.2 Verificar disponibilidade do A26 e distinguir execução automatizada de validação humana. O A26 não apareceu em `adb devices -l` nesta rodada; não houve suíte instrumentada Android após a troca do wrapper.
- [x] 3.3 Revisar o diff contra esta spec, validar OpenSpec em modo estrito e registrar ganho, custo e limite da distinção entre comando e consulta. Considerar ADR técnico somente se uma decisão estrutural duradoura resultar do exercício.
- [x] 3.4 Arquivar a mudança OpenSpec e atualizar `docs/STATUS.md` e o checkpoint do vault somente após os testes e a revisão. Não fazer commit nem push sem autorização específica.

## Evidência desta sessão

- `./gradlew --version` identificou Gradle 8.14.5, Java 21. O build Android conservou AGP 8.13.2 e Kotlin 2.2.20.
- O teste inicial de domínio falhou antes da implementação por ausência de `CareRequestCatalog`; o teste de duplicidade falhou com `created=true` na segunda chamada antes da regra ser aplicada. O teste HTTP falhou antes da aplicação Spring existir por ausência de `@SpringBootConfiguration`.
- `./gradlew -p labs/backend clean test bootJar --console=plain` passou com oito testes (quatro de domínio e quatro HTTP). `./gradlew --quiet :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` passou com 91 testes unitários debug e 91 release, sem falhas nos relatórios XML.
- O smoke test com `curl` obteve lista inicial `200 []`, criação `201`, repetição `200` com ID `1` e lista `200 []` após reinício. `ss -ltnp` mostrou a escuta em `127.0.0.1:8081`; o processo foi encerrado após a verificação.
- A suíte instrumentada Android não foi executada nesta rodada por ausência de dispositivo ADB. Não houve validação humana nova de UI, commit ou push.

## Verificação posterior no Galaxy A26

- O responsável conectou o A26 (`SM_A266M`); `adb devices -l` confirmou o dispositivo. Após `./gradlew --quiet :app:assembleDebug :app:assembleDebugAndroidTest`, os dois APKs foram instalados com `adb install --no-streaming -r`.
- `adb -s RQCY503QPEM shell am instrument -w com.viniciusrio.dengo.test/androidx.test.runner.AndroidJUnitRunner` terminou com `OK (24 tests)` após a atualização para Gradle 8.14.5. A transferência USB do APK principal levou cerca de 135 segundos; a instalação e o runner terminaram com sucesso.
- Esta rodada foi uma execução automatizada, sem revisão humana nova da UI. Nenhum commit ou push foi feito.
