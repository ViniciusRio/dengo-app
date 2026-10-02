# Screens — Especificação Inicial

Este documento define hierarquia e comportamento. A referência visual aprovada **não é pixel-perfect**.

## 1. Home — Lidianne

### Objetivo

Permitir que Lidianne expresse rapidamente o que sente/precisa e veja feedback recente.

### Hierarquia

1. `GreetingHeader`
   - `Olá, Lidianne`
   - frase curta e acolhedora.
2. `CharacterHero`
   - personagem de Lidianne em destaque.
3. `PrimaryCareAction`
   - CTA sugerido: `Pedir dengo ♡`.
4. seção `Como posso cuidar de você hoje?`
5. grid de pedidos rápidos:
   - Bolsa quente;
   - Remédio;
   - Fique comigo;
   - Outro pedido.
6. ação separada e visualmente mais calma:
   - `Preciso ficar sozinha`.
7. `MoodSelector`
   - mood do dia.
8. `MuralPreview` ou última interação, quando houver.
9. `BottomNavigation`.

### Estados importantes

- pedido enviado;
- pedido pendente;
- pedido aceito;
- parceiro não pode agora;
- espaço pessoal ativo;
- mood selecionado.

## 2. Home — Vinícius

### Objetivo

Dar a Vinícius contexto rápido sobre Lidianne e permitir responder aos pedidos sem transformar a tela em painel de tarefas.

### Hierarquia

1. `GreetingHeader`
   - `Olá, Vinícius`.
2. `CharacterHero`
   - personagem de Lidianne ou composição do casal, conforme o estado.
3. `MoodSummary`
   - exemplo: `🥺 Dengosa hoje`.
4. seção `Pedidos dela`.
5. `IncomingRequestCard` para cada pedido ativo.
   - ação principal: `Estou indo ❤️`;
   - ação secundária: `Não consigo agora`.
6. `PersonalSpaceCard` substitui CTAs insistentes quando espaço pessoal estiver ativo.
7. preview discreto do Mural; na Etapa 7, mostra o recado mais recente e abre a aba Mural.
8. `BottomNavigation`.

Esta hierarquia era uma proposta inicial. A Home do Vinícius aprovada no dispositivo prioriza contexto, pedidos e espaço pessoal. Ela não incentiva contato insistente durante espaço pessoal ativo. O preview visual de recado desenhado está especificado, mas ainda não foi implementado nem validado no Samsung.

## 3. Pedido recebido — detalhe/estado

Conteúdo:
- tipo do pedido;
- quem pediu;
- horário;
- estado atual;
- mensagem opcional;
- ações pertinentes.

Não mostrar ações que não façam sentido para o estado atual.

## 4. Mood

Pode começar como seção da Home, sem tela dedicada. Seleção rápida com emoji + texto.

## 5. Mural

Tela simples:
- conteúdo recente;
- recados curtos de texto e emojis, com nome do autor e horário;
- ação “Deixar um recado” com composição curta em bottom sheet;
- estado vazio afetivo e lista aberta, sem cards pesados.

Na Etapa 7 o Mural é compartilhado apenas pelo estado em memória do protótipo e contém recados textuais. A tela e seus estados foram aprovados no Samsung.

Evolução especificada, ainda não implementada: “Deixar um recado” oferece **Escrever** (composição textual atual) ou **Desenhar** (traços livres feitos com o dedo, sem teclado). A composição manual oferece área confortável, poucas cores, desfazer e limpar; a lista mantém recados individuais com autoria e horário. Se o último recado for desenhado, ambas as Homes mostram o traço real em preview discreto, sem perder a hierarquia aprovada. Proporção da área, cores exatas e peso visual exigem validação no Samsung; ver `docs/features/mural.md`. Sincronização entre dispositivos permanece futura.

## 6. Histórico

Timeline simples, não gamificada:
- data/horário;
- evento;
- estado/resposta relevante.

Sem rankings, métricas de carinho ou cobranças.

## 7. Perfil

No protótipo:
- nome;
- personagem;
- identificação de qual perspectiva está sendo visualizada, se necessário para demo.

Configurações avançadas ficam para depois.

## Navegação

Bottom navigation inicial:
- Início
- Mural
- Histórico
- Perfil

Durante desenvolvimento local, pode existir um mecanismo de debug/preview para alternar entre a perspectiva de Lidianne e Vinícius. Esse mecanismo não faz parte da UX final.
