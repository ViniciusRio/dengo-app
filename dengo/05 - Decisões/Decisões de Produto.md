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

## Histórico — Etapa 6
- mesma linha do tempo somente de leitura nas duas perspectivas, com nomes “Lidianne” e “Vinícius”;
- seis tipos de evento já existentes, inclusive cada mudança de mood separadamente;
- pedido aceito significa “Estou indo ❤️” e pedido recusado significa “Não consigo agora”, sem sugerir conclusão ou culpa;
- acontecimentos ordenados do mais recente ao mais antigo e agrupados por dia;
- estado em memória nesta primeira versão; timeline e estado vazio aprovados no Samsung.

## Mural — Etapa 7
- cantinho compartilhado para recados de texto e emojis, com autoria explícita;
- até 160 pontos de código Unicode, sem edição ou exclusão;
- mesma lista em memória para as duas perspectivas; publicações não geram `HistoryEvent`;
- permitido durante espaço pessoal ativo, sem incentivo a contato insistente;
- previews das duas Homes mostram somente o recado mais recente e abrem o Mural;
- na Etapa 7, desenhos ficavam como possibilidade futura; o Mural textual e seus previews foram aprovados visual e funcionalmente no Samsung.

## Mural — recados desenhados à mão especificados
- A decisão posterior à Etapa 7 mantém o recado textual e acrescenta o recado desenhado como alternativa sob “Deixar um recado”: **Escrever** ou **Desenhar**, sem misturar teclado e traços no mesmo recado;
- o desenho/escrita manual com o dedo compõe um recado individual com autoria e horário, na mesma coleção local, sem canvas colaborativo, edição posterior ou evento de Histórico;
- a composição manual permite cor, desfazer e limpar sem confirmação; rascunho com traços exige proteção ao abandonar e desenho vazio não publica;
- o último recado desenhado terá preview visual real nas duas Homes; o estado continua em memória;
- esta evolução está especificada em `docs/features/mural.md`, mas ainda não foi implementada nem validada no Samsung. A próxima etapa de produto e as escolhas arquiteturais não foram definidas.
