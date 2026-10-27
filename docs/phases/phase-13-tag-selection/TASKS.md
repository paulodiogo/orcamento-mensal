# Tarefas futuras — Fase 13

A implementação foi autorizada em 2026-10-27. F13-01 a F13-04 foram iniciadas; validação completa ainda pendente.

## F13-01 — Aprovar decisão de interação

**Estado:** concluída; decisões aprovadas pelo usuário em 2026-10-27.

- Confirmar modal Material 3 para seleção múltipla.
- Confirmar se pesquisa fica sempre visível ou aparece apenas acima de um limiar de quantidade.
- Fechar a política para tag removida durante edição.

## F13-02 — Modelar estados do seletor

**Estado:** implementada no componente; testes manuais pendentes.

- Definir estado temporário confirmado/cancelado sem alterar o formulário antes de `Aplicar`.
- Mapear carregando, erro, lista vazia, lista filtrada vazia e seleção existente.
- Garantir que a lista fornecida ao modal seja atualizada sem perder IDs válidos.

**Dependência:** F13-01.

## F13-03 — Implementar componente Material 3

**Estado:** implementada em `ExpenseForm.kt`; validação interativa pendente.

- Criar/adaptar campo compacto com rótulo “Tags (opcional)” e resumo textual.
- Abrir modal com lista rolável, checkbox, pesquisa local opcional e ações claras.
- Suportar nomes longos, rotação/recomposição e telas pequenas sem overflow.

**Dependência:** F13-02.

## F13-04 — Integrar cadastro e edição

**Estado:** integrada aos caminhos compartilhados; validação de persistência e edição manual pendente.

- Reutilizar o componente no `CreateExpenseDialog` compartilhado por `AppShell` e `MainActivity`.
- Cadastro inicia sem seleção; edição carrega `initialTagIds` via `findByIdWithTags`.
- Aplicar envia as associações atuais; salvar mantém transação e validação existentes.

**Dependência:** F13-03.

## F13-05 — Tratar estados dinâmicos e exclusão/renomeação

- Definir comportamento quando tags carregam vazio ou falham.
- Atualizar nomes após renomeação sem alterar IDs selecionados.
- Cobrir tag removida enquanto o formulário está aberto, conforme decisão F13-01.

**Dependência:** F13-04.

## F13-06 — Testar e validar

- Adicionar testes de lista, seleção, cancelamento, aplicar, pesquisa, nomes longos e vazio.
- Testar persistência de zero, uma e várias tags e recuperação na edição.
- Executar unitários, instrumentados, build/lint e roteiro manual no emulador somente após implementação autorizada.

**Dependência:** F13-05.

## F13-07 — Fechar documentação

- Registrar comandos/evidências em `VALIDATION.md`.
- Atualizar `STATUS.md` e documentos globais somente se o procedimento do projeto autorizar.
- Revisar diff para confirmar ausência de mudanças em modelo, dependências e regras.

**Dependência:** F13-06.
