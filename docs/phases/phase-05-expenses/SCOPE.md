# Escopo — Fase 05

## Objetivo
Entregar a gestão completa de lançamentos locais, preservando as entidades, regras monetárias, tags globais e relações Room já validadas nas Fases 02–04.

## Incluído
- CRUD de lançamentos com descrição normalizada, valor positivo em centavos (`Long`) e data local.
- Lançamentos sem tags, com uma tag ou com várias tags opcionais.
- Criação/reutilização de tags no formulário, com associação atômica.
- Edição que pode transferir o lançamento entre meses e exclusão com confirmação.
- Inclusão retroativa no mês da data informada.
- Lista com pesquisa por descrição, filtro por mês/período, tag e “sem tag”.
- Estados de vazio, validação e erro aplicáveis às telas de cadastro e lista.

## Excluído
- Resumo financeiro agregado, gráficos e relatórios (Fase 06).
- Histórico de orçamento/meses (Fase 07).
- Sincronização, servidor, autenticação, backup, restauração e rede.
- Limites, metas, rateio por tag ou hierarquia de tags.

## Dependências reutilizadas
`ExpenseEntity`, `TagEntity`, `ExpenseTagCrossRef`, DAOs e `BudgetDatabase`; normalização, validações monetárias e associações transacionais aprovadas nas fases anteriores.

## Referências
`docs/FUNCTIONAL_SPECIFICATION.md`, `docs/UI_UX_SPECIFICATION.md`, `docs/BUSINESS_RULES.md`, `docs/DATA_MODEL.md`, `docs/ACCEPTANCE_CRITERIA.md`.

