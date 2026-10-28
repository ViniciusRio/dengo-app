# ADR-020 — Contrato assíncrono do repository no Incremento 1

## Status

Aceito para o Incremento 1 da persistência local. Implementado e validado neste checkpoint. Não aprova Room nem inicia o Incremento 2.

## Contexto

O protótipo usa `FakeCoupleRepository` em memória. Antes de introduzir armazenamento durável, as telas precisam representar leitura em andamento ou falha e aguardar o resultado de escritas assíncronas sem perder rascunhos publicados apenas após confirmação.

## Decisão

`CoupleRepository` expõe leitura como `Loading`, `Ready` ou `ReadError` e escritas suspensas com resultado confirmado, revertido ou incerto. Os quatro ViewModels dependem da interface. Os compositores de pedido `OTHER` e do Mural mantêm texto ou traços até confirmação; resultado incerto não causa repetição automática. Cancelamento é propagado.

Ações rápidas das Homes apresentam um único aviso referente à intenção mais recente. Mood e espaço pessoal executam na ordem dos toques; pedidos e respostas de IDs distintos podem prosseguir de modo independente. O responsável aceitou a consequência: a falha de uma ação independente anterior pode não ser anunciada depois que outra intenção for iniciada. Uma operação incerta deve ser conferida no estado atual antes de tentativa manual de repetição.

O Fake continua sendo a fonte de estado do protótipo. Room, snapshots transacionais, armazenamento binário de desenhos, migração e sincronização não pertencem a este incremento.

## Evidência e limites

As correções passaram por revisões independentes sem defeito confirmado. `./gradlew --quiet test assembleDebug` passou com 90 testes unitários debug e 90 release; os instrumentados no Galaxy A26 terminaram com `OK (14 tests)` após corrigir o seletor de dois testes de rascunho. `git diff --check` passou. O responsável percorreu o roteiro manual no A26 e relatou que tudo funcionou; é uma aprovação geral dos fluxos observados, sem evidência individual de falhas injetadas, TalkBack ou fonte ampliada. Testes automatizados e revisão humana têm alcances distintos.

Commit e push exigem autorização humana específica, conforme `AGENTS.md`.
