# Status do projeto

Fonte única do estado atual. Atualize ao parar um agente e mantenha em ~15 linhas: evidências ficam nos [ADRs](decisions/README.md), nas [features](features/) e no Git.

**Feito:** Etapas 1–7 e as evoluções de pedidos (ADR-012), recados desenhados (ADR-014), Perfil B.1 (ADR-018) e Histórico afetivo (ADR-019), aprovadas no Galaxy A26. Dados publicados persistem com Room no mesmo aparelho (ADR-020, ADR-021).

**Laboratórios técnicos, sem ligação com o app:** escrita com resultado incerto (OpenSpec arquivado), backend Spring Boot (ADR-022) e RabbitMQ (ADR-023). Nenhum aprova backend de produto.

**Alcance das aprovações:** cada aprovação humana vale só para o que o ADR ou a feature descreve. TalkBack e fonte ampliada não foram validados em todas as telas.

**Próximo:** dois aparelhos com backend próprio, escolhidos em 2026-11-03 (ADR-025). A spec `sync-couple-between-devices` foi aprovada. A etapa 1, `backend/`, foi integrada à `main` pelo PR #7, testada só localmente com dados sintéticos e sem ligação com o app. A VPS foi adiada: a ordem agora é identidade no app, sincronização, Mural, notificações, implantação e fechamento. Próxima etapa: `feat/app-device-identity`.

**Bloqueios:** nenhum.

**Não mexer sem decisão registrada:** backend de produto, sincronização e autenticação remota fora do que a spec aprovada definir; persistência de rascunhos e perspectiva; o banco não é apagado em falha de leitura. Decisões de produto vigentes: rosa e azul sobre base creme, e espaço pessoal separado de pedido de cuidado.

**Ainda pode mudar:** nome definitivo do app, textos finais dos CTAs, pequenos ajustes de paleta e contraste, recortes dos personagens como assets de runtime e arquitetura do backend futuro.
