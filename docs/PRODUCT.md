# Product Specification

## Nome

Dengo.

O produto não é uma reprodução do brinquedo/jogo Tamagotchi.

## Visão

Um aplicativo Android privado para **Lidianne e Vinícius** usarem pequenas interações digitais como extensão do cuidado cotidiano. Lidianne pode comunicar pedidos de cuidado, companhia ou espaço e registrar como está se sentindo. Vinícius recebe essas informações em uma interface própria e pode responder de maneira simples e carinhosa.

O objetivo não é resolver um problema crítico: é tornar a comunicação cotidiana mais divertida, explícita e afetiva.

## Usuários iniciais

- Lidianne
- Vinícius

O repositório será público no GitHub, portanto nenhum segredo, credencial, dado privado real, token ou informação sensível deve ser versionado.

## Plataforma

Inicialmente somente Android.

## Princípios do produto

### Afeto sem pressão
O app deve sugerir interações, nunca transformar humor ou pedidos em obrigação.

### Comunicação explícita
Evitar inferir automaticamente o que a outra pessoa deseja. Mood e pedidos são sinais fornecidos voluntariamente.

### Poucos toques
Um pedido rápido ou mudança de mood deve levar poucos segundos.

### Duas perspectivas, um produto
Lidianne e Vinícius usam o mesmo app e o mesmo design system, mas a Home prioriza necessidades diferentes conforme o contexto.

### Privacidade
O produto é pensado inicialmente para duas pessoas. Dados compartilhados devem ser apenas os necessários para as funcionalidades.

## Conceitos do domínio

- **Pair:** vínculo entre Lidianne e Vinícius.
- **Partner:** uma das duas pessoas vinculadas.
- **Care Request:** pedido explícito de cuidado/ação.
- **Mood:** estado informado pela pessoa naquele dia.
- **Interaction:** resposta ou pequena ação afetiva.
- **Personal Space:** estado temporário indicando desejo de ficar sozinha/ter espaço.
- **Mural:** espaço compartilhado para desenhos/recados.
- **History:** linha do tempo de pedidos e respostas relevantes.

## Pedidos iniciais

- Bolsa quente/térmica.
- Remédio.
- Quero dengo/carinho.
- Quero passar um tempo com você.
- Outro pedido.

`Quero ficar sozinha` deve ser tratado como **Personal Space**, não como um pedido equivalente aos demais.

## Mood inicial

- 😞 Chateada
- 😠 Brava
- 😴 Cansada
- 😐 Normal
- 😊 Feliz
- 🥰 Amorosa
- 🥺 Dengosa
- 🤩 Animada

A lista pode ser refinada após uso real.

## Fora do escopo inicial

- Rede social.
- Gamificação complexa.
- IA interpretando emoções.
- Estatísticas de relacionamento.
- Cadastro público/múltiplos casais antes de validar o uso real.
