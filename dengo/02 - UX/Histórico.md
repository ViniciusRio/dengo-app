# Histórico
Status: 🟢 Implementado e aprovado no dispositivo

## Objetivo
Rever acontecimentos relevantes como memória leve do cuidado e da comunicação, sem auditoria, métricas ou cobrança.

## Primeira versão
- mesma timeline, somente leitura, nas perspectivas de Lidianne e Vinícius;
- nomes fixos para as duas pessoas;
- pedidos criados, aceitos e recusados; cada alteração de mood; espaço pessoal iniciado e encerrado;
- pedidos relacionados são identificados pelo ID para apresentar seu tipo e o texto de Outro pedido; se não forem encontrados, o acontecimento permanece com texto humano genérico;
- mais recentes primeiro; empate de horário decidido pela ordem de inclusão do evento;
- agrupamento no fuso local por Hoje, Ontem ou data, com horário em cada item;
- composição vertical aberta, sem card para cada acontecimento;
- estado vazio: “Nossos momentos vão aparecer por aqui.”

O estado permanece apenas em memória. Não há `SEEN`, conclusão de pedidos nem persistência nesta etapa. A timeline preenchida e a apresentação compartilhada foram validadas no Samsung; o estado vazio foi aprovado após reduzir o espaço antes do coração.
