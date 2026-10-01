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
7. seção de sugestões discretas:
   - desenhar no mural;
   - mandar recado;
   - oferecer companhia quando apropriado.
8. última interação/mural.
9. `BottomNavigation`.

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
- canvas/área de desenho local;
- recado curto;
- salvar localmente.

Sincronização remota fica fora do protótipo inicial.

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
