# Feature — Perfil

## Estado atual do protótipo

A aba Perfil preserva a troca local da perspectiva de Lidianne ou Vinícius. A escolha retorna imediatamente à Home correspondente; não há autenticação nem identidade de sessão persistida. A perspectiva também determina a autoria no Mural. Ver ADR-009.

## Experimento visual implementado — validação funcional realizada

O Perfil foi reorganizado como primeiro experimento controlado de linguagem visual própria do Dengo: título, hero do casal, nomes do casal, seção “Usando o Dengo como”, duas escolhas verticais com avatares individuais e “Em uso” na ativa, seguidas de explicação secundária. O hero já utilizado na Home de Lidianne foi reaproveitado. Os avatares foram recortados tecnicamente da [prancha aprovada de personagens](../design/REFERENCES.md), sem redesenho. A composição do mockup da Opção 1 orientou a hierarquia, sem fixar medidas, textos ou detalhes decorativos.

O experimento mantém base creme, acentos rosa/azul, texto e semântica de seleção. A tela rola quando necessário. Não há animação própria nesta versão; selecionar uma pessoa continua abrindo sua Home sem atraso. Não houve mudança nos models, na navegação compartilhada ou no armazenamento em memória.

**Validação humana no Galaxy A26:** o responsável abriu e usou a nova tela; confirmou a troca entre Lidianne e Vinícius, o retorno à Home correspondente, a perspectiva ativa refletida corretamente e a continuidade do fluxo existente do app. Relatou que somente o Perfil foi alterado visualmente nesta evolução. Isso valida o funcionamento observado, mas **não aprova definitivamente a aparência do Perfil**. Hierarquia, espaçamento e aparência dos seletores podem ser refinados em rodada separada.

Os testes instrumentados foram compilados, mas não executados na rodada de implementação: não havia dispositivo conectado e o emulador local encerrou durante o boot. O relato humano não documenta teste individual de autoria no Mural, TalkBack, fonte ampliada, contraste ou rolagem. A aprovação visual final e esses cenários continuam sem evidência específica. Ver ADR-016.

## Possíveis itens futuros

- nome/apelido;
- avatar/personagem;
- pedidos rápidos favoritos;
- preferências de notificação;
- vínculo com parceiro.

Esses itens futuros não foram aprovados como funcionalidades. Também não foi decidido usar os avatares em Home, Mural ou Histórico.
