# ADR-022 — Primeiro laboratório backend isolado

## Status

Aceito para laboratório técnico após escolha do responsável e implementação verificada no alcance abaixo. Não aprova backend de produto, MVP 2 nem próxima etapa de produto.

## Contexto

O Dengo concluiu Room no mesmo aparelho e um exercício SDD sobre `Uncertain`. O plano de oito temas propunha CQRS proporcional, mas o Android já distingue leitura por fluxo de comandos suspensos. Para estudar backend sem forçar uma refatoração do app, o responsável escolheu um laboratório Kotlin/Spring Boot isolado. O AGP 8.13.2 do app exige Gradle 8.13 como mínimo, enquanto Spring Boot 4.1.2 exige Gradle 8.14 ou superior.

## Decisão

`labs/backend/` é um build Gradle separado, executado pelo wrapper único da raiz atualizado para 8.14.5. O build Android continua incluindo somente `:app`; não há dependência entre os projetos. O laboratório usa Kotlin 2.2.20, Spring Boot 4.1.2 e Java 21. Ele escuta somente em `127.0.0.1:8081` e mantém pedidos sintéticos predefinidos em memória, sem nomes ou mensagens.

Um módulo de catálogo concentra criação e consulta com uma única coleção. Criação repetida devolve o pedido existente; acesso concorrente cria no máximo um pedido do mesmo tipo. O HTTP usa `201` na criação, `200` na repetição e `400` em entrada inválida. A lista é ordenada por ID. A distinção entre comando e consulta é útil para explicar intenção e resultado; interfaces separadas, projeções independentes e consistência eventual não foram necessárias neste recorte.

## Alternativas e limites

Um wrapper próprio no laboratório manteria a versão Gradle Android inalterada, mas duplicaria arquivos e manutenção. O wrapper compartilhado foi escolhido após a regressão Android passar. Uma refatoração CQRS no Android acrescentaria estrutura sem consulta nova ou problema de escala demonstrado. Um backend ligado ao app exigiria decisões de identidade, privacidade e sincronização que seguem abertas.

O estado desaparece no reinício. O laboratório não oferece durabilidade, autenticação, API de produto, comunicação entre aparelhos, mensageria, cache ou implantação. Não usar dados afetivos reais nem expor o servidor na rede.

## Evidência e aprovação

Nesta sessão, `./gradlew -p labs/backend test bootJar` passou com oito testes (quatro de domínio e quatro HTTP). O smoke test local confirmou lista vazia, criação `201`, repetição `200` com o mesmo ID, escuta somente em loopback e lista vazia após reiniciar. Com Gradle 8.14.5, passaram 91 testes unitários Android debug e 91 release e os builds debug e androidTest. `openspec validate --strict` e `git diff --check` passaram. Em rodada posterior, o A26 foi conectado, os dois APKs foram instalados e `adb shell am instrument -w` terminou com `OK (24 tests)` após a atualização do wrapper. Não houve revisão humana nova de UI; o código de produção Android não foi alterado.

Commit e push exigem autorização específica conforme `AGENTS.md`.
