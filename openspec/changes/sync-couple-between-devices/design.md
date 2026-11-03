# Design

## Contexto

`CoupleRepository` já separa leitura por fluxo de comandos assíncronos, com `Confirmed`, `Reverted` e `Uncertain`. Isso permite trocar a implementação sem redesenhar as telas. As regras vivem em `RoomCoupleRepository`, os IDs são gerados pelo Room, e parte dos comandos não informa quem age porque só existe um aparelho. O app hoje não pede permissão de internet.

Escolhas do responsável em 2026-11-03: dois aparelhos, backend próprio, Mural com texto e desenho antes do uso diário, servidor começando vazio e VPS paga. Os dois aparelhos são Samsung Galaxy A26 (Vinícius) e um Motorola, com modelo e versão do Android ainda a confirmar (Lidianne).

## Objetivos e limites

Atender duas pessoas com simplicidade: uma instância, um banco, um casal implícito. Não há multi-inquilino, alta disponibilidade nem mensageria. Uma falha do servidor deixa o app em leitura da cópia local, sem perder dados.

## Decisões

### 1. Backend em `backend/`, separado do laboratório

Build Gradle próprio, com o wrapper da raiz, Kotlin, Spring Boot, Java 21, PostgreSQL, migrações com Flyway e testes com Testcontainers. O build Android continua incluindo só `:app`. `labs/backend/` não muda e não vira dependência. **Alternativa descartada:** evoluir o laboratório, cujas specs dizem que ele é isolado e usa dados sintéticos.

### 2. O servidor é a fonte da verdade; o Room é cópia

As regras de transição, duplicidade, espaço pessoal, autoria e limites passam ao servidor. O app valida só para dar retorno imediato na interface. **Alternativa descartada:** Room como fonte e sincronização bidirecional, que exigiria resolver conflitos para um caso em que escrever sem internet não é requisito.

### 3. Sem escrita offline

Sem conexão, o app mostra a última cópia e a ação falha com aviso. **Motivo:** um pedido de cuidado enviado horas depois, sem a pessoa perceber, é pior do que uma falha visível. Uma fila pode ser reavaliada depois do uso real.

### 4. Identidade por código de uso único

Um comando administrativo no servidor gera um código curto para uma pessoa, válido por 15 minutos, com tentativas limitadas. O app troca o código por um token aleatório de 256 bits. O servidor guarda só o hash, e o app guarda o token cifrado com chave do Android Keystore. Vincular de novo revoga o aparelho anterior da mesma pessoa, o que cobre troca e perda de celular. Um comando administrativo também revoga diretamente. **Alternativa descartada:** login com senha, que traria recuperação de senha e cadastro sem benefício para duas pessoas.

### 5. A API deriva quem age da sessão

Os comandos não recebem o autor: o servidor usa a pessoa do token. Assim, `acknowledgeRequest(requestId, actorId)` e os `authorId` do Mural perdem esse parâmetro, e o Fake dos testes recebe a identidade na construção.

### 6. Sincronização por revisão

Cada escrita recebe um número de revisão crescente, gravado nas linhas que alterou. As escritas são serializadas por um bloqueio de linha em um contador único, o que evita que uma leitura pule uma revisão ainda não confirmada; duas pessoas não geram disputa relevante.

`GET /v1/sync?after=<revisão>&limit=<n>` devolve as linhas alteradas, a revisão alcançada e se há mais páginas. O servidor corta as páginas só entre revisões, nunca no meio de uma, então `limit` é aproximado. O cliente grava cada página e a revisão em uma única transação Room. Não há exclusão, então não há marcadores de remoção. **Alternativa descartada:** baixar tudo a cada atualização, porque desenhos tendem a pesar com o tempo.

**Restauração de backup:** acontece com a API fechada e invalida todas as sessões e todos os códigos de vínculo antes de aceitar tráfego, porque o backup pode conter a sessão de um aparelho revogado ou um código já usado depois dele. Na conexão seguinte, cada celular recebe a revogação e apaga a cópia e as tentativas pendentes, como em qualquer revogação. O novo vínculo, com código novo, baixa o estado restaurado desde o início. Assim, nenhum aparelho fica com um cursor à frente do servidor nem com dados que não existem mais nele. **Limite aceito:** entre a restauração e o novo vínculo, o celular que já se conectou não mostra dados. **Alternativa descartada:** manter a cópia visível durante a reconstrução, que exigiria um identificador de geração e deixaria dados acessíveis em uma sessão inválida, para um evento raro.

**Quando sincronizar:** ao abrir o app, ao voltar para o primeiro plano, ao receber aviso, depois de cada escrita, em `retryRead()` e a cada 30 segundos em primeiro plano, como rede de segurança caso um aviso se perca.

### 7. Escrita idempotente e resultados

Cada comando envia uma chave `Idempotency-Key`. O servidor grava a chave e o resultado na mesma transação da escrita. A mesma chave devolve o mesmo resultado; com conteúdo diferente, é recusada.

| Situação | Resultado |
|---|---|
| Resposta 2xx | `Confirmed`, depois sincroniza |
| Regra recusada (4xx) | `Reverted` |
| Falha ao conectar, antes de enviar | `Reverted`, com aviso de falta de conexão |
| Tempo esgotado, conexão perdida após enviar, ou 5xx | `Uncertain`, sem reenvio automático, e sincroniza para conferir |
| Sessão revogada ou inválida (401 com código próprio, por HTTPS) | `Reverted`, e o app apaga a sessão, a cópia e as tentativas pendentes, e volta à tela de vínculo |

**Tentativa pendente:** antes de enviar, o app grava a chave e o conteúdo do comando em uma tabela de `dengo-sync.db`. `Confirmed` ou `Reverted` apagam o registro. Com `Uncertain`, ele fica, inclusive depois de reabrir o app, e o aviso oferece **Tentar de novo** e **Descartar**. Tentar de novo reenvia a mesma chave com o mesmo conteúdo. Isso resolve a dúvida com segurança, inclusive se a transação original ainda estiver em andamento: a restrição de unicidade da chave faz o segundo envio esperar e devolver o resultado original. Um comando igual ao pendente reaproveita a chave. Descartar exige conexão e chama `DELETE /v1/operations/{chave}`, que tenta reservar a chave como cancelada. A restrição de unicidade faz essa chamada esperar uma transação original em andamento. Se a chave for cancelada, um envio atrasado com ela não pode mais confirmar, e o registro local é apagado. Se a escrita já estava confirmada, o servidor devolve a revisão dela, e o registro só é apagado quando a cópia local alcançar essa revisão; até lá, um comando igual reaproveita a chave e recebe o resultado original. Enquanto o descarte não termina, um comando igual espera por ele. Depois que o recado aparece na cópia, publicar o mesmo texto de novo é uma ação nova e legítima. Sem conexão, a tentativa continua pendente.

O servidor mantém as chaves sem prazo, porque o volume de duas pessoas é pequeno e uma tentativa pode ficar pendente por tempo indeterminado. **Alternativa descartada:** consultar o resultado pela chave, porque "não encontrado" não prova que a transação original não vá confirmar depois.

A tentativa pendente não é um rascunho: é algo que a pessoa já mandou publicar, e por isso é a única exceção à regra de rascunhos fora do armazenamento durável.

### 8. Cópia local em banco novo; banco atual preservado

A cópia usa um arquivo novo, `dengo-sync.db`, com IDs vindos do servidor e a revisão local. O `dengo-local.db` atual fica intacto e sem uso. **Motivo:** o servidor começa vazio por decisão, e assim nenhuma migração pode perder os dados existentes. Apagar esse arquivo exige decisão futura. Os testes não podem usar o arquivo novo do app, para não repetir o problema corrigido no PR #5.

A regra atual continua: falha de leitura não recria o banco.

**Revogação:** quando o servidor informa que a sessão foi revogada, o app apaga o token, `dengo-sync.db` e as tentativas pendentes antes de voltar à tela de vínculo. A revogação cancela as chamadas em andamento, e cada resposta só é gravada depois de conferir, na mesma transação, se a sessão que a pediu ainda é a ativa; assim, uma resposta atrasada não recria a cópia apagada. Só esse código explícito apaga dados; erros de rede ou de servidor nunca fazem isso. Vincular outra pessoa no mesmo aparelho também começa por apagar a cópia anterior. **Limite inevitável:** um aparelho perdido que nunca volta a se conectar não recebe a revogação e continua com a última cópia, protegida só pelo sandbox do Android e pelo bloqueio do celular. Bloqueio adicional dentro do app fica fora deste escopo e precisa ser dito a Lidianne, junto das demais informações de privacidade.

### 9. Notificações com aviso sem conteúdo

| Acontecimento | Quem recebe | Tipo de aviso |
|---|---|---|
| Pedido novo, sem espaço pessoal ativo | Vinícius | Alta prioridade, com notificação visível |
| Resposta ao pedido | Lidianne | Alta prioridade, com notificação visível |
| Pedido novo com espaço pessoal ativo, agradecimento, mood, espaço pessoal e recado | A outra pessoa | Prioridade normal, sem notificação |

O aviso leva só o tipo e a revisão. O app busca os dados, com tempo curto, e monta a notificação; se falhar, mostra um texto genérico. O texto de `OTHER` nunca entra na notificação. **Motivo:** o Google não recebe conteúdo afetivo, e a documentação do FCM reserva a alta prioridade a mensagens que resultam em notificação visível, por isso os demais casos usam prioridade normal.

A entrega não é garantida, e a sincronização ao abrir o app cobre as perdas. Um app encerrado à força não recebe aviso até ser aberto de novo.

**Outbox:** a escrita grava o aviso na mesma transação, e um processo agendado envia com tentativas limitadas, descartando avisos velhos. Falhar no FCM nunca desfaz a escrita. **Alternativa descartada:** enviar direto depois do commit, que perderia o aviso se o processo caísse nesse intervalo.

A configuração do Firebase fica em `local.properties`, com `FirebaseOptions` montado no código, sem o plugin `google-services`. Sem ela, o app compila e os testes rodam, com notificações desligadas. A chave de serviço fica só no servidor.

### 10. Desenhos

O desenho vai como um documento JSON e é guardado inteiro no PostgreSQL, sempre publicado de uma vez, em uma única transação. O compositor hoje não tem limite de pontos, então o servidor precisa de um teto documentado. **Antes de fixar o teto,** a etapa 6 mede desenhos reais no aparelho; a medição define o número, e acima dele o app avisa sem publicar nada.

### 11. Horário e data

Horários vêm do relógio do servidor. A data do mood usa um fuso configurado no servidor, `America/Sao_Paulo`. **Limite aceito:** em viagem para outro fuso, a virada do dia do mood segue esse fuso.

### 12. Implantação

VPS pequena com pelo menos 2 GB de RAM, Docker Compose, PostgreSQL local à VPS e proxy reverso com HTTPS automático. Só as portas 80 e 443 ficam expostas publicamente. Segredos ficam em arquivo fora do repositório. Backup diário cifrado, copiado para fora da VPS, com restauração testada. Os logs não registram conteúdo afetivo nem tokens.

**Limite aceito:** o responsável administra o servidor e tem acesso técnico aos dados. Isso precisa ser dito a Lidianne antes do uso.

## API inicial

| Método e caminho | Uso |
|---|---|
| `POST /v1/pairings` | Trocar código por token |
| `PUT /v1/devices/me/push-token` | Registrar o token do FCM |
| `GET /v1/sync` | Ler alterações |
| `POST /v1/requests` | Criar pedido |
| `POST /v1/requests/{id}/accept`, `/decline`, `/acknowledge` | Responder e agradecer |
| `PUT /v1/mood` | Definir mood |
| `POST` e `DELETE /v1/personal-space` | Ativar e encerrar espaço pessoal |
| `POST /v1/mural/notes` e `/v1/mural/drawings` | Publicar recado |
| `DELETE /v1/operations/{chave}` | Descartar uma tentativa incerta, com resposta definitiva |

`DELETE` de espaço pessoal encerra um estado; nenhum dado é excluído. Repetir uma ação sem mudança efetiva responde 200 com `changed: false`, como `WriteResult.Confirmed` faz hoje.

## Riscos

- **Economia de bateria do Motorola ou do Samsung atrasar avisos:** testar nos dois aparelhos em repouso e documentar ajustes, se forem necessários.
- **Servidor exposto com dados íntimos:** tokens revogáveis, limite de tentativas, HTTPS, backup cifrado e atualizações de segurança da VPS.
- **Manutenção recai sobre uma pessoa:** preferir componentes simples e documentar a restauração.
- **Restauração de backup perde o que veio depois dele:** é inerente ao backup diário; o novo vínculo faz os aparelhos refletirem o servidor restaurado.
- **Perda de cobertura da `MainActivity` real:** já existente desde o PR #5; a validação nos dois aparelhos cobre esse caminho.

## Branches e entregas

| Etapa | Branch | Entrega |
|---|---|---|
| 0 | `docs/two-device-sync-spec` | Esta spec, ADR-025 e estado do projeto |
| 1 | `feat/backend-core` | Backend com identidade, pedidos, Histórico, mood e espaço pessoal, testado localmente |
| 2 | `chore/backend-deploy` | Implantação, HTTPS e backup com restauração testada |
| 3 | `feat/app-device-identity` | Vínculo e identidade no app, com troca de perspectiva removida |
| 4 | `feat/app-remote-sync` | Cópia local e sincronização no app |
| 5 | `feat/push-notifications` | Outbox e notificações |
| 6 | `feat/mural-sync` | Mural com texto e desenho |
| 7 | `docs/two-device-sync-closing` | Validação com os dois aparelhos e fechamento documental |

Cada etapa segue o fluxo de `docs/AI-WORKFLOW.md`: PR próprio, revisão do Codex e merge por Vinícius.

## Decisões em aberto

- Domínio a usar e VPS exata, com preço conferido na hora de contratar.
- Modelo e versão do Android do Motorola.
- Textos finais de notificações e da tela de vínculo.
- Teto de tamanho do desenho, depois da medição.
