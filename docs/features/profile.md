# Feature — Perfil

## Estado atual do protótipo

A aba Perfil preserva a troca local da perspectiva de Lidianne ou Vinícius. A escolha retorna imediatamente à Home correspondente; não há autenticação nem identidade de sessão persistida. A perspectiva também determina a autoria no Mural. Ver ADR-009.

## Experimento visual — refinamento 1.1 aprovado no aparelho

O Perfil foi reorganizado como primeiro experimento controlado de linguagem visual própria do Dengo: título, hero do casal, nomes do casal, seção “Usando o Dengo como”, duas escolhas verticais com avatares individuais e “Em uso” na ativa, seguidas de explicação secundária. O hero já utilizado na Home de Lidianne foi reaproveitado. Os avatares foram recortados tecnicamente da [prancha aprovada de personagens](../design/REFERENCES.md), sem redesenho. A composição do mockup da Opção 1 orientou a hierarquia, sem fixar medidas, textos ou detalhes decorativos.

O experimento mantém base creme, acentos rosa/azul, texto e semântica de seleção. A tela rola quando necessário. Não há animação própria nesta versão; selecionar uma pessoa continua abrindo sua Home sem atraso. Não houve mudança nos models, na navegação compartilhada ou no armazenamento em memória.

No refinamento 1.1 foi escolhida a alternativa A, “Retratos com destaque só na perspectiva ativa”. A escolha ativa usa superfície suave da cor pessoal, contorno discreto e “Em uso”; a inativa fica integrada à base creme, sem um card equivalente e com avatar e nome legíveis. O título da seção, os controles e a explicação inferior ficaram menos dominantes. A altura das escolhas é mínima, não fixa, para permitir crescimento do conteúdo. Hero, identidade do casal, avatares, semântica de radio e troca imediata foram preservados. Ver ADR-017.

**Validação humana:** no checkpoint anterior, o responsável confirmou no Galaxy A26 a troca de perspectiva, a abertura das Homes correspondentes, a perspectiva ativa e a continuidade do fluxo existente; ver ADR-016. Nesta rodada, executou o APK no mesmo aparelho, observou o Perfil com Lidianne ativa, trocou para Vinícius, observou o Perfil com Vinícius ativo, confirmou a seleção visual correta nas duas perspectivas e a opção inativa compreensível como escolha. Aprovou visualmente o refinamento 1.1 para este checkpoint.

**Limites:** os testes automatizados desta rodada são evidência separada da revisão humana. TalkBack, fonte ampliada e acessibilidade completa não foram validados individualmente. O relato desta rodada também não documenta verificação manual específica de autoria no Mural, contraste ou rolagem. A aprovação do Perfil 1.1 não decide o uso dos avatares em outras telas nem torna a hipótese visual um design system global.

## Possíveis itens futuros

- nome/apelido;
- avatar/personagem;
- pedidos rápidos favoritos;
- preferências de notificação;
- vínculo com parceiro.

Esses itens futuros não foram aprovados como funcionalidades. Também não foi decidido usar os avatares em Home, Mural ou Histórico.
