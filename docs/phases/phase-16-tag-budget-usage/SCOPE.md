# Escopo — Fase 16

## Incluído

- Calcular, por tag, o consumo do orçamento no mês atual local como `gasto / orçamento × 100`.
- Reutilizar `Long` em centavos, `BigDecimal` e arredondamento `HALF_UP` com duas casas apenas para apresentação.
- Exibir na Home e em Relatórios o nome da tag, gasto mensal, orçamento configurado, percentual e estado de ultrapassagem.
- Incluir tags com orçamento configurado e sem lançamentos, com gasto zero e consumo de `0,00%` quando o orçamento for positivo.
- Tratar `NULL` como ausência de orçamento e `0L` como orçamento explicitamente zero, sem divisão por zero.
- Limitar a barra visual a 100%, preservando o percentual textual real acima de 100%.
- Reutilizar um único modelo e cálculo de domínio para Home e Relatórios.
- Tornar o resultado reativo a alterações em despesas, associações com tags e dados da própria tag.

## Excluído

- Tabelas, migrações Room, dependências e alterações de versões.
- Rateio de uma despesa entre tags; cada tag continua recebendo o valor integral da despesa associada.
- Alterações no CRUD de tags, no formulário de orçamento de tag ou nas regras do orçamento mensal global.
- Alertas, notificações, gráficos, dashboards e bloqueio de lançamentos por estouro.
- Comparação com intervalos de vários meses: o relatório atual é mensal.
- Histórico versionado de orçamento por tag.

## Período e limitação histórica aprovados

O cálculo nesta fase usa somente o mês atual local do dispositivo. O orçamento exibido é o valor atualmente configurado na tag. Não haverá seletor de mês na Home, novos filtros no relatório ou versionamento mensal. A estrutura atual não preserva valores anteriores do orçamento; histórico mensal de orçamento permanece fora de escopo.

## Diagnóstico técnico confirmado

- `TagEntity` já armazena `budgetAmountCents: Long?` em `budget_amount_cents`.
- `ExpenseDao.sumAmountByTagBetween` é uma consulta não reativa iniciada por `expense`, portanto não inclui tags sem lançamento.
- `ReportViewModel` recebe um único mês `YYYY-MM` e observa somente `ExpenseDao.observeAll()`.
- A Home reutiliza `ReportState`, mas fixa o mês atual e não possui seletor de mês.
- `FinancialCalculations.kt` já usa `BigDecimal` e `RoundingMode.HALF_UP` no cálculo do orçamento global.
