# Status — Fase 13

## Situação atual

**Fase concluída e aprovada em 2026-10-27.** A implementação e a correção de sincronização foram concluídas; a validação funcional direcionada foi executada com evidências. Os cenários complementares sem validação individual foram formalmente dispensados pela decisão final de encerramento, sem serem apresentados como testados. A autorização foi recebida em 2026-10-27. O componente modal foi integrado ao formulário compartilhado. A investigação confirmou que as tags eram carregadas por fotografias (`findAll`) independentes nos ViewModels: após criação/renomeação/remoção, o Room persistia corretamente, mas os consumidores não recebiam nova emissão. `TagDao.observeAll()` agora é a fonte reativa compartilhada; `TagViewModel` e `ExpenseListViewModel` observam o mesmo fluxo, atualizando gerenciamento, seletor e filtros sem recriar ViewModel/Activity. Não houve alteração de modelo, esquema, associações, dependências ou versões.

## Evidências do levantamento

- `CreateExpenseDialog` mantém `selectedTagIds: Set<String>` e renderiza um `FilterChip` por tag.
- O mesmo diálogo é usado no cadastro e na edição em `AppShell.kt` e `MainActivity.kt`.
- `ExpenseListViewModel` carrega tags com `TagDao.findAll()`, e a edição obtém IDs por `findByIdWithTags`.
- `ExpenseOperations.create/updateTags` valida IDs e grava associações na transação; `TagOperations.rename` preserva ID e `delete` remove associações.
- Testes existentes cobrem persistência de múltiplas associações em `ExpenseOperationsTest`, CRUD de tags em `TagOperationsTest` e persistência do formulário em `ExpenseFormPersistenceTest`.

## Implementação realizada até o momento

`TagSelector` foi criado em `ExpenseForm.kt` e integrado ao cadastro/edição compartilhados. O modal usa lista rolável, pesquisa local sempre visível, checkboxes, estado temporário e ações `Aplicar`/`Cancelar`. A lista vazia oferece `Gerenciar tags`; o fluxo navega para a tela de Tags. A atualização da lista remove IDs que não existem mais.

## Decisão recomendada

Modal Material 3 para seleção múltipla, com estado temporário e confirmação explícita. Pesquisa local é obrigatória conforme autorização recebida.

## Decisões

Nenhuma decisão funcional permanece pendente: pesquisa sempre visível, descarte de IDs removidos e atalho para gerenciamento foram autorizados e implementados. A decisão final de 2026-10-27 aprovou o encerramento sem novas validações complementares.

A primeira tentativa mostrou tela preta, mas o reinício do emulador recuperou a renderização. `uiautomator dump` passou a identificar `ComposeView`, “Olá, vamos organizar?”, “Tags (opcional)”, “Nenhuma tag selecionada”, “Selecionar tags”, “Pesquisar tags”, “Aplicar” e “Cancelar”. A busca inexistente também exibiu “Nenhuma tag corresponde à pesquisa.”. O estado inicial, abertura do formulário/modal, seleção de uma tag, aplicação do resumo `1 tag`, cancelamento preservando `1 tag`, limpeza da pesquisa e pesquisa sem resultados foram observados; persistência, edição, seleção múltipla, remoção/renomeação e temas ainda precisam de execução completa.

## Investigação e correção de sincronização

- Persistência: `TagOperations.createOrReuse`, `rename` e `delete` executam transações Room corretamente; não havia falha silenciosa observada.
- Causa comum: `TagDao.findAll()` era `suspend`, e cada consumidor mantinha uma fotografia local. `TagViewModel.refresh()` só era chamado após operações na tela de gerenciamento; o `ExpenseListViewModel` só chamava `refreshTags()` após salvar despesas. Assim, novas tags não alcançavam o seletor nem os filtros enquanto os ViewModels permaneciam vivos.
- Correção: `TagDao.observeAll(): Flow<List<TagEntity>>`; `TagViewModel` publica o estado diretamente do fluxo; `ExpenseListViewModel` observa o mesmo fluxo, repõe a lista do seletor/filtros e invalida uma seleção de filtro removida. A pesquisa do seletor continua sendo apenas uma transformação local da lista recebida.
- Teste de regressão: `TagOperationsTest.createdTagAppearsInReactiveQuery` confirma que a consulta reativa inclui a tag criada.

## Validação funcional direcionada — 2026-10-27

A tag `VALIDACAO_TAG_2710` foi criada no BUSAO e permaneceu disponível após navegação sem reiniciar o aplicativo. Ela apareceu no gerenciamento, foi encontrada pela pesquisa do `TagSelector`, selecionada e aplicada; a despesa `DESPESA_VALIDACAO` foi salva. A mesma tag apareceu imediatamente entre os filtros de `Lançamentos`; o filtro mostrou a despesa associada e a remoção do filtro restaurou a listagem normal. Evidências detalhadas estão em `phase13-tag-selection/VALIDATION.md` e nos XMLs registrados em `app/build/`.

## Encerramento administrativo

A decisão final aprova a Fase 13 em sua totalidade. Renomeação, remoção, seleção múltipla complementar, edição detalhada, lista vazia e temas claro/escuro não foram executados individualmente nesta rodada; foram dispensados de nova validação por decisão explícita de encerramento. Essa dispensa não altera os resultados reais registrados.

Não há bloqueio técnico ou pendência de fase. Nenhuma implementação adicional foi realizada nesta tarefa.
