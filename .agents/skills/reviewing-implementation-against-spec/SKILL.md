---
name: reviewing-implementation-against-spec
description: Use when a Dengo implementation is already done and someone asks whether it matches an approved specification, plan, or decision, including after green tests or a developer's sign-off. Not for planning a new feature or implementing an unbuilt one.
---

# Revisar implementação contra especificação

## Princípio

Compare **requisito aprovado → comportamento implementado**. Código, testes verdes e relato do implementador são evidências a examinar; não redefinem o contrato nem equivalem a aprovação visual. Esta é uma revisão independente, anterior às correções e à validação no Samsung.

## Apuração

1. Leia `AGENTS.md`. Localize a especificação/plano aprovado e confira `docs/`, ADRs recentes e contexto pertinente do vault conforme a precedência do projeto. Registre versão, escopo e conflitos. Nota exploratória não revoga decisão consolidada; conflito oficial sem precedência demonstrável pede decisão humana.
2. Separe cada afirmação da spec em **obrigatório**, **escolha permitida**, **exemplo**, **hipótese**, **adiado** ou **fora de escopo**. Não cobre uma solução literal onde houve liberdade deliberada.
3. Inspecione código e testes reais, além de diff/commits pertinentes quando existirem. Trace cada requisito obrigatório até arquivos, trechos e testes; verifique também invariantes, casos de borda, efeitos colaterais e ausências exigidas. Um teste que confirma o comportamento atual sem testar o contrato não fecha a lacuna.
4. Procure omissão, implementação parcial, desvio semântico, escopo extra, regressão, arquitetura sem justificativa, decisão de produto silenciosa e documentação ou alegação de aprovação prematura. Execute verificações automatizadas proporcionais à mudança quando úteis; registre comando e resultado. Testes verdes nunca substituem a comparação com a spec.
5. Separe o que código/testes podem demonstrar do que requer uso real no Samsung: composição, densidade, legibilidade, teclado, TalkBack, fonte ampliada, animação percebida, feedback, toque e regressões visuais. Compose e screenshot isolado não aprovam o dispositivo. Formule um checklist específico para a etapa.

## Entrega

Comece pelos **achados**, em ordem de severidade: bloqueadores, importantes, menores. Para cada divergência, indique requisito, evidência concreta (`arquivo:linha` quando disponível), consequência e o que falta verificar. Distinga os estados **conforme**, **divergente**, **parcial**, **não verificável estaticamente** e **decisão humana necessária**. Ausência de evidência não vira conformidade nem defeito confirmado.

Mesmo numa resposta curta, apresente a matriz com as três colunas `requisito | evidência | estado`; escreva “evidência indisponível” na coluna quando couber. Cubra também limites de escopo relevantes. Em seguida, registre verificações executadas, validação pendente no Samsung, escopo adicional, decisões humanas pendentes e veredito. Use um veredito: **conforme para seguir à revisão no dispositivo**, **requer correções antes da revisão no dispositivo**, **bloqueado por decisão humana** ou **não foi possível verificar com a evidência disponível**. Não chame a etapa de “aprovada” por causa de build, testes ou screenshots.

Não corrija código ou documentos nesta revisão sem pedido explícito de uma etapa posterior. Não faça commit nem push sem autorização explícita.
