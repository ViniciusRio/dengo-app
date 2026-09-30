# Decisões de Produto

Decisões arquiteturais detalhadas permanecem nos ADRs do repositório.

## Home Lidianne
- Home de expressão;
- “Pedir dengo” é CTA principal;
- mood horizontal;
- “Um tempo só seu” para espaço pessoal;
- ilustração aprovada no dispositivo.

## Home Vinícius
- Home de compreensão e resposta;
- não copiar Home Lidianne;
- azul representa Vinícius;
- pedidos de Lidianne são conteúdo principal;
- `PENDING → ACCEPTED` por “Estou indo ❤️”;
- `PENDING → DECLINED` por “Não consigo agora”;
- visualizar não altera status;
- sem `SEEN` e sem conclusão nesta etapa;
- espaço pessoal ativo tem precedência;
- mood antigo recebe contexto temporal;
- protótipo terá troca local de perspectiva.
- troca de perspectiva fica em Perfil e retorna à Home escolhida; as duas Homes observam o mesmo estado em memória;
- aceitação e recusa geram eventos de histórico; espaço pessoal suspende respostas sem alterar pedidos.
