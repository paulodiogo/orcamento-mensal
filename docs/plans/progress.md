# Progresso

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
