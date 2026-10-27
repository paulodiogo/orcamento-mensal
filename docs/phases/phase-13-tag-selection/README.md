# Fase 13 — Redesign da seleção de tags

## Estado

Planejamento concluído; nenhuma implementação autorizada ou realizada.

## Objetivo e contexto

O cadastro/edição de despesas usa atualmente uma `FlowRow` de `FilterChip`: cada tag disponível fica visível e o toque alterna a associação. Isso é funcional para poucas tags, mas ocupa espaço vertical, não escala bem para nomes longos/listas extensas e não oferece uma leitura compacta do estado selecionado.

A análise do código confirma que uma despesa aceita **zero, uma ou várias tags**. A seleção é `Set<String>` no estado local de `CreateExpenseDialog`, enviada como `List<String>` ao `ExpenseListViewModel`, validada em `ExpenseOperations` e persistida em `expense_tag` por associação composta. A tag é opcional.

## Recomendação

Adotar a **Alternativa C: seletor modal compacto para seleção múltipla**, preferencialmente um `AlertDialog`/modal Material 3 com lista de opções, seleção por checkbox e ações `Cancelar`/`Aplicar`. Não recomendar um dropdown simples: ele comunica seleção singular, enquanto o domínio permite várias tags. A Alternativa B pode ser incorporada ao modal como campo de pesquisa quando a lista justificar, sem nova dependência.

A seleção temporária do modal deve ser uma cópia do conjunto confirmado. `Aplicar` devolve o conjunto; `Cancelar`, voltar ou fechar descarta alterações. O campo no formulário exibe “Nenhuma tag selecionada” quando vazio e, quando houver associações, mostra uma síntese compacta (“1 tag” ou “N tags”), evitando chips excessivos. A lista modal mostra nomes completos com truncamento visual seguro e rolagem.

Essa solução reduz toques acidentais e preserva múltipla seleção, funciona em telas pequenas, oferece pesquisa escalável e separa claramente editar da confirmar. O componente deve usar apenas Material 3 já presente no projeto; `ExposedDropdownMenuBox` não é recomendado como componente principal porque seu padrão é mais adequado a uma escolha única.

## Fluxo atual verificado

- `AppShell` e `MainActivity` coletam `ExpenseListViewModel.tags` e abrem o mesmo `CreateExpenseDialog`.
- Tags são carregadas por `TagDao.findAll()`, ordenadas por `normalizedName`, em `ExpenseListViewModel.refreshTags()`.
- Criação chama `ExpenseOperations.create`; edição chama `findByIdWithTags`, depois `update` e `updateTags`.
- Tags inexistentes são rejeitadas; renomear preserva IDs/associações; excluir remove associações em transação sem excluir a despesa.
- Não há associação automática nem tag obrigatória. Lista vazia deve permanecer válida e explicar que tags podem ser criadas na tela Tags.

## Decisões principais

1. Preservar o modelo múltiplo e a persistência existentes.
2. Usar modal Material 3 com confirmação explícita, não dropdown singular.
3. Adicionar pesquisa no modal desde que a implementação permaneça local e sem dependência nova; para poucas tags o campo pode ser discreto, mas a lista deve continuar utilizável sem pesquisa.
4. Tratar tags removidas durante a edição como associações obsoletas: não exibi-las como opção disponível e, antes de salvar, impedir associação inválida ou exigir limpeza explícita; a política final está pendente.
5. Não alterar Room, entidades, ViewModels, contratos ou regras nesta fase.

## Referências

- `docs/BUSINESS_RULES.md`, `docs/DATA_MODEL.md`, `docs/UI_UX_SPECIFICATION.md`
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/ExpenseForm.kt`
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseListViewModel.kt`
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseOperations.kt`
