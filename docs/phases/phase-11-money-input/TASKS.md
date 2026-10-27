# Tarefas — Fase 11

A fase foi autorizada e executada em etapas; implementação e validação concluídas.

## F11-01 — Autorizar escopo e confirmar decisões
- **Estado:** concluída.
- O usuário confirmou o orçamento no escopo, sanitização de colagem e conversão centesimal; limite implementado em 19 dígitos validado por `Long`.

## F11-02 — Extrair funções puras de dinheiro
- **Estado:** concluída e testada.
- Definir sanitização, formatação brasileira, conversão segura para centavos, conversão inversa e limites.
- Preservar RN-006 e compatibilidade com `Long` persistido.

## F11-03 — Implementar `MoneyTextField`
- **Estado:** concluída e testada.
- Aplicar teclado numérico, estado de edição, cursor, seleção, exclusão, foco, recomposição e erros.
- Não incluir persistência nem regras de tela.

## F11-04 — Integrar cadastro e edição de despesas
- **Estado:** concluída e testada em cadastro e edição.
- Reutilizar o componente em `CreateExpenseDialog`, com inicialização por centavos e salvamento validado no domínio.

## F11-05 — Avaliar/alinha campo de orçamento
- **Estado:** concluída e testada, incluindo orçamento zero.
- Reutilizar funções puras somente se o escopo for aprovado; manter regras distintas para despesa e orçamento.

## F11-06 — Testar e documentar validação
- **Estado:** concluída; build, lint, 23 testes JVM, 27 testes instrumentados e inspeção real do emulador registrados em `VALIDATION.md`.
- Executar unitários, Compose/instrumentados, regressão, build/lint e registrar evidências nos documentos de validação.
