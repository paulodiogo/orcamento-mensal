# Escopo — Fase 15

## Incluído

- Adicionar orçamento opcional por tag global, apenas para cadastro, edição e persistência.
- Permitir criar tag sem orçamento, criar com orçamento, editar o valor, incluir valor ausente e remover o orçamento.
- Reutilizar o padrão existente de `MoneyTextField`: dígitos com centavos implícitos, apresentação `pt-BR`, prefixo monetário e `Long` em centavos.
- Validar entrada vazia como ausência (`null`), entradas não numéricas/negativas e limite máximo antes de persistir.
- Evoluir a entidade `tag`, o modelo de domínio/operações e o Room por migração incremental, preservando dados e relações.
- Manter nome, normalização, identidade e associações de despesas independentes da edição do orçamento.

## Excluído

Não incluir Home, cartões/listas de tags, histórico, relatórios, filtros, consumo, comparação, saldo, rateio, alertas, notificações, gráficos, dashboards, orçamento mensal global, regras de associação de tags ou apresentação do orçamento fora dos formulários de tags. Não incluir auditoria de acessibilidade.

## Diagnóstico técnico atual

- `TagEntity` (`tag`) contém `id`, `name`, `normalizedName`, `createdAt` e `updatedAt`; não há coluna monetária.
- `TagDao` fornece inserção, busca, observação, atualização, exclusão e contagem; não precisa de consulta nova para o campo, mas suas operações devem receber a entidade atualizada.
- `TagViewModel` delega criação/renomeação/exclusão a `TagOperations` e observa `TagDao.observeAll()`.
- `TagManagementScreen` mantém somente `editingName`; o editor é um `AlertDialog` com `OutlinedTextField`.
- `BudgetDatabase` está na versão 1, sem callback de migração explícito.
- `MoneyTextField` aceita string de dígitos, usa centavos implícitos e `MAX_MONEY_DIGITS = 19`; `parseMoneyDigitsToCents` rejeita overflow via `Long`.

## Decisão recomendada

O orçamento é opcional. Adicionar `budgetAmountCents: Long?` (coluna SQLite `INTEGER`, nullable) à `TagEntity`. `null` significa “sem orçamento definido”; zero é um orçamento explicitamente definido de zero e não pode ser confundido com ausência. Registros existentes recebem `NULL` na migração. A edição de orçamento altera somente essa coluna e o timestamp de atualização; não renomeia a tag nem toca `expense_tag`.
