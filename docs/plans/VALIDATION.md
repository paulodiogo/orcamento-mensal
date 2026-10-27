# Registro de Validação

## Fase 13 — 2026-10-27 — seleção de tags — concluída e aprovada

- `rtk .\gradlew.bat testDebugUnitTest` — sucesso.
- `rtk .\gradlew.bat assembleDebug` — sucesso.
- `rtk .\gradlew.bat lintDebug` — sucesso.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`.
- `rtk .\gradlew.bat ktlintCheck` — tarefa inexistente; não aplicável.
- `rtk git diff --check` — sucesso.
- APK atual reinstalado com `adb install -r`; a primeira captura mostrou tela preta e `uiautomator dump` retornou `ERROR: null root node returned by UiTestAutomationBridge`.
- `adb reboot` foi executado sem limpar dados. Após reinício, `MainActivity` renderizou; `uiautomator dump` identificou a árvore Compose, formulário, modal, campo de pesquisa, tag disponível e ações. A pesquisa `zzzzz` exibiu `Nenhuma tag corresponde à pesquisa.`.
- Logs não apresentaram exceção não tratada do aplicativo, Compose, Room ou SQLite. A evidência aponta falha transitória de renderização/UiAutomation do emulador, recuperada pelo reboot, sem correção de código.

A Fase 13 foi encerrada e aprovada por decisão administrativa final. Os cenários complementares sem evidência manual individual foram dispensados de novas verificações; essa dispensa não representa execução desses cenários.

## Correção de sincronização após criação de despesa — 2026-10-27

- `rtk .\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — sucesso em 9s; testes JVM, build debug e lint concluídos sem falhas.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no BUSAO (AVD) - 16; testes instrumentados concluídos sem falhas. Houve apenas warnings de depreciação preexistentes em `createComposeRule`.
- Validação manual ainda não executada para esta correção.


## Fase 11 — 2026-10-27 — entrada monetária

- `rtk .\\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — sucesso: build e lint aprovados; 23 testes JVM, 0 falhas/erros/ignorados.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — sucesso no `emulator-5554`; 27 testes instrumentados, 0 falhas/erros/ignorados.
- `rtk git diff --check` — sucesso.
- Inspeção real do APK debug: `123456` exibido como `R$ 1.234,56`; `dumpsys input_method` reportou `inputType=2` e IME solicitado (`mInputShown=true`). O AVD não mostrou o keypad visual na captura; a solicitação de teclado numérico foi confirmada também pelos testes instrumentados.
- `show_ime_with_hard_keyboard` foi verificado/restaurado para o valor inicial `0`. Sem mudanças de esquema/migrações Room, dependências, versões, branch ou commits.
- Registro detalhado, incluindo correções intermediárias de testes e warnings não bloqueantes, em `docs/phases/phase-11-money-input/VALIDATION.md`.

## Fase 10 — 2026-10-27 — correções finais de UX

- `rtk .\\gradlew.bat assembleDebug lintDebug testDebugUnitTest connectedDebugAndroidTest` — sucesso; build, lint, testes JVM e instrumentados no BUSAO (AVD) - 16 passaram.
- `rtk git diff --check` — sucesso.
- APK instalado e iniciado com `adb`; capturas legíveis examinadas: `app/build/final-navigation-readable.jpg` e `app/build/final-history-readable.jpg`.
- Navegação para `Lançamentos` e abertura do formulário foram executadas manualmente. O preenchimento manual não foi concluído por coordenadas inválidas após alteração da geometria do teclado; nenhum CRUD foi declarado aprovado.
- Drawables vetoriais locais adicionados para navegação e adição; nenhuma dependência foi alterada.
- Escopo confirmado pelo usuário: somente despesas e tags sem limites financeiros por tag; receitas e limites por tag estão fora do escopo e não são pendências.

## Fases 08 e 09 — 2026-10-27

- Implementado e testado o fluxo de orçamento mensal: abertura, sugestão do mês anterior, edição sem duplicação e histórico isolado.
- `rtk .\\gradlew.bat testDebugUnitTest` — sucesso; 15 testes JVM, 0 falhas, 0 erros, 0 ignorados.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — sucesso no BUSAO (AVD) - 16; 15 testes, 0 falhas, 0 erros, 0 ignorados.
- `rtk .\\gradlew.bat assembleDebug lintDebug assembleRelease` — sucesso.
- `rtk git diff --check` — sucesso.
- `ktlintCheck` — não aplicável, tarefa inexistente.
- Release gerado como `app-release-unsigned.apk`; não há configuração de assinatura e nenhuma credencial foi criada.

A Fase 08 está concluída. A Fase 09 permanece parcialmente concluída até decisão autorizada sobre a assinatura do release.

## Fase 07 — 2026-10-27

- `rtk .\\gradlew.bat testDebugUnitTest --stacktrace` — **sucesso**. Os XMLs de `app/build/test-results/testDebugUnitTest` registram 15 testes efetivamente executados (DomainRules 8, ExpenseFilters 3, FinancialCalculations 4), 0 falhas, 0 erros e 0 ignorados. Não foi `NO-SOURCE`.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — **sucesso** no BUSAO (AVD) - 16; XML registra 12 testes, 0 falhas, 0 erros e 0 ignorados.
- `rtk .\\gradlew.bat assembleDebug lintDebug` — **sucesso** para ambos.
- `rtk .\\gradlew.bat assembleRelease` — **sucesso**, gerando `app-release-unsigned.apk`; a configuração não possui assinatura release, sem alteração de credenciais.
- `rtk .\\gradlew.bat ktlintCheck` — **não aplicável/falhou por tarefa inexistente**; nenhuma configuração ktlint foi encontrada.
- `rtk git diff --check` — **sucesso**.
- `rtk adb devices` — dispositivo `emulator-5554` conectado.

Limitações: validação manual completa da Fase 05, evidência dedicada de RF-003 e cobertura complementar de UI permanecem pendentes. A compilação release não comprova artefato pronto para distribuição porque o APK é unsigned.

A investigação complementar de 2026-10-27 reproduziu `rtk .\gradlew.bat cleanTest testDebugUnitTest --stacktrace --info` com código 1. Um controle adicional executou exatamente a mesma build por uma junction NTFS ASCII (`C:\orcamento-test` apontando para o checkout original) e terminou com `BUILD SUCCESSFUL` em 12s. Essa diferença reproduzível demonstra que o problema não é descoberta JUnit nem ausência do `.class`: o caminho original contém `Ç` (`APP_ORÇAMENTO`) e o classloader/worker do AGP 9.4.1/Gradle 9.6 perde a classe quando o classpath passa pelo caminho Unicode; o caminho ASCII equivalente executa os testes. O log do caminho original mostra o worker Gradle iniciando e recebendo um classpath que contém `app/build/intermediates/built_in_kotlinc/debugUnitTest/compileDebugUnitTestKotlin/classes`; a exceção mais interna continua sendo `java.lang.ClassNotFoundException` em `JUnitTestExecutor.shouldRunTestClass`, antes de qualquer método de teste, e o relatório registra uma classe/falha e zero métodos executados. `javap` continua carregando o mesmo `.class`, que declara os quatro métodos JUnit esperados. A configuração efetiva de `testDebugUnitTest` usa o runner JUnit local/worker Gradle, enquanto `connectedDebugAndroidTest` usa `AndroidJUnitRunner` no dispositivo; portanto, os runners não foram confundidos. O daemon e o worker executaram com Microsoft JDK 17.0.15; `gradle/gradle-daemon-jvm.properties` seleciona Java 17. A documentação oficial do AGP 9.4 confirma Gradle 9.6 e JDK 17, e a documentação do Gradle 9.6 exige `testClassesDirs` e `classpath`, ambos presentes no diagnóstico anterior. Um teste Java mínimo já havia reproduzido a mesma falha e foi removido. Não foi identificada correção de baixo risco que não altere a integração do build; nenhuma mudança estrutural foi aplicada. A causa demonstrada é uma falha do classloader do worker local ao resolver a classe a partir da saída `built_in_kotlinc`, não uma falha de compilação, descoberta de nome ou incompatibilidade do JDK. A causa interna desse classloader permanece não publicada/comprovada nesta versão do AGP 9.4.1.

A investigação final F6-F de 2026-10-27 confirmou que `FinancialCalculationsTest` está em `app/src/test/kotlin/br/com/paulodiogo/orcamento/domain`, declara o pacote e a classe esperados, e é compilada por `compileDebugUnitTestKotlin` em `app/build/intermediates/built_in_kotlinc/debugUnitTest/compileDebugUnitTestKotlin/classes`. `javap -classpath <diretório-de-classes>` leu a classe Kotlin corretamente (major version 61/JDK 17 e quatro métodos de teste). O diagnóstico temporário imprimiu esse diretório em `testClassesDirs` e no classpath de `:app:testDebugUnitTest`, mas `cleanTest testDebugUnitTest --stacktrace --info` ainda falhou: o worker iniciou, selecionou uma classe e não executou método algum, terminando em `ClassNotFoundException`. Um teste Java JUnit temporário, compilado em `app/build/intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes` e também legível por `javap` (major version 61), falhou com a mesma exceção; ele foi removido após o diagnóstico. Portanto, a falha é geral ao classloader do worker, não específica de Kotlin ou de `FinancialCalculationsTest`. A evidência mais interna disponível era a exigência de Java 25 em `gradle/gradle-daemon-jvm.properties`, enquanto a compatibilidade oficial do AGP 9.4 fixa JDK 17 e o projeto declara `jvmToolchain(17)`. Não foi encontrada, na documentação oficial de Kotlin integrado/AGP 9.4, uma limitação publicada que exija configurar o runner manualmente. Com autorização, o mecanismo oficial `updateDaemonJvm --jvm-version=17 --no-native-image-capable --rerun` atualizou somente o arquivo gerado de critérios do daemon, preservando Gradle 9.6, AGP 9.4.1, Kotlin 2.2.10, plugins, dependências e source sets. O JDK 17 Microsoft 17.0.15 estava instalado e disponível. A validação solicitada foi executada com o daemon selecionando critérios Java 17, mas `cleanTest testDebugUnitTest --stacktrace --info` continuou falhando com a mesma `ClassNotFoundException`; o relatório registra 1 classe/falha e 0 métodos executados. A execução foi interrompida conforme a condição de parada; `assembleDebug`, `lintDebug` e `git diff --check` não foram executados nesta etapa. A próxima hipótese mínima é um defeito/incompatibilidade na integração do classpath do worker de testes com a saída `built_in_kotlinc` do AGP 9, não mais o JDK do daemon. A cobertura Compose básica da tela foi adicionada. A Fase 06/F6-E foi executada em 2026-10-27: `connectedDebugAndroidTest` passou no BUSAO (AVD) - 16 com 22 testes, 0 falhas e 0 ignorados; `assembleDebug`, `lintDebug` e `git diff --check` passaram. A F6-E corrigiu o cálculo do fim do mês com `YearMonth.atEndOfMonth()`. A Fase 06/F6-D foi implementada em 2026-10-27 com tela de relatórios, `ReportViewModel`, seleção mensal, resumo e detalhamento por tag. A Fase 06/F6-C foi validada em 2026-10-27 com cálculo percentual determinístico e regras de intervalo. A Fase 06/F6-B foi validada em 2026-10-27 com consultas Room e teste instrumentado determinístico. A Fase 01 foi validada com Gradle `9.6.0` e AGP `9.4.1`. A Fase 02 foi validada com Kotlin `2.2.10`, KSP `2.3.12`, Room `2.8.5` e testes instrumentados no BUSAO. A Fase 03 foi validada com 7 testes instrumentados sem falhas. A Fase 04 foi validada com 10 testes instrumentados sem falhas, `assembleDebug` e `lintDebug` aprovados. A Etapa 1 da Fase 05 foi validada em 2026-10-27 com `connectedDebugAndroidTest`: BUSAO (AVD) - 16, 12 testes, 0 falhas e 0 erros; `assembleDebug` e `lintDebug` passaram. A Etapa 2 foi validada no mesmo dispositivo com 14 testes instrumentados, 0 falhas e 0 erros; `assembleDebug` e `lintDebug` passaram. A Etapa 3 teve as funcionalidades previstas implementadas: `assembleDebug`, `lintDebug` e `connectedDebugAndroidTest` passaram; os testes instrumentados finais totalizaram 16, incluindo 1 teste Compose e 1 teste de filtros. `testDebugUnitTest` retornou `NO-SOURCE`. A Etapa 4 foi validada parcialmente em 2026-10-27: `testDebugUnitTest` retornou `NO-SOURCE`; `assembleDebug`, `lintDebug` e `connectedDebugAndroidTest` passaram; o APK foi instalado e a atividade principal iniciada no BUSAO API 36 sem `FATAL EXCEPTION` observado. A navegação manual completa de lançamentos e filtros não foi executada.
