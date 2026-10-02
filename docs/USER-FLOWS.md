# User Flows

## Fluxo 1 — Lidianne faz um pedido de cuidado

Lidianne abre Home → escolhe pedido rápido ou CTA principal → pedido fica `PENDING` → Vinícius visualiza sem mudar o status → responde com `ACCEPTED` ou `DECLINED` → Lidianne recebe feedback. Se o carinho aceito acontecer, Lidianne toca em “Obrigada, meu amor ❤️” e o pedido vai para `ACKNOWLEDGED`. Criação, resposta e agradecimento aparecem separadamente no Histórico. Esta evolução foi aprovada no Samsung; ver ADR-012.

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

Fluxo implementado e aprovado no Samsung: Lidianne ou Vinícius abre Mural → toca em “Deixar um recado” → escreve até 160 pontos de código Unicode → publica → o recado aparece no topo, com nome e horário, para as duas perspectivas e nos previews das Homes. Espaço pessoal ativo não bloqueia este gesto assíncrono; publicar não gera evento de Histórico.

Evolução especificada, ainda não implementada: sob “Deixar um recado”, escolher **Escrever** mantém esse fluxo; escolher **Desenhar** abre a composição manual sem teclado. O desenho publicado entra na mesma lista e, quando for o último recado, aparece visualmente nas duas Homes. Ver `docs/features/mural.md` para o fluxo completo, proteção do rascunho e critérios de aceitação.

Sincronização entre dispositivos pertence ao MVP 2.
