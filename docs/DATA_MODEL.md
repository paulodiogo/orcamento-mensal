# Modelo de Dados

## Entidades
| Entidade | Campos e restrições |
|---|---|
| `monthly_budget` | `month` TEXT PK (`YYYY-MM`), `amount_cents` INTEGER >= 0, `created_at`, `updated_at`; um orçamento por mês. |
| `expense` | `id` TEXT PK, `description` TEXT não vazia, `amount_cents` INTEGER > 0, `occurred_on` TEXT ISO-8601, `created_at`, `updated_at`; índice em `occurred_on`. |
| `tag` | `id` TEXT PK, `name` TEXT, `normalized_name` TEXT UNIQUE, `budget_amount_cents` INTEGER nullable, timestamps; índice único na normalizada. `NULL` significa sem orçamento e `0` é zero explícito. A coluna foi adicionada pela migração Room 1→2 na Fase 15. |
| `expense_tag` | `expense_id` FK, `tag_id` FK, PK composta; índices por ambas as FKs. |

`expense` não possui FK para orçamento: gastos podem existir em mês sem orçamento e meses históricos não são descartados. `expense_tag` representa classificação opcional: nenhum lançamento precisa ter uma associação. Para excluir uma tag, a camada de domínio informa o impacto e, após confirmação, remove em uma transação as linhas `expense_tag` da tag e então a tag; nenhuma linha de `expense` é excluída. A exclusão de lançamento remove apenas suas associações. O orçamento não pode ser excluído pela UI na v1. `tag.budget_amount_cents` é o orçamento atualmente configurado para a tag; não há histórico mensal/versionado. O cálculo comparativo da Fase 16 usa esse valor atual e os gastos associados no mês atual. Não há tabelas ou campos de hierarquia ou histórico mensal de orçamento por tag.

```mermaid
erDiagram
  MONTHLY_BUDGET { string month PK; long amount_cents }
  EXPENSE { string id PK; string description; long amount_cents; date occurred_on }
  TAG { string id PK; string name; string normalized_name UK; long budget_amount_cents nullable }
  EXPENSE ||--o{ EXPENSE_TAG : classifica
  TAG ||--o{ EXPENSE_TAG : classifica
  EXPENSE_TAG { string expense_id FK; string tag_id FK }
```

Valores monetários usam `Long` em centavos: elimina erro binário de ponto flutuante e permite somas inteiras. Validar limites para evitar overflow antes de persistir ou somar.

