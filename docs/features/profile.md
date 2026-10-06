# Feature — Perfil

## Estado atual do protótipo — composição Opção 1, B.1

A aba Perfil permite escolher localmente a perspectiva de Lidianne ou Vinícius. A escolha abre imediatamente a Home correspondente; não há autenticação nem identidade de sessão persistida. A perspectiva também determina a autoria no Mural. Essa regra funcional não mudou na reconstrução visual; ver ADR-009 e [ADR-018](../decisions/ADR-018-option-1-profile-composition.md).

A abertura reúne título compacto “Perfil”, formas afetivas rosa/azul e corações desenhados em Compose, ilustração transparente do casal e painel creme curvo que encontra a base da arte. O lettering “Lidianne e Vinícius” inicia o conteúdo do painel, seguido da copy vigente “Um espaço para cuidar um do outro.” e da seção “Usando o Dengo como”. A Opção 1 do mockup orientou a composição, sem tornar seus textos, ícones ou medidas uma especificação literal. A ilustração e o lettering específicos do Perfil convivem com os avatares derivados da prancha aprovada; o hero anterior permanece na Home de Lidianne.

As duas escolhas verticais têm superfícies pessoais suaves, rosa para Lidianne e azul para Vinícius. Somente a ativa recebe contorno reforçado e a pill textual “Em uso”; ambas são acionáveis como um único alvo cada e mantêm semântica de radio button e seleção única. A explicação “Escolher uma pessoa abre a Home dela neste aparelho.” aparece como informação secundária, com ícone informativo, sem uma terceira superfície concorrente. A bottom navigation real permanece. A tela rola; controles crescem com o conteúdo, e o lettering usa texto Compose quando a largura é insuficiente ou a fonte está ampliada. Não há animação própria, mudança de regras de negócio ou alteração das demais telas.

O B.1 refinou a proporção da abertura e dos seletores e retirou a superfície rosa da explicação, preservando a composição aprovada na Fase B. A implementação atual usa Compose e os assets `dengo_profile_couple_cutout.png` e `dengo_profile_couple_lettering.png` em `app/src/main/res/drawable-nodpi/`, além dos avatares existentes. O uso desses elementos em outras telas não foi aprovado.

## Evidências e alcance da aprovação

**Automatizado, no checkpoint B.1:** 132 testes unitários passaram; `assembleDebug`, `assembleDebugAndroidTest` e `git diff --check` passaram; 7/7 testes instrumentados passaram no Galaxy A26 pelo `AndroidJUnitRunner`. A instalação pela tarefa Gradle expirou antes da execução direta pelo runner; isso não foi falha dos testes.

**Revisão humana visual:** o responsável observou no Galaxy A26 Lidianne ativa e Vinícius ativo, comparou B.1 com a Fase B e aprovou a direção da Opção 1, o protagonismo do casal, o encontro com o painel, o lettering, as duas superfícies pessoais, os estados ativos e a hierarquia mais leve da explicação. A rolagem foi observada. Esta é aprovação do Perfil B.1, sem declarar que o mockup inteiro seja requisito ou que a tela seja definitiva para toda evolução futura.

**Acessibilidade no Galaxy A26:** com TalkBack ativo, houve confirmação humana de leitura e navegação funcionais, escolhas alcançáveis, anúncio do estado selecionado e troca para Vinícius e de volta para Lidianne com abertura das Homes correspondentes; a decoração não produziu problema observado. Não há transcrição literal de todos os anúncios. Com `font_scale = 1.5`, foram observados os dois estados: fallback textual do lettering, legibilidade dos textos e de “Em uso”, crescimento das escolhas, explicação alcançável por rolagem e bottom navigation funcional, sem clipping ou sobreposição relevante observados. Essas verificações não constituem conformidade WCAG, auditoria exaustiva, teste em múltiplos aparelhos ou em todas as escalas de fonte.

## Histórico e limites

O checkpoint funcional inicial está no ADR-016. O refinamento 1.1 em `f327d5f` foi aprovado visualmente no alcance do ADR-017: só a escolha ativa tinha superfície, enquanto a inativa se integrava à base creme. A comparação humana posterior com a Opção 1 mostrou que essa composição ainda estava visualmente distante da referência. A Fase B reconstruiu a abertura em `e1ca72d`; o B.1 aprovado e validado está em `7cb05ec`. A decisão visual vigente do Perfil é o ADR-018. ADR-016 e ADR-017 permanecem válidos como registros dos checkpoints anteriores.

A validação de acessibilidade descrita acima se limita aos cenários exercitados no Galaxy A26. Não há relato nesta etapa de nova verificação manual específica da autoria no Mural, de rotação ou de outras escalas de fonte. A aprovação do Perfil não autoriza aplicação transversal dos avatares, do lettering ou da composição em Home, Mural ou Histórico.

## Possíveis itens futuros

- nome/apelido;
- avatar/personagem;
- pedidos rápidos favoritos;
- preferências de notificação;
- vínculo com parceiro.

Esses itens futuros não foram aprovados como funcionalidades.
