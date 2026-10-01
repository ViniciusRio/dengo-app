# Home Vinícius
Status: 🟢 Implementada e aprovada no dispositivo

## Objetivo
Mostrar o que [[Lidianne]] comunicou e fornecer contexto para [[Vinícius]] responder.

> Como posso cuidar dela agora?

## Responsabilidades
- compreender como Lidianne está;
- visualizar [[Espaço Pessoal]];
- receber [[Pedidos]];
- responder aos pedidos.

## Hierarquia proposta
1. “Olá, Vinícius”
2. identidade compacta com acento azul
3. estado atual de Lidianne
4. “Pedidos dela”
5. preview discreto do [[Mural]]

Não repetir o hero grande da [[Home Lidianne]].

## Respostas
- “Estou indo ❤️”: `PENDING → ACCEPTED`; não significa conclusão.
- “Não consigo agora”: `PENDING → DECLINED`; sem linguagem punitiva.

## Regras aprovadas
- visualizar não muda status;
- sem `SEEN`;
- `COMPLETED` sem ação nesta etapa;
- pedidos recentes primeiro;
- pendentes têm prioridade;
- espaço pessoal ativo ganha precedência;
- mood antigo recebe contexto temporal;
- duas Homes observam a mesma instância do repository;
- alternância local Lidianne ↔ Vinícius, sem autenticação.

## Implementação atual
- alternância discreta em Perfil; a escolha retorna à Home correspondente;
- estado de espaço pessoal aparece antes do mood e suspende as respostas aos pedidos;
- pedidos pendentes mostram duas respostas; pedidos respondidos continuam visíveis em tom secundário;
- mood antigo aparece como “Último mood” acompanhado da data;
- Mural continua como preview vazio.

A composição visual e os fluxos de pedidos, mood, espaço pessoal e alternância de perspectiva foram aprovados no Samsung. Dois pedidos com respostas distintas permaneceram visíveis com seus estados corretos.

## Princípio
Não parecer dashboard, Jira ou sistema de tickets.
