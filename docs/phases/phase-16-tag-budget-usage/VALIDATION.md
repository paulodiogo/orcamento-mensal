# Estratégia de validação — Fase 16

## Estado e resultados executados

- `rtk .\gradlew.bat testDebugUnitTest` — sucesso: 28 testes JVM, 0 falhas, 0 erros, 0 ignorados (XMLs de resultados).
- `rtk .\gradlew.bat assembleDebug lintDebug` — sucesso.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`: 41 testes, 0 falhas, 0 ignorados. A execução anterior encontrou falhas intermitentes do AVD e uma falha em teste preexistente; nesta execução a suíte completa passou.
- `rtk .\gradlew.bat connectedDebugAndroidTest --tests ...` — falhou imediatamente; o task Android não aceita a opção Gradle `--tests`.
- Tentativa de filtro via `-Pandroid.testInstrumentationRunnerArguments.class=...` — falhou por parsing do PowerShell/Gradle do argumento com `#`; task reportada como inexistente. Não é evidência de falha do código.
- `rtk git diff --check` — sucesso.
- `rtk adb devices` — AVD respondeu inicialmente, mas desconectou durante a última execução instrumentada.

A implementação permanece parcial. Não declarar a fase concluída antes da validação instrumentada completa.

## Testes unitários

- Cálculo de R$ 200,00 gastos sobre R$ 500,00 orçados como `40,00%`.
- Cálculo de R$ 650,00 gastos sobre R$ 500,00 orçados como `130,00%`.
- Igualdade em `100,00%`, gasto zero em `0,00%` e arredondamento `HALF_UP` com duas casas.
- Ausência de orçamento sem percentual.
- Orçamento zero sem divisão por zero e com estado de estouro quando houver gasto.
- Proteção contra valores negativos e overflow conforme as regras de domínio existentes.

## Testes Room

- Consulta por mês inclui tags com orçamento e sem lançamentos, retornando gasto zero.
- Consulta inclui tags sem orçamento que tenham gastos.
- Inclusão, edição e exclusão de despesas atualizam a projeção reativa.
- Mudanças em associações `expense_tag` atualizam as tags afetadas.
- Edição de orçamento da tag atualiza o resultado.
- Uma despesa com múltiplas tags é contabilizada integralmente em cada tag.

## Testes instrumentados / Compose

- Relatório mostra gasto, orçamento e `40,00%` para uma tag orçada.
- Relatório mostra percentual real e indicação de estouro acima de 100%.
- Relatório trata `Sem orçamento` e orçamento `R$ 0,00` sem percentual enganoso.
- Home e Relatórios apresentam os mesmos dados para a mesma tag e mês.
- Tags orçadas sem lançamentos são apresentadas com gasto zero.
- A mudança do mês selecionado altera os valores utilizados, se o seletor da Home for aprovado.

## Comandos previstos

```powershell
rtk .\gradlew.bat testDebugUnitTest
```

```powershell
rtk .\gradlew.bat assembleDebug lintDebug
```

```powershell
rtk .\gradlew.bat connectedDebugAndroidTest
```

```powershell
rtk git diff --check
```

Registrar resultados, quantidades de testes e erros somente depois de execução real.
