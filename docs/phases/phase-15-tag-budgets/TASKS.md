# Tarefas futuras — Fase 15

A ordem abaixo é para uma futura implementação autorizada. Nenhuma tarefa foi executada nesta fase.

## F15-01 — Confirmar contrato monetário e baseline

- Reconfirmar `Long` em centavos, entrada brasileira e distinção entre `null` e zero.
- Fixar limite funcional recomendado em **`Long.MAX_VALUE` centavos = R$ 92.233.720.368.547.758,07**; os 19 dígitos continuam sendo o teto textual técnico, e valores acima do intervalo de `Long` devem ser rejeitados.
- Rejeitar valor negativo, caracteres inválidos, overflow, entrada não parseável e valor vazio apenas quando o fluxo exigir orçamento; no campo da tag, vazio representa `null`.
- Definir mensagens de erro inline, sem fechar o diálogo nem apagar o nome.

## F15-02 — Evoluir a persistência Room

- Adicionar `budgetAmountCents: Long?` à entidade `TagEntity`, coluna `budget_amount_cents` conforme o mapeamento de nomes adotado pelo projeto.
- Incrementar a versão do `BudgetDatabase` de 1 para 2.
- Criar `Migration(1, 2)` com `ALTER TABLE tag ADD COLUMN budget_amount_cents INTEGER` sem `NOT NULL` e sem valor zero padrão.
- Registrar a migração no builder; não recriar banco, apagar linhas ou alterar `expense_tag`.
- Atualizar modelos/mapeamentos de domínio se `TagOperations` usar objetos distintos. Revisar consultas/updates para não perder o novo campo.

**Dependência:** F15-01.

## F15-03 — Adaptar regras e operações

- Estender criação/reutilização e renomeação para transportar o orçamento solicitado.
- Em tag existente reutilizada, definir explicitamente se criação não sobrescreve orçamento existente; recomendação: criação idempotente não altera tag já existente, enquanto edição é a única operação que substitui/remove o valor.
- Preservar unicidade normalizada e as associações.
- Validar nome e orçamento antes da transação; atualizar `updatedAt` somente em alteração efetiva.

**Dependência:** F15-02.

## F15-04 — Atualizar criação e edição do formulário

- Acrescentar `MoneyTextField` abaixo de Nome, com rótulo “Orçamento da tag (opcional)” e ajuda “Deixe vazio para não definir um orçamento.”
- Criar estado de dígitos vazio no modo novo; no modo edição, carregar `moneyDigitsFromCents` quando não nulo e vazio quando nulo.
- Manter estado local até Salvar; Cancelar/fora do diálogo descarta nome, orçamento e erros sem persistir.
- Mostrar erro abaixo do campo; manter o diálogo aberto enquanto houver erro.
- Salvar `null` para vazio e o `Long` convertido para valor preenchido. Não alterar o nome nem associações ao editar orçamento.

**Dependência:** F15-03.

## F15-05 — Ajustar estado e contratos de UI

- Expandir a assinatura de `TagViewModel.save`/operações sem quebrar os chamadores existentes além do formulário de tags.
- Garantir que falhas sejam apresentadas no formulário, não somente como estado de lista, e que fluxo de sucesso feche o diálogo.
- Não exibir orçamento em `SummaryCard`, filtros, Home ou relatórios.

**Dependência:** F15-04.

## F15-06 — Implementar testes da persistência e regras

- Testar criação sem/com valor, edição, inclusão, remoção, valores inválidos, limite e distinção zero/ausente.
- Testar migração 1→2 com tags antigas e associações `expense_tag` preservadas.
- Testar reinício/reabertura e round-trip de `Long?`.

**Dependência:** F15-05.

## F15-07 — Implementar testes de UI e regressão

- Testar instrumentadamente criação/edição/cancelamento e mensagens de validação; confirmar que formulário não altera o nome ou associações.
- Validar manualmente o padrão monetário existente nos cenários de tag.
- Executar somente após a implementação e registrar resultados reais.

**Dependência:** F15-06.

## F15-08 — Revisar limites da fase

- Confirmar por diff que nenhuma tela fora do editor de tags passou a exibir ou calcular orçamento.
- Atualizar documentação global e fechar a fase somente após validação.

**Dependência:** F15-07.
