## ADDED Requirements

### Requirement: Vínculo por código de uso único
O sistema SHALL vincular um aparelho a Lidianne ou a Vinícius somente por um código gerado administrativamente, de uso único e com validade de 15 minutos, sem oferecer cadastro público.

#### Scenario: Código válido
- **WHEN** a pessoa informa um código válido no app
- **THEN** o aparelho recebe uma sessão da pessoa para quem o código foi gerado e abre a Home dela

#### Scenario: Código já usado ou expirado
- **WHEN** a pessoa informa um código já usado ou expirado
- **THEN** o vínculo é recusado, nenhuma sessão é criada e o app orienta a pedir outro código

#### Scenario: Muitas tentativas inválidas
- **WHEN** o limite de tentativas inválidas é ultrapassado
- **THEN** o servidor recusa novas tentativas temporariamente, mesmo com um código correto

### Requirement: Sessão protegida
O sistema SHALL armazenar no servidor somente o hash do token da sessão e SHALL armazenar o token no aparelho cifrado com chave do Android Keystore, fora de backup e de transferência entre aparelhos.

#### Scenario: Consulta ao banco do servidor
- **WHEN** alguém lê a tabela de sessões do servidor
- **THEN** o token original não pode ser obtido a partir do que está gravado

#### Scenario: App reaberto
- **WHEN** um aparelho vinculado reabre o app
- **THEN** a pessoa continua identificada, sem novo código

### Requirement: Um aparelho ativo por pessoa
O sistema SHALL manter no máximo uma sessão ativa por pessoa e SHALL permitir a revogação administrativa de uma sessão.

#### Scenario: Troca de celular
- **WHEN** uma pessoa vincula um novo aparelho
- **THEN** a sessão do aparelho anterior da mesma pessoa deixa de ter acesso, e a sessão da outra pessoa não muda

#### Scenario: Sessão revogada
- **WHEN** um aparelho com sessão revogada faz uma requisição
- **THEN** o servidor recusa o acesso, e o app apaga o token, a cópia local e as tentativas pendentes, volta à tela de vínculo e não mostra a ação como enviada

#### Scenario: Resposta atrasada depois da revogação
- **WHEN** uma resposta de sincronização pedida antes da revogação chega depois de o app apagar a cópia
- **THEN** a resposta é descartada, e nenhum dado dela é gravado

#### Scenario: Aparelho substituído reconecta
- **WHEN** o aparelho anterior se conecta depois de a pessoa vincular outro celular
- **THEN** ele apaga a cópia local da mesma forma, sem mostrar dados do casal

#### Scenario: Erro diferente de revogação
- **WHEN** o aparelho encontra falha de rede ou erro interno do servidor
- **THEN** a sessão e a cópia local são preservadas

#### Scenario: Revogação sem conexão
- **WHEN** um aparelho revogado continua sem conexão
- **THEN** a última cópia permanece nele, protegida pelo sandbox do Android e pelo bloqueio do celular, e esse limite está documentado

#### Scenario: Outra pessoa vinculada no mesmo aparelho
- **WHEN** um aparelho é vinculado a uma pessoa diferente da sessão anterior
- **THEN** a cópia local e as tentativas pendentes anteriores são apagadas antes de abrir a nova sessão

### Requirement: Identidade derivada da sessão
O sistema SHALL determinar quem age somente pela sessão e SHALL aplicar a permissão por pessoa no servidor. O app SHALL determinar a Home e a autoria pela sessão, sem oferecer troca de perspectiva.

#### Scenario: Ação de outra pessoa
- **WHEN** a sessão de Vinícius tenta criar pedido, agradecer, definir mood ou alterar espaço pessoal, ou a de Lidianne tenta aceitar ou recusar um pedido
- **THEN** o servidor recusa sem alterar nada e o app recebe `Reverted`

#### Scenario: Autoria de recado
- **WHEN** uma sessão publica um recado
- **THEN** o autor é a pessoa da sessão, sem que o cliente possa informar outro autor

#### Scenario: Sem sessão
- **WHEN** o app abre em um aparelho sem vínculo
- **THEN** ele mostra a tela de vínculo e nenhum dado do casal
