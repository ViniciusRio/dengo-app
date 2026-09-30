# ADR-007 — Estado em memória do protótipo

## Status

Aceito para a Etapa 2.

## Decisão

`FakeCoupleRepository` é a única fonte do estado do casal e expõe um `StateFlow<CoupleState>`. As ações são síncronas e alteram um snapshot imutável em memória. O `HomeViewModel` recebe diretamente essa implementação e projeta apenas o estado necessário à futura Home de Lidianne.

O histórico registra criação de pedido, alteração de mood, ativação e encerramento de espaço pessoal, conforme o escopo definido para a Etapa 2. Selecionar novamente o mesmo mood no mesmo dia ou repetir uma ação de espaço pessoal que já está no estado desejado não cria novo evento. O estado atual de mood e espaço pessoal permanece no mesmo snapshot dos pedidos e do histórico.

Os status do pedido são `PENDING`, `ACCEPTED`, `COMPLETED` e `DECLINED`. Na Etapa 2, apenas a criação em `PENDING` foi implementada; as respostas foram adicionadas na Etapa 5 conforme o ADR-009. O relógio é injetável e os IDs de pedido são sequenciais por instância do repository para permitir testes determinísticos sem infraestrutura extra.

Como o `minSdk` é 24 e os models usam `java.time`, habilitar desugaring das APIs Java para manter suporte a Android 7.

## Consequências

Estado e identificadores reiniciam com o processo. As respostas e seus eventos foram implementados na Etapa 5; `COMPLETED` permanece sem ação nesta fase.
