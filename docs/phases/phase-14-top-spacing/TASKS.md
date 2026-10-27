# Tarefas futuras — Fase 14

As tarefas da fase foram executadas e validadas. A primeira captura inválida foi diagnosticada como corrupção de PNG pelo redirecionamento PowerShell; capturas válidas foram obtidas com `cmd.exe`.

## F14-01 — Consolidar inventário e baseline

- Enumerar as rotas realmente alcançáveis pelo fluxo atual e confirmar se `ExpenseListScreen` alternativo ainda é usado.
- Registrar para cada superfície o arquivo, componente, cadeia de modifiers e origem do topo.
- Capturar baseline no mesmo emulador, resolução, densidade, fonte, tema e estado de dados.

## F14-02 — Definir contrato de insets

- Escolher um único responsável por consumir o inset superior do sistema, preferencialmente o contêiner `Scaffold`/host da navegação.
- Passar o `innerPadding` às telas que ocupam o conteúdo do Scaffold, incluindo Relatórios e Orçamento.
- Confirmar que o host também respeita o inset inferior da `NavigationBar` e FAB sem cobrir conteúdo.
- Não adicionar `statusBarsPadding()`/`safeDrawingPadding()` em filhos que já recebem o inset consumido.

**Dependência:** F14-01.

## F14-03 — Definir padrão visual

- Separar inset do sistema de espaçamento visual: o host fornece a área segura; o conteúdo fornece uma única distância visual consistente até o primeiro elemento.
- Harmonizar cabeçalhos equivalentes por meio de `ScreenTitle` e contêiner compartilhado somente se a repetição justificar a abstração.
- Definir exceções para formulário/modal e telas sem cabeçalho, sem alterar seus espaçamentos internos.

**Dependência:** F14-01.

## F14-04 — Corrigir contêiner compartilhado

- Aplicar o contrato aos destinos do `AppShell`, começando pelas rotas que atualmente não recebem `PaddingValues`.
- Remover apenas `Spacer`/padding superior redundante comprovado; manter padding horizontal e gaps internos.
- Tratar o fluxo alternativo de `MainActivity` separadamente, sem criar dois padrões conflitantes.

**Dependências:** F14-02, F14-03.

## F14-05 — Ajustar exceções justificadas

- Revisar listas, filtros, cabeçalhos, formulário, seletores e diálogos.
- Documentar qualquer diferença legítima por causa de título, controles iniciais, modal ou rolagem.
- Confirmar ausência de dupla aplicação de `innerPadding`, `statusBarsPadding` ou `safeDrawingPadding`.

**Dependência:** F14-04.

## F14-06 — Validar visual e regressão

- Executar testes automatizados pertinentes, build e lint existentes após implementação.
- Comparar capturas antes/depois com o mesmo dispositivo e estado.
- Percorrer navegação, formulário, filtros, relatórios, orçamento, tags e modais; registrar diferenças justificadas.

**Dependência:** F14-05.

## F14-07 — Fechar documentação

- Atualizar inventário, contrato, evidências e decisões.
- Registrar comandos reais em `VALIDATION.md` e status sem declarar cobertura não executada.

**Dependência:** F14-06.
