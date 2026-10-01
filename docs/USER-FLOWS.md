# User Flows

## Fluxo 1 — Lidianne faz um pedido de cuidado

Lidianne abre Home → escolhe pedido rápido ou CTA principal → pedido fica `PENDING` → Vinícius visualiza sem mudar o status → responde com `ACCEPTED` ou `DECLINED` → Lidianne recebe feedback → criação e resposta aparecem no Histórico. Conclusão de pedido ainda não tem ação aprovada.

## Fluxo 2 — Vinícius responde a um pedido

Vinícius abre Home → vê pedido, horário e contexto → escolhe `Estou indo ❤️`/equivalente ou `Não consigo agora` → resposta é registrada → Lidianne recebe retorno.

O pedido não deve desaparecer imediatamente; o estado deve continuar compreensível.

## Fluxo 3 — Mood do dia

Lidianne abre Home → seleciona mood → mood atual fica visível → Vinícius vê o mood → app pode sugerir pequenas ações adequadas, sem assumir que são necessárias.

### Mood inicial

- 😞 Chateada
- 😠 Brava
- 😴 Cansada
- 😐 Normal
- 😊 Feliz
- 🥰 Amorosa
- 🥺 Dengosa
- 🤩 Animada

## Fluxo 4 — Espaço pessoal

Lidianne ativa `Preciso ficar sozinha` → Vinícius vê um estado calmo e não invasivo → interface evita CTAs insistentes → pode existir `Mandar carinho e deixar espaço` → Lidianne encerra/muda o estado quando desejar.

No protótipo local, não definir expiração automática; o estado permanece até mudança explícita.

## Fluxo 5 — Histórico

Usuário abre Histórico → vê os acontecimentos compartilhados do mais recente ao mais antigo, agrupados por dia → identifica pedidos, respostas, mudanças de mood e períodos de espaço pessoal.

Exemplo:
- 18:42 — Lidianne compartilhou como estava: 😴 Cansada.
- 14:34 — Vinícius respondeu `Estou indo ❤️`.
- 14:32 — Lidianne pediu dengo.

## Fluxo 6 — Mural local

Usuário abre Mural → cria desenho/recado local → salva → item aparece no mural/preview da Home.

Sincronização entre dispositivos pertence ao MVP 2.
