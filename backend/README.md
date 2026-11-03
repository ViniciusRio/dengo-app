# Backend do Dengo

Fonte da verdade dos dados do casal (ADR-025). A spec é `openspec/changes/sync-couple-between-devices/`. Este módulo tem build próprio, separado do app Android e de `labs/backend/`.

A etapa 1 roda só localmente, com dados sintéticos. Implantação, HTTPS, backup, notificações e Mural são etapas seguintes.

## Requisitos

Java 21 e Docker em execução. Os testes sobem um PostgreSQL descartável com Testcontainers.

## Comandos

Da raiz do repositório:

```bash
./gradlew -p backend test -q                # testes
./gradlew -p backend test --tests "*Sync*"  # teste focado
./gradlew -p backend bootJar                # gera build/libs/dengo-backend-0.1.0.jar
```

O servidor usa as propriedades padrão do Spring para o banco (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) e aplica as migrações ao subir.

## Comandos administrativos

Não têm porta HTTP. Rodam no servidor, com as mesmas variáveis de banco:

```bash
java -jar dengo-backend-0.1.0.jar admin pairing-code LIDIANNE   # imprime um código válido por 15 minutos
java -jar dengo-backend-0.1.0.jar admin revoke LIDIANNE         # revoga a sessão ativa da pessoa
java -jar dengo-backend-0.1.0.jar admin invalidate-all          # invalida todas as sessões e códigos
```

Gerar um código invalida o anterior ainda não usado da mesma pessoa. `invalidate-all` é o passo de invalidação da restauração de backup, antes de aceitar tráfego.

## Erros da API

Toda recusa traz `{"code": "..."}`. Só `SESSION_REVOKED` autoriza o app a apagar a cópia local; `AUTH_REQUIRED` indica requisição sem credencial e não apaga nada.

| Status | Código | Quando |
|---|---|---|
| 400 | `MALFORMED_BODY`, `MALFORMED_REQUEST` | Corpo ou parâmetro inválido; não fica associado à chave |
| 400 | `IDEMPOTENCY_KEY_REQUIRED`, `IDEMPOTENCY_KEY_INVALID` | Chave ausente ou fora de `[A-Za-z0-9_-]{1,128}` |
| 400 | `PAIRING_CODE_INVALID` | Código errado, usado ou expirado |
| 401 | `AUTH_REQUIRED`, `SESSION_REVOKED` | Sem credencial; sessão revogada ou desconhecida |
| 403 | `NOT_ALLOWED_FOR_PERSON` | A ação não é permitida à pessoa da sessão |
| 404 | `REQUEST_NOT_FOUND` | Pedido inexistente |
| 409 | `INVALID_TRANSITION`, `PERSONAL_SPACE_ACTIVE` | Regra de pedido |
| 409 | `IDEMPOTENCY_KEY_REUSED`, `OPERATION_CANCELLED` | Chave com outro conteúdo; chave já descartada |
| 422 | `INVALID_REQUEST` | Texto incompatível com o tipo do pedido |
| 429 | `PAIRING_LOCKED` | Vínculo bloqueado temporariamente |

## Escrita idempotente

Cada comando envia `Idempotency-Key`. A chave pertence à pessoa da sessão, não expira e guarda o resultado na mesma transação da escrita, inclusive uma recusa de regra: repetir a chave devolve sempre o resultado original.

`DELETE /v1/operations/{chave}` responde `{"outcome", "revision"}`:

- `CANCELLED`: nada foi gravado, e um envio atrasado com essa chave recebe `OPERATION_CANCELLED`.
- `CONFIRMED`: a escrita já existia; o registro local só pode sair quando a cópia alcançar `revision`.
- `REFUSED`: o comando original foi recusado por uma regra, e nada foi gravado.

## Sincronização

`GET /v1/sync?after=<revisão>&limit=<n>` corta as páginas só entre revisões. A página traz também os pedidos citados pelos acontecimentos dela, que podem ter `revision` à frente do cursor e voltar em uma página seguinte: a cópia regrava pelo `id` e usa como cursor sempre o `revision` da página, nunca o de uma linha.
