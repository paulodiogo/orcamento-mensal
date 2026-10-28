# Progresso

## Redesign compacto da tela de orçamentos — implementado

A listagem de orçamentos removeu o texto redundante `Orçamento confirmado` e passou a usar uma linha compacta com mês, valor e ação `IconButton` de lápis alinhada à direita. A ação mantém a abertura do fluxo existente e expõe a descrição acessível específica `Editar orçamento de <mês>`, com área de toque Material de 48 dp. Não houve alteração de domínio, persistência, dependências ou regras de negócio. `	estDebugUnitTest` e `ssembleDebug lintDebug` passaram. O teste instrumentado específico passou após sincronizar explicitamente o `BudgetViewModel` com o orçamento persistido por meio de `iewModel.refresh()` antes da composição.

## Melhorias no gerenciamento de tags — implementadas e validadas tecnicamente

A listagem de tags agora exibe o saldo disponível no mês atual somente quando há orçamento configurado, calculado pela regra existente `orçamento da tag - gastos associados no mês`; tags sem orçamento continuam sem valor monetário. O fluxo reativo combina a listagem de tags com a consulta agregada Room de uso por tag. As ações passaram a usar área independente, descrições acessíveis específicas e destaque discreto pela cor de erro na exclusão. Testes instrumentados cobrem saldo com/sem orçamento e descrições acessíveis. `testDebugUnitTest assembleDebug lintDebug`, `connectedDebugAndroidTest` (49 testes, 0 falhas) e `git diff --check` passaram em 2026-10-28. Não houve alteração de esquema, dependências ou regras de contabilização.

## Tags padrão na primeira inicialização — implementadas e parcialmente validadas

`TagOperations.initializeDefaultTags` cria `Alimentação` e `Mercado` na mesma transação, reutiliza tags existentes sem sobrescrever dados e não cria registros financeiros. A execução é disparada no `MainActivity` após a recuperação do banco. Testes instrumentados cobrem inicialização, idempotência, criação parcial e preservação de dados. `testDebugUnitTest assembleDebug lintDebug` e `git diff --check` passaram. `connectedDebugAndroidTest` ficou bloqueado por ausência de dispositivo conectado.

## Auditoria e complementação de testes Data/Domain — 2026-10-28

Auditoria das 13 classes de produção em `domain` e 12 em `data`, dos testes JVM existentes e dos testes Room/instrumentados relacionados. A matriz resumida identificou lacunas unitárias em parsing decimal, validação de datas/meses, filtros por texto/tag/sem tag e estados/ordenação de execução de orçamento por tag. Foram adicionados cenários em `DomainRulesTest`, `ExpenseFiltersTest` e o novo `TagBudgetExecutionTest`. Repositórios, DAOs e persistência Room já possuem cobertura instrumentada relevante em `BudgetDatabaseTest`, `BudgetOperationsTest`, `TagOperationsTest` e `ExpenseOperationsTest`; por isso não foram duplicados em testes JVM que exigiriam Android/Room. Não foram identificados defeitos de produção.

Matriz antes/depois: validações monetárias e regras financeiras — parcial → ampliada; filtros — mês/período → texto/tag/sem tag também; orçamento por tag — cenários básicos em `DomainRulesTest` → estados, progresso, ordenação e entradas inválidas dedicados; repositórios/DAOs — cobertura Room instrumentada existente → mantida; backup — testes de manifesto/gate existentes → mantidos, SAF/restauração real continuam pendentes.

`testDebugUnitTest` passou antes e depois; `git diff --check` passou. Build, lint e testes instrumentados não foram repetidos porque não houve alteração de produção, persistência ou dependências.

## Mensagem de backup no topo da tela — implementada e validada tecnicamente

A mensagem de resultado foi movida para imediatamente abaixo da `TopAppBar`, antes do subtítulo e das ações, mantendo o cartão Material 3. `testDebugUnitTest assembleDebug lintDebug` e `git diff --check` passaram em 2026-10-28. Validação manual no dispositivo permanece pendente.

## Melhoria das mensagens de backup e restauração — implementada e validada tecnicamente

As mensagens de existência de backups e de conclusão/erro da recuperação agora usam cartões Material 3 com títulos, contagem, contraste e espaçamento consistentes. A mensagem de segurança também foi agrupada em cartão de alerta. `testDebugUnitTest assembleDebug lintDebug` passou em 2026-10-28. A aparência final e o fluxo em dispositivo permanecem sem validação manual.

## Correção do espaçamento superior do cabeçalho de backup — implementada e validada tecnicamente

O `PaddingValues.top` do `Scaffold` deixou de ser aplicado novamente antes da `TopAppBar`; os demais insets foram preservados. `testDebugUnitTest assembleDebug lintDebug` passou em 2026-10-28. Inspeção manual de layout no dispositivo permanece pendente.

## Ajuste do cabeçalho de backup — implementado e validado tecnicamente

A tela de backup agora usa `TopAppBar` Material 3 com ícone de retorno acessível e título na mesma barra; o retorno do sistema é tratado pelo mesmo estado de destino da tela. As ações de criação e restauração não foram alteradas. `testDebugUnitTest assembleDebug lintDebug`, `connectedDebugAndroidTest` e `git diff --check` passaram em 2026-10-28. Inspeção manual de layout responsivo, fontes ampliadas e navegação no dispositivo permanece pendente.

## Fase 17 — planejamento de backup e restauração local

Implementação parcial realizada após autorização em 2026-10-28: `VACUUM INTO`, manifesto JSON/SHA-256, exportação SAF por streams, validação SQLite, tentativa de restauração com cópia anterior/estado persistente e acesso pelo menu da Home. `testDebugUnitTest assembleDebug lintDebug` passou na versão final; não foi reportada contagem no sumário Gradle. `connectedDebugAndroidTest` passou com 43 testes, 0 falhas e 0 ignorados em BUSAO(AVD)-16. Não há teste que exerça provedor SAF real, restauração com arquivo existente ou morte/reinício de processo. Fallback SQLite <3.27 não implementado; a função falha com erro em vez de gerar backup inseguro. A restauração agora recupera a árvore SAF persistida, valida explicitamente o URI de árvore com `DocumentFile.fromTreeUri`, lista somente pares reconhecidos por geração exata e manifesto correspondente, ou pede seleção da pasta existente quando o URI/grant não é utilizável. Não requer criar backup novo. A UI continua com apenas as ações Criar e Restaurar (mais listagem/seleção interna de pares). `connectedDebugAndroidTest` final: 43 testes, 0 falhas/ignorados; o teste novo verifica seleção da pasta existente e ação Restaurar inativa sem par. SAF real ainda não foi exercitado. Pendências A/B/C continuam abertas; detalhes em [status](../phases/phase-17-local-backup/STATUS.md). `rtk git diff --check` passou (código 0); documentos não rastreados foram revisados e verificados separadamente. As tentativas anteriores de checagem alternativa com erros de citação estão no registro de validação; as checagens finais passaram. MinSdk permanece 28.

## Fase 16 — acompanhamento de orçamento por tag — implementada e validada tecnicamente

Escopo autorizado em 2026-10-27: exibir consumo por tag no mês atual em Home e Relatórios; usar orçamento atual da tag; sem seletor de mês na Home, histórico versionado, migração ou alteração do orçamento global. Planejamento e evidências em `docs/phases/phase-16-tag-budget-usage/`.

Implementados cálculo compartilhado, consulta agregada Room reativa, integração no estado do relatório e exibição nas duas telas. `testDebugUnitTest` passou com 28 testes; `assembleDebug lintDebug` passou; `connectedDebugAndroidTest` passou com 41 testes no `BUSAO(AVD) - 16`; `git diff --check` passou. Execuções anteriores tiveram falhas transitórias de AVD e de uma asserção, corrigidas ou não reproduzidas na execução completa aprovada. Não houve alteração de persistência, dependências, versões ou orçamento global. Nenhuma fase posterior foi iniciada.

## Fase 15 — orçamento associado a tags — concluída e validada

Implementados `TagEntity.budgetAmountCents: Long?`, migração Room 1→2 com coluna nullable, operações independentes do orçamento mensal, formulário de criação/edição com `MoneyTextField` e testes instrumentados semânticos de CRUD. A migração histórica preservou dados, relações, índices e orçamento mensal; a independência aprovou alteração cruzada, soma superior e remoção. `TagManagementScreenTest` aprovou criação sem/com orçamento, edição, remoção, cancelamento, duplicidade, nome vazio e persistência em banco isolado. `connectedDebugAndroidTest` passou com 38 testes no `BUSAO(AVD) - 16`; `testDebugUnitTest`, `assembleDebug`, `lintDebug` e `git diff --check` também passaram. Não houve interação por coordenadas arbitrárias nem limpeza de dados do aplicativo.

## Fase 14 — padronização do espaçamento superior — concluída e validada

`ScreenContent` centraliza o consumo único do `PaddingValues` do `Scaffold` para Home, histórico, Tags, Relatórios e Orçamento; Relatórios e Orçamento passaram a receber o padding antes ausente. Modais Material 3 permaneceram fora do contêiner e sem alterações funcionais. `testDebugUnitTest`, `assembleDebug`, `lintDebug`, `connectedDebugAndroidTest` e `git diff --check` passaram; `ktlintCheck` não existe no projeto. A captura inicialmente preta foi diagnosticada como PNG corrompido pelo redirecionamento PowerShell (`FF FE FD FF`); capturas válidas foram produzidas com `cmd.exe`. Home, histórico, Relatórios, Orçamento, Tags e formulário foram verificados visualmente no `emulator-5554`, sem defeitos de alinhamento ou sobreposição observados.

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
