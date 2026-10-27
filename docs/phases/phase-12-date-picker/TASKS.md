# Tarefas futuras — Fase 12

Todas as tarefas abaixo são planejadas, não executadas. Cada uma exige evidência própria antes de ser marcada como concluída.

## F12-01 — Consolidar levantamento e decisões

- Confirmar os dois caminhos de UI (`AppShell` e `MainActivity`) e os campos listados em `SCOPE.md`.
- Decidir explicitamente se mês de orçamento e relatório receberão seletor mensal e qual será seu valor inicial.
- Verificar que a política de data local não altera contratos de domínio.
- **Conclusão:** concluída; autorização recebida e matriz de comportamento aplicada.

## F12-02 — Definir componente de data Material 3

- Criar, somente após autorização, um componente reutilizável para texto legível e acionamento por toque/ícone.
- Manter estado temporário do `DatePickerState` separado do valor confirmado.
- Abrir na data selecionada; confirmar atualiza imediatamente; cancelar/fechar sem confirmação preserva o valor.
- Validar dialog em telas pequenas, foco, rótulo e ação de calendário.
- **Dependência:** F12-01.
- **Conclusão:** implementada em `DateFields.kt`, sem dependências novas; interação manual completa ainda pendente.

## F12-03 — Integrar cadastro e edição de despesas

- Inicializar cadastro com `LocalDate.now()` local no momento de abertura.
- Passar a data da entidade ao modo de edição, sem fallback para hoje quando houver registro.
- Converter somente entre `LocalDate`, `dd/MM/yyyy` e `yyyy-MM-dd` na UI, preservando o contrato do domínio.
- Garantir que recomposição e virada do dia não sobrescrevam escolha do usuário.
- **Dependência:** F12-02.
- **Conclusão:** integrada e coberta por testes de composição/persistência; validação manual dos dois caminhos ainda pendente.

## F12-04 — Integrar filtros e revisar seletores de período

- Aplicar DatePicker aos limites `De`/`Até` quando o filtro existir, mantendo vazio como “sem limite”.
- Preservar validação de ordem e limites inclusivos em `ExpenseFilters`.
- Tratar mês de orçamento/relatório como seletor mensal independente; não reutilizar DatePicker diário sem decisão.
- Remover valores fixos apenas com regra aprovada e sem mudar a consulta inicial indevidamente.
- **Dependências:** F12-01 e F12-02.
- **Conclusão:** filtros, relatório e orçamento integrados; testes instrumentados passaram; inspeção manual de seleção/navegação ainda pendente.

## F12-05 — Cobrir testes automatizados e validação manual

- Adicionar testes unitários para conversões locais, limites e virada de mês/ano sem fuso.
- Adicionar testes Compose/instrumentados para abertura, confirmação, cancelamento, criação e edição.
- Executar regressão existente, build/lint e instrumentados usando o wrapper, somente quando implementação for autorizada.
- Executar roteiro manual no emulador/dispositivo e registrar evidências.
- **Dependências:** F12-03 e F12-04.
- **Conclusão:** critérios de `ACCEPTANCE.md` atendidos com comandos e observações registrados.

## F12-06 — Revisar documentação e fechar fase

- Atualizar documentos globais apenas se as regras do repositório autorizarem e registrar isso como tarefa.
- Revisar diff, versões, dependências, Room e ausência de conversões UTC.
- Marcar status somente com evidências; manter bloqueios explícitos.
- **Dependência:** F12-05.
- **Conclusão:** documentação final e validação apontam estado concluído ou bloqueado de forma verificável.
