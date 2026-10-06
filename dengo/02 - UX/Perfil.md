# Perfil
Status: 🟢 Composição Opção 1/B.1 aprovada visualmente e validada nos cenários de acessibilidade exercitados no Galaxy A26; ver ADR-018

O Perfil mantém a escolha local de perspectiva, a Home correspondente após cada toque e a relação existente com a autoria no Mural. A composição vigente integra casal transparente, fundo afetivo, painel creme curvo e lettering “Lidianne e Vinícius”. A copy continua “Um espaço para cuidar um do outro.” Duas superfícies suaves, rosa e azul, apresentam os avatares; só a ativa tem contorno reforçado e “Em uso”. A explicação inferior é auxiliar, e a bottom navigation permanece. O responsável aprovou visualmente B.1 no A26 com Lidianne e Vinícius ativos e confirmou o ritmo visual após o refinamento da Fase B.

TalkBack foi exercitado no A26 com confirmação humana de leitura, estados de seleção e troca entre as duas perspectivas. Com fonte `1.5×`, os dois estados, fallback textual do lettering, rolagem e acesso à explicação foram observados sem clipping ou sobreposição relevante. Não há transcrição literal de todos os anúncios, teste em outros aparelhos ou auditoria exaustiva de acessibilidade; ver `docs/features/profile.md`.

O checkpoint funcional inicial está no ADR-016 e o Perfil 1.1, aprovado no seu momento, está no ADR-017. A comparação posterior com a Opção 1 mostrou fidelidade composicional insuficiente no 1.1; o ADR-018 registra a direção visual atual. Não há decisão de uso transversal dos avatares ou novos assets, autenticação ou configurações.
