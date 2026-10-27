# Validação — Fase 03

**Planejada:** testes de domínio, build, lint e regressão instrumentada.

## Execução

- `gradlew.bat connectedDebugAndroidTest` — primeira execução encontrou uma expectativa de overflow incorreta; teste ajustado para somar dois valores máximos.
- `gradlew.bat connectedDebugAndroidTest` — **sucesso** no BUSAO: 7 testes, 0 falhas, 0 erros.
- `gradlew.bat assembleDebug` — **sucesso**.
- `gradlew.bat lintDebug` — **sucesso**, sem erros.

Coberturas principais: cenário financeiro de referência, saldo negativo, orçamento zero, valores inválidos, datas inválidas, overflow, tags sem hierarquia, normalização case-insensitive, zero tags e total único com múltiplas tags. A Fase 04 não foi iniciada.

