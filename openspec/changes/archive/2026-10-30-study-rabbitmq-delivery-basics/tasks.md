# Tasks

## 1. Perfil e ambiente local

- [x] 1.1 Fixar versão da imagem RabbitMQ e criar Docker Compose com porta AMQP limitada a `127.0.0.1`; documentar subida, parada e limpeza sem dados reais.
- [x] 1.2 Adicionar Spring AMQP e configuração `rabbit-lab` sem afetar o perfil padrão; primeiro testar que `/lab/requests` inicia sem broker.

## 2. Publicação

- [x] 2.1 Escrever testes para UUID válido/inválido, confirmação positiva, retorno sem rota e confirmação inconclusiva; observar as falhas antes da implementação.
- [x] 2.2 Implementar endpoint e publicador com publisher confirms e roteamento obrigatório, sem retry automático; verificar HTTP `202` somente para aceite confirmado.

## 3. Consumo e reentrega

- [x] 3.1 Escrever teste de integração com broker real para ACK após sucesso e falha injetada antes do ACK; observar reentrega do mesmo `eventId` e contagens separadas.
- [x] 3.2 Implementar worker mínimo com ACK manual, requeue controlado e processamento simulado, sem efeito de produto nem deduplicação durável.

## 4. Evidência e fechamento

- [x] 4.1 Executar testes sem broker e com broker real, smoke test local e regressão pertinente do backend/Android; registrar comandos e resultados, inclusive limitações.
- [x] 4.2 Revisar a implementação contra a spec e a garantia real do RabbitMQ; documentar por que outbox, DLQ e idempotência durável ficam para incrementos posteriores.
- [x] 4.3 Validar OpenSpec em modo estrito, atualizar roadmap/status/ADR se a decisão se consolidar e arquivar a mudança somente após a verificação. Commit e push exigem autorização específica.
