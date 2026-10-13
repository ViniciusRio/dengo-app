# ADR-019 — Composição afetiva do Histórico por pedido

## Status

Direção visual aprovada pelo responsável no Samsung Galaxy A26 nas perspectivas Lidianne e Vinícius. Validação final automatizada e verificações manuais relatadas concluídas no alcance descrito abaixo; pronto para revisão de commit, sem aprovação de uma auditoria exaustiva de acessibilidade.

## Contexto

O Histórico somente de leitura do ADR-010 registrava acontecimentos separados. O checkpoint `595afe4` passou a reunir eventos do mesmo pedido por `requestId`, mas uma exploração posterior com passagens sem pontos e cartões rosa mais saturados não foi aprovada visualmente. Na comparação no A26, o responsável preferiu os cartões muito suaves com pontos rosa e azul e conectores discretos. Também observou que a superfície do cartão deveria acompanhar a perspectiva ativa.

## Decisão

Manter a projeção compartilhada em memória: cada história reúne somente os eventos registrados do mesmo pedido, em ordem de inclusão, e fica ordenada pelo acontecimento mais recente. Criação, aceitação, recusa e agradecimento mantêm seus significados próprios. Mood e espaço pessoal permanecem independentes. O Histórico não cria eventos, altera pedidos nem incorpora recados do Mural.

Recuperar a composição de passos do checkpoint `595afe4`: cartão arredondado, superfície suave, pontos ligados à autoria e conectores discretos. Na perspectiva Lidianne, o cartão usa `surfaceVariant` (`#FFF2F4`); na de Vinícius, usa a interpolação de `surface` com `ViniciusBlueSoft` em 40%, de intensidade semelhante. A borda usa `outline`. Os pontos continuam `LidianneAction` e `ViniciusAction` de acordo com quem produziu cada acontecimento, independentemente da perspectiva. Textos factuais, datas, horários e ordem de leitura não mudam. Não há novo token global, asset ou dependência.

Esta decisão atualiza apenas a apresentação da evolução do Histórico. O ADR-010 permanece como registro da Etapa 6, e o ADR-012 continua definindo o ciclo dos pedidos. A aprovação visual em ambos os perfis é distinta dos resultados automatizados e das verificações manuais de acessibilidade registrados abaixo.

## Evidência e pendências

O responsável relatou aprovação visual das duas perspectivas no Galaxy A26. Em 2026-10-13, `./gradlew test :app:assembleDebug :app:assembleDebugAndroidTest --rerun-tasks` passou: 67 testes unitários debug e 67 release, sem falhas. Em execução posterior no A26, a instalação automática do Gradle falhou no modo streaming; os APKs foram instalados com `adb install --no-streaming` e a bateria instrumentada terminou com `OK (11 tests)`, conforme evidência fornecida pelo responsável. Na auditoria final, `./gradlew test :app:assembleDebug :app:assembleDebugAndroidTest` passou com as tarefas atualizadas e `adb shell am instrument -w com.viniciusrio.dengo.test/androidx.test.runner.AndroidJUnitRunner` reproduziu `OK (11 tests)` no A26. O responsável também confirmou manualmente TalkBack e fonte ampliada em aproximadamente `1,5×` como satisfatórios. A revisão estática confirmou rótulos textuais e ordem de leitura no código e contraste calculado de pelo menos 4,7:1 para os textos secundários sobre ambas as superfícies. Os testes automatizados cobrem agrupamento, ordenação, datas, ausência de dados, sentido dos eventos, somente leitura, duas perspectivas, recusa com `OTHER` longo, mood e espaço pessoal independentes; o relato manual não discrimina cada cenário de pedido e não representa auditoria exaustiva de acessibilidade.
