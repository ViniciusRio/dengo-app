# Tasks

## 0. Spec (`docs/two-device-sync-spec`)

- [x] 0.1 Revisar esta spec com o Codex e corrigir bloqueantes; verificar `openspec validate sync-couple-between-devices --strict`. Três rodadas antes da aprovação em 2026-11-03; as correções da terceira não passaram por nova rodada antes dela. A revisão do PR #6 apontou uma contradição na restauração, corrigida depois da aprovação com a retirada da geração do banco.
- [x] 0.2 Obter a aprovação de Vinícius antes de qualquer código. Spec aprovada em 2026-11-03, sem quarta rodada, por decisão dele.

## 1. Backend (`feat/backend-core`)

- [x] 1.1 Criar `backend/` com build próprio, Flyway e Testcontainers; verificar que o build Android e `labs/backend` continuam passando.
- [x] 1.2 Implementar vínculo, tokens com hash e revogação, e comandos administrativos, escrevendo primeiro testes de expiração, uso único, limite de tentativas e token revogado.
- [x] 1.3 Implementar revisão serializada e `GET /v1/sync` com paginação; verificar por teste que escritas concorrentes não fazem a leitura pular revisões.
- [x] 1.4 Implementar idempotência com chaves sem prazo; verificar repetição com a mesma chave, chave com conteúdo diferente, segundo envio e descarte com a primeira transação em andamento e envio atrasado depois do descarte.
- [x] 1.5 Implementar pedidos e Histórico, portando as regras de `RoomCoupleRepository`; verificar transições, duplicidade, espaço pessoal, permissão por pessoa e atomicidade contra a spec.
- [x] 1.6 Implementar mood e espaço pessoal; verificar repetição sem novo evento e a data no fuso configurado.

## 2. Implantação (`chore/backend-deploy`)

- [ ] 2.1 Obter de Vinícius o domínio e a contratação da VPS, com preço conferido antes.
- [ ] 2.2 Escrever Docker Compose, proxy com HTTPS e documentação de operação, sem segredos no repositório; verificar HTTPS e que o PostgreSQL não está exposto publicamente.
- [ ] 2.3 Configurar backup diário cifrado fora da VPS; verificar restauração completa em ambiente separado, com dados sintéticos, e que o procedimento invalida todas as sessões e códigos de vínculo antes de aceitar tráfego, inclusive uma sessão revogada e um código usado depois do backup.
- [ ] 2.4 Verificar que os logs não contêm tokens nem conteúdo, com dados sintéticos.

## 3. Identidade no app (`feat/app-device-identity`)

- [x] 3.1 Adicionar a permissão de internet e bloquear tráfego sem HTTPS, com exceção só no build debug para servidor local.
- [x] 3.2 Guardar o token cifrado com o Keystore; verificar que `allowBackup` e as regras de extração continuam excluindo os dados.
- [ ] 3.3 Criar a tela de vínculo, com estados de erro, código expirado e sem conexão, consultando `docs/design/*`; verificar acessibilidade e toque.
- [x] 3.4 Derivar a perspectiva da sessão, retirar a troca do Perfil e retirar quem age de `CoupleRepository`; adaptar Fake, ViewModels e testes; verificar a suíte unitária e instrumentada.

## 4. Sincronização no app (`feat/app-remote-sync`)

- [ ] 4.1 Criar `dengo-sync.db` com IDs do servidor e revisão, mantendo `dengo-local.db` intacto e apagando os arquivos `dengo-session-*.db` da etapa 3; verificar por teste que o arquivo antigo não é aberto nem alterado.
- [ ] 4.2 Implementar a nova implementação de `CoupleRepository` sobre a API, com as regras de resultado da spec e a tabela de tentativas pendentes; verificar com servidor falso cada linha da tabela de resultados, tentar de novo e descartar, inclusive depois de reabrir o app, com descarte que encontra escrita confirmada e comando igual antes de a cópia alcançar a revisão.
- [ ] 4.3 Implementar os gatilhos de sincronização e o aplicar por página em transação; verificar interrupção no meio da paginação.
- [ ] 4.4 Comunicar dados possivelmente desatualizados e falta de conexão, sem criar novo componente se um aviso existente servir; verificar leitura sem internet.
- [ ] 4.7 Apagar token, cópia e tentativas pendentes somente diante do código explícito de revogação ou ao vincular outra pessoa; verificar por teste que erros de rede e 5xx não apagam nada e que um 200 atrasado, depois do 401, não grava dados.
- [ ] 4.5 Ligar a implementação ao ponto de composição; verificar a suíte instrumentada com a confirmação de que `dengo-sync.db` e `dengo-local.db` não mudam nos testes.
- [ ] 4.6 Testar nos dois aparelhos com dados sintéticos e servidor de teste: pedido, resposta, agradecimento, mood e espaço pessoal, com internet ligada e desligada.

## 5. Notificações (`feat/push-notifications`)

- [ ] 5.1 Criar o projeto Firebase, com ação de Vinícius, e a configuração opcional via `local.properties`; verificar que o build sem configuração passa e que nada secreto entra no repositório.
- [ ] 5.2 Implementar registro de token e outbox com tentativas e descarte; verificar por teste a matriz de avisos, inclusive pedido com espaço pessoal ativo, e que falha no FCM não desfaz a escrita.
- [ ] 5.3 Implementar recebimento, busca, montagem da notificação e fallback; verificar ausência de conteúdo no aviso, de `OTHER` na notificação e de notificação duplicada.
- [ ] 5.4 Pedir a permissão de notificação no momento certo; verificar negação sem quebrar o app.
- [ ] 5.5 Testar nos dois aparelhos com app fechado e em repouso; registrar atrasos reais e ajustes necessários.

## 6. Mural (`feat/mural-sync`)

- [ ] 6.1 Medir tamanho de desenhos reais nos aparelhos e propor o teto para a decisão de Vinícius.
- [ ] 6.2 Implementar recados de texto e desenho no servidor; verificar limites, atomicidade, ordem e ausência de `HistoryEvent`.
- [ ] 6.3 Implementar no app, com previews nas Homes e aviso sobre o teto; verificar a regressão das regras de `docs/features/mural.md`.
- [ ] 6.4 Testar nos dois aparelhos a publicação de cada modalidade e o traço aparecendo igual no outro.

## 7. Fechamento (`docs/two-device-sync-closing`)

- [ ] 7.1 Confirmar com Lidianne onde os dados ficam e quem tem acesso, antes de usar dados reais.
- [ ] 7.2 Rodar os critérios da spec nos dois aparelhos, incluindo revogação com e sem conexão e restauração de backup com os aparelhos sincronizando depois; registrar exatamente o que cada pessoa validou.
- [ ] 7.3 Depois da aprovação humana, atualizar `AGENTS.md`, `docs/features/`, `docs/MVP.md`, `docs/STATUS.md`, ADR e vault, e arquivar esta change.
