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

`PENDING → ACCEPTED` por “Estou indo ❤️” ou `PENDING → DECLINED` por “Não consigo agora”. Visualizar não altera o status; não existe `SEEN`. `COMPLETED` está modelado, mas a ação de conclusão ainda não foi definida.

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
