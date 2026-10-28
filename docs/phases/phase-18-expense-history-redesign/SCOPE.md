# Escopo e estratégia — Fase 18

## Referência visual

A referência obrigatória existe em [`docs/references/lancamentos_novo.png`](../../references/lancamentos_novo.png). Ela orienta a organização visual do histórico: data compacta à esquerda, descrição com tag, valor abaixo e ações à direita. Não é especificação de novas regras de negócio.

## Diagnóstico fundamentado no código atual

A tela usada pela navegação principal é `ExpenseHistoryScreen` em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/AppShell.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/AppShell.kt). Ela:

- é selecionada pelo destino `MainDestination.EXPENSES`;
- apresenta título, busca por descrição, chips de filtro por tag e “Sem tag”, estados de carregamento/vazio/erro e uma `LazyColumn`;
- renderiza cada lançamento com `SummaryCard`, seguido em outra linha por botões textuais `Editar` e `Excluir`;
- abre `AlertDialog` de confirmação antes de chamar `ExpenseListViewModel.delete`;
- mantém o FAB de criação e a `NavigationBar` definidos pelo `Scaffold` de `AppShell`.

`SummaryCard` está em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt) e também atende a resumos em outras telas. Portanto, o redesign não deve alterar esse componente compartilhado para acomodar cartões do histórico; a implementação deve criar um cartão específico, como `ExpenseHistoryCard`.

A persistência do lançamento está em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseEntity.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseEntity.kt):

- `description` fornece a descrição;
- `amountCents` fornece o valor em centavos;
- `occurredOn` fornece a data ISO `yyyy-MM-dd`;
- a ordenação vigente vem de `ExpenseDao.findAll` e `ExpenseDao.observeAll`, em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseDao.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseDao.kt), por `occurredOn DESC, id DESC`.

Tags são globais em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/data/TagEntity.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/data/TagEntity.kt) e são associadas opcionalmente e em múltiplos por lançamento por `ExpenseTagCrossRef`. Os IDs associados vêm de `ExpenseTagDao.findTagIdsForExpense`, em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseTagDao.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseTagDao.kt).

No estado atual, `ExpenseListViewModel.publish` em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseListViewModel.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseListViewModel.kt) constrói relações consultando os IDs de tags para cada lançamento. Além de não fornecer nomes para chips no cartão, essa estratégia pode executar $N$ consultas para uma lista de $N$ lançamentos. O redesign deve substituir essa leitura por uma projeção reativa única de lançamentos e tags, sem perder os filtros existentes.

A criação e a edição compartilham `CreateExpenseDialog` em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/ExpenseForm.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/ExpenseForm.kt). `ExpenseOperations` em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseOperations.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseOperations.kt) valida e persiste criação, edição, associações e exclusão em transações apropriadas. Não há motivo demonstrado para alterar esses contratos.

`formatBrazilianCents` em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt) continua sendo a única fonte de formatação monetária. `DateFields.kt` já usa `LocalDate` para converter a data ISO a formatos de leitura; o novo formato resumido deve ser específico do histórico e não mudar os campos de formulário.

O tema é `BudgetTheme`, também em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt), baseado no `colorScheme` Material 3 e no tema do sistema. As cores do redesign devem vir de `MaterialTheme.colorScheme`.

Existe `ExpenseListScreen` privado em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/MainActivity.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/MainActivity.kt), mas ele não é o caminho da navegação de `AppShell`. Ele não integra o escopo previsto, salvo se a implementação futura demonstrar que é invocado.

## Layout proposto

Cada lançamento passa a usar um cartão Material 3 específico, compacto e adaptável:

1. **Data à esquerda:** coluna de largura mínima estável com dia em destaque e mês abreviado abaixo, derivada de `occurredOn`, por exemplo `15` e `out`; o ano não é apresentado.
2. **Conteúdo central:** descrição em tipografia de destaque. A descrição e os chips de tag usam `FlowRow`: descrição e primeiro chip permanecem na mesma linha sempre que houver largura; chips seguintes ocupam linhas adicionais quando necessário. Lançamentos sem tag não reservam espaço para chips.
3. **Valor abaixo:** valor em `formatBrazilianCents(amountCents)`, visualmente destacado abaixo de descrição e tags. O domínio atual contém despesas positivas; a implementação não deve criar regra de receita ou semântica de cor não suportada pelo domínio.
4. **Ações à direita:** editar e excluir ficam em controles separados, alinhados à direita, com espaço ou divisor discreto entre ações e conteúdo. Excluir usa `colorScheme.error`; editar usa cor semântica de ação existente, como `onSurfaceVariant`.
5. **Cartão:** superfície, forma e espaçamento vêm do Material 3 e do tema existente. Não aplicar cores fixas que prejudiquem tema claro/escuro.

O cartão deve preservar a ordem visual e semântica: data, descrição/tags, valor e ações. Nenhum clique no cartão substitui as ações explícitas.

## Dados e arquitetura

### Mudança proposta na camada `data`

Adicionar uma projeção e uma consulta `Flow` única em `ExpenseDao`, com `LEFT JOIN` entre `expense`, `expense_tag` e `tag`. A consulta deve incluir lançamentos sem tag e emitir novamente quando `expense`, `expense_tag` ou `tag` mudarem. A projeção achatada pode conter todos os campos de `ExpenseEntity`, mais `tagId` e `tagName` anuláveis.

O agrupamento das linhas por lançamento deve ocorrer uma vez no ViewModel ou em um mapeador dedicado, formando um item de apresentação com:

- lançamento;
- lista ordenada de tags associadas, incluindo ID e nome;
- dados suficientes para aplicar filtros existentes.

A mudança não requer entidade nova, alteração de schema, migração Room ou mudança de versão do banco. Não remover as consultas atuais enquanto houver consumidores; a implementação deve preservar contratos vigentes.

### Mudança proposta na camada `domain`/apresentação

`ExpenseListState.Content` deve passar a representar itens enriquecidos ou disponibilizar as relações já agrupadas. `filterExpenses` em [`app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseFilters.kt`](../../../app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseFilters.kt) deve continuar preservando semântica de pesquisa por descrição, tag, período, mês e “Sem tag”; só deve ser adaptado ao novo modelo se necessário.

`ExpenseOperations`, regras de validação, transações, `CreateExpenseDialog`, armazenamento de centavos e relações de tags permanecem inalterados. A edição continua carregando as associações pelo fluxo existente, salvo uma refatoração comprovadamente necessária e coberta por testes.

## Preservação funcional obrigatória

- Pesquisa por descrição, com a semântica case-insensitive atual.
- Filtros por tags, incluindo “Sem tag”, “Alimentação” e “Mercado”, preservando reatividade de `TagDao.observeAll`.
- Criação e edição de lançamentos no formulário compartilhado.
- Exclusão com o atual diálogo de confirmação.
- Formatação brasileira por `formatBrazilianCents`.
- Ordenação atual da lista.
- FAB de adição, navegação inferior e destino de Despesas.
- Tema claro/escuro e identidade Material 3.

## Responsividade e acessibilidade

| Condição | Comportamento necessário |
|---|---|
| Descrição longa | Quebrar em até duas linhas e aplicar reticências após o limite que os testes visuais confirmarem. |
| Tag extensa | Quebrar para linha própria ou truncar com reticências dentro do chip; nunca invadir data, valor ou ações. |
| Múltiplas tags | Organizar em `FlowRow`; o cartão cresce verticalmente sem altura fixa. |
| Tela estreita | Data e ações mantêm largura mínima; conteúdo central recebe espaço restante e quebra. |
| Fonte ampliada | Sem alturas rígidas; cartão expande; ações mantêm alvos de toque de ao menos 48 dp. |
| Tema escuro | Usar somente cores de `MaterialTheme.colorScheme`. |
| Leitor de tela | Fornecer descrições inequívocas, como “Editar lançamento <descrição>” e “Excluir lançamento <descrição>”, e leitura lógica de data, descrição, tags e valor. |

## Arquivos prováveis

| Arquivo | Impacto previsto |
|---|---|
| `app/src/main/kotlin/br/com/paulodiogo/orcamento/AppShell.kt` | Trocar o item atual do histórico pelo cartão específico, preservando eventos e estados. |
| `app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseListViewModel.kt` | Publicar lançamentos enriquecidos sem leitura individual de tags. |
| `app/src/main/kotlin/br/com/paulodiogo/orcamento/data/ExpenseDao.kt` | Adicionar projeção/consulta reativa única. |
| Novo arquivo em `data` ou `domain` | Projeção Room e/ou modelo agrupado de apresentação; não é entidade. |
| `app/src/main/kotlin/br/com/paulodiogo/orcamento/DateFields.kt` | Adicionar formatador de data resumida apenas para o histórico. |
| `app/src/main/kotlin/br/com/paulodiogo/orcamento/DesignSystem.kt` | Somente se for preciso extrair token ou componente visual reutilizável; não alterar `SummaryCard` para o histórico. |
| `app/src/main/res/values/strings.xml` | Adicionar rótulos novos e descrições de acessibilidade. |
| Testes em `app/src/test` e `app/src/androidTest` | Cobertura de formatação, consulta, filtro e UI. |

## Exclusões

- Implementar o redesign nesta fase de planejamento.
- Alterar entidades, schema Room, migrações ou versão do banco sem necessidade comprovada.
- Alterar dependências, Kotlin, Gradle, AGP, SDK ou configurações.
- Mudar regras de negócio, criar receitas, rateio, tags hierárquicas ou novo fluxo de navegação.
- Alterar o fluxo privado legado de `MainActivity.kt` sem evidência de uso.
