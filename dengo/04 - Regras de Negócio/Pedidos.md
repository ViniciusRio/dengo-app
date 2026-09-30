# Pedidos
Status: 🔵 Em evolução

## Tipos
Dengo, Bolsa quente, Remédio, Passar tempo juntos e Outro.

## Estados
- **Pendente:** criado, sem resposta.
- **Aceito:** “Estou indo ❤️”; não significa conclusão.
- **Recusado:** “Não consigo agora”; não é rejeição à pessoa.
- **Concluído:** existe no domínio, regra ainda não definida.

## Transições Etapa 5
`PENDING → ACCEPTED`
`PENDING → DECLINED`

## Regras
- visualizar não muda status;
- sem `SEEN`;
- `OTHER` exige texto;
- pedidos para Vinícius: mais recentes primeiro.
- somente pedidos pendentes dirigidos a Vinícius admitem resposta;
- cada resposta válida atualiza pedido e histórico no mesmo estado;
- resposta inválida não altera estado nem registra evento;
- com espaço pessoal ativo, pedidos seguem visíveis e respostas ficam indisponíveis até o encerramento.

## Em aberto
Quem conclui, quando concluir, cancelamento e permanência de pedidos respondidos.
