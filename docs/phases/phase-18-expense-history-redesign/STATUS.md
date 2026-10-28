# Status — Fase 18

## Situação

**Fase concluída e encerrada em 2026-10-28.**

A implementação foi concluída, os testes automatizados registrados foram aprovados e as verificações visuais, de responsividade, tema e acessibilidade foram aprovadas manualmente pelo usuário. Os critérios de aceitação estão marcados como atendidos.

A Fase 17 era a fase numerada mais recente encontrada nos documentos de planejamento e de fases. Não existia uma Fase 18 prévia nem documento relacionado ao redesign da tela de lançamentos; por isso esta entrega usa a identificação sequencial `phase-18-expense-history-redesign`.

## Evidências do levantamento

- A referência visual existe em [`docs/references/lancamentos_novo.png`](../../references/lancamentos_novo.png).
- A tela navegada de lançamentos é `ExpenseHistoryScreen` em `AppShell.kt`.
- A tela atual já possui busca, filtros de tag/sem tag, `LazyColumn`, FAB, navegação inferior e confirmação de exclusão.
- `ExpenseEntity` já contém data, descrição e centavos; tags são relações opcionais e múltiplas.
- A lista atual consulta IDs de tags para cada lançamento em `ExpenseListViewModel.publish`; a implementação futura deve substituir isso por uma projeção reativa única com nomes de tags.
- O tema utiliza Material 3 e `BudgetTheme`; a formatação monetária vem de `formatBrazilianCents`.

## Decisões técnicas propostas

1. Criar um cartão específico de histórico e preservar `SummaryCard` como componente de resumo compartilhado.
2. Usar `LEFT JOIN` e modelo agrupado para evitar consultas por lançamento e apresentar tags sem alterar o schema Room.
3. Exibir todas as tags de um lançamento em `FlowRow`; não descartar associações quando a referência visual mostrar somente uma tag.
4. Formatar a data exclusivamente para leitura no cartão, mantendo o armazenamento ISO e os campos de formulário existentes.
5. Não alterar `ExpenseOperations`, entidades, migrações, dependências ou navegação sem necessidade comprovada.

## Pendências e bloqueios

Não há pendências conhecidas dentro do escopo da Fase 18. As verificações manuais de tema, responsividade, fontes ampliadas e acessibilidade foram confirmadas como aprovadas pelo usuário.

## Implementação realizada

- `ExpenseDao` recebeu uma projeção com `LEFT JOIN` e `Flow`, além da variante suspensa usada por `refresh`, evitando consultas individuais de tags na lista.
- `ExpenseListViewModel` agrupa as linhas por lançamento, preserva filtros e publica itens com nomes de tags.
- `DateFields.kt` recebeu o formato de leitura `d MMM`, sem ano, mantendo o formato dos formulários.
- `AppShell.kt` passou a renderizar cartão específico com data à esquerda, descrição/chips, valor abaixo e ações independentes à direita.
- Foram adicionados ícones vetoriais de editar e excluir e um teste unitário do formato de data.

## Validação executada

- `rtk .\\gradlew.bat testDebugUnitTest`: primeira execução após a implementação passou; após adicionar o teste de data, uma falha revelou pontuação de abreviações do locale; a normalização foi corrigida e a segunda execução passou com 40 testes.
- `rtk .\\gradlew.bat assembleDebug lintDebug`: sucesso.
- `problems` não reportou erros nos arquivos Kotlin alterados.
- `connectedDebugAndroidTest` passou no dispositivo `BUSAO(AVD) - 16`; a saída resumida não apresentou contagem, falhas ou ignorados.
- Inspeção visual, responsividade, temas claro/escuro, fontes ampliadas e acessibilidade/TalkBack foram aprovadas manualmente pelo usuário; essa confirmação não é uma nova execução automatizada.

## Ajuste fino posterior

Os chips de tags foram reduzidos para uma superfície compacta com fonte `11sp`, padding horizontal de `6dp`, padding vertical de `2dp`, altura mínima de `24dp` e espaçamento reduzido no `FlowRow`. A descrição mantém tipografia `titleMedium` e altura mínima própria, enquanto as tags continuam fluindo para linhas adicionais sem alterar a associação dos dados ou as ações.

`testDebugUnitTest`, `assembleDebug lintDebug`, `problems` e `git diff --check` foram executados novamente após o ajuste. A primeira tentativa de compilação revelou que `AssistChip` não aceita `contentPadding` nesta versão; o chip foi substituído por uma `Surface` Material 3 compacta, mantendo as cores do tema. A correção final compilou e validou.

## Alterações desta etapa

A implementação modificou somente os arquivos de produção/teste diretamente relacionados à Fase 18 e os documentos da fase. Não houve alteração de dependência, versão, branch, commit ou push. A pasta `docs/references/` já estava não rastreada antes desta implementação e foi preservada.
