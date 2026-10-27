# Validação — Fase 02

**Planejada:** testes DAO/Room e integridade.

## Investigação técnica

- AGP `9.4.1` exige Gradle mínimo/default `9.6.0`, JDK 17 e suporta API 37, conforme [notas oficiais do AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes).
- O Kotlin integrado ao AGP 9 elimina `org.jetbrains.kotlin.android`; a documentação oficial também informa que `org.jetbrains.kotlin.kapt` é incompatível e recomenda KSP ou `com.android.legacy-kapt`: [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin).
- A documentação oficial do KSP lista Room como suportado e, em 30/09/2026, informa KSP `2.3.10` com KGP `2.2.10–2.3.x`, AGP `8.12+`, Gradle `8.13+` e JDK `17+`: [KSP overview](https://kotlinlang.org/docs/ksp-overview.html). A release `2.3.12` também declara AGP mínimo `8.12.0`: [KSP releases](https://github.com/google/ksp/releases).
- Isso não comprova compatibilidade de KSP `2.3.12` com KGP/Kotlin `2.4.21`; a tentativa local falhou em `kspDebugKotlin` com `unexpected jvm signature V`.
- Room `2.8.5` possui runtime e compiler publicados no Google Maven. A investigação não alterou o projeto principal nem adotou essa versão.

## Experimentos isolados

O experimento fora do projeto principal usou AGP `9.4.1`, Gradle `9.6.0`, JDK 17, Kotlin `2.4.21` e Room `2.8.5`.

1. **KSP `2.3.12` + Room:** resolução ocorreu, mas o processamento falhou com `unexpected jvm signature V`.
2. **`com.android.legacy-kapt` + Room:** a configuração do plugin foi aceita, mas o processamento falhou com `Provided Metadata instance has version 2.4.0, while maximum supported version is 2.3.0`. Isso demonstra que Room `2.8.5` não é compatível com os metadados Kotlin 2.4.0 nessa combinação; não é apenas um problema da IDE.
3. O experimento não foi mantido no projeto principal. Não houve teste de persistência executável porque o processamento não gerou o código Room.

## Estado do problema da IDE

`gradlew.bat tasks --all` no projeto principal lista `prepareKotlinBuildScriptModel`; portanto, a ausência dessa tarefa não reproduz no Gradle Wrapper atual. O build básico também funciona. Se o Android Studio ainda reportar a tarefa ausente, a causa provável é sincronização/modelo Gradle em cache ou integração da versão da IDE, não ausência real no projeto. Reimportar o projeto e invalidar caches pode ser necessário, mas a investigação não altera configurações globais da IDE.

## Conclusão da investigação

A autorização recebida permitiu usar Kotlin `2.2.10`, dentro da faixa documentada pelo KSP `2.3.12`. A Fase 02 foi então implementada no projeto principal com Room `2.8.5` e KSP `2.3.12`.

- `gradlew.bat testDebugUnitTest` — **sucesso**, sem testes JVM aplicáveis (`NO-SOURCE`).
- `gradlew.bat assembleDebug` — **sucesso**; KSP processou as entidades e o APK foi gerado.
- `gradlew.bat lintDebug` — **sucesso**, sem erros.
- `gradlew.bat connectedDebugAndroidTest` — primeira execução com BUSAO conectado encontrou `StackOverflowError` no helper `runBlocking` dos testes; causa corrigida de forma focada.
- `gradlew.bat connectedDebugAndroidTest` — segunda execução falhou na compilação por ausência de JDK 17 detectável no processo Gradle; executado novamente com `JAVA_HOME` apontando para JDK 17.
- `gradlew.bat connectedDebugAndroidTest` com BUSAO e JDK 17 — **sucesso**. Resultado: 2 testes, 0 falhas, 0 erros, 0 ignorados.

A persistência Room e os testes instrumentados da fase foram validados no emulador BUSAO. A Fase 03 não foi iniciada.

