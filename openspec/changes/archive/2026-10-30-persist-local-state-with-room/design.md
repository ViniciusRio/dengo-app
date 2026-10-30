# Design

## Context

O `CoupleRepository` já é a seam usada pelos quatro ViewModels. O `FakeCoupleRepository` mantém `CoupleState` em memória; `PrototypeStateViewModel` o instancia na abertura do app. O ADR-020 definiu leitura assíncrona, `WriteResult` e preservação dos compositores. A instalação atual não possui banco anterior nem dados demonstrativos a migrar. Os contratos de produto continuam em `docs/features/` e o comportamento novo está em `specs/local-couple-state/spec.md`.

## Goals / Non-Goals

**Goals:**

- Introduzir um adapter Room atrás do `CoupleRepository`, mantendo os ViewModels e a semântica do Fake.
- Garantir gravação transacional de cada operação e reconstrução determinística do `CoupleState`.
- Testar o adapter real com banco fechado e reaberto, falhas e concorrência, além da UI existente.

**Non-Goals:**

- Persistir estado de composição, perspectiva ou identidade de sessão.
- Criar sincronização, login, backup, migração de dados inexistentes, telas novas ou regras de produto novas.
- Introduzir infraestrutura de backend ou laboratório técnico no app principal.

## Decisions

### 1. Adapter Room no contrato existente

Implementar `RoomCoupleRepository` e injetá-lo no ponto de composição do app. Manter o Fake para testes e comparação de contrato. Não criar métodos de armazenamento nos ViewModels nem alterar a interface pública sem necessidade demonstrada. A alternativa de substituir o Fake por chamadas diretas a DAO espalharia regra de negócio e tratamento de falhas pelas telas.

### 2. Tabelas relacionais explícitas

Usar tabelas para pedidos, eventos de Histórico, mood atual, espaço pessoal, recados, traços e pontos. Armazenar enumerações por identificador estável e tempo em representação estável; incluir ordem explícita de inserção para eventos e ordem dos traços/pontos. Reconstituir os modelos de domínio em um único lugar e validar dados lidos antes de publicar `Ready`. Uma linha de recado identifica texto ou desenho, sem misturar as modalidades.

Alternativas consideradas: um único snapshot serializado simplificaria a escrita inicial, mas tornaria a evolução e a inspeção do esquema opacas; um BLOB binário por desenho reduziria linhas, mas exigiria um formato e um codec versionados já neste incremento. As tabelas explícitas favorecem testes e inspeção para o volume esperado neste projeto pessoal. Se medições em aparelho mostrarem custo excessivo, rever o armazenamento de desenhos em decisão posterior.

### 3. Transações por operação de domínio

Cada comando lê o estado necessário, valida as regras vigentes e grava todas as linhas relacionadas na mesma transação Room. IDs persistidos são gerados de forma que não sejam reutilizados depois de reabrir o banco. Criar ou responder a pedido grava também o evento correspondente; publicar desenho grava recado, traços e pontos; mood e espaço pessoal gravam estado atual e evento quando houver mudança efetiva. Uma transação abortada não publica novo estado `Ready`.

Alternativa: gravar linhas separadamente com compensação manual. Isso permitiria estados parciais, contrariando o contrato de produto.

### 4. Leitura, falha e retorno de escrita

Na abertura, publicar `Loading`, ler o banco e só então publicar `Ready`. Atualizações confirmadas alimentam uma leitura consistente; `retryRead()` tenta novamente os dados existentes. Erro de abertura, consulta ou conversão publica `ReadError`, sem chamar migração destrutiva, `clearAllTables()` ou recriação automática. Se a confirmação durável não puder ser determinada, retornar `Uncertain`; regras de domínio rejeitadas retornam `Reverted`; `Confirmed` somente após a transação concluir. Propagar `CancellationException`. Não fazer retry automático de escrita. Durante falha de leitura, impedir que uma escrita baseada em estado desconhecido substitua dados.

Alternativa: restaurar um snapshot vazio após erro. Foi rejeitada pela decisão humana de preservar o banco e tentar nova leitura.

### 5. Inicialização e evolução do banco

Criar um banco nomeado na instalação atual e manter `android:allowBackup="false"`. Não há migração de dados em memória ou de esquema anterior, pois nenhum banco foi distribuído. Exportar o esquema Room e evitar fallback destrutivo. No helper SQLite usado pelo Room 2.8.5, `allowDataLossOnRecovery(false)` evita recriação após falha de abertura, mas o `onCorruption` padrão ainda exclui o arquivo; a fábrica de abertura deve substituir esse callback para preservar os bytes e deixar a falha chegar ao repository como `ReadError`. A troca do adapter fica concentrada na composição do app; telas continuam reagindo ao contrato do Incremento 1.

## Risks / Trade-offs

- **Muitos pontos em desenhos** → medir abertura e publicação no Galaxy A26 com desenhos representativos; evitar limite arbitrário neste incremento.
- **Falha depois do commit, antes da resposta ao chamador** → tratar como `Uncertain` e orientar conferência do estado antes de repetir, conforme ADR-020.
- **Ordem do Histórico com horários iguais** → persistir sequência de inserção independente do timestamp e verificar após reabertura.
- **Leitura de dados inválidos ou esquema incompatível** → `ReadError` e preservação do banco; não tentar reparo automático que possa destruir dados.
- **Banco local sem criptografia adicional** → o app permanece no sandbox Android e com backup desativado; proteção adicional de dispositivo exigiria decisão própria.

## Migration Plan

1. Adicionar Room e esquema inicial; cobrir mapeamento e comandos com testes de banco real.
2. Ligar o adapter ao ponto de composição e rodar regressões unitárias e instrumentadas.
3. Instalar sem desinstalar no Galaxy A26; validar publicação, encerramento forçado/reabertura, consistência nas duas perspectivas e desenho recuperado. A instalação anterior não tem banco a migrar, então seus dados em memória não podem ser recuperados após o processo terminar.
4. Reverter a integração antes de distribuir, se necessário, preservando o banco criado para diagnóstico. Não usar migração destrutiva como rollback.
5. Após revisão independente, testes e aprovação humana, consolidar a decisão técnica e o estado real em `docs/` e no checkpoint do vault. Commit/push continuam dependendo de autorização específica.
