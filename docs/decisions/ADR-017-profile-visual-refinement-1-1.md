# ADR-017 — Refinamento visual 1.1 do Perfil

## Status

Aceito como checkpoint visual do Perfil 1.1, aprovado pelo responsável no Galaxy A26 em 2026-10-06. Não define a próxima etapa de produto nem um design system global.

## Contexto

O ADR-016 registrou a Direção A — casal em primeiro plano, identidade do casal e escolhas individuais — como checkpoint funcional, ainda sem aprovação visual definitiva. Nas capturas do Galaxy A26, as duas escolhas tinham peso de cards equivalentes. A alternativa A, “Retratos com destaque só na perspectiva ativa”, foi escolhida para refinar a mesma composição.

## Decisão implementada

O hero, os nomes do casal, os avatares, as duas escolhas verticais, “Em uso”, a semântica de radio e a troca imediata de perspectiva permanecem. A escolha ativa usa superfície suave da cor pessoal e contorno discreto; a inativa se integra à base creme, sem preenchimento ou contorno de card equivalente e sem perder contraste de avatar e nome. O título “Usando o Dengo como” e a explicação inferior perderam peso; avatares e altura mínima das escolhas foram reduzidos. A altura continua podendo crescer com o conteúdo e a tela permanece rolável. Não foram alterados assets, navegação, regras de negócio, outras telas ou tokens globais; não foi adicionada animação.

## Evidências e limites

O responsável executou o APK no Galaxy A26, observou o Perfil com Lidianne ativa, trocou para Vinícius e observou o Perfil com Vinícius ativo. Confirmou a seleção visual ativa correta nas duas perspectivas, a opção inativa compreensível como escolha e aprovou visualmente o refinamento 1.1 para este checkpoint. A validação funcional anterior permanece registrada no ADR-016; o relato desta rodada não acrescenta teste manual individual da Home ou da autoria no Mural.

Os resultados automatizados são separados dessa aprovação humana: `./gradlew test assembleDebug assembleDebugAndroidTest` passou, com 132 testes unitários nas variantes debug e release, sem falhas; a suíte instrumentada executada no Galaxy A26 passou com 4 de 4 testes. Esses testes verificam apenas os fluxos cobertos, sem constituir revisão visual ou teste de acessibilidade completo. Não há evidência específica nesta rodada de TalkBack, fonte ampliada ou acessibilidade completa. Contraste e rolagem também não foram relatados como cenários individuais de validação manual.

## Hipótese visual

O refinamento sugere que hero ilustrado, avatares reconhecíveis, rosa/azul como acentos pessoais, base creme e uma única superfície de ênfase podem dar continuidade à linguagem afetiva com menos contornos e ornamento. Esta é uma hipótese de linguagem reutilizável para investigação futura, não um token ou padrão global aprovado e não autoriza aplicação em outras telas.

Este ADR registra o estado posterior ao checkpoint do ADR-016 e ao planejamento descrito no ADR-015, sem alterar o alcance das decisões históricas.
