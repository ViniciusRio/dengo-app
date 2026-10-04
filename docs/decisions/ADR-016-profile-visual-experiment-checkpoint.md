# ADR-016 — Checkpoint funcional do experimento visual do Perfil

## Status

Aceito como checkpoint da implementação atual, com funcionamento validado no Galaxy A26. A aprovação visual definitiva do Perfil permanece pendente.

## Contexto

O Perfil servia como seletor local de perspectiva. Após a aprovação da identidade dos personagens no ADR-015, foi escolhida para o experimento a composição com o casal em primeiro plano e as escolhas individuais abaixo. O mockup da Opção 1 orientou a hierarquia, sem ser uma especificação pixel-perfect.

## Decisão implementada

A tela mantém o título Perfil, destaca o hero do casal já usado na Home de Lidianne, identifica Lidianne e Vinícius e apresenta duas escolhas verticais com avatares recortados da prancha aprovada. “Em uso” indica textualmente a perspectiva ativa, além dos sinais de cor e contorno. A explicação sobre a abertura da Home neste aparelho é secundária.

A seleção continua local e abre imediatamente a Home escolhida. Não houve mudança no modelo `PartnerId`, no estado em memória nem na ligação existente entre perspectiva e autoria no Mural. A implementação usa Compose e os tokens atuais, sem animação nova, conta ou autenticação. Os avatares foram preparados por recorte da arte aprovada, sem redesenho. Este uso no Perfil não define uso dos avatares nas outras telas.

## Validação e limite da evidência

No Galaxy A26, o responsável abriu e usou o novo Perfil e confirmou a troca entre Lidianne e Vinícius, a abertura da Home correspondente, a perspectiva ativa refletida corretamente e a continuidade do fluxo existente. Relatou que somente o Perfil mudou visualmente nesta evolução. Essa é uma validação funcional da implementação atual, **não uma aprovação visual definitiva**.

Os testes unitários e o build foram executados antes deste checkpoint. Os testes instrumentados novos compilaram, mas não rodaram na rodada de implementação por ausência de dispositivo conectado e falha de boot do emulador local. O relato do Galaxy A26 não especifica teste de TalkBack, fonte ampliada, contraste, rolagem ou autoria de um recado no Mural. Esses cenários não devem ser registrados como validados individualmente.

## Próximos passos fora deste checkpoint

Possíveis refinamentos de hierarquia, espaçamento e aparência dos seletores serão avaliados em rodada visual separada. Este ADR não fecha a aprovação visual nem define a próxima etapa de produto.
