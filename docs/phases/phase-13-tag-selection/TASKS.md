# Tarefas futuras — Fase 13

A implementação foi autorizada em 2026-10-27. A Fase 13 foi concluída e aprovada em 2026-10-27. Os cenários complementares sem evidência manual individual foram dispensados da execução adicional pela decisão final de encerramento; nenhum resultado foi inventado.

## F13-01 — Aprovar decisão de interação

**Estado:** concluída; decisões aprovadas pelo usuário em 2026-10-27.

- Confirmar modal Material 3 para seleção múltipla.
- Confirmar se pesquisa fica sempre visível ou aparece apenas acima de um limiar de quantidade.
- Fechar a política para tag removida durante edição.

## F13-02 — Modelar estados do seletor

**Estado:** implementada e aceita; testes manuais complementares dispensados pela decisão final.

- Definir estado temporário confirmado/cancelado sem alterar o formulário antes de `Aplicar`.
- Mapear carregando, erro, lista vazia, lista filtrada vazia e seleção existente.
- Garantir que a lista fornecida ao modal seja atualizada sem perder IDs válidos.

**Dependência:** F13-01.

## F13-03 — Implementar componente Material 3

**Estado:** implementada em `ExpenseForm.kt` e aceita; validação interativa complementar dispensada.

- Criar/adaptar campo compacto com rótulo “Tags (opcional)” e resumo textual.
- Abrir modal com lista rolável, checkbox, pesquisa local opcional e ações claras.
- Suportar nomes longos, rotação/recomposição e telas pequenas sem overflow.

**Dependência:** F13-02.

## F13-04 — Integrar cadastro e edição

**Estado:** integrada aos caminhos compartilhados e aceita; validação manual complementar de persistência/edição dispensada.

- Reutilizar o componente no `CreateExpenseDialog` compartilhado por `AppShell` e `MainActivity`.
- Cadastro inicia sem seleção; edição carrega `initialTagIds` via `findByIdWithTags`.
- Aplicar envia as associações atuais; salvar mantém transação e validação existentes.

**Dependência:** F13-03.

## F13-05 — Tratar estados dinâmicos e exclusão/renomeação

**Estado:** aceita por decisão final de encerramento; validação manual complementar dispensada.

- Definir comportamento quando tags carregam vazio ou falham.
- Atualizar nomes após renomeação sem alterar IDs selecionados.
- Cobrir tag removida enquanto o formulário está aberto, conforme decisão F13-01.

**Dependência:** F13-04.

## F13-06 — Testar e validar

**Estado:** testes automatizados e validação funcional direcionada concluídos; cenários complementares dispensados por decisão de encerramento.

- Adicionar testes de lista, seleção, cancelamento, aplicar, pesquisa, nomes longos e vazio.
- Testar persistência de zero, uma e várias tags e recuperação na edição.
- Executar unitários, instrumentados, build/lint e roteiro manual no emulador somente após implementação autorizada.

**Dependência:** F13-05.

## F13-07 — Fechar documentação

**Estado:** concluída; documentação da fase e dos planos globais harmonizada em 2026-10-27.

- Registrar comandos/evidências em `VALIDATION.md`.
- Atualizar `STATUS.md` e documentos globais somente se o procedimento do projeto autorizar.
- Revisar diff para confirmar ausência de mudanças em modelo, dependências e regras.

**Dependência:** F13-06.
