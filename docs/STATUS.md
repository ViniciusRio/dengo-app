# Status do projeto

Etapas 1–7 concluídas e aprovadas no dispositivo. A evolução posterior do ciclo afetivo dos pedidos foi revisada e aprovada funcional e visualmente no Samsung Galaxy A26 5G. Consulte o ADR-012 para a decisão vigente. A próxima etapa de produto ainda não foi definida.

A evolução de recados desenhados do Mural foi implementada no protótipo local, passou pela revisão contra a spec e pelos testes automatizados. Após os ajustes do compositor e da paleta, recebeu aprovação humana na validação final no Galaxy A26: abertura no tamanho esperado, rótulos de cor em uma linha, troca Rosa → Azul → Escuro → Rosa sem recuo visual, desenho, cores, Desfazer, Limpar, Cancelar e publicação no Mural. Esse relato não documenta testes individuais de TalkBack, fonte ampliada ou previews nas duas Homes. A aprovação da Etapa 7 continua se referindo à versão textual; a evolução desenhada é posterior e não define uma nova etapa de produto. Consulte o ADR-014.

O Perfil atual é a composição da Opção 1 refinada em B.1, aprovada visualmente pelo responsável no Galaxy A26 nas perspectivas de Lidianne e Vinícius. O painel curvo, a ilustração transparente, o lettering e as duas superfícies pessoais integram a direção vigente; a escolha ativa mantém contorno e “Em uso”. TalkBack e fonte `1.5×` foram exercitados no mesmo aparelho nos cenários descritos em `docs/features/profile.md`, sem auditoria exaustiva de acessibilidade. O checkpoint funcional inicial (ADR-016) e o refinamento 1.1 (ADR-017) permanecem como histórico; a precedência visual atual está no ADR-018.

## Aprovado

- visão do produto;
- Android-first;
- Kotlin + Jetpack Compose + Material 3;
- protótipo local/fake antes de backend;
- usuários iniciais: Lidianne e Vinícius;
- direção visual rosa/azul sobre base creme;
- identidade visual ilustrada de Lidianne, Vinícius e do casal aprovada em prancha própria (ADR-015); composição atual do Perfil B.1 aprovada visualmente no Galaxy A26 no alcance do ADR-018, após os checkpoints históricos dos ADR-016 e ADR-017;
- Home de Lidianne focada em expressão/pedidos;
- Home de Vinícius focada em contexto/resposta;
- Personal Space separado de Care Request.
- Mural de recados da Etapa 7 aprovado visual e funcionalmente no Samsung.
- Evolução posterior de recados desenhados aprovada no Galaxy A26, no alcance da validação relatada acima.
- Ciclo afetivo dos pedidos aprovado funcional e visualmente no Samsung; sem evidência específica de teste com fonte ampliada ou TalkBack.

## Ainda pode mudar após protótipo

- nome definitivo do app;
- textos finais dos CTAs;
- pequenos ajustes de paleta/contraste;
- usos e eventuais recortes dos personagens como assets de runtime;
- arquitetura do backend futuro.
