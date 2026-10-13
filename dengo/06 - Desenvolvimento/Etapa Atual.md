# Etapa Atual

## Etapa 7 — Mural de recados do casal
Status: 🟢 Concluída e aprovada no Samsung

## Escopo implementado
- recados de texto e emojis, com autoria explícita e limite de 160 pontos de código Unicode;
- mesma lista em memória nas duas perspectivas, ordenada por horário e ID decrescentes;
- composição curta em bottom sheet e estado vazio afetivo;
- previews discretos nas duas Homes, abrindo a aba Mural;
- publicação permitida durante espaço pessoal e sem `HistoryEvent`;
- testes unitários, sem persistência ou sincronização.

## Fora do escopo da Etapa 7
Desenhos, fotos, anexos, edição, exclusão, comentários, reações, filtros, busca, novos eventos de Histórico, backend, persistência e sincronização.

## Validação da Etapa 7
Revisão visual e funcional concluída no Samsung: estado vazio, CTA, bottom sheet e teclado, contador e limite, texto e emoji, publicação imediata, lista compartilhada e ordenada, autoria, previews das Homes e navegação para o Mural. A próxima etapa de produto ainda não foi definida e dependerá de novo planejamento.

## Evolução posterior — ciclo afetivo dos pedidos

Status: 🟢 Aprovada funcional e visualmente no Samsung Galaxy A26 5G. O contrato vigente está no ADR-012. Esta evolução não define uma nova etapa de produto; a próxima etapa permanece em aberto.

## Mural — evolução de recados desenhados aprovada no Samsung

Recados desenhados à mão foram implementados como alternativa posterior aos recados textuais da Etapa 7; o contrato de produto está em `docs/features/mural.md`. A implementação passou pela revisão contra a spec e pelos testes automatizados. Após correções do compositor e da paleta, a validação manual final no Galaxy A26 foi aprovada: abertura no tamanho esperado, Rosa/Azul/Escuro em uma linha, troca Rosa → Azul → Escuro → Rosa sem recuo, desenho, cores, Desfazer, Limpar, Cancelar e publicação no Mural. O relato não discrimina testes de TalkBack, fonte ampliada ou previews nas duas Homes. Não foi definida uma nova etapa de produto. O ADR-014 registra as direções técnicas e a aprovação posterior; a aprovação da Etapa 7 continua se referindo à versão textual entregue naquela etapa.

## Perfil — composição Opção 1/B.1

Status: 🟢 B.1 aprovado visualmente e validado nos cenários de acessibilidade exercitados no Galaxy A26; próxima etapa de produto ainda não definida.

O ADR-016 registra o checkpoint funcional inicial e o ADR-017 registra a aprovação visual histórica do Perfil 1.1. Após a comparação com a Opção 1, a Fase B reconstruiu a composição com casal transparente, painel curvo, lettering e duas superfícies pessoais. O B.1 refinou proporção e hierarquia da explicação; o responsável aprovou visualmente os dois estados no A26. No checkpoint B.1 passaram 132 testes unitários, os builds debug e androidTest, `git diff --check` e 7/7 instrumentados no A26. Em validação posterior no aparelho, TalkBack e fonte `1.5×` foram exercitados nos cenários registrados em `docs/features/profile.md`; não houve auditoria exaustiva de acessibilidade nem nova verificação individual da autoria no Mural. O ADR-018 registra a decisão visual vigente. Esta evolução não define uma nova etapa de produto.

## Histórico afetivo — histórias por pedido

Status: 🟢 Composição aprovada visualmente nas perspectivas Lidianne e Vinícius no Galaxy A26; validação final registrada no alcance do ADR-019, pronta para revisão de commit.

Os pedidos são agrupados por `requestId`, com criação, resposta e eventual agradecimento preservados como acontecimentos distintos. Mood e espaço pessoal permanecem independentes. Passaram 67 testes unitários debug, 67 release e 11 instrumentados no A26 (via ADB, após `adb install --no-streaming`). O responsável confirmou TalkBack e fonte aproximadamente `1,5×` como satisfatórios. O relato manual não discrimina todos os cenários individualmente. O Histórico continua somente leitura e em memória; esta evolução não define uma nova etapa de produto.

## Planejamento — persistência local

**Escopo aprovado para especificação:** sobreviver ao encerramento e à reabertura no mesmo aparelho, mantendo todos os dados publicados já existentes: pedidos e estados, eventos factuais do Histórico, mood atual de Lidianne, estado do espaço pessoal e recados publicados do Mural, incluindo texto e desenho completo. Fora do escopo: backend, comunicação entre aparelhos, autenticação, pareamento, sincronização, backup/exportação/recuperação após desinstalação, rascunhos, perspectiva e identidade de sessão. Não há dados demonstrativos de runtime nem banco anterior a migrar.

**Direção técnica proposta, ainda não aprovada nem implementada:** Room como armazenamento local único atrás de `CoupleRepository`, com leitura reativa de snapshots coerentes obtidos transacionalmente, escritas assíncronas e entidades Room ocultas dos ViewModels. Manter IDs de domínio `Long` locais ao banco; armazenar desenhos publicados em formato binário versionado. Distinguir `Loading`, `Ready` e `ReadError`; distinguir escritas confirmadas, revertidas e incertas. Preservar os compositores de `OTHER` e do Mural até confirmação, sem repetição automática de uma operação incerta.

A revisão arquitetural requer consolidar a semântica de falhas/cancelamentos, a adaptação dos quatro ViewModels e callbacks hoje síncronos, e testes de IDs e de snapshot reativo antes de iniciar código. Próximo passo: consolidar a especificação; iniciar o Incremento 1 somente após autorização explícita. Este registro não aprova a arquitetura final nem inicia implementação.
