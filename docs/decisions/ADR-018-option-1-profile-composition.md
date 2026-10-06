# ADR-018 — Composição visual da Opção 1 para o Perfil

## Status

Aceito como direção visual vigente do Perfil B.1, aprovado pelo responsável no Galaxy A26 em 2026-10-06. Não define um design system global nem a próxima etapa de produto.

## Contexto

O ADR-016 registrou o primeiro experimento funcional com o casal em destaque. O ADR-017 registrou o refinamento 1.1, aprovado visualmente no seu checkpoint: somente a perspectiva ativa tinha superfície de destaque. Ao comparar o 1.1 com o mockup da Opção 1, o responsável concluiu que a arquitetura de informação estava próxima, mas a composição ainda parecia uma sequência de imagem, textos e seletores independentes. Uma tentativa posterior usando apenas Compose e o hero composto existente melhorou o encontro estrutural, mas não recuperou a personalidade visual desejada. O problema observado era de composição e adequação dos assets, não uma limitação inerente do Material 3 ou do Jetpack Compose.

## Decisão

Adotar para o Perfil uma reconstrução visual inspirada na **Opção 1**, adaptada ao app Android real. Um recorte transparente do casal, criado conforme a identidade aprovada no ADR-015, ocupa a abertura sobre formas afetivas e corações desenhados em Compose. Um painel creme de topo curvo se sobrepõe à base da ilustração e reúne lettering próprio “Lidianne e Vinícius”, subtítulo vigente, seção “Usando o Dengo como”, duas escolhas e informação auxiliar. Casal, painel e conteúdo devem ser percebidos como uma composição única.

As duas escolhas usam superfícies suaves pessoais, rosa e azul. A perspectiva ativa recebe contorno mais forte e “Em uso”; a inativa permanece colorida e acionável. Esta decisão **supera visualmente**, para o Perfil atual, a regra do ADR-017 de deixar somente a escolha ativa com superfície. Preserva seleção única, semântica de radio button, troca imediata para a Home correspondente, autoria ligada à perspectiva, navegação inferior real e copy vigente. O mockup orienta composição e hierarquia; não impõe cópia pixel a pixel, seta de retorno, textos históricos ou controles ilustrados.

Compose monta as camadas, desenha fundo e corações, define o painel e mantém texto e seletores funcionais. Os PNGs específicos do Perfil fornecem a ilustração recortada e o lettering; os avatares existentes continuam nas escolhas. O lettering tem alternativa textual/semântica e fallback em largura insuficiente ou fonte ampliada. A tela permanece rolável, respeita os insets do `AppScaffold` e permite crescimento dos controles pelo conteúdo. O B.1 reduziu moderadamente a ocupação vertical da abertura e dos seletores e deixou a explicação diretamente no painel, sem card rosa concorrente.

Esta decisão é específica do Perfil. Não modifica a Home, o Mural, o Histórico, tokens globais, regras de negócio, persistência ou navegação; não cria animação nem aprova uso dos novos assets em outras telas.

## Evidências

A Fase B foi registrada em `e1ca72d`; o refinamento B.1 está em `7cb05ec`. No checkpoint B.1, 132 testes unitários, `assembleDebug`, `assembleDebugAndroidTest`, `git diff --check` e 7/7 testes instrumentados no Galaxy A26 passaram. Os instrumentados rodaram diretamente pelo `AndroidJUnitRunner` após a instalação via Gradle expirar; os testes não falharam.

O responsável revisou no Galaxy A26 as duas perspectivas, a composição geral, o protagonismo do casal, lettering, painel, seletores, hierarquia da explicação e rolagem. Aprovou visualmente B.1 em comparação à Fase B e não solicitou uma rodada B.2.

Em validação posterior no mesmo aparelho, o TalkBack foi exercitado com confirmação humana da leitura e navegação funcionais, seleção e anúncio do estado ativo, ativação de Vinícius com abertura da Home correspondente e retorno para Lidianne. Elementos decorativos não causaram problema observado. Não foi registrada transcrição literal de todas as falas. Com fonte em `1.5×`, os dois estados foram observados: fallback textual do lettering, conteúdo legível, escolhas acomodadas, explicação alcançável com rolagem e bottom navigation funcional, sem clipping ou sobreposição relevante observados. As configurações temporárias foram restauradas.

## Alcance e relação com decisões anteriores

ADR-015 continua definindo a identidade dos personagens. ADR-016 e ADR-017 continuam documentando fielmente seus checkpoints e respectivas limitações; não descrevem a aparência vigente após B.1. Este ADR prevalece apenas onde a composição visual atual do Perfil diverge deles.

A validação cobre os cenários exercitados no Galaxy A26. Não comprova conformidade WCAG completa, auditoria exaustiva de acessibilidade, comportamento em múltiplos aparelhos ou todas as escalas de fonte, nem verificação linguística palavra por palavra do TalkBack. Não há nova validação individual de autoria no Mural nesta etapa.
