# Validação — Fase 09

## Resultados reais — 2026-10-27

- `rtk .\\gradlew.bat testDebugUnitTest` — sucesso; 15 testes JVM efetivamente executados, 0 falhas, 0 erros, 0 ignorados.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — sucesso no BUSAO (AVD) - 16; 15 testes, 0 falhas, 0 erros, 0 ignorados.
- `rtk .\\gradlew.bat assembleDebug lintDebug assembleRelease` — sucesso.
- `rtk git diff --check` — sucesso.
- `ktlintCheck` — não aplicável; tarefa não configurada.

## Release e assinatura

O artefato `app/build/outputs/apk/release/app-release-unsigned.apk` foi gerado. A inspeção de `app/build.gradle.kts` não encontrou `signingConfigs`, `storeFile`, aliases ou senhas. Não é possível validar um APK assinado sem uma chave autorizada. Nenhum segredo foi inventado, exposto ou gravado no repositório. Publicação e distribuição não foram realizadas.

## Critérios

A regressão automatizada, persistência Room, isolamento mensal, sugestão/editabilidade do orçamento anterior, CRUD, tags, filtros, cálculos financeiros, estados de UI e tema foram validados no escopo coberto. O APK debug foi instalado com sucesso e iniciado no emulador; a hierarquia de acessibilidade confirmou `Lançamentos`, `Relatórios`, `Orçamentos` e `Nenhum lançamento`, e os logs não apresentaram `FATAL EXCEPTION` do aplicativo. Por decisão explícita, o release unsigned foi aceito como limitação: não é artefato distribuível assinado.

