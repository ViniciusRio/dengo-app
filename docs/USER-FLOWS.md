# User Flows

## Fluxo 1 — Lidianne faz um pedido de cuidado

Lidianne abre Home → escolhe pedido rápido ou CTA principal → revisa/confirma quando necessário → pedido vira `REQUESTED` → Vinícius visualiza → pedido vira `SEEN` → Vinícius responde → Lidianne recebe feedback → pedido pode ser marcado `COMPLETED` → evento relevante aparece no histórico.

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

Usuário abre Histórico → vê eventos relevantes em ordem cronológica → identifica pedido, resposta, conclusão e data/hora.

Exemplo:
- 14:32 — Lidianne pediu dengo.
- 14:34 — Vinícius respondeu `Estou indo ❤️`.
- 15:47 — Pedido concluído.

## Fluxo 6 — Mural local

Usuário abre Mural → cria desenho/recado local → salva → item aparece no mural/preview da Home.

Sincronização entre dispositivos pertence ao MVP 2.
