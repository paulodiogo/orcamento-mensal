# Escopo — Fase 14

## Incluído

- Inventariar o fluxo normal do app: resumo mensal/Home, histórico de lançamentos, novo/editar lançamento, gerenciamento de tags, relatórios, orçamento e histórico/seletores/filtros presentes no código.
- Verificar diálogos e o modal de seleção de tags apenas quanto ao alinhamento superior quando o comportamento diferir do problema das telas.
- Mapear, por arquivo e componente Compose, o contêiner, `Scaffold`/`PaddingValues`, insets do sistema, cabeçalho, `Spacer`/padding superior, sobreposições e correção proposta.
- Definir o contrato entre o `Scaffold` raiz, telas, listas roláveis, cabeçalhos e modais.
- Implementar posteriormente somente ajustes de espaçamento superior, preservando regras de negócio, modelo Room, temas, navegação e espaçamentos internos não relacionados.

## Fora do escopo

- Auditoria de acessibilidade.
- Mudança de dependências, versões, banco, entidades, DAOs, regras financeiras ou navegação funcional.
- Redesign visual amplo, troca de tipografia, cores, componentes de negócio ou barra inferior.
- Novo componente de navegação ou abstração genérica sem evidência de necessidade.
- Alterações no código nesta fase e execução de testes nesta etapa de planejamento.

## Superfícies identificadas

| Superfície | Arquivo/componente | Observação para diagnóstico |
|---|---|---|
| Resumo mensal/Home | `AppShell.kt` / `HomeScreen` | Recebe `padding` do `Scaffold` principal, usa `padding(horizontal = 20.dp)` e `Spacer(12.dp)` antes de `ScreenTitle`. |
| Histórico | `AppShell.kt` / `ExpenseHistoryScreen` | Mesmo `padding` do `Scaffold`, horizontal 20 dp e `Spacer(12.dp)`; cabeçalho `ScreenTitle`. |
| Lista alternativa | `MainActivity.kt` / `ExpenseListScreen` | Segundo `Scaffold`; usa `innerPadding` e `padding(16.dp)`, cabeçalho próprio em `Row`; não é o destino principal atual, mas deve ser preservado/inventariado. |
| Relatórios | `ReportScreen.kt` / `ReportScreen` | Chamado sem `PaddingValues` pelo `AppShell`; `padding(16.dp)` próprio e `ScreenTitle`; não recebe o inset do Scaffold raiz. |
| Orçamento | `BudgetScreen.kt` / `BudgetScreen` | Chamado sem `PaddingValues`; horizontal 20 dp e `Spacer(12.dp)`; `ScreenTitle`; risco de regra superior divergente. |
| Tags | `TagManagementScreen.kt` / `TagManagementScreen` | Recebe `padding` do Scaffold, horizontal 20 dp e `Spacer(12.dp)`; `ScreenTitle`. |
| Novo/editar lançamento | `ExpenseForm.kt` / `CreateExpenseDialog` | `AlertDialog` Material 3 com insets próprios; não deve receber o padding de tela como se fosse conteúdo raiz. |
| Seleção de tags | `ExpenseForm.kt` / `TagSelector` | `AlertDialog` aninhado/relacionado ao formulário; avaliar apenas sobreposição modal e não impor espaçamento de tela. |
| Abertura/edição de orçamento | `BudgetScreen.kt` / `MonthOpeningDialog` | `AlertDialog` Material 3; mesma ressalva de insets próprios. |
| Editor/exclusão de tags | `TagManagementScreen.kt` | `AlertDialog` Material 3; não faz parte do padrão de cabeçalho de telas. |
| Filtros e mês | `AppShell.kt`, `MainActivity.kt`, `DateFields.kt` | São controles dentro das telas; não constituem telas independentes, mas devem manter alinhamento após o cabeçalho. |

## Contrato implementado

`AppShell` é o responsável por entregar o `PaddingValues` do `Scaffold`. `ScreenContent` consome esse valor uma única vez e aplica o espaçamento vertical comum; as telas não adicionam `statusBarsPadding`, `safeDrawingPadding` ou outro inset redundante. Relatórios e Orçamento agora participam do mesmo contrato. Os diálogos Material 3 continuam fora desse contêiner e preservam os insets próprios.

## Causa diagnosticada

A causa observável é a ausência de um contrato comum: o `Scaffold` principal entrega `PaddingValues` apenas a algumas rotas, enquanto Relatórios e Orçamento não os recebem; além disso, cada tela combina padding horizontal próprio com `Spacer(12.dp)` ou `padding(16.dp)`, e a lista alternativa possui seu próprio `Scaffold`. Não há evidência de padding duplicado explícito de `WindowInsets`; há, sim, caminhos inconsistentes para consumir o espaço do contêiner e espaçamento visual superior. A causa não deve ser corrigida por um valor fixo universal antes de validar a geometria no emulador.
