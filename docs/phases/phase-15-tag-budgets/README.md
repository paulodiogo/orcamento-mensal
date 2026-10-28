# Fase 15 — Orçamento associado a tags

## Objetivo

Planejar, sem implementação, o armazenamento e a edição de um valor de orçamento opcional associado a cada tag global.

O valor será cadastrado nos formulários de criação e edição de tags e persistido localmente. Esta fase não planeja exibição, acompanhamento, comparação ou cálculo do orçamento em outras telas.

## Contexto atual

A gestão de tags está em `TagManagementScreen`, com estado e operações em `domain/TagViewModel.kt` e `domain/TagOperations.kt`. `TagEntity` possui atualmente identidade, nome normalizado e timestamps; `BudgetDatabase` está na versão 1. O aplicativo já usa entrada monetária brasileira com centavos implícitos e `Long`, compartilhada por despesas e orçamento mensal.

A alteração futura deve preservar nomes, identificadores, associações `expense_tag` e todas as despesas existentes.

## Estado

**Planejamento concluído; implementação não iniciada.**

Nenhum código, entidade, esquema Room, dependência, versão ou migração foi alterado por esta fase.

## Documentos

- [Escopo](SCOPE.md)
- [Tarefas](TASKS.md)
- [Critérios de aceitação](ACCEPTANCE.md)
- [Status](STATUS.md)
- [Estratégia de validação](VALIDATION.md)
