# Feature — Persistência local dos dados publicados

## Objetivo e alcance

No mesmo aparelho e na mesma instalação, pedidos e seus estados, acontecimentos factuais do Histórico, mood atual de Lidianne, espaço pessoal e recados publicados de texto ou desenho sobrevivem ao encerramento do processo e à reabertura do Dengo. As duas perspectivas consultam o mesmo estado local. A implementação do Incremento 2 usa Room, conforme o ADR-021; o contrato comportamental verificável está também em `openspec/specs/local-couple-state/spec.md`.

## Dados preservados

- Pedidos: tipo, mensagem de `OTHER`, IDs, participantes, estado e horário. Criação, resposta e agradecimento mantêm seus acontecimentos de Histórico na mesma operação.
- Histórico: eventos factuais, autoria, horários e ordem de inserção, inclusive quando dois eventos têm o mesmo horário. Mood e espaço pessoal continuam independentes; publicar no Mural não cria evento.
- Mood: opção atual e data de Lidianne. Repetir o mesmo mood no mesmo dia não cria novo acontecimento.
- Espaço pessoal: estado ativo/inativo e horário de ativação quando ativo. Não expira automaticamente; uma ação sem mudança não cria evento.
- Mural: texto aparado e limitado conforme a spec vigente; desenho com proporção, traços, cores, pontos e ordem, além de autoria, horário e ID. O último recado continua alimentando o preview nas duas Homes.

Cada escrita relacionada é transacional. Confirmação significa que o dado pode ser recuperado após reabertura. Rejeição conhecida não altera o estado; resultado incerto não provoca reenvio automático. Cancelamento é propagado. Uma leitura com falha apresenta erro e permite tentar novamente sem apagar ou recriar automaticamente o banco.

## Fora do escopo

Rascunhos, perspectiva ativa, identidade de sessão, backup, exportação, recuperação após desinstalação, autenticação, backend, comunicação e sincronização entre aparelhos. O backup automático do app permanece desativado. Não havia banco anterior a migrar; interações mantidas apenas na memória de versões anteriores não podiam ser recuperadas após encerramento do processo.

## Validação do Incremento 2

Em 2026-10-30 passaram 90 testes unitários debug e 90 release, os builds debug e androidTest e a suíte instrumentada completa no Galaxy A26 (`OK (23 tests)`, nove deles Room). Os cenários automatizados cobrem reabertura do banco, rollback de pedido e desenho, duplicidade, falha de leitura e retry na mesma instância e preservação do arquivo ao falhar a abertura de um banco de teste corrompido. Duas rodadas de revisão independente terminaram sem defeito confirmado após correções.

O responsável criou pedido, escolheu mood, ativou espaço pessoal e publicou recados de texto e desenho no A26. Após encerramento forçado e reabertura por ADB, confirmou que reapareceram pedido/estado, Histórico, mood, espaço pessoal, os dois recados e os previews nas Homes. Esse relato não comprova teste manual de corrupção, falhas injetadas, TalkBack, fonte ampliada, desinstalação ou comunicação entre aparelhos.
