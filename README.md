# Orçamento Mensal

Aplicativo Android para registrar despesas e acompanhar o orçamento mensal, com organização e acompanhamento dos gastos por tags. É voltado a pessoas que querem visualizar despesas, saldo e consumo dos limites definidos sem depender de um serviço de sincronização.

> **Estado:** projeto em desenvolvimento. O código inclui cadastro e edição de despesas, tags, orçamento mensal e acompanhamento de orçamento por tag. Os registros do projeto marcam a Fase 16 — consumo mensal por tag na Home e em Relatórios — como implementada e validada tecnicamente; isso não significa que todas as etapas futuras ou validações manuais estejam concluídas.

## Funcionalidades

- Registrar, editar e excluir despesas, com descrição, valor e data.
- Pesquisar lançamentos por descrição e filtrá-los por tag ou pela ausência de tag.
- Criar, editar e excluir tags e associar uma despesa a nenhuma, uma ou várias tags.
- Definir um orçamento global por mês; ao abrir um novo mês, o valor do mês anterior pode ser sugerido.
- Definir um orçamento opcional por tag.
- Consultar na Home um resumo do mês atual no dispositivo: total gasto, saldo e consumo do orçamento, além do resumo por tag.
- Consultar relatórios de um mês selecionado e comparar despesas com o orçamento global e os orçamentos das tags.
- Acompanhar o percentual e o estado dos orçamentos por tag; gastos acima do limite recebem destaque visual.
- Persistir os dados em banco local Room.

## Regras e limitações importantes

- Valores monetários são armazenados em centavos inteiros. Cada lançamento de despesa é contabilizado uma vez no total mensal.
- Tags classificam despesas: quando uma despesa possui várias tags, seu valor integral aparece no total de cada tag. Portanto, somar os subtotais das tags pode contar a mesma despesa mais de uma vez; isso não altera o total global.
- O orçamento por tag é o valor configurado atualmente, não um histórico versionado por mês. Seu consumo é calculado para o mês consultado.
- Tag sem orçamento é exibida como **Sem orçamento**, sem percentual ou barra de consumo. Orçamento igual a zero não gera percentual numérico; gasto positivo é indicado como acima do orçamento.
- Percentuais são arredondados para duas casas apenas para exibição. A barra visual não ultrapassa 100%, mas o percentual apresentado mantém o valor real, inclusive acima de 100%.
- A Home usa o mês atual local e não oferece seletor de mês. Relatórios e configuração do orçamento permitem escolher o mês.
- O aplicativo atualmente trata despesas; não documenta receitas, sincronização, exportação, importação ou backup de dados.

## Tecnologias

| Tecnologia | Uso / versão configurada |
|---|---|
| Kotlin | 2.2.10 |
| Android Gradle Plugin | 9.4.1 |
| Gradle Wrapper | 9.6.0 |
| Android SDK | `compileSdk` 37, `targetSdk` 36 e `minSdk` 28 |
| Jetpack Compose e Material 3 | Interface; versão Compose definida pelo BOM 2026.09.00 |
| Kotlin Coroutines e Flow | Operações assíncronas e observação reativa de dados |
| Room | Persistência SQLite, versão 2.8.5 |
| KSP | Processamento de anotações, versão 2.3.12 |
| JUnit | Testes JVM, versão 4.13.2 |
| AndroidX Test e Compose UI Test | Testes instrumentados; versões gerenciadas pelo catálogo Gradle |
| JDK | Toolchain 17 |

As versões são as declaradas no [catálogo Gradle](gradle/libs.versions.toml), no [build do app](app/build.gradle.kts) e no [Gradle Wrapper](gradle/wrapper/gradle-wrapper.properties).

## Organização do projeto

O repositório contém um módulo Android, `:app`, com uma Activity e telas Compose. Os ViewModels mantêm o estado da interface e usam operações de domínio e consultas Room; os fluxos observados permitem atualizar os resumos quando os dados relacionados mudam.

```text
app/src/main/kotlin/br/com/paulodiogo/orcamento/
├── data/       # Entidades, DAOs e banco Room
├── domain/     # Operações, validações, cálculos e ViewModels
├── AppShell.kt # Navegação e composição das telas
├── MainActivity.kt
├── BudgetScreen.kt
├── ExpenseForm.kt
├── ReportScreen.kt
└── TagManagementScreen.kt
app/src/main/res/       # Recursos e strings
app/src/test/           # Testes JVM
app/src/androidTest/    # Testes instrumentados Android/Compose
```

Documentação complementar: [regras de negócio](docs/BUSINESS_RULES.md) e [modelo de dados](docs/DATA_MODEL.md).

## Requisitos para desenvolvimento

- JDK 17.
- Android SDK configurado com a plataforma necessária para compilar o `compileSdk` 37.
- Para executar o app, dispositivo ou emulador Android com API 28 ou superior.
- O repositório inclui Gradle Wrapper. Os comandos abaixo são para Windows PowerShell; em macOS/Linux, use `./gradlew` no lugar de `./gradlew.bat`.

## Clonar, compilar e executar

Substitua o marcador pela URL pública do repositório quando ela estiver definida. Essa URL não é presumida neste documento.

```powershell
git clone <URL_PUBLICA_DO_REPOSITORIO> app-orcamento
cd app-orcamento
.\gradlew.bat assembleDebug
```

Para executar, abra o projeto no Android Studio, selecione um dispositivo ou emulador com API 28 ou superior e inicie a configuração `app`. O Gradle gera a variante de depuração; o caminho usual do APK é `app/build/outputs/apk/debug/app-debug.apk`.

## Testes e verificações

Os testes JVM cobrem regras de domínio, filtros, formatação monetária e campos de data. Os testes instrumentados cobrem telas Compose, operações, persistência e banco Room. Com um dispositivo ou emulador conectado, os comandos disponíveis incluem:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

Esses comandos são instruções para executar as verificações; este README não afirma que foram executados nesta revisão.

## Dados e privacidade

Despesas, tags e orçamentos são persistidos localmente pelo Room no armazenamento do aplicativo. O manifesto Android não declara a permissão `INTERNET`, e não foi localizada integração com serviço remoto no fluxo inspecionado. Isso não é uma auditoria formal de segurança, não comprova criptografia específica e não deve ser entendido como garantia absoluta de privacidade.

## Contribuições

Contribuições podem ser propostas por pull request:

1. Crie um fork e uma branch para a alteração.
2. Faça a mudança mantendo o escopo e a arquitetura existentes.
3. Execute os testes e verificações pertinentes.
4. Abra um pull request com a motivação e um resumo do que foi alterado.

## Licença

Este repositório não contém um arquivo de licença. A licença ainda precisa ser definida antes de apresentar o projeto como formalmente licenciado como código aberto. A publicação no GitHub, por si só, não concede autorização para reutilizar o código.
