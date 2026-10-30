# Proposal

## Why

O Dengo já concluiu a persistência local e um exercício SDD sobre escrita incerta. O plano de oito temas ainda descrevia Room como futuro e sugeria CQRS no Android sem ganho comprovado. Um laboratório backend isolado permite estudar Kotlin/Spring Boot com um comando, uma consulta e uma regra real de duplicidade, preservando o protótipo Android e as decisões de produto.

## What Changes

- Criar em `labs/backend/` um build Kotlin/Spring Boot independente do build Android, executado pelo wrapper Gradle da raiz.
- Atualizar o wrapper único de Gradle 8.13 para 8.14.5, com regressão do app Android como condição de aceitação.
- Expor somente em `127.0.0.1:8081` uma API de laboratório para criar e consultar pedidos predefinidos sintéticos, mantidos em memória até o processo terminar.
- Demonstrar criação, consulta, duplicidade e concorrência com testes e instruções de reprodução. Distinguir comandos e consultas sem criar bancos ou projeções separados.
- Manter o laboratório desconectado do app e fora do MVP 2.

## Capabilities

### New Capabilities

- `backend-care-request-lab`: comportamento reproduzível de criação e leitura de pedidos sintéticos em um processo local.

### Modified Capabilities

Nenhuma capacidade do produto Android.

## Impact

- Documentação: revisar o roadmap de estudo, registrar resultados e limites do laboratório após execução.
- Build: wrapper Gradle da raiz e novo build isolado em `labs/backend/`; `settings.gradle.kts` da raiz continua incluindo apenas `:app`.
- Código e testes novos somente sob `labs/backend/`; código de produção Android e banco Room não mudam.
- Não inclui autenticação, pareamento, persistência remota, sincronização, mensageria, Redis, AWS, dados reais ou conexão com o Android.

Esta proposta registra o recorte escolhido para estudo, não a conclusão da implementação nem aprovação de uma etapa de produto.
