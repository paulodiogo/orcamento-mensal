# Status — Fase 14

## Situação atual

**Implementação validada; fase concluída.** A primeira captura inválida foi causada pelo redirecionamento PowerShell que corrompeu o PNG binário (`FF FE FD FF` antes da assinatura PNG). A captura correta foi obtida usando redirecionamento nativo do `cmd.exe`, sem reiniciar ou limpar dados do AVD. As telas principais e o formulário foram visualmente verificados; a fase está concluída.

## Implementação realizada

- `ScreenContent` foi adicionado ao design system para consumir `PaddingValues` uma única vez e aplicar o espaçamento vertical comum.
- Home, histórico, Tags, Relatórios e Orçamento usam o contêiner compartilhado.
- Relatórios e Orçamento recebem o `innerPadding` do `AppShell`.
- Modais e formulário não foram alterados funcionalmente nem receberam padding externo.

## Diagnóstico resumido

O `AppShell` possui um `Scaffold` com barra inferior e FAB. Home, histórico e Tags consomem seu `PaddingValues` e acrescentam `Spacer(12.dp)`. Relatórios e Orçamento são chamados sem esse `PaddingValues` e aplicam seus próprios modifiers. A lista alternativa em `MainActivity` possui outro `Scaffold` e `padding(16.dp)`. Não foram encontrados usos explícitos de `WindowInsets`, `statusBarsPadding`, `safeDrawingPadding` ou `navigationBarsPadding` no código principal. A inconsistência atual é de consumo do contêiner e de espaçamento visual, não uma duplicação explícita de inset identificada.

## Pendências para implementação

- Confirmar rotas alcançáveis e baseline visual.
- Aprovar o contrato final de ownership dos insets.
- Implementar F14-02 a F14-05 em ordem.
- Executar F14-06 e registrar evidências antes de qualquer encerramento.
