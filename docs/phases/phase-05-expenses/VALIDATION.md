# Validação — Fase 05

## Estratégia planejada

- **Unitários:** validação de descrição, data e centavos; inclusão retroativa; edição entre meses; seleção zero/uma/várias tags; filtros e totalização sem dupla contagem.
- **Room/instrumentados:** CRUD, reinício/persistência, associação opcional e múltipla, reuso/criação de tags, exclusão confirmada e consultas combináveis.
- **Compose/instrumentados:** formulário válido e inválido, lista geral, filtros, vazio, erro, limpar filtros e confirmação de exclusão.
- **Regressão:** cenários aprovados das Fases 02–04.
- **Projeto:** `gradlew.bat assembleDebug`, `gradlew.bat lintDebug` e `gradlew.bat connectedDebugAndroidTest`, quando o dispositivo/emulador estiver disponível.

## Resultados da Etapa 1

- Baseline: `rtk .\gradlew.bat testDebugUnitTest lintDebug assembleDebug` — sucesso; não havia testes unitários JVM (`NO-SOURCE`), lint e build concluíram.
- Produção: `rtk .\gradlew.bat lintDebug assembleDebug` — sucesso.
- Instrumentados: `rtk .\gradlew.bat connectedDebugAndroidTest` — BUSAO (AVD) - 16, 12 testes, 0 falhas, 0 erros, 0 ignorados.
- Evidência: `app/build/outputs/androidTest-results/connected/debug/TEST-BUSAO(AVD) - 16.xml`, incluindo 2 testes de `ExpenseOperationsTest` e regressão de BudgetDatabase, tags e regras de domínio.

## Resultados da Etapa 2

- Baseline histórica desta execução: `rtk .\gradlew.bat connectedDebugAndroidTest lintDebug assembleDebug` — sucesso, BUSAO (AVD) - 16, 12 testes antes das alterações da Etapa 2.
- Validação final: `rtk .\gradlew.bat connectedDebugAndroidTest lintDebug assembleDebug` — sucesso, BUSAO (AVD) - 16, 14 testes, 0 falhas, 0 erros, 0 ignorados.
- `ExpenseOperationsTest`: 4 testes aprovados, incluindo criação com múltiplas tags, leitura, remoção total, referências inexistentes e preservação atômica.
- `lintDebug` e `assembleDebug`: sucesso.
- `git diff --check`: sucesso.

## Resultados da Etapa 3

- Baseline: `rtk .\gradlew.bat connectedDebugAndroidTest lintDebug assembleDebug` — sucesso, BUSAO (AVD) - 16, 14 testes, 0 falhas e 0 erros.
- Após a implementação: `rtk .\gradlew.bat assembleDebug lintDebug` — a primeira execução falhou por argumento `modifier` duplicado em `MainActivity.kt`; correção única aplicada.
- Reexecução: `rtk .\gradlew.bat assembleDebug lintDebug` — sucesso.
- Regressão instrumentada: `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso, BUSAO (AVD) - 16, 14 testes, 0 falhas, 0 erros, 0 ignorados.
- `git diff --check`: sucesso.

A Etapa 3 foi concluída: listagem, pesquisa, estados básicos, criação, edição, seleção de tags, filtros por mês/período/tag/sem tag e exclusão confirmada estão disponíveis.

## Resultados da Etapa 4 — regressão final

- Preparação: `rtk git status --short`, `rtk git diff --name-only`, `rtk git branch --show-current` e `rtk git diff --check` executados; a árvore já continha alterações preexistentes, a branch permaneceu `master` e não houve erro de whitespace.
- `rtk .\gradlew.bat testDebugUnitTest assembleDebug lintDebug connectedDebugAndroidTest` — sucesso; `testDebugUnitTest` retornou `NO-SOURCE` (nenhum teste JVM executado), `assembleDebug` e `lintDebug` passaram, e os testes instrumentados executaram no `BUSAO(AVD) - 16` com sucesso. O relatório instrumentado existente registra 16 testes, 0 falhas, 0 erros e 0 ignorados.
- Persistência e domínio: os testes instrumentados existentes cobrem CRUD sem tags, múltiplas associações, remoção total de tags, rejeição de referência inexistente e preservação após entrada inválida; `ExpenseFiltersTest` cobre mês e intervalo de datas. Não foram encontrados cálculos monetários em `Float`/`Double` nos fluxos validados.
- Execução real: `rtk adb devices` confirmou `emulator-5554` conectado; `rtk adb install -r .\app\build\outputs\apk\debug\app-debug.apk` retornou `Success`; a atividade principal foi iniciada via `adb shell monkey`. A API reportada foi 36 e os logs consultados não apresentaram `FATAL EXCEPTION` relacionado ao pacote durante a inicialização. A navegação manual completa de todos os fluxos não foi executada; a cobertura de UI disponível permanece a dos testes Compose/instrumentados existentes.
- Correções: nenhuma correção de código foi necessária nesta etapa; a correção preexistente de `ExpenseListViewModel` foi preservada.

## Validação adicional das pendências

- `assembleDebug lintDebug` — sucesso após inclusão das dependências de teste Compose no catálogo/BOM.
- `connectedDebugAndroidTest` — primeira execução falhou no teste Compose por uma asserção inadequada; a asserção foi corrigida e a reexecução passou com 16 testes, 0 falhas e 0 erros.
- Testes instrumentados finais: `MainActivityComposeTest` (1), `ExpenseFiltersTest` (1), regressão existente (14), total de 16 testes instrumentados, 0 falhas, 0 erros e 0 ignorados.
- `testDebugUnitTest` — `NO-SOURCE`; nenhum teste JVM foi executado.
- `git diff --check` — sucesso.

