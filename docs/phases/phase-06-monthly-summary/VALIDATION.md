# Validação — Fase 06

**Planejada:** U-02, U-03 e UI-01, confirmando que lançamentos sem tags contam no resumo e grupos por tag não duplicam o total.

## F6-B — Consultas e agregações Room

- `ExpenseDao.sumAmountBetween` usa `COALESCE(SUM(amountCents), 0)` e retorna zero para períodos vazios.
- `ExpenseDao.countBetween` retorna zero para períodos vazios.
- `ExpenseDao.sumAmountByMonthBetween` agrupa por `substr(occurredOn, 1, 7)`, preservando a data local `YYYY-MM-DD` sem conversão de fuso.
- `ExpenseDao.sumAmountByTagBetween` faz `JOIN` somente para o detalhamento por tag; cada associação recebe o valor integral do lançamento. O total geral permanece em consulta separada e não sofre multiplicação por tags.
- Consultas agrupadas retornam lista vazia sem dados; não foram criadas entidades ou alterações de esquema.
- Teste adicionado em `BudgetDatabaseTest`: cobre total, contagem, limites inclusivos, meses distintos, lançamento sem tag no total, lançamento multitag sem duplicação, total integral por tag e período vazio.

### Resultados reais

- `testDebugUnitTest` — sucesso do Gradle, mas `NO-SOURCE`; não há testes JVM.
- `connectedDebugAndroidTest` — sucesso no BUSAO (AVD) - 16; o comando não exibiu contagem detalhada no terminal, sem falhas reportadas.
- `assembleDebug` — sucesso.
- `lintDebug` — sucesso.
- `git diff --check` — sucesso.

## F6-C — Percentuais e regras de período

- `calculateBudgetExecution` calcula percentual com `BigDecimal`, escala padrão de 2 casas e `RoundingMode.HALF_UP`.
- O percentual é `null` quando o orçamento é zero; não são produzidos infinito, `NaN` ou valores arbitrários.
- O saldo permanece separado em `availableCents` e pode ser negativo quando os gastos excedem o orçamento.
- Gastos negativos e intervalos invertidos são rejeitados.
- Datas continuam sendo comparadas como datas civis `YYYY-MM-DD`, com limites inclusivos e sem conversão de fuso.
- Testes adicionados para percentual normal, zero, igualdade, excesso, arredondamento, valores grandes, overflow, orçamento zero, intervalos inclusivos e intervalo invertido.

### Resultados reais da F6-C

- `testDebugUnitTest` — falhou ao tentar executar `FinancialCalculationsTest` com `ClassNotFoundException`, embora a compilação tenha produzido a classe; o teste JVM foi removido após a investigação para não deixar uma suíte quebrada. A ausência de testes JVM permanece pendência.
- `connectedDebugAndroidTest` — primeira execução falhou por asserção incorreta no novo caso de overflow; após correção focada, passou no BUSAO (AVD) - 16.
- `assembleDebug` — sucesso.
- `lintDebug` — sucesso.
- `git diff --check` — sucesso.

## F6-D — Interface e navegação

- Criados `ReportViewModel` e `ReportScreen`.
- A tela apresenta orçamento, total gasto, saldo, percentual ou “indisponível” para orçamento zero, e detalhamento por tag.
- A seleção mensal atualiza o `StateFlow` e consulta o período civil correspondente.
- A navegação foi integrada localmente à tela de lançamentos, sem reestruturar a atividade.
- Não foram adicionados gráficos, dependências, migrações ou funcionalidades de histórico.
- Valores de apresentação usam `BigDecimal` para conversão de centavos, sem cálculos financeiros em `Double` ou `Float`.

### Resultados reais da F6-D

- `testDebugUnitTest` — sucesso do Gradle com `NO-SOURCE`; não há testes JVM executados.
- `connectedDebugAndroidTest` — bloqueado por ambiente: `DeviceException: No connected devices!`.
- `assembleDebug` — sucesso.
- `lintDebug` — sucesso.
- `git diff --check` — sucesso.

## F6-E — Regressão e validação final

### Ambiente

- `rtk adb devices` — `emulator-5554 device` disponível antes da execução.

### Resultados reais

- `rtk .\\gradlew.bat testDebugUnitTest` — sucesso do Gradle com `NO-SOURCE`; nenhum teste JVM foi executado.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO (AVD) - 16`: 22 testes, 0 falhas, 0 ignorados, 100% de sucesso.
- `rtk .\\gradlew.bat assembleDebug` — sucesso.
- `rtk .\\gradlew.bat lintDebug` — sucesso.
- `rtk git diff --check` — sucesso.

A correção realizada na F6-E foi localizada no `ReportViewModel`: o intervalo mensal agora usa `YearMonth.atEndOfMonth()`, evitando consultar datas inexistentes como dia 31 em meses com 28, 29 ou 30 dias. Também foi adicionado teste Compose básico para a tela de relatórios.

### Critérios da Fase 06

| Critério | Estado | Evidência |
|---|---|---|
| Total geral sem dupla contagem | Aprovado | Consulta separada e testes Room da F6-B |
| Agrupamento por tag integral | Aprovado | Consulta `JOIN` e testes Room |
| Agrupamento mensal | Aprovado | `substr(occurredOn, 1, 7)` e testes instrumentados |
| Filtros inclusivos | Aprovado | `BETWEEN`, testes de limites e filtros |
| Períodos vazios | Aprovado | `COALESCE`, listas vazias e testes |
| Percentual e arredondamento | Aprovado | `BigDecimal`, `HALF_UP`, testes de domínio |
| Orçamento zero | Aprovado | Percentual `null` e testes instrumentados |
| Interface integrada | Aprovado | `ReportScreen`, `ReportViewModel` e navegação localizada |
| Estados de tela | Aprovado | Estados no ViewModel e composição da tela |
| Regressão existente | Aprovado | 22 testes instrumentados, 0 falhas |
| Build e lint | Aprovado | `assembleDebug` e `lintDebug` |
| Testes instrumentados | Aprovado | 22 testes, 0 falhas |
| Testes JVM | Bloqueado | `testDebugUnitTest` = `NO-SOURCE` |

## F6-F — Resolução de pendências

### Investigação e correção dos testes JVM

- A causa do `NO-SOURCE` era estrutural: `app/src/test/kotlin/...` continha somente diretórios vazios; os testes existentes estavam corretamente no source set instrumentado `app/src/androidTest/kotlin/...`.
- A configuração já usava o source set convencional, `testImplementation(libs.junit)` e Kotlin integrado ao AGP 9; não havia source set personalizado nem dependência ausente.
- Foi criado somente o teste JVM real para a lógica independente de Android em `app/src/test/kotlin/br/com/paulodiogo/orcamento/domain/FinancialCalculationsTest.kt`, mantendo pacote correspondente e os testes de percentual, orçamento zero, saldo negativo e arredondamento.
- A investigação reproduziu a falha histórica: a classe foi compilada em `app/build/intermediates/built_in_kotlinc/debugUnitTest/compileDebugUnitTestKotlin/classes`, mas o runner falhou com `ClassNotFoundException`. O problema não era localização física do arquivo; a tentativa de contorno em configuração foi descartada e nenhuma configuração/versionamento foi alterada.
- Após a correção de localização, a execução atual ainda falhou com `ClassNotFoundException`; portanto, a descoberta física foi corrigida, mas a integração runner AGP 9/Kotlin built-in permanece bloqueada e não há aprovação dos testes JVM.

### Cobertura complementar

- `ReportScreenTest` cobre a renderização inicial do título, seletor de mês e ação de retorno.
- Alteração de mês, dados persistidos, orçamento zero, saldo negativo e falha de carregamento permanecem sem cobertura de UI automatizada dedicada.
- Os testes de domínio e Room existentes cobrem as regras financeiras e agregações correspondentes, mas não substituem a cobertura de fluxo da tela.

### Critérios de encerramento

| Critério | Estado | Evidência |
|---|---|---|
| Consultas e agregações Room | Aprovado | 22 testes instrumentados sem falhas |
| Cálculos percentuais e períodos | Aprovado | Testes de domínio instrumentados e `BigDecimal` |
| Interface e navegação | Aprovado | Build, lint e teste Compose básico |
| Regressão instrumentada | Aprovado | 22 testes, 0 falhas |
| Testes JVM | Bloqueado | `NO-SOURCE`; tentativa anterior com `ClassNotFoundException` |
| Build e lint | Aprovado | `assembleDebug` e `lintDebug` |
| Pendências da Fase 05 | Bloqueado | Validação manual completa não realizada |

A F6-F foi concluída. A Fase 06 permanece parcialmente validada; não iniciar a Fase 07 sem decisão explícita sobre os critérios bloqueados.

