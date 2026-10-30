# Tasks

## 1. Base Room e reconstrução do estado

- [x] 1.1 Adicionar dependências Room e processamento de código, configurar exportação do esquema e criar o banco inicial sem fallback destrutivo; verificar `:app:assembleDebug` e o arquivo de esquema gerado.
- [x] 1.2 Criar tabelas e mapeamento de pedidos, eventos com ordem de inserção, mood, espaço pessoal, recados, traços e pontos; escrever primeiro testes de ida e volta de cada modelo e verificar que passam em banco real.
- [x] 1.3 Reconstituir `CoupleState` a partir de leitura consistente, incluindo estado vazio e IDs/ordens estáveis; verificar testes que fecham e reabrem o banco com dados de todas as modalidades.

## 2. Escritas transacionais pelo contrato existente

- [x] 2.1 Implementar criação e transições de pedidos com o evento factual na mesma transação; verificar testes de duplicidade, transições inválidas, falha parcial e reabertura.
- [x] 2.2 Implementar mudanças de mood e espaço pessoal com eventos somente quando houver mudança efetiva; verificar testes de repetição, sequência de toques e reabertura.
- [x] 2.3 Implementar publicação de recados textuais e desenhados, preservando todos os traços/pontos em transação; verificar testes de falha parcial, conteúdo recuperado, autoria e ordenação.
- [x] 2.4 Comparar o comportamento do adapter Room ao Fake nos casos de contrato relevantes; verificar a suíte de contrato para ambos, incluindo IDs após reabertura e ausência de eventos de Mural.

## 3. Estados assíncronos e integração no app

- [x] 3.1 Implementar `Loading`, `Ready`, `ReadError` e `retryRead()` sobre o banco existente, sem reset; verificar testes de falha, nova tentativa e preservação dos dados.
- [x] 3.2 Mapear `Confirmed`, `Reverted`, `Uncertain` e cancelamento às transações, sem retry automático de escrita; verificar testes de resultado e ausência de duplicação em falhas e concorrência.
- [x] 3.3 Ligar `RoomCoupleRepository` ao ponto de composição do app sem mudar os fluxos das quatro telas; verificar testes de integração de Home, Histórico e Mural e `:app:assembleDebugAndroidTest`.

## 4. Verificação integrada e checkpoint

- [x] 4.1 Executar testes unitários debug/release, build debug, build instrumentado e `git diff --check`; registrar comandos, contagens e falhas reais no relatório do checkpoint.
- [x] 4.2 Executar no Galaxy A26 os instrumentados de persistência e a suíte relevante; verificar o resultado do runner, distinguindo compilação de execução. Em 2026-10-30, `adb -s RQCY503QPEM shell am instrument -w com.viniciusrio.dengo.test/androidx.test.runner.AndroidJUnitRunner` terminou com `OK (23 tests)`, incluindo os nove testes Room.
- [x] 4.3 Submeter código e diff a revisão independente contra esta spec e contra riscos técnicos; registrar achados e correções necessárias antes de declarar o incremento concluído. Duas rodadas concluídas: corrupção do banco e classificação de falha de escrita corrigidas; segunda rodada sem defeito confirmado. Testes Room executados no A26 em 2026-10-30; validação humana ainda pendente.
- [x] 4.4 Revisar manualmente no Galaxy A26 publicação de cada modalidade, encerramento forçado/reabertura, duas perspectivas, previews e tratamento de erro que puder ser exercitado; registrar exatamente o que a pessoa validou. Em 2026-10-30, o responsável criou pedido, mood, espaço pessoal, recado textual e desenho; após `am force-stop` e reabertura confirmou que reapareceram pedido/estado, Histórico, mood, espaço pessoal, ambos os recados e previews. Falhas injetadas não foram verificadas manualmente.
- [x] 4.5 Após implementação e aprovação humana, propor ADR técnico e atualizar `docs/features/`, `docs/STATUS.md` e `dengo/06 - Desenvolvimento/Etapa Atual.md` com comportamento e evidência efetivos; verificar consistência entre os documentos e o código. ADR-021 e `docs/features/local-persistence.md` registram decisão e contrato; status e vault delimitam testes e aprovação de 2026-10-30.

## Workflow follow-up

- Arquivar a mudança OpenSpec somente após implementação, revisões, validação humana e documentação de fechamento; validar a spec consolidada após o arquivo.
- Solicitar autorização específica antes de commit ou push.
