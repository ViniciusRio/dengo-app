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

## Perfil — checkpoint da implementação atual

Status: 🟡 Implementado e validado funcionalmente no Galaxy A26; aprovação visual final pendente.

O responsável abriu e usou o novo Perfil no aparelho. Confirmou a troca entre Lidianne e Vinícius, a abertura de cada Home correspondente, a perspectiva ativa refletida corretamente e a continuidade do fluxo existente. Somente o Perfil mudou visualmente nesta evolução. A validação não cobre individualmente TalkBack, fonte ampliada ou autoria no Mural e não constitui aprovação visual definitiva. Refinamentos de hierarquia, espaçamento e seletores ficam para outra rodada. Ver `docs/features/profile.md` e ADR-016. Este checkpoint não define uma nova etapa de produto.
