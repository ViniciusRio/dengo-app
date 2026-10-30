# Status do projeto

Etapas 1–7 concluídas e aprovadas no dispositivo. A evolução posterior do ciclo afetivo dos pedidos foi revisada e aprovada funcional e visualmente no Samsung Galaxy A26 5G. Consulte o ADR-012 para a decisão vigente. A próxima etapa de produto ainda não foi definida.

A evolução de recados desenhados do Mural foi implementada no protótipo local, passou pela revisão contra a spec e pelos testes automatizados. Após os ajustes do compositor e da paleta, recebeu aprovação humana na validação final no Galaxy A26: abertura no tamanho esperado, rótulos de cor em uma linha, troca Rosa → Azul → Escuro → Rosa sem recuo visual, desenho, cores, Desfazer, Limpar, Cancelar e publicação no Mural. Esse relato não documenta testes individuais de TalkBack, fonte ampliada ou previews nas duas Homes. A aprovação da Etapa 7 continua se referindo à versão textual; a evolução desenhada é posterior e não define uma nova etapa de produto. Consulte o ADR-014.

O Perfil atual é a composição da Opção 1 refinada em B.1, aprovada visualmente pelo responsável no Galaxy A26 nas perspectivas de Lidianne e Vinícius. O painel curvo, a ilustração transparente, o lettering e as duas superfícies pessoais integram a direção vigente; a escolha ativa mantém contorno e “Em uso”. TalkBack e fonte `1.5×` foram exercitados no mesmo aparelho nos cenários descritos em `docs/features/profile.md`, sem auditoria exaustiva de acessibilidade. O checkpoint funcional inicial (ADR-016) e o refinamento 1.1 (ADR-017) permanecem como histórico; a precedência visual atual está no ADR-018.

O Histórico afetivo agrupa acontecimentos reais por pedido e mantém mood e espaço pessoal independentes. A composição com cartões suaves, pontos por autoria e superfície ajustada à perspectiva foi aprovada visualmente no Galaxy A26. Naquela evolução passaram 67 testes unitários debug, 67 release e 11 instrumentados no aparelho; o responsável confirmou TalkBack e fonte aproximadamente `1,5×` como satisfatórios. A validação e seus limites estão no ADR-019; naquele checkpoint o estado ainda permanecia em memória.

## Persistência local — Incremento 1

**Escopo de produto aprovado para especificação:** persistir localmente, no mesmo aparelho, todos os dados publicados já existentes: pedidos e estados, eventos factuais do Histórico, o mood atual de Lidianne, espaço pessoal e recados publicados de texto e desenho. Rascunhos, perspectiva, identidade de sessão, backup/exportação, recuperação após desinstalação e comunicação entre aparelhos ficam fora do escopo.

**Incremento 1 implementado e validado naquele checkpoint:** a interface `CoupleRepository` assíncrona, o Fake, os quatro ViewModels e a UI passaram a distinguir `Loading`, `Ready`, `ReadError` e resultados de escrita confirmados, revertidos ou incertos. Os compositores de `OTHER` e do Mural preservam o rascunho até a confirmação. Ações rápidas usam aviso da intenção mais recente; mood e espaço pessoal seguem a ordem dos toques. O responsável aceitou que uma falha anterior de ação independente possa ficar sem aviso após uma intenção mais recente. Naquele momento, o armazenamento ainda era em memória; Room e o formato durável dos desenhos ficaram para um incremento posterior. Ver [ADR-020](decisions/ADR-020-async-repository-increment-1.md).

A revisão independente das correções não encontrou defeito confirmado. Nesta rodada, `./gradlew --quiet test assembleDebug` passou; a suíte instrumentada executada no Galaxy A26 terminou com `OK (14 tests)` após a correção do seletor dos testes de rascunho. `git diff --check` passou. O responsável percorreu o roteiro de Home, pedidos, mood, espaço pessoal, Mural e apresentação no A26 e relatou que tudo funcionou. É aprovação humana geral dos fluxos observados, sem evidência individual de estados de falha injetados, TalkBack ou fonte ampliada. Os instrumentados validam os cenários automatizados, não substituem essa revisão humana.

Não existe banco anterior com dados a migrar, e o protótipo não popula interações demonstrativas em runtime.

## Persistência local — Incremento 2 concluído

O responsável aprovou Room para persistir pedidos e seus estados, eventos factuais do Histórico, mood atual, espaço pessoal e recados publicados de texto ou desenho no mesmo aparelho. A spec consolidada está em `openspec/specs/local-couple-state/spec.md`, o comportamento de produto em [local-persistence.md](features/local-persistence.md) e a decisão técnica no [ADR-021](decisions/ADR-021-room-local-persistence.md). O adapter Room está integrado ao app. O armazenamento preserva o arquivo em falha de abertura/corrupção e não faz reset automático; duas rodadas de revisão independente terminaram sem defeito confirmado após correções.

Em 2026-10-30, `./gradlew --quiet :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` passou com 90 testes unitários debug e 90 release, sem falhas. A suíte instrumentada completa executada no Galaxy A26 terminou com `OK (23 tests)`, incluindo nove testes Room. `openspec validate persist-local-state-with-room --strict --no-interactive` e `git diff --check` passaram. O responsável criou pedido, mood, espaço pessoal e recados de texto e desenho; após encerramento forçado do processo e reabertura no A26, confirmou que pedido/estado, Histórico, mood, espaço pessoal, os dois recados e previews reapareceram. A aprovação humana cobre esses fluxos observados, sem evidência de teste manual de corrupção, falhas injetadas, TalkBack ou fonte ampliada. O Incremento 2 está concluído neste alcance.

## Laboratório técnico posterior — escrita com resultado incerto

Um exercício SDD/OpenSpec ampliou a evidência do contrato existente de `Uncertain` para criação de pedido predefinido: um teste simula perda da resposta após commit Room e verifica o pedido no estado da Home e após reabertura, sem reenvio automático; o teste de falha anterior ao commit agora confere também a ausência de pedido e evento após reabertura antes de uma nova tentativa explícita. Não houve alteração de código de produção, nova funcionalidade ou nova etapa de produto. A simulação ocorre na resposta do repository e não reproduz uma falha física do SQLite.

Nesta sessão, passaram 91 testes unitários debug, 91 release, os builds debug e androidTest, a validação OpenSpec estrita e `git diff --check`. A suíte instrumentada completa no Galaxy A26 terminou com `OK (24 tests)`. Essa execução automatizada no aparelho não constitui revisão humana de UI; nenhuma nova aprovação visual ou funcional do produto foi registrada.

## Laboratório backend isolado — primeiro recorte técnico

Após escolha do responsável, foi criado `labs/backend/` com Kotlin/Spring Boot, build separado e wrapper Gradle único atualizado para 8.14.5 (ADR-022). A API em `127.0.0.1:8081` cria e consulta somente pedidos predefinidos sintéticos em memória; não se conecta ao app Android. A mesma coleção atende comando e consulta. Não há backend de produto, sincronização, banco remoto, mensageria ou nova etapa de produto.

Nesta sessão, `./gradlew -p labs/backend test bootJar` passou com oito testes, e o smoke test local confirmou `200 []`, criação `201`, repetição `200` com o mesmo ID, escuta em loopback e `[]` após reinício. Com Gradle 8.14.5, passaram 91 testes unitários Android debug, 91 release e os builds debug/androidTest. Em rodada posterior, o A26 foi conectado, os dois APKs foram instalados e a suíte instrumentada completa terminou com `OK (24 tests)` após a troca do wrapper. Trata-se de execução automatizada, sem revisão humana nova de UI. A spec OpenSpec do laboratório e o roteiro de reprodução registram o alcance.

## Laboratório RabbitMQ isolado

O responsável aprovou a mudança OpenSpec `study-rabbitmq-delivery-basics`. O perfil opcional `rabbit-lab` usa sinais sintéticos, broker local e ACK manual; permanece separado de `/lab/requests` e do Android. O ADR-023 registra a decisão e seus limites. Testes do backend com broker real e smoke HTTP passaram em 2026-10-30; o perfil padrão foi verificado sem broker. Não há aprovação de backend de produto, sincronização ou nova etapa de produto.

## Aprovado

- visão do produto;
- Android-first;
- Kotlin + Jetpack Compose + Material 3;
- protótipo local/fake antes de backend;
- usuários iniciais: Lidianne e Vinícius;
- direção visual rosa/azul sobre base creme;
- identidade visual ilustrada de Lidianne, Vinícius e do casal aprovada em prancha própria (ADR-015); composição atual do Perfil B.1 aprovada visualmente no Galaxy A26 no alcance do ADR-018, após os checkpoints históricos dos ADR-016 e ADR-017;
- Home de Lidianne focada em expressão/pedidos;
- Home de Vinícius focada em contexto/resposta;
- Personal Space separado de Care Request.
- Mural de recados da Etapa 7 aprovado visual e funcionalmente no Samsung.
- Evolução posterior de recados desenhados aprovada no Galaxy A26, no alcance da validação relatada acima.
- Ciclo afetivo dos pedidos aprovado funcional e visualmente no Samsung; sem evidência específica de teste com fonte ampliada ou TalkBack.
- Histórico afetivo aprovado visualmente nas duas perspectivas no alcance do ADR-019; a persistência local posterior está no ADR-021, sem sincronização.
- Persistência local dos dados publicados, aprovada no Galaxy A26 no alcance do ADR-021.

## Ainda pode mudar após protótipo

- nome definitivo do app;
- textos finais dos CTAs;
- pequenos ajustes de paleta/contraste;
- usos e eventuais recortes dos personagens como assets de runtime;
- arquitetura do backend futuro.
