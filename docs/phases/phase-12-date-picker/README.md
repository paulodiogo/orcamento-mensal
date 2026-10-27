# Fase 12 — Preenchimento automático de datas e DatePicker

## Estado

Implementação parcial autorizada e validada tecnicamente. O cadastro/edição, filtros de intervalo e seletores de mês foram integrados; a validação manual completa dos fluxos ainda está pendente.

## Objetivo

Fazer com que datas de lançamento de novas despesas usem a data local atual como sugestão e oferecer seleção visual por calendário, preservando datas existentes na edição e a semântica própria de filtros, relatórios e meses de orçamento.

## Contexto verificado

- `ExpenseEntity.occurredOn` é `String` persistida como `yyyy-MM-dd`; não há necessidade comprovada de alterar Room ou criar migração.
- `CreateExpenseDialog` é compartilhado pela interface principal em `AppShell.kt` e pela tela/teste em `MainActivity.kt`.
- A data de lançamento atualmente inicia vazia no cadastro e é carregada da entidade na edição.
- Filtros de intervalo em `MainActivity.kt` iniciam vazios; mês de orçamento e mês de relatório estão atualmente fixados em `2026-10`.
- `AppShell.kt` calcula o mês da tela inicial com `YearMonth.now()` uma única vez via `rememberSaveable`.

## Decisões orientadoras

1. Usar `LocalDate.now()` no momento de abrir um novo formulário, sem valor fixo de compilação e sem observar a passagem do dia para sobrescrever uma escolha existente.
2. Edição recebe `expense.occurredOn` como fonte de verdade e nunca substitui a data por hoje.
3. Manter datas como strings ISO no domínio/banco; converter somente na borda da UI, com `LocalDate` e sem `Instant`/fuso horário.
4. Reutilizar `DatePicker` de Material 3 já disponível, encapsulado em componente pequeno e controlado pelo estado do formulário.
5. Não transformar filtros de período nem seletores de mês em data de hoje sem decisão específica; seus padrões atuais devem ser preservados até a etapa de integração.

## Documentos

- [Escopo e levantamento](SCOPE.md)
- [Etapas futuras](TASKS.md)
- [Critérios de aceitação](ACCEPTANCE.md)
- [Estado e decisões pendentes](STATUS.md)
- [Estratégia de validação](VALIDATION.md)
