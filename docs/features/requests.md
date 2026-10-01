# Feature — Care Requests

## Objetivo

Permitir comunicar rapidamente uma necessidade de cuidado e receber uma resposta clara do parceiro.

## Tipos iniciais

- Bolsa quente/térmica.
- Remédio.
- Dengo/carinho.
- Passar tempo junto.
- Outro pedido com texto.

A lista deve ser pequena no MVP e poderá ser personalizável futuramente.

## Estados

`PENDING → ACCEPTED` por “Estou indo ❤️” ou `PENDING → DECLINED` por “Não consigo agora”. Se o carinho aceito acontecer, somente Lidianne pode agradecer: `ACCEPTED → ACKNOWLEDGED`. `ACCEPTED` não significa conclusão e permanece aberto sem prazo, mesmo se o carinho não acontecer. Visualizar não altera o status; não existe `SEEN`. O domínio em memória usa `ACKNOWLEDGED` no lugar do antigo `COMPLETED` sem migração persistida.

Cada tipo predefinido admite no máximo um pedido `PENDING` ou `ACCEPTED`. `DECLINED` e `ACKNOWLEDGED` liberam novo pedido do mesmo tipo imediatamente. `OTHER` é sempre independente, inclusive quando o texto é igual. A proteção, as transições e os acontecimentos de Histórico são atômicos no repository. Esta evolução foi aprovada no Samsung; ver ADR-012.

## Ações de quem recebe

Exemplos:
- `Estou indo ❤️`
- `Não consigo agora`

O texto final deve manter linguagem natural do casal.

## Requisitos de UX

- Criar pedido em poucos toques.
- Confirmar visualmente que foi enviado.
- Mostrar claramente a resposta do parceiro, quando houver.
- Não apagar o contexto imediatamente após resposta.
- Registrar eventos relevantes no histórico.
