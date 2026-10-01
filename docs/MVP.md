# MVP e Roadmap

## Objetivo do MVP

Validar se o ciclo `comunicar → receber → responder → registrar` é divertido e útil para Lidianne e Vinícius.

## MVP 1 — protótipo local

1. Home personalizada de Lidianne.
2. Home personalizada de Vinícius.
3. Pedidos rápidos de cuidado.
4. Visualização de pedidos recebidos.
5. Resposta simples ao pedido.
6. Mood do dia.
7. Estado de espaço pessoal.
8. Histórico básico.
9. Mural representado visualmente; interação local simples pode ser adicionada após as Homes.
10. Dados fake/local durante a validação visual e de fluxo.

### Estados atuais de um pedido

- `PENDING` — solicitado, sem resposta.
- `ACCEPTED` — Vinícius respondeu “Estou indo ❤️”; não significa conclusão.
- `DECLINED` — Vinícius respondeu “Não consigo agora”.
- `COMPLETED` — representável no domínio, mas sem ação aprovada.

Visualizar não altera o pedido; não existe `SEEN` no protótipo atual.

## MVP 2 — dois dispositivos

- Pareamento/autenticação real.
- Sincronização remota.
- Notificações.
- Mural compartilhado real.
- Desenhos/recados sincronizados.

## Futuro / experimental

- Personagens reagindo ao mood.
- Mais expressões dos personagens.
- Pequenas animações.
- Memórias/momentos especiais.
- Personalização de pedidos rápidos.

## Regra de escopo

Uma funcionalidade nova só entra no MVP 1 se for necessária para validar o ciclo principal ou a linguagem visual aprovada.
