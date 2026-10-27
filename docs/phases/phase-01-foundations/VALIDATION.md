# Validação — Fase 01

**Planejada:** `assembleDebug`, `lintDebug` e testes mínimos previstos.

## Execução

- `gradlew.bat assembleDebug` com Gradle `9.6.0` e AGP `9.4.1` — **sucesso**, APK `app-debug.apk` gerado.
- `gradlew.bat lintDebug` com Gradle `9.6.0` — **sucesso**, sem erros.
- `gradlew.bat testDebugUnitTest` com Gradle `9.6.0` — **sucesso**, sem testes (`NO-SOURCE`).

Gradle `9.6.0` e AGP `9.4.1` estão configurados como versões oficiais. O aviso de stripping da biblioteca nativa `libandroidx.graphics.path.so` não impediu o empacotamento. Não foram executados testes instrumentados nem instalação em dispositivo.

