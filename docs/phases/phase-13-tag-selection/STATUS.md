# Status — Fase 13

## Situação atual

**Implementação concluída; validação manual em andamento.** A autorização foi recebida em 2026-10-27. O componente modal foi integrado ao formulário compartilhado, sem alterações de modelo, persistência, dependências ou versões. Após reiniciar o emulador, a UI Compose passou a renderizar e a árvore UI foi inspecionada. Os cenários completos de persistência e edição ainda precisam ser concluídos.

## Evidências do levantamento

- `CreateExpenseDialog` mantém `selectedTagIds: Set<String>` e renderiza um `FilterChip` por tag.
- O mesmo diálogo é usado no cadastro e na edição em `AppShell.kt` e `MainActivity.kt`.
- `ExpenseListViewModel` carrega tags com `TagDao.findAll()`, e a edição obtém IDs por `findByIdWithTags`.
- `ExpenseOperations.create/updateTags` valida IDs e grava associações na transação; `TagOperations.rename` preserva ID e `delete` remove associações.
- Testes existentes cobrem persistência de múltiplas associações em `ExpenseOperationsTest`, CRUD de tags em `TagOperationsTest` e persistência do formulário em `ExpenseFormPersistenceTest`.

## Implementação realizada até o momento

`TagSelector` foi criado em `ExpenseForm.kt` e integrado ao cadastro/edição compartilhados. O modal usa lista rolável, pesquisa local sempre visível, checkboxes, estado temporário e ações `Aplicar`/`Cancelar`. A lista vazia oferece `Gerenciar tags`; o fluxo navega para a tela de Tags. A atualização da lista remove IDs que não existem mais.

## Decisão recomendada

Modal Material 3 para seleção múltipla, com estado temporário e confirmação explícita. Pesquisa local é obrigatória conforme autorização recebida.

## Decisões pendentes

Nenhuma decisão funcional permanece pendente: pesquisa sempre visível, descarte de IDs removidos e atalho para gerenciamento foram autorizados e implementados.

A primeira tentativa mostrou tela preta, mas o reinício do emulador recuperou a renderização. `uiautomator dump` passou a identificar `ComposeView`, “Olá, vamos organizar?”, “Tags (opcional)”, “Nenhuma tag selecionada”, “Selecionar tags”, “Pesquisar tags”, “Aplicar” e “Cancelar”. A busca inexistente também exibiu “Nenhuma tag corresponde à pesquisa.”. O estado inicial, abertura do formulário/modal, seleção de uma tag, aplicação do resumo `1 tag`, cancelamento preservando `1 tag`, limpeza da pesquisa e pesquisa sem resultados foram observados; persistência, edição, seleção múltipla, remoção/renomeação e temas ainda precisam de execução completa.

## Bloqueios

Não há bloqueio técnico atual. O fechamento da fase continua condicionado aos cenários manuais restantes e à documentação de cada evidência.
