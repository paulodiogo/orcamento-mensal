# Status — Fase 16

## Situação

**Implementação e validação técnica concluídas.**

A Fase 15 disponibilizou o orçamento opcional em `TagEntity`. A Fase 16 adiciona modelo/cálculo compartilhado, consulta agregada reativa, apresentação na Home e Relatórios, e testes. Nenhum esquema Room, CRUD de tag ou regra global foi alterado.

## Escopo autorizado

- Período exclusivamente mensal, limitado ao mês atual local do dispositivo.
- Home sem seletor de mês; relatório sem filtros novos.
- Usar o orçamento atual da tag também ao apresentar gastos do mês atual; não criar histórico de orçamento.
- Omitir tags sem orçamento e sem gastos; incluir tags sem orçamento com gastos e tags orçadas sem gastos.
- Implementar Home e Relatórios sem alterar orçamento global, persistência ou versões tecnológicas.

## Decisões já propostas

1. `NULL` significa sem orçamento; `0L` é orçamento explicitamente zero.
2. O percentual é gasto acumulado dividido pelo orçamento da tag, nunca percentual restante.
3. O valor textual acima de 100% é preservado; somente a barra é limitada a 100%.
4. Tags com orçamento e sem lançamentos aparecem com consumo de 0% quando o orçamento é positivo.
5. O relatório permanece mensal; não há comparação direta com intervalos de vários meses.

## Implementado

- Modelo `TagBudgetExecution` com estados sem orçamento, orçamento zero, consumo normal e estouro; cálculo percentual compartilhado.
- Consulta Room reativa com agregação por tag e inclusão de tags com orçamento e gasto zero.
- `ReportViewModel` disponibiliza execuções comuns à Home e aos Relatórios.
- Home mostra gastos por tag (marcando as sem orçamento) e até quatro orçamentos por tag prioritários.
- Relatório mostra a execução mensal por tag, incluindo tags sem orçamento que têm gastos.
- Testes de domínio e testes instrumentados para dados agregados e interface adicionados.

## Validação concluída

- `testDebugUnitTest`: 28 testes JVM, sem falhas.
- `assembleDebug lintDebug`: sucesso.
- `connectedDebugAndroidTest`: 41 testes no `BUSAO(AVD) - 16`, sem falhas ou ignorados.
- `git diff --check`: sucesso.

A fase não inicia trabalho posterior. O orçamento global e as versões tecnológicas permanecem inalterados.
