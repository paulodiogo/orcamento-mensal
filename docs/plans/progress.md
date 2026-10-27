# Progresso

## Fase 13 — redesign da seleção de tags e sincronização reativa — concluída e aprovada

Autorizada em 2026-10-27. `TagSelector` modal Material 3 foi integrado ao formulário compartilhado de cadastro/edição, com pesquisa local, seleção múltipla temporária, aplicação/cancelamento, resumo compacto, lista rolável e encaminhamento para gerenciamento quando não há tags. IDs removidos da lista são descartados da seleção local. Modelo Room, operações transacionais, regras e dependências permanecem inalterados.

Validação técnica: `testDebugUnitTest`, `assembleDebug`, `lintDebug`, `connectedDebugAndroidTest` e `git diff --check` passaram após a correção. Os testes instrumentados foram executados no `BUSAO(AVD) - 16`. O APK foi instalado no `emulator-5554`, a atividade iniciou e `uiautomator dump` concluiu com sucesso; não houve `FATAL EXCEPTION` da aplicação. `ktlintCheck` não existe no projeto. A investigação confirmou que `TagDao.findAll()` era uma fotografia independente nos ViewModels; a correção adiciona `TagDao.observeAll(): Flow<List<TagEntity>>` compartilhado por gerenciamento, seletor e filtros. A validação funcional direcionada foi executada no BUSAO sem reiniciar o aplicativo: `VALIDACAO_TAG_2710` foi criada, apareceu no seletor mediante pesquisa, foi associada à `DESPESA_VALIDACAO`, apareceu nos filtros, filtrou a despesa e teve o filtro removido com restauração da listagem. Evidências XML foram salvas em `app/build/phase13-*.xml`. Pela decisão final de encerramento, os cenários complementares sem evidência individual — renomeação, remoção, múltiplas tags, edição detalhada, lista vazia e temas — foram dispensados de novas verificações, sem serem declarados testados.

## Correção de sincronização após criação de despesa — implementada; validação técnica pendente

Causa identificada: `ExpenseDao` expunha apenas consultas `suspend`, e `ExpenseListViewModel`/`ReportViewModel` carregavam dados somente por chamadas explícitas. Após `create`, a lista era recarregada, mas o resumo da tela principal permanecia no `ReportViewModel` sem invalidação, portanto não observava a alteração do Room.

A correção adiciona `ExpenseDao.observeAll(): Flow<List<ExpenseEntity>>`, mantém a filtragem da lista derivada desse fluxo e faz o relatório mensal observar o mesmo fluxo, recalculando total, saldo disponível e totais por tag após inserção, edição ou exclusão. O fluxo transacional de persistência e o comportamento para outros meses permanecem preservados.

Validação técnica: `rtk .\gradlew.bat testDebugUnitTest assembleDebug lintDebug` passou em 9s; testes JVM, build de debug e lint concluídos sem falhas. Testes instrumentados e validação manual ainda pendentes.

**Estado:** implementação concluída e tecnicamente validada em JVM/build/lint; instrumentação e validação manual permanecem pendentes.

## Fase 12 — datas e DatePicker — implementação parcial

Implementado componente reutilizável em `DateFields.kt` com apresentação brasileira, DatePicker Material 3 sem conversão UTC de data civil, confirmação/cancelamento e seletor mensal simples. Novo cadastro usa `LocalDate.now()`, edição preserva `occurredOn`, filtros De/Até permanecem vazios e orçamento/relatório usam `YearMonth.now()` sem redefinição por recomposição. Não houve mudança em Room, dependências, versões ou regras de negócio.

Validação: `testDebugUnitTest assembleDebug lintDebug` passou; `connectedDebugAndroidTest` passou com 27 testes; APK debug instalado e atividade iniciada no emulador. A validação manual interativa dos fluxos de calendário, filtros e navegação ainda não foi concluída. Detalhes em `docs/phases/phase-12-date-picker/VALIDATION.md`.

**Estado:** implementação parcial; não declarar fase concluída até completar a validação manual e revisar os critérios pendentes.

## Fase 11 — entrada monetária — implementada e validada localmente

Implementados o formatador/conversor compartilhado e `MoneyTextField` para cadastro/edição de despesas e orçamento mensal. O campo utiliza dígitos com centavos implícitos, máscara brasileira, centavos `Long`, colagem sanitizada, cursor/seleção e teclado numérico; despesas continuam positivas e orçamento aceita zero. Não houve mudança no esquema Room, regras financeiras, dependências ou versões.

Validação final: `testDebugUnitTest assembleDebug lintDebug` passou com 23 testes JVM; `connectedDebugAndroidTest` passou com 27 testes no `emulator-5554`; `git diff --check` passou. Inspeção real confirmou `123456` como `R$ 1.234,56` e `inputType=2`. A solicitação do IME foi verificada, embora a configuração do AVD não tenha exibido o keypad visual na captura. Registro detalhado em `docs/phases/phase-11-money-input/VALIDATION.md`.

**Estado:** Fase 08 concluída; Fase 09 parcialmente concluída; Fase 10 parcialmente implementada e validada tecnicamente.

A Fase 10 avançou com tela dedicada de gerenciamento de tags, novo ViewModel de tags, relatórios com cartões e barras de distribuição e refinamento do orçamento mensal com hierarquia visual, estados vazios e confirmação explícita das sugestões. O modelo Room, regras financeiras e contratos existentes foram preservados. `assembleDebug`, `lintDebug`, `testDebugUnitTest`, `connectedDebugAndroidTest` e `git diff --check` passaram após correções de compatibilidade textual nos rótulos esperados por testes existentes. O APK foi instalado e iniciado no `emulator-5554`.

Inspeção visual efetiva: `app/build/redesign-final-home-readable.jpg` e `app/build/redesign-screens-contact-sheet.jpg` foram abertas e examinadas. A tela inicial mostrou boa hierarquia, espaçamento consistente, valores legíveis e navegação sem conteúdo cortado. A composição confirmou Relatórios, Orçamento e Tags. A captura do histórico selecionou a aba errada e a tentativa de captura do cadastro não abriu o diálogo; esses fluxos permanecem sem evidência visual manual completa.

A navegação foi refinada com drawables vetoriais locais para Início, Despesas, Relatórios, Orçamento, Tags e adição, sem dependências novas. `final-navigation-readable.jpg` e `final-history-readable.jpg` foram abertas e examinadas; a tela de histórico foi confirmada visualmente. O formulário foi aberto e examinado, mas o preenchimento manual falhou por uso de coordenadas presumidas após mudança de geometria do teclado, portanto CRUD, tags, orçamento mensal e persistência não foram declarados como executados manualmente.

Escopo confirmado pelo usuário: o aplicativo trabalha somente com despesas e tags sem limites financeiros por tag; receitas e limites por tag não fazem parte da Fase 10 e não são pendências. Permanecem pendentes apenas a validação manual interativa completa de CRUD, orçamento, relatórios e persistência. Não foi realizada auditoria de acessibilidade, pois está explicitamente fora do escopo.

Fases 01–07 permanecem preservadas conforme histórico. A Fase 08 adicionou o fluxo de abertura/edição de mês, sugestão do orçamento anterior, histórico mensal, estado vazio e tema claro/escuro do sistema. Testes instrumentados específicos cobrem isolamento e sugestão de orçamento.

Validação final executada: 15 testes JVM efetivamente executados e 15 testes instrumentados no BUSAO (AVD) - 16, todos sem falhas, erros ou ignorados; `assembleDebug`, `lintDebug`, `assembleRelease` e `git diff --check` passaram. Não existe tarefa `ktlintCheck` configurada.

A release foi compilada como `app-release-unsigned.apk`. Por decisão explícita, não existe configuração de assinatura e nenhuma chave/credencial foi inventada ou alterada. O APK debug foi instalado e iniciado no emulador; a UI exibiu `Lançamentos`, `Relatórios`, `Orçamentos` e `Nenhum lançamento`, sem `FATAL EXCEPTION` do aplicativo. A versão 1.0 está tecnicamente validada localmente, mas não é distribuível como APK assinado.
