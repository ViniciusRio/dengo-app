# Etapa Atual

## Etapa 7 — Mural de recados do casal
Status: 🟢 Concluída e aprovada no Samsung

## Escopo implementado
- recados de texto e emojis, com autoria explícita e limite de 160 pontos de código Unicode;
- mesma lista em memória nas duas perspectivas, ordenada por horário e ID decrescentes;
- composição curta em bottom sheet e estado vazio afetivo;
- previews discretos nas duas Homes, abrindo a aba Mural;
- publicação permitida durante espaço pessoal e sem `HistoryEvent`;
- testes unitários, sem persistência ou sincronização.

## Fora do escopo da Etapa 7
Desenhos, fotos, anexos, edição, exclusão, comentários, reações, filtros, busca, novos eventos de Histórico, backend, persistência e sincronização.

## Validação da Etapa 7
Revisão visual e funcional concluída no Samsung: estado vazio, CTA, bottom sheet e teclado, contador e limite, texto e emoji, publicação imediata, lista compartilhada e ordenada, autoria, previews das Homes e navegação para o Mural. A próxima etapa de produto ainda não foi definida e dependerá de novo planejamento.

## Evolução posterior — ciclo afetivo dos pedidos

Status: 🟢 Aprovada funcional e visualmente no Samsung Galaxy A26 5G. O contrato vigente está no ADR-012. Esta evolução não define uma nova etapa de produto; a próxima etapa permanece em aberto.

## Mural — evolução de recados desenhados aprovada no Samsung

Recados desenhados à mão foram implementados como alternativa posterior aos recados textuais da Etapa 7; o contrato de produto está em `docs/features/mural.md`. A implementação passou pela revisão contra a spec e pelos testes automatizados. Após correções do compositor e da paleta, a validação manual final no Galaxy A26 foi aprovada: abertura no tamanho esperado, Rosa/Azul/Escuro em uma linha, troca Rosa → Azul → Escuro → Rosa sem recuo, desenho, cores, Desfazer, Limpar, Cancelar e publicação no Mural. O relato não discrimina testes de TalkBack, fonte ampliada ou previews nas duas Homes. Não foi definida uma nova etapa de produto. O ADR-014 registra as direções técnicas e a aprovação posterior; a aprovação da Etapa 7 continua se referindo à versão textual entregue naquela etapa.

## Perfil — composição Opção 1/B.1

Status: 🟢 B.1 aprovado visualmente e validado nos cenários de acessibilidade exercitados no Galaxy A26; próxima etapa de produto ainda não definida.

O ADR-016 registra o checkpoint funcional inicial e o ADR-017 registra a aprovação visual histórica do Perfil 1.1. Após a comparação com a Opção 1, a Fase B reconstruiu a composição com casal transparente, painel curvo, lettering e duas superfícies pessoais. O B.1 refinou proporção e hierarquia da explicação; o responsável aprovou visualmente os dois estados no A26. No checkpoint B.1 passaram 132 testes unitários, os builds debug e androidTest, `git diff --check` e 7/7 instrumentados no A26. Em validação posterior no aparelho, TalkBack e fonte `1.5×` foram exercitados nos cenários registrados em `docs/features/profile.md`; não houve auditoria exaustiva de acessibilidade nem nova verificação individual da autoria no Mural. O ADR-018 registra a decisão visual vigente. Esta evolução não define uma nova etapa de produto.

## Histórico afetivo — histórias por pedido

Status: 🟢 Composição aprovada visualmente nas perspectivas Lidianne e Vinícius no Galaxy A26; validação final registrada no alcance do ADR-019, pronta para revisão de commit.

Os pedidos são agrupados por `requestId`, com criação, resposta e eventual agradecimento preservados como acontecimentos distintos. Mood e espaço pessoal permanecem independentes. Passaram 67 testes unitários debug, 67 release e 11 instrumentados no A26 (via ADB, após `adb install --no-streaming`). O responsável confirmou TalkBack e fonte aproximadamente `1,5×` como satisfatórios. O relato manual não discrimina todos os cenários individualmente. O Histórico continua somente leitura e em memória; esta evolução não define uma nova etapa de produto.

## Persistência local — Incremento 1

**Escopo aprovado para especificação:** sobreviver ao encerramento e à reabertura no mesmo aparelho, mantendo todos os dados publicados já existentes: pedidos e estados, eventos factuais do Histórico, mood atual de Lidianne, estado do espaço pessoal e recados publicados do Mural, incluindo texto e desenho completo. Fora do escopo: backend, comunicação entre aparelhos, autenticação, pareamento, sincronização, backup/exportação/recuperação após desinstalação, rascunhos, perspectiva e identidade de sessão. Não há dados demonstrativos de runtime nem banco anterior a migrar.

**Estado naquele checkpoint:** Incremento 1 implementado e validado. `CoupleRepository` assíncrono, Fake, quatro ViewModels e UI tratam leitura e escrita; os compositores preservam rascunhos até confirmação. Ações rápidas mostram o aviso da intenção mais recente, escolha aceita pelo responsável. Mood e espaço pessoal seguem a ordem dos toques. O app ainda permanecia em memória; Room e armazenamento durável dos desenhos ficaram para um incremento posterior. A decisão está no `docs/decisions/ADR-020-async-repository-increment-1.md`.

As correções passaram por revisão independente sem defeito confirmado. `./gradlew --quiet test assembleDebug` passou nesta rodada; a suíte instrumentada no Galaxy A26 terminou com `OK (14 tests)` e `git diff --check` passou. O responsável executou o roteiro manual no A26 e relatou que tudo funcionou; essa aprovação geral não discrimina teste individual de falhas injetadas, TalkBack ou fonte ampliada.

## Persistência local — Incremento 2 concluído

Room foi aprovado para os dados publicados já delimitados: pedidos/estados, Histórico factual, mood atual, espaço pessoal e recados de texto/desenho. A spec OpenSpec consolidada está em `openspec/specs/local-couple-state/spec.md`, o contrato de produto em `docs/features/local-persistence.md` e a decisão no ADR-021. O app foi integrado ao adapter Room e o banco é preservado em falha de leitura/corrupção, sem reset automático. Duas rodadas independentes revisaram o diff; os defeitos apontados foram corrigidos e a segunda rodada não confirmou novos defeitos.

Em 2026-10-30, 90 testes unitários debug e 90 release passaram; o build debug e o de androidTest passaram; no Galaxy A26 a suíte completa terminou com `OK (23 tests)`, incluindo nove cenários Room. O responsável criou pedido, mood, espaço pessoal e recados de texto e desenho; após encerramento forçado e reabertura confirmou que pedido/estado, Histórico, mood, espaço pessoal, ambos os recados e previews reapareceram. Essa aprovação humana não inclui teste manual de corrupção, falhas injetadas, TalkBack ou fonte ampliada. Incremento 2 concluído nesse alcance; próxima etapa de produto não definida.

## Laboratório técnico posterior — retorno `Uncertain`

Sem definir nova etapa de produto, a mudança OpenSpec `demonstrate-uncertain-write-recovery` documentou e testou os dois desfechos de uma criação de pedido predefinido com resultado incerto: falha antes do commit e perda simulada da resposta após commit. O app não reenvia automaticamente; o estado publicado permite conferir o pedido. Nenhum código de produção ou tela foi alterado. Nesta sessão, passaram 91 testes unitários debug, 91 release, builds debug/androidTest e 24 testes instrumentados no Galaxy A26; a validação OpenSpec estrita e `git diff --check` passaram. A execução no aparelho foi automatizada e não representa nova aprovação humana de UI. A próxima etapa de produto permanece em aberto.

## Laboratório backend isolado — primeiro recorte

O responsável escolheu um laboratório técnico Kotlin/Spring Boot separado do app. `labs/backend/` usa dados sintéticos e memória volátil, com API restrita a `127.0.0.1:8081`; o wrapper único subiu para Gradle 8.14.5. O contrato e os limites estão na mudança OpenSpec arquivada `2026-10-30-start-isolated-backend-lab` e no ADR-022. Nesta sessão, oito testes do laboratório e os builds Android passaram; 91 testes unitários debug e 91 release passaram após a troca do wrapper. O smoke test local confirmou criação, repetição, loopback e perda dos dados no reinício. Em rodada posterior, os dois APKs foram instalados no A26 e a suíte instrumentada completa terminou com `OK (24 tests)` após a troca. Não houve revisão humana nova de UI. A próxima etapa de produto permanece indefinida.

## Laboratório RabbitMQ — primeiro recorte isolado

O responsável aprovou a mudança OpenSpec `study-rabbitmq-delivery-basics`. O perfil `rabbit-lab` no build backend usa somente sinais sintéticos, com publisher confirm, mandatory return, worker com ACK manual e teste de reentrega após falha injetada antes do ACK. Broker e HTTP escutam no loopback. Em 2026-10-30, testes sem broker e com broker real passaram; smoke HTTP retornou `202` para sinal aceito e `400` para inválido. Com broker removido, `/lab/requests` funcionou no perfil padrão e `/lab/signals` retornou `404`. O ADR-023 delimita as garantias e a ausência de outbox, DLQ, deduplicação durável ou integração com Android. A próxima etapa de produto permanece indefinida.
