# Escopo — Fase 13

## Estado documental

O escopo abaixo permanece preservado. A Fase 13 foi encerrada e aprovada por decisão final em 2026-10-27. Critérios complementares sem validação manual individual foram formalmente dispensados de nova verificação; isso não constitui evidência de execução desses cenários.

## Incluído

- Redesenhar apenas a apresentação e o fluxo de seleção de tags no formulário compartilhado de criação/edição.
- Substituir a `FlowRow` de `FilterChip` por um campo compacto que abre seletor modal Material 3 para múltiplas tags.
- Exibir estado vazio, carregamento/erro já fornecidos pela arquitetura e pesquisa/filtragem local no modal.
- Integrar o mesmo comportamento aos caminhos de `AppShell` e `MainActivity`.
- Preservar `Set<String>` local, callback `List<String>`, validação, persistência, renomeação e exclusão atuais.
- Criar testes Compose/unitários e roteiro de validação manual, sem executá-los nesta fase.

## Exclusões

- Alteração de `ExpenseEntity`, `TagEntity`, `ExpenseTagCrossRef`, Room, DAOs ou migrações.
- Mudança de regras: zero/uma/várias tags, associação opcional, normalização ou transações.
- Criação de tags dentro do modal, limites financeiros, hierarquia ou rateio.
- Redesign da tela de gerenciamento de tags, filtros da lista ou relatórios.
- Dependências, versões, configurações, navegação ou strings executáveis sem autorização.

## Locais afetados

| Local | Papel | Impacto previsto |
|---|---|---|
| `ExpenseForm.kt` / `CreateExpenseDialog` | Campo e estado de seleção no cadastro/edição | Componente visual e modal, mantendo callback |
| `AppShell.kt` | Cadastro e edição pelo fluxo principal | Apenas integração visual indireta pelo componente compartilhado |
| `MainActivity.kt` | Fluxo alternativo/lista e seus diálogos | Apenas integração visual indireta pelo componente compartilhado |
| `ExpenseListViewModel.kt` | Carga de tags e estado de edição | Revisar somente se necessário para estados de erro/atualização |
| `ExpenseOperations.kt` | Validação e persistência | Sem mudança; referência de contrato |
| Testes Compose/instrumentados | Comportamento visual e persistência | Novas asserções para seleção múltipla e edição |
