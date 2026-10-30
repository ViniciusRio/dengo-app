# Design

## Context

`labs/backend/` já usa Kotlin 2.2.20, Spring Boot 4.1.2, Java 21 e Gradle 8.14.5 em build separado. A API `/lab/requests` mantém estado apenas em memória. O Docker local está disponível nesta máquina, mas sua disponibilidade em outras máquinas não é presumida. A documentação oficial do RabbitMQ distingue publisher confirms de consumer acknowledgements: o primeiro cobre a aceitação da mensagem pelo broker; o segundo cobre a entrega ao consumidor. Ver `https://www.rabbitmq.com/docs/confirms` e `https://www.rabbitmq.com/docs/reliability`.

## Goals / Non-Goals

**Goals:** publicação de sinal sintético com confirmação e roteamento, processamento simulado com ACK manual, reentrega controlada em teste e roteiro local reproduzível. Medir/observar qual evento ocorreu em cada etapa e explicar as garantias alcançadas.

**Non-Goals:** ligar a fila a `/lab/requests`, persistir evento junto com pedido, enviar notificação, manter deduplicação durável, prometer processamento exatamente uma vez, criar retry com limite, DLQ, outbox, múltiplos brokers, telemetria remota ou tocar no app Android.

## Decisions

### 1. Mesmo build, perfil separado

Adicionar Spring AMQP ao build existente e colocar configuração, publicador, consumidor e endpoint sintético atrás do perfil `rabbit-lab`. Sem o perfil, o backend atual inicia e seus oito testes continuam sem broker. O perfil usa o RabbitMQ local iniciado por Docker Compose; broker e porta HTTP continuam restritos a `127.0.0.1`. Um build novo repetiria o esqueleto Kotlin/Spring sem melhorar o experimento.

### 2. Envelope mínimo e fronteira de confirmação

`POST /lab/signals` recebe somente `eventId` em formato UUID, sem nomes, mensagens ou conteúdo afetivo. A mensagem usa ID e payload sintético fixo. O publicador usa mensagem persistente, exchange/fila duráveis, publisher confirms e mandatory return (ou mecanismo equivalente) para distinguir mensagem aceita e roteada de mensagem devolvida. HTTP `202` significa somente aceitação confirmada pelo broker, nunca processamento pelo worker. Uma confirmação ausente/negativa retorna resultado de falha ou incerteza sem reenvio automático; o README explica que repetir manualmente pode duplicar a entrega.

### 3. ACK manual e falha provocada

O worker confirma a entrega apenas após uma operação simulada bem-sucedida. Um teste de integração injeta falha **antes** desse ponto na primeira tentativa, provoca requeue e observa nova entrega do mesmo `eventId`. O teste distingue tentativas de entrega de processamento concluído. Ele não afirma idempotência após crash do worker nem deduplicação entre processos.

### 4. Verificação local

Testes sem Docker cobrem entrada, estado HTTP e a decisão de ACK/falha por uma interface pequena. Testes de integração com broker real cobrem confirm, roteamento, ACK e reentrega. O roteiro de reprodução usa dados sintéticos, portas loopback e limpeza do container. Não executar broker permanentemente. O app Android não muda; uma regressão Android de build/testes basta se apenas arquivos do laboratório forem alterados.

## Risks / Trade-offs

- Um publisher confirm positivo não comprova que o consumidor processou a mensagem; o HTTP deve refletir essa distinção.
- Falha entre efeito simulado e ACK pode gerar novo processamento. A garantia aqui é entrega ao menos uma vez, com duplicidade possível; deduplicação durável exige outro incremento.
- Sem banco/outbox, não há atomicidade entre criação de pedido e publicação. Por isso o endpoint de sinal é independente da API de pedidos.
- Broker via Docker acrescenta custo de ambiente; o perfil padrão e os testes sem Docker devem continuar utilizáveis.
