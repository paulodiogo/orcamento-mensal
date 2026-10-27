# Validação — Fase 11 — 2026-10-27

## Resultados finais

- `rtk .\\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — sucesso. Relatórios XML registram **23 testes JVM**, 0 falhas, 0 erros e 0 ignorados (8 MoneyFormatting, 8 DomainRules, 3 ExpenseFilters, 4 FinancialCalculations).
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — sucesso no `emulator-5554`/BUSAO (AVD) - 16. Relatório XML registra **27 testes instrumentados**, 0 falhas, 0 erros e 0 ignorados.
- Cobertura de UI inclui digitação progressiva com todos os exemplos, cursor no meio e posição após edição, backspace, colagem válida/inválida, overflow sem truncamento, erro corrigido, perda/retorno de foco, edição reaberta, despesa zero, bloqueio durante salvamento, persistência de `12.345` centavos e proteção contra request repetido.
- Cobertura do orçamento verifica exibição `123,45`, persistência exata em centavos e aceitação de zero pelas operações.
- Testes de formatação incluem vazio, zeros à esquerda, algarismos ASCII, milhares, máximo de `Long`, overflow, conversão inversa e rejeição de negativos.
- `rtk git diff --check` — sucesso.

## Inspeção real do emulador

- APK debug instalado e `MainActivity` iniciada em `emulator-5554`.
- Campo focado: `dumpsys input_method` reportou `currentEditorInfo packageName=br.com.paulodiogo.orcamento, inputType=2` (classe numérica) e `mInputShown=true`.
- Entrada de `123456` pelo emulador exibiu `R$ 1.234,56`; o prefixo permanece visual separado do texto editável.
- A configuração temporária do emulador `show_ime_with_hard_keyboard` foi restaurada ao valor inicial `0` após a inspeção.

## Falhas observadas e correções

- Primeira compilação de `testDebugUnitTest` falhou por imports de `KeyboardOptions` e `parseMoneyDigitsToCents`; imports corrigidos e reexecução passou.
- Primeira execução instrumentada teve falhas nos novos testes por tentar chamar `setContent` duas vezes e por buscar mensagens em nós semânticos separados; os testes foram adaptados ao ciclo de composição e à árvore semântica agregada.
- Uma execução instrumentada posterior apontou chamada de inspeção semântica sem sincronização; leitura movida para fora do bloco `runOnIdle` e reexecução passou.
- Depois, a compilação instrumentada identificou uso de `assertTextDoesNotContain`, indisponível na versão do Compose adotada; substituída por inspeção do valor semântico do nó e reexecução aprovada.
- Nenhum teste preexistente foi removido ou enfraquecido; as falhas foram de integração/asserção dos novos testes, não do comportamento de produção.

## Comandos finais executados

- `rtk .\\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — exit 0.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — exit 0, 27 testes.
- `rtk adb install -r .\\app\\build\\outputs\\apk\\debug\\app-debug.apk` — sucesso.
- `rtk adb shell am start -n br.com.paulodiogo.orcamento/.MainActivity` — atividade iniciada.
- `rtk adb shell input text 123456` — texto inserido no campo focado; captura mostrou `R$ 1.234,56`.
- `rtk git diff --check` — exit 0.

Warnings observados, sem falha: configuração AGP experimental `android.overridePathCheck=true` e aviso de depreciação da API `createComposeRule` nos testes Compose; não foi alterada configuração nem versão, e a API atual funcionou.
