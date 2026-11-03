## ADDED Requirements

### Requirement: Servidor como fonte da verdade
O servidor SHALL guardar os dados publicados do casal e SHALL aplicar as regras vigentes de `docs/features/requests.md`, `docs/features/mural.md` e da spec `local-couple-state`, confirmando cada escrita e seus acontecimentos de Histórico em uma única transação.

#### Scenario: Pedido visto nos dois aparelhos
- **WHEN** Lidianne cria um pedido e os dois aparelhos sincronizam
- **THEN** os dois mostram o mesmo pedido, estado, ID, horário e acontecimento de Histórico

#### Scenario: Pedido predefinido duplicado
- **WHEN** chega um pedido predefinido de um tipo que já tem pedido `PENDING` ou `ACCEPTED`
- **THEN** nenhum pedido nem acontecimento novo é criado e a resposta indica `changed: false`

#### Scenario: Resposta durante espaço pessoal
- **WHEN** Vinícius responde com o espaço pessoal ativo, mesmo que a cópia local dele ainda não mostre isso
- **THEN** o servidor recusa sem alterar o pedido e o app recebe `Reverted` e sincroniza

#### Scenario: Resposta a pedido já respondido
- **WHEN** chega uma resposta a um pedido que não está mais `PENDING`
- **THEN** o servidor recusa sem criar acontecimento

#### Scenario: Servidor inicia vazio
- **WHEN** o servidor entra em operação
- **THEN** ele começa sem interações, e nada do `dengo-local.db` de nenhum aparelho é enviado a ele

### Requirement: Escrita idempotente
O servidor SHALL tratar comandos com a mesma chave de idempotência como uma única operação e SHALL recusar a chave quando o conteúdo for diferente.

#### Scenario: Resposta perdida
- **WHEN** um comando é confirmado, a resposta se perde e a pessoa repete a mesma ação com a mesma chave
- **THEN** existe uma única alteração e um único acontecimento, e o resultado original é devolvido

#### Scenario: Mesma chave com outro conteúdo
- **WHEN** a chave é reutilizada com outro conteúdo
- **THEN** o servidor recusa e não altera nada

#### Scenario: Segundo envio com a transação original em andamento
- **WHEN** a mesma chave chega enquanto a primeira transação ainda não terminou
- **THEN** existe uma única alteração, e o segundo envio devolve o resultado da primeira ou repete a escrita somente se a primeira não tiver sido confirmada

### Requirement: Tentativa incerta durável
O app SHALL gravar a chave e o conteúdo de cada comando antes de enviá-lo, SHALL manter esse registro enquanto o resultado for incerto, inclusive depois de reabrir o app, e SHALL permitir repetir o envio com a mesma chave ou descartá-lo somente por ação explícita da pessoa.

#### Scenario: Reabertura com tentativa incerta
- **WHEN** um recado ou um pedido `OTHER` termina em `Uncertain` e o app é reaberto
- **THEN** o aviso continua disponível, e tentar de novo não publica em duplicidade, mesmo que o primeiro envio tenha sido confirmado

#### Scenario: Descartar tentativa
- **WHEN** a pessoa descarta uma tentativa incerta com conexão
- **THEN** o servidor cancela a chave, um envio atrasado com ela não pode mais confirmar, e o registro local é apagado

#### Scenario: Nova publicação depois de descartar
- **WHEN** a transação original ainda está em andamento, a pessoa descarta e publica de novo o mesmo conteúdo
- **THEN** o descarte espera a transação original terminar, o comando igual espera o descarte, e o conteúdo fica publicado uma única vez

#### Scenario: Descarte encontra escrita confirmada
- **WHEN** o servidor informa que a tentativa já estava confirmada e a cópia local ainda não alcançou essa revisão
- **THEN** o registro continua associado à chave, um comando igual recebe o resultado original, e o registro só é apagado quando a publicação aparece na cópia local

#### Scenario: Republicação intencional depois de aparecer
- **WHEN** a publicação já aparece na cópia local e a pessoa publica o mesmo texto de novo
- **THEN** o app trata como uma ação nova, com outra chave

#### Scenario: Descartar sem conexão
- **WHEN** a pessoa tenta descartar sem conexão
- **THEN** o app avisa que não foi possível e mantém a tentativa pendente

#### Scenario: Resultado definitivo
- **WHEN** o comando termina em `Confirmed` ou `Reverted`
- **THEN** o registro da tentativa é apagado

### Requirement: Sincronização sem perda
O sistema SHALL entregar a cada aparelho todas as alterações confirmadas, em ordem de revisão, e o app SHALL gravar cada página e a revisão alcançada em uma única transação local.

#### Scenario: Escritas concorrentes
- **WHEN** os dois aparelhos escrevem ao mesmo tempo enquanto outro lê alterações
- **THEN** nenhuma alteração confirmada fica de fora das leituras seguintes

#### Scenario: Interrupção no meio da paginação
- **WHEN** a sincronização é interrompida entre duas páginas
- **THEN** a cópia local mantém um estado de uma revisão completa e a próxima sincronização continua dela sem duplicar dados

#### Scenario: Servidor restaurado de backup anterior
- **WHEN** um aparelho se conecta depois de o servidor ser restaurado de um backup
- **THEN** a sessão é recusada, o aparelho apaga a cópia local, e depois do novo vínculo mostra exatamente o estado do servidor, incluindo alterações novas

#### Scenario: Atualização sem aviso
- **WHEN** um aviso se perde e o app está aberto ou volta ao primeiro plano
- **THEN** a alteração aparece pela sincronização periódica ou de retomada

### Requirement: Uso sem conexão
O app SHALL mostrar a última cópia local quando o servidor estiver inacessível, SHALL indicar que os dados podem estar desatualizados e SHALL NOT enfileirar escritas para envio posterior.

#### Scenario: Abrir sem internet
- **WHEN** um aparelho vinculado abre o app sem internet
- **THEN** a última cópia aparece com a indicação de possível desatualização

#### Scenario: Ação sem internet
- **WHEN** a pessoa faz uma ação e não há conexão
- **THEN** o app avisa que não enviou, não mostra a ação como enviada e não a reenvia sozinho ao voltar a conexão

#### Scenario: Conexão recuperada
- **WHEN** a conexão volta com o app aberto
- **THEN** a cópia é atualizada e a indicação de desatualização desaparece

### Requirement: Recados entre aparelhos
O sistema SHALL sincronizar recados de texto e de desenho completos com autoria, horário, ordem e conteúdo, sem criar `HistoryEvent`, mantendo os previews nas duas Homes. O servidor SHALL recusar um desenho acima do teto documentado sem gravar nenhuma parte dele.

#### Scenario: Desenho recebido
- **WHEN** uma pessoa publica um desenho e o outro aparelho sincroniza
- **THEN** o outro aparelho mostra todos os traços, cores e proporção no Mural e no preview da Home, quando ele é o mais recente

#### Scenario: Desenho acima do teto
- **WHEN** uma pessoa tenta publicar um desenho acima do teto
- **THEN** nada é publicado, o rascunho continua disponível e o app explica o motivo

#### Scenario: Recado durante espaço pessoal
- **WHEN** alguém publica um recado com o espaço pessoal ativo
- **THEN** a publicação é aceita e nenhuma notificação visível é gerada

### Requirement: Operação segura do servidor
O servidor SHALL aceitar tráfego de clientes somente por HTTPS, SHALL NOT registrar conteúdo afetivo nem tokens em logs, SHALL NOT expor o banco publicamente e SHALL manter backup cifrado fora da VPS com restauração verificada.

#### Scenario: Restauração
- **WHEN** um backup é restaurado em um ambiente separado
- **THEN** o servidor restaurado devolve os mesmos dados confirmados até o momento do backup, invalida todas as sessões e códigos de vínculo, e os aparelhos voltam a sincronizar depois de novo vínculo

#### Scenario: Backup anterior à revogação
- **WHEN** o backup restaurado contém a sessão de um aparelho revogado depois dele
- **THEN** esse aparelho não consegue autenticar nem ler dados

#### Scenario: Backup com código de vínculo
- **WHEN** o backup restaurado contém um código de vínculo ainda válido no momento do backup, mesmo que usado depois dele
- **THEN** o código é invalidado antes de o servidor aceitar tráfego, e novo vínculo exige um código gerado depois da restauração

#### Scenario: Conexão sem HTTPS
- **WHEN** um build de uso real tenta falar com o servidor sem HTTPS
- **THEN** a conexão é bloqueada pelo app
