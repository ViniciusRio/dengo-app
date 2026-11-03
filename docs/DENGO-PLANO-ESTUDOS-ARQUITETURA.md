# Dengo — Plano de evolução técnica e laboratório de arquitetura Kotlin

> **Status:** roadmap de estudo revisado em 2026-10-30. O primeiro laboratório backend está no ADR-022; o primeiro recorte de RabbitMQ isolado está no ADR-023. As fases posteriores continuam propostas.
> **Objetivo:** servir como briefing para análise humana e por agente de IA, sem substituir as especificações e decisões vigentes do repositório.
> **Repositório:** https://github.com/ViniciusRio/dengo-app
> **Data da proposta:** 2026-10-29

## 1. Contexto e motivação

O Dengo é um aplicativo Android pessoal de comunicação afetiva entre duas pessoas. A intenção desta iniciativa é utilizá-lo também como **laboratório de engenharia de software em Kotlin**, estudando na prática oito temas recorrentes em entrevistas de backend e arquitetura:

1. Spec-Driven Development (SDD) / OpenSpec;
2. RabbitMQ;
3. Apache Kafka;
4. Amazon SQS;
5. CQRS;
6. Cache / Redis;
7. Disponibilidade e resiliência;
8. Observabilidade.

A meta não é adicionar tecnologias apenas para constarem no currículo. Cada experimento deve demonstrar **problema, decisão, implementação, testes, falhas simuladas, observação e trade-offs**, preservando a utilidade e a simplicidade do produto.

**Diretriz de stack proposta:** Android em **Kotlin + Jetpack Compose** e, se houver aprovação explícita para uma etapa de backend, **Kotlin + Spring Boot**. Não se propõe Java no backend. Infraestrutura de estudo preferencialmente local com Docker Compose e Testcontainers; AWS somente em experimento controlado, com atenção a custos.

## 2. Fontes de verdade e limites de autorização

Antes de propor alterações, ler **AGENTS.md** e, sob demanda conforme ele, a documentação de produto, MVP, fluxos, design, especificações de funcionalidades e ADRs. Verificar também `docs/STATUS.md`, o código-fonte e os testes atuais.

**Esta proposta não autoriza**: iniciar MVP 2, adicionar backend de produto, autenticação remota, sincronização, PostgreSQL, Redis ou mensageria de produto; alterar comportamento de produto; executar commits ou push; declarar fases concluídas. Tudo isso exige decisão e aprovação humana conforme as regras do repositório. Em 2026-10-30, o responsável escolheu o laboratório backend isolado e depois aprovou o recorte RabbitMQ sintético da mudança OpenSpec `study-rabbitmq-delivery-basics`. Essas escolhas não ampliam o escopo de produto.

Em caso de divergência entre este briefing e documentação/código atual, **não presumir que este documento está correto**. Apontar a divergência, citar os arquivos e solicitar decisão.

## 3. Estado do projeto conhecido na análise inicial

**Nota de atualização (2026-10-30):** esta seção preserva a fotografia de 2026-10-29. O estado vigente está em `docs/STATUS.md`, ADR-021 e `openspec/specs/local-couple-state/spec.md`: Room já persiste os dados publicados no mesmo aparelho. O laboratório posterior de retorno `Uncertain` foi especificado, testado no A26 e arquivado em OpenSpec, sem alterar código de produção. A próxima etapa de produto permanece indefinida. O HEAD conferido nesta revisão é `1f598b4`, com o laboratório anterior ainda sem commit.

A inspeção do GitHub em 2026-10-29 indicou:

- Um módulo Android `:app`, Kotlin, Compose e Material 3.
- Domínio existente: `CareRequest`, `Mood`, `PersonalSpace`, `MuralNote`, `HistoryEvent`, `CoupleState`.
- Fluxos de pedidos com estados `PENDING`, `ACCEPTED`, `DECLINED` e `ACKNOWLEDGED`.
- `CoupleRepository` com `StateFlow<RepositoryReadState>`, métodos suspensos e `WriteResult` (`Confirmed`, `Reverted`, `Uncertain`).
- `FakeCoupleRepository` ainda mantém estado em memória.
- `docs/STATUS.md` e ADR-020 registram **Incremento 1 de persistência local concluído e validado**: contrato assíncrono, tratamento de leitura/escrita e preservação de rascunhos. **Room / Incremento 2 ainda não iniciado** nesse checkpoint.
- Não há backend, banco persistente, autenticação remota ou sincronização entre aparelhos na versão analisada.
- O `README.md` contém uma visão geral mais resumida; conferir `docs/STATUS.md`, ADRs e código antes de tomar decisões.

**Atenção:** esta é uma fotografia, não uma garantia do estado futuro da branch. A IA deve confirmar o HEAD atual antes de qualquer planejamento detalhado.

## 4. Princípios para a evolução

1. **Produto antes da tecnologia:** não modificar a experiência afetiva só para acomodar padrões arquiteturais.
2. **Incrementos pequenos e reversíveis:** uma decisão e um conjunto claro de critérios de aceitação por mudança.
3. **Android primeiro:** concluir a persistência local planejada antes de propor sincronização remota, salvo decisão humana diferente.
4. **Separar produto e laboratório:** recursos de estudo que não agreguem valor ao casal devem ser isolados em módulos, perfis, ambientes ou documentação de experimentos.
5. **Sem overengineering:** CQRS não exige bancos separados; Kafka, RabbitMQ e SQS não precisam coexistir no ambiente real.
6. **Confiabilidade demonstrável:** testar duplicidade, concorrência, falhas, reprocessamento e recuperação, em vez de apenas validar o caminho feliz.
7. **Privacidade:** não versionar dados afetivos reais, tokens ou credenciais; usar fixtures sintéticas, redigir logs e limitar telemetria.
8. **Evidência verificável:** diferenciar testes executados, revisão humana, hipóteses e trabalho ainda pendente.
9. **Sem ações irreversíveis automáticas:** nenhuma alteração de infraestrutura paga, publicação, commit ou push sem autorização explícita.

## 5. Mapeamento dos oito temas para o Dengo

| Tema | Experimento / caso de uso | Aprendizado e evidência esperada | Dependência |
|---|---|---|---|
| **SDD / OpenSpec** | Especificar incremento de persistência e, depois, sincronização; registrar requisitos, design, tarefas e critérios de aceitação | Rastreabilidade entre spec, testes e implementação; revisão de mudanças | Imediato, respeitando documentação vigente |
| **RabbitMQ** | Evento de pedido de cuidado confirmado gera trabalho assíncrono de notificação simulada | Exchange/queue, publisher confirm, ACK, retries limitados, DLQ, idempotência e outbox | Backend/laboratório |
| **Kafka** | Eventos `CareRequestCreated`, `CareRequestAccepted`, `MoodChanged` alimentam uma projeção de leitura experimental do Histórico | Tópicos, chave por agregado, partições, consumer group, offset, replay e lag | Backend/laboratório |
| **Amazon SQS** | Implementação alternativa do worker de notificações, isolada do RabbitMQ | Standard vs FIFO, Visibility Timeout, DeleteMessage, redrive e DLQ; custo e diferenças | Backend/laboratório AWS |
| **CQRS** | Separar operações de alteração de pedidos das consultas de Home/Histórico | Commands vs queries, projeções, consistência; **não** introduzir complexidade sem necessidade | Pode começar no Android |
| **Cache / Redis** | Cache experimental de consultas de leitura compartilhadas, com invalidação após mudança | Cache-aside, hit/miss, TTL, invalidação, stampede, stale data | Backend |
| **Disponibilidade / resiliência** | Simular backend lento ou indisponível durante envio de pedido ou recado | Timeout, retry seguro, circuit breaker, idempotência, estados incertos e recuperação | Android já oferece base; backend amplia |
| **Observabilidade** | Seguir uma operação de pedido da API ao consumidor e à projeção | Logs estruturados, métricas, traces, correlação, SLI/SLO, dashboards e alertas | Backend/laboratório |

**Importante:** os casos acima são propostas de aprendizado. Notificações remotas, eventos distribuídos e sincronização **não estão aprovados** como novas funcionalidades de produto. Não adicionar eventos de Histórico para publicações do Mural sem revisão da especificação vigente.

## 6. Arquitetura-alvo hipotética (não aprovada)

```text
Android (Kotlin + Compose)
  ├── CoupleRepository (contrato atual; avaliar adaptação)
  ├── Room (persistência local concluída no Incremento 2)
  └── Cliente HTTP / sincronização (futuro, condicionado ao MVP 2)
           |
           v
Backend (Kotlin + Spring Boot) [proposta]
  ├── API REST / autenticação e autorização
  ├── Commands / Queries (CQRS proporcional)
  ├── PostgreSQL (fonte de verdade remota)
  ├── Redis (cache de consultas justificadas)
  ├── Outbox / eventos de domínio
  └── Adapter de mensageria [um por experimento]
       ├── RabbitMQ → worker de notificações simuladas + DLQ
       ├── Kafka → projeção experimental + replay
       └── SQS → alternativa experimental para worker

Observabilidade: OpenTelemetry → backend de métricas/logs/traces
Ambiente local: Docker Compose + Testcontainers
```

**Decisão implementada no laboratório:** Kotlin/Spring Boot isolado em `labs/backend/`, com `settings.gradle.kts` próprio, armazenamento em memória e dados sintéticos. O wrapper Gradle único da raiz subiu para 8.14.5; a regressão Android de testes unitários e builds passou, e uma rodada posterior da suíte instrumentada no A26 terminou com `OK (24 tests)`. O laboratório não é o backend do produto nem participa do fluxo Android. **Continuam abertas:** localização e stack de um eventual backend de produto, autenticação, pareamento, sincronização, resolução de conflitos, infraestrutura compartilhada e eventual AWS. Não transportar decisões do laboratório para o MVP 2 automaticamente.

## 7. Roadmap por fases e critérios de saída

### Fase 0 — Diagnóstico e decisão arquitetural (CONCLUÍDA NO ALCANCE ATUAL)

- Conferir branch/HEAD, `AGENTS.md`, documentação, ADRs, implementação e testes.
- Produzir inventário do que existe, lacunas, divergências e dependências.
- Confirmar que Room / Incremento 2 foi concluído e que o plano anterior estava defasado.
- Definir fronteira entre evolução do produto e laboratório técnico.
- Propor, **sem implementar**, ADR(s), especificação e plano de execução para aprovação.

**Saída:** diagnóstico e escolha humana de um laboratório backend isolado, sem aprovação de backend de produto.

### Fase 1 — Persistência local real (CONCLUÍDA)

- Implementar o contrato de persistência já especificado, sem alterar fluxos de UX sem autorização.
- Preservar pedidos, estados, eventos factuais, mood, espaço pessoal e recados publicados (inclusive desenhos).
- Testar encerramento/reabertura, atomicidade, falha de escrita, migração se aplicável, cancelamento e preservação de rascunhos.
- Executar testes unitários/instrumentados e revisão no dispositivo.

**Saída:** dados publicados recuperados após reabertura no A26, no alcance do ADR-021. Os testes e a aprovação humana históricos estão registrados em `docs/STATUS.md`.

### Fase 2 — SDD contínuo e avaliação de CQRS (EM ANDAMENTO)

- Identificar operações de escrita e consultas reais; propor separação apenas onde houver ganho de clareza/testabilidade.
- Documentar invariantes e estados, especialmente resultados `Uncertain` e idempotência.
- Aplicar especificação → implementação → testes → revisão em cada mudança.

**Estado:** o laboratório OpenSpec de `Uncertain` foi concluído. `CoupleRepository` já oferece leitura por fluxo e comandos suspensos; não há ganho demonstrado em separar mais interfaces ou criar projeções persistidas no Android. CQRS continua uma hipótese a testar onde houver uma consulta e um comando reais.

### Fase 2A — Primeiro laboratório backend isolado (IMPLEMENTADO NO ALCANCE DO ADR-022)

- Especificar em OpenSpec uma API HTTP de laboratório para criar e consultar pedidos predefinidos sintéticos.
- Usar Kotlin/Spring Boot em build próprio dentro de `labs/backend/`, com o wrapper Gradle único da raiz atualizado e verificado nos dois builds, armazenamento em memória e execução local; não integrar ao app Android.
- Demonstrar uma regra de duplicidade, a diferença entre comando e consulta, o resultado após reinício e os limites de um único armazenamento.
- Testar contrato HTTP e regra de domínio com dados sintéticos; documentar comandos de reprodução e quando esta separação simples não justifica CQRS mais elaborado.

**Saída:** laboratório reproduzível com oito testes, smoke test local e regressão Android de testes unitários/builds; em rodada posterior, 24 testes instrumentados passaram no A26 após atualizar Gradle. O resultado e os limites estão no ADR-022; o próximo experimento depende de nova escolha. Não aprova MVP 2.

### Fase 3 — Backend de produto e sincronização (PROPOSTA, sujeita a ADR/MVP 2)

- Especificar autenticação, autorização, pareamento, contrato REST e proteção de dados.
- Criar persistência remota com PostgreSQL, migrações e testes de integração.
- Projetar sincronização e resolução de conflitos, incluindo operações incertas e concorrência entre aparelhos.
- Integrar Android somente após validação do backend e aprovação do produto.

**Saída:** fluxo ponta a ponta validado em ambiente de teste com dados sintéticos.

### Fase 4 — RabbitMQ como primeiro laboratório de mensageria

**Primeiro recorte implementado (ADR-023):** perfil `rabbit-lab` com sinal sintético independente de pedidos, publisher confirm e roteamento obrigatório, ACK manual e teste de reentrega após falha anterior ao ACK. Não há outbox, retries limitados ou DLQ neste recorte. As linhas seguintes permanecem propostas para estudos posteriores, sem aprovação de integração com produto.

- Publicar evento após confirmação da escrita (avaliar transactional outbox).
- Consumir em worker Kotlin com ACK somente após sucesso.
- Demonstrar duplicidade, falha antes do ACK, retries com limites, DLQ e reprocessamento manual seguro.

**Saída:** testes reproduzíveis de falhas e explicação dos trade-offs.

### Fase 5 — Kafka e SQS como experimentos alternativos

- Kafka: particionar por chave adequada, consumer groups, commits, replay, rebalances e projeção experimental.
- SQS: comparar Standard/FIFO, visibilidade, exclusão, DLQ e idempotência.
- Usar payloads e contratos compatíveis para comparar sem acoplar o domínio ao broker.

**Saída:** tabela comparativa baseada em experimentos, sem obrigação de manter três brokers ativos.

### Fase 6 — Cache, resiliência e observabilidade

- Redis: definir hipótese de desempenho, medir antes/depois e testar invalidação.
- Resiliência: simular timeout, indisponibilidade, lentidão, retries e circuit breaker; garantir que operações não sejam duplicadas.
- Observabilidade: instrumentar HTTP, banco e consumers; traces correlacionados, métricas de latência/erro/filas e logs sem dados sensíveis.
- Documentar SLI/SLO **de laboratório**, sem prometer SLA de produção.

**Saída:** dashboards, testes de falha, resultados medidos e runbook de diagnóstico.

## 8. Padrão de entrega para cada experimento

Cada tópico deve gerar, quando aplicável:

- **Problema e hipótese:** por que usar a técnica neste caso?
- **Especificação:** comportamento esperado, invariantes e critérios de aceitação.
- **ADR:** alternativas consideradas, decisão e trade-offs.
- **Implementação isolada:** sem degradar o produto principal.
- **Testes:** unitários, integração, concorrência, falhas e reprocessamento.
- **Observabilidade:** métricas/traces/logs relevantes e exemplos sintéticos.
- **Reprodução:** comandos para executar localmente e provocar falhas.
- **Resultado:** o que foi aprendido, limites, quando não usar a tecnologia.
- **Revisão humana:** registro do que foi efetivamente validado e aprovado.

## 9. Questões que a análise deve responder antes de codificar

As perguntas 1, 2, 6 e 7 receberam respostas parciais nos Incrementos 2 e no laboratório de `Uncertain`: Room foi concluído; o primeiro exercício posterior demonstrou os dois desfechos de escrita incerta; CQRS adicional no Android não mostrou ganho concreto. As perguntas de backend de produto, mensageria, AWS e sincronização permanecem abertas e não bloqueiam o laboratório isolado.

1. A documentação atual ainda aponta Room como próximo incremento, ou houve mudança posterior?
2. Qual a menor alteração que gera aprendizado real sem desviar o produto?
3. O backend deve residir no mesmo repositório ou em repositório próprio? Quais custos de cada escolha?
4. O laboratório de mensageria deve ser separado do fluxo real de notificações?
5. Como preservar privacidade, identidade de duas pessoas e a semântica de espaço pessoal?
6. Como garantir que uma operação com resultado `Uncertain` não seja repetida indevidamente?
7. Como demonstrar CQRS sem criar projeções e bancos desnecessários?
8. Quais experimentos são locais e quais exigem conta AWS, custos e permissões?
9. Que testes e verificações no Samsung são obrigatórios para cada incremento Android?
10. Quais ADRs e especificações precisam ser propostos antes de alterar arquitetura?

## 10. Instrução pronta para Codex / Claude Code

**Registro histórico:** a instrução abaixo era adequada ao diagnóstico de 2026-10-29. Não deve ser reutilizada como comando para implementar Room nem para aprovar as fases futuras. O primeiro laboratório backend está registrado no ADR-022 e na mudança OpenSpec arquivada `2026-10-30-start-isolated-backend-lab`; o primeiro recorte RabbitMQ isolado está no ADR-023 e na mudança `study-rabbitmq-delivery-basics`.

> **Tarefa: análise e planejamento apenas. Não implemente ainda.**
>
> Analise este documento como proposta, não como decisão aprovada. Leia o `AGENTS.md` do repositório Dengo e siga suas regras de leitura. Inspecione a branch/HEAD atual, o código, testes, `docs/STATUS.md`, specs e ADRs, com atenção ao ADR-020.
>
> Quero evoluir o Dengo para estudar SDD/OpenSpec, RabbitMQ, Kafka, Amazon SQS, CQRS, Redis/cache, disponibilidade/resiliência e observabilidade, utilizando Kotlin no Android e **preferencialmente Kotlin + Spring Boot** em um backend futuro. O objetivo é aprendizado demonstrável, sem introduzir complexidade artificial ou alterar o escopo do produto sem autorização.
>
> Entregue, **sem editar arquivos, sem criar commits e sem push**:
>
> 1. **Diagnóstico factual:** arquitetura atual, funcionalidades, persistência, testes, documentação e estado da próxima etapa; cite arquivos e caminhos.
> 2. **Divergências:** diferenças entre este briefing e o repositório atual, com evidências e recomendação de como resolvê-las.
> 3. **Decisões pendentes:** opções e trade-offs para backend, localização do laboratório, sincronização e escopo.
> 4. **Roadmap revisado:** dependências, incrementos pequenos, critérios de aceitação, testes e gates de aprovação.
> 5. **Próximo passo recomendado:** uma única mudança inicial, com escopo explícito, arquivos provavelmente afetados, riscos e plano de validação.
> 6. **Proposta documental:** quais ADRs/specs/planos criar ou atualizar **após minha aprovação**, sem sobrescrever decisões vigentes.
>
> Não assuma que o roadmap desta proposta está aprovado. Se faltar informação para uma decisão de produto, apresente alternativas e faça perguntas objetivas. Não diga que testes passaram sem executá-los e não afirme que houve validação humana sem evidência.

## 11. Definição de sucesso da iniciativa

A iniciativa terá sucesso quando for possível explicar e demonstrar, com código e testes, **por que cada conceito foi escolhido, como se comporta sob falha, como é observado e quando não deveria ser usado**. O Dengo deve continuar coerente com seu propósito de comunicação afetiva, mesmo que parte dos experimentos permaneça apenas no laboratório técnico.

---

**Próxima ação:** revisar a evidência do primeiro recorte RabbitMQ e escolher um problema seguinte de estudo ou uma etapa de produto; não iniciar automaticamente backend de produto, CQRS adicional, outbox ou DLQ.
