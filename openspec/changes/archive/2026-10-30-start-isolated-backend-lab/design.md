# Design

## Context

O build Android usa Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20 e Java 21 disponível no ambiente. O laboratório escolhido é técnico e local. A documentação oficial do Spring Boot 4.1.2 exige Java 17+ e Gradle 8.14+; o AGP 8.13 exige Gradle 8.13 como mínimo. Gradle 8.14.5 é um patch da série 8.14. Referências: `https://docs.spring.io/spring-boot/system-requirements.html`, `https://developer.android.com/build/releases/agp-8-13-0-release-notes` e `https://docs.gradle.org/8.14.5/release-notes.html`.

## Goals / Non-Goals

**Goals:** um comando HTTP de criação, uma consulta HTTP de listagem, uma regra de duplicidade sob concorrência, testes automatizados e reprodução local. Explicar o que a distinção entre comando e consulta simplifica neste caso e por que não há projeção separada.

**Non-Goals:** backend de produto, equivalência completa com `CoupleRepository`, exposição na rede, dados pessoais reais, estados de resposta/agradecimento, pedidos `OTHER`, banco durável, recuperação após reinício, autenticação, Android HTTP client, sincronização, mensageria, cache ou observabilidade distribuída.

## Decisions

### 1. Build separado, wrapper único

`labs/backend/settings.gradle.kts` define um build independente. O wrapper da raiz sobe para Gradle 8.14.5 e executa o laboratório por `./gradlew -p labs/backend ...`. A raiz continua contendo somente `:app`; o laboratório não importa o módulo Android. Se a regressão Android falhar por incompatibilidade do wrapper, a mudança não está pronta: investigar e corrigir ou reconsiderar um wrapper próprio para o laboratório antes de concluir.

### 2. Serviço local e dados sintéticos

Usar Spring Boot 4.1.2, Kotlin 2.2.20 e Java 21. Configurar `server.address=127.0.0.1` e `server.port=8081`. A entrada recebe apenas `type`, enum dos quatro pedidos predefinidos do Dengo; não recebe nomes, texto, autoria ou dados reais. O estado fica em memória do processo, começa vazio e desaparece no reinício. O README do laboratório deve avisar isso explicitamente.

### 3. Um módulo de domínio atrás do HTTP

Um módulo pequeno em memória concentra a regra de criar e consultar pedidos. Cada pedido tem ID sequencial, tipo, estado `PENDING` e horário UTC injetável para teste. Criar um tipo já ativo devolve o mesmo pedido sem mudar o estado; chamadas concorrentes do mesmo tipo devem criar exatamente um. A consulta retorna uma cópia ordenada por ID crescente. O controlador traduz criação em HTTP 201 e repetição em HTTP 200; tipos inválidos retornam HTTP 400 sem alterar a coleção.

Há comandos e consultas distintos no comportamento, mas uma única fonte de estado. Separar interfaces, projetar outra tabela ou introduzir consistência eventual agora aumentaria o custo sem responder a uma necessidade do laboratório.

### 4. Verificação e limites

Testes de domínio cobrem vazio, criação, duplicidade, tipos distintos e concorrência. Testes HTTP cobrem códigos, corpo, erro de entrada e listagem. A regressão Android cobre testes unitários debug/release e builds debug/androidTest após a troca de wrapper; a suíte instrumentada no A26 é desejável para verificar integração do novo Gradle, mas só será registrada se executada. Um smoke test local com `curl` usa dados sintéticos e verifica que o serviço não escuta em interface pública.

## Risks / Trade-offs

- **Wrapper compartilhado:** há uma pequena dependência de ferramenta entre app e laboratório; compensá-la com regressão Android real. Não alterar AGP nem Kotlin do app apenas para acomodar o laboratório.
- **Memória volátil:** o laboratório demonstra semântica de comando/consulta, não persistência, idempotência distribuída nem recuperação após crash.
- **Sem autenticação:** aceitável somente porque o serviço escuta em loopback e não é implantado; não usar a API com dados afetivos reais ou em rede compartilhada.
- **Parecido com o domínio do produto:** nomes de tipos servem para aprendizado, mas a API não vira contrato do MVP 2 por consequência.
