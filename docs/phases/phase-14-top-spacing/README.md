# Fase 14 — Padronização do espaçamento superior

## Objetivo

Diagnosticar e, em uma etapa posterior, corrigir as diferenças de posicionamento vertical no topo das telas Compose, mantendo a arquitetura, navegação, regras de negócio e espaçamentos internos não relacionados.

## Contexto

O levantamento do código atual encontrou dois caminhos de composição: `AppShell` é o fluxo principal com `Scaffold`, barra inferior e FAB; `MainActivity` ainda contém um fluxo alternativo de lista com `Scaffold`. As telas de orçamento e relatório são chamadas pelo `AppShell`, mas não recebem os `PaddingValues` do `Scaffold` principal. O código não aplica explicitamente `statusBarsPadding`, `safeDrawingPadding`, `navigationBarsPadding` ou `WindowInsets` próprios.

O topo, portanto, não é governado por uma regra única: Home e histórico recebem `padding` do `Scaffold` e adicionam `Spacer(12.dp)`; Tags recebe o mesmo padrão; Orçamento e Relatórios não recebem o `innerPadding` do `Scaffold` principal e usam apenas `Spacer(12.dp)` ou `padding(16.dp)`. O fluxo alternativo de `MainActivity` usa outro `Scaffold`, `innerPadding` e `padding(16.dp)`. Diálogos Material 3 têm insets próprios do componente e devem ser avaliados separadamente, sem aplicar o padrão de tela diretamente neles.

## Resultado obtido nesta implementação

O contrato foi centralizado em `ScreenContent`: o host fornece `PaddingValues` do `Scaffold`, e o contêiner aplica uma única vez o inset recebido, o padding horizontal e o espaçamento vertical comum de 12 dp. Home, histórico, Tags, Relatórios e Orçamento utilizam esse contêiner. Relatórios e Orçamento passaram a receber o `innerPadding` do `AppShell`. Modais Material 3 permaneceram inalterados e continuam responsáveis pelos próprios insets.

A validação automatizada passou. A captura manual do emulador produziu uma tela preta com ícone de imagem quebrada, embora `uiautomator dump` tenha sido concluído; por isso a comparação visual completa permanece pendente e a fase não é declarada concluída.

## Documentos

- [SCOPE.md](SCOPE.md)
- [TASKS.md](TASKS.md)
- [ACCEPTANCE.md](ACCEPTANCE.md)
- [STATUS.md](STATUS.md)
- [VALIDATION.md](VALIDATION.md)
