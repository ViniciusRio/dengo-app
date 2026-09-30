# ADR-002 — Protótipo local antes do backend

## Status
Aceita inicialmente.

## Decisão

A primeira implementação visual/funcional deve usar dados fake ou armazenamento local. Spring Boot/PostgreSQL só entram quando houver necessidade de sincronização real entre os dois dispositivos.

## Motivo

Permite validar produto, UX e design antes de introduzir autenticação, rede, deploy e infraestrutura.
