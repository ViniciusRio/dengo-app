# Mural
Status: 🟢 Implementado e aprovado no Samsung

Cantinho compartilhado do casal para deixar pequenos recados e carinho, sem aparência de feed social. O status acima se refere à primeira versão textual.

## Primeira versão
- Lidianne e Vinícius publicam e consultam a mesma lista em memória;
- texto com emojis digitados normalmente, até 160 pontos de código Unicode;
- autor por nome e horário local visíveis;
- mais recentes primeiro; empate por ID mais recente;
- “Deixar um recado” abre uma composição curta em bottom sheet;
- o contador mostra o tamanho; vazio ou acima do limite não pode ser publicado;
- a publicação aparece imediatamente, sem confirmação adicional;
- os previews das duas Homes mostram o recado mais recente e abrem o Mural.

## Estado vazio
“Um cantinho para deixar carinho um para o outro.”

## Limites da primeira versão
Publicar e visualizar são permitidos durante espaço pessoal ativo, sem sugestões de contato insistente. Não há edição, exclusão, comentários, reações, fotos, anexos ou desenhos. O estado reinicia com o processo e não sincroniza entre aparelhos. Publicar não gera evento de Histórico.

## Validação da primeira versão no dispositivo
Foram aprovados estado vazio, CTA, bottom sheet com teclado e campo multilinha, contador e bloqueio acima do limite, texto e emoji, publicação imediata no topo, ordenação visual, autoria nas duas perspectivas, previews nas Homes e navegação para o Mural.

## Evolução posterior — recados desenhados à mão

Implementada, revisada contra a spec e aprovada na validação manual final no Galaxy A26. “Deixar um recado” oferece **Escrever** para a composição textual existente e **Desenhar** para criar um recado individual com traços livres do dedo, inclusive escrita manual, sem teclado. A composição manual oferece escolha simples de cor, desfazer e limpar sem confirmação. Desenho vazio não publica; sair com traços não publicados exige escolha entre continuar e descartar. Depois de publicado, o desenho real aparece com autoria e horário na lista e, se for o último recado, em preview visual nas duas Homes, sem edição ao toque. A coleção continua local e em memória, sem evento de Histórico. O relato final confirma o compositor estável, desenho, cores, controles e publicação; não discrimina teste dos previews nas Homes. Detalhes e critérios de aceitação estão em `docs/features/mural.md`.
