# Tasks

## 1. Demonstrar o contrato existente

- [x] 1.1 Criar teste instrumentado com Room real e decorador somente de teste que converta em `Uncertain` a resposta de uma criação já confirmada; verificar uma chamada, um pedido e um evento após reabertura.
- [x] 1.2 Verificar no chamador representativo que `Uncertain` aparece sem reenvio automático e que o `Ready` publicado permite conferir o pedido; manter a regra da intenção mais recente do ADR-020. Teste JVM `committedRequestWithLostResponseRemainsVisibleWithoutRetry` executado em debug e release.
- [x] 1.3 Confirmar no teste de falha anterior ao commit que pedido e evento estão ausentes após releitura ou reabertura e que somente uma nova tentativa explícita os cria.

## 2. Verificar e registrar alcance

- [x] 2.1 Executar os testes focados e regressões pertinentes; registrar comandos e resultados reais, sem reutilizar contagens históricas como execução nova.
- [x] 2.2 Validar a mudança OpenSpec em modo estrito e revisar o diff contra a spec vigente e os ADRs 020/021.
- [x] 2.3 Se não houver mudança de comportamento, registrar o resultado como laboratório técnico, sem nova etapa de produto ou ADR obrigatório; se houver defeito, especificar e revisar o alcance da correção antes de implementá-la.
- [x] 2.4 Arquivar a mudança e atualizar os documentos de estado somente após implementação, revisão e verificação efetivas. Commit e push exigem autorização explícita.

## Checkpoint de execução — 2026-10-30

- `./gradlew --quiet :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` passou nesta sessão: 91 testes JVM debug e 91 release, sem falhas nos relatórios XML. O APK de teste instrumentado compilou; isso não equivale à execução no aparelho.
- `openspec validate demonstrate-uncertain-write-recovery --strict --no-interactive` e `git diff --check` passaram nesta sessão.
- Posteriormente nesta sessão, `adb devices -l` reconheceu o Galaxy A26 (`SM_A266M`). Após `:app:assembleDebug :app:assembleDebugAndroidTest` e instalação dos APKs com `adb install --no-streaming -r`, `adb shell am instrument -w com.viniciusrio.dengo.test/androidx.test.runner.AndroidJUnitRunner` terminou com `OK (24 tests)`, incluindo os cenários Room alterados. Não houve validação humana nova de UI; este laboratório não altera telas ou comportamento de produto.
