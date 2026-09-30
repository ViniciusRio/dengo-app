# ADR-007 — Estado em memória do protótipo

## Status

Aceito para a Etapa 2.

## Decisão

`FakeCoupleRepository` é a única fonte do estado do casal e expõe um `StateFlow<CoupleState>`. As ações são síncronas e alteram um snapshot imutável em memória. O `HomeViewModel` recebe diretamente essa implementação e projeta apenas o estado necessário à futura Home de Lidianne.

O histórico registra a criação de pedidos nesta etapa. Mood fica como estado do dia e espaço pessoal como estado ativo até encerramento explícito; não criamos eventos de timeline para eles antes de validar sua utilidade. O relógio é injetável para testar datas sem persistência ou infraestrutura extra.

Como o `minSdk` é 24 e os models usam `java.time`, habilitar desugaring das APIs Java para manter suporte a Android 7.

## Consequências

Estado e identificadores reiniciam com o processo. Respostas a pedidos e eventos correspondentes entram somente quando seus fluxos forem implementados.
