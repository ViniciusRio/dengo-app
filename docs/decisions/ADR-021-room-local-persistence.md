# ADR-021 — Room para persistência local no Incremento 2

## Status

Aceito e validado no alcance descrito abaixo. Complementa o contrato assíncrono do ADR-020; não aprova backend, sincronização nem uma nova etapa de produto.

## Contexto

O Incremento 1 deixou os quatro ViewModels dependentes de `CoupleRepository`, com estados de leitura e resultado de escrita assíncronos, mas o Fake ainda perdia dados ao encerrar o processo. O responsável aprovou Room para preservar os dados publicados no mesmo aparelho e definiu que falhas de leitura não podem apagar o banco automaticamente. Não havia banco anterior nem dados demonstrativos de runtime a migrar.

## Decisão

O app usa `RoomCoupleRepository` no ponto de composição e mantém `FakeCoupleRepository` para testes. O esquema inicial tem tabelas para pedidos, eventos, mood atual, espaço pessoal, recados, traços e pontos. Eventos, traços e pontos possuem ordem explícita; IDs persistidos continuam únicos após reabertura. Cada comando valida o estado e grava as linhas relacionadas em uma transação Room. Os desenhos são armazenados em linhas relacionais, sem BLOB binário neste incremento.

O adapter publica `Loading`, `Ready` e `ReadError` pelo contrato existente. `retryRead()` relê o banco preservado. Falha de escrita retorna `Uncertain`; se o banco ainda puder ser lido, mantém `Ready`, e só uma falha de releitura produz `ReadError`. `Confirmed` vem após a transação terminar, `Reverted` representa rejeição conhecida, e cancelamento é propagado. Não há retry automático de escrita. A abertura usa um helper que não exclui o arquivo em corrupção nem permite recuperação com perda de dados; não há fallback destrutivo de migração. `android:allowBackup="false"` permanece ativo.

## Alternativas e limites

Um snapshot serializado único simplificaria a primeira gravação, mas dificultaria inspeção e evolução do esquema. Um BLOB de desenho exigiria um formato e codec versionados agora. Tabelas explícitas mantêm dados inspecionáveis e transações claras para o volume esperado de um projeto pessoal; muitos pontos de desenho podem exigir medição e revisão futura. O banco fica no sandbox Android, sem criptografia adicional. Recuperação após desinstalação, migração de banco distribuído anteriormente, rascunhos, perspectiva, backend e sincronização não fazem parte desta decisão.

Os documentos das etapas anteriores que dizem “em memória” registram aquele checkpoint histórico. Para o estado vigente de persistência, prevalecem esta decisão e `docs/features/local-persistence.md`.

## Evidência e aprovação

Duas rodadas de revisão independente examinaram o código e o diff. A primeira apontou risco de exclusão em corrupção e classificação incorreta de falha apenas de escrita; ambos foram corrigidos, e a segunda não encontrou defeito confirmado. Em 2026-10-30 passaram 90 testes unitários debug e 90 release, os builds debug e androidTest e 23 testes instrumentados no Galaxy A26, incluindo nove cenários Room. `openspec validate --strict` e `git diff --check` passaram.

No mesmo aparelho, o responsável criou pedido, mood, espaço pessoal e recados de texto e desenho. Após `adb shell am force-stop` e reabertura, confirmou que pedido/estado, Histórico, mood, espaço pessoal, recados e previews reapareceram. A aprovação humana se limita a esses fluxos observados; não documenta teste manual de falha injetada, corrupção, TalkBack ou fonte ampliada. Commit e push exigem autorização específica.
