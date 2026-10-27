# Tarefas — Fase 05

A execução permanece bloqueada até autorização explícita da fase. Os identificadores RF-004 e RF-007 são os requisitos oficiais rastreáveis desta fase; os subtarefas abaixo organizam sua entrega sem criar novos requisitos.

## Ordem de execução

### F05-01 — Confirmar autorização e baseline
- **Estado:** concluída nesta etapa.
- **Pré-requisitos:** Fases 02, 03 e 04 concluídas e validadas; autorização explícita recebida para a Etapa 1.
- **Ações:** estado da árvore, contratos, versões e baseline foram confirmados; nenhuma versão ou dependência foi alterada.
- **Aceite:** escopo e exclusões confirmados; baseline executada com sucesso.

### F05-02 — Completar persistência e casos de uso de CRUD
- **Estado:** concluída nesta etapa.
- **Objetivo:** implementar inserção, consulta, edição e exclusão de lançamentos, mantendo integridade das associações.
- **Arquivos prováveis:** `data/ExpenseDao.kt`, `data/ExpenseTagDao.kt`, `data/BudgetDatabase.kt`, repositórios/casos de uso existentes ou a criar.
- **Comportamento:** validar descrição, data e centavos; suportar retroativos; edição atualiza o mês antigo e o novo; exclusão preserva tags globais e demais lançamentos.
- **Aceite:** RF-004 e RF-007 atendidos; persistência sobrevive ao reinício; operações relacionadas são atômicas; nenhum gasto válido é bloqueado por orçamento.
- **Testes:** unitários de validação; Room/instrumentados para CRUD, zero tags, retroativo, transferência de mês e exclusão.

### F05-03 — Implementar tags no formulário de lançamento
- **Estado:** concluída nesta etapa, somente no caso de uso/persistência; nenhum formulário ou tela foi implementado.
- **Objetivo:** permitir zero, uma ou várias tags, criando ou reutilizando tags globais.
- **Arquivos prováveis:** serviços/repositórios de tags, estado de tela e componentes Compose existentes.
- **Aceite:** duplicatas são ignoradas; colisões normalizadas são rejeitadas conforme Fase 04; salvar lançamento sem tag continua válido; associação e criação ocorrem atomicamente.
- **Testes:** casos com zero/uma/várias tags, reuso case-insensitive, associação e desassociação.

### F05-04 — Implementar lista, pesquisa, filtros e estados de UI
- **Estado:** concluída nesta execução; listagem, pesquisa por descrição, estados básicos, criação, edição, seleção de tags, filtros por mês/período/tag/sem tag e exclusão confirmada foram implementados.
- **Objetivo:** listar lançamentos e permitir pesquisa por descrição e filtros combináveis por mês/período/tag/sem tag.
- **Arquivos prováveis:** `ExpenseDao.kt`, ViewModel/StateFlow, telas Compose, strings e testes instrumentados.
- **Aceite:** filtros não alteram dados; lançamentos sem tag aparecem na lista geral; vazio, erro e limpar filtros são tratados.
- **Testes:** Compose/instrumentados para salvar e listar sem tags, uma tag e várias tags, filtros e exclusão confirmada.

### F05-05 — Regressão, documentação e encerramento
- **Estado:** validação parcial; testes, build, lint e inicialização foram executados, mas a navegação manual completa não foi realizada.
- **Pré-requisitos:** F05-02 a F05-04 concluídas.
- **Ações:** executar testes focados e `assembleDebug`, `lintDebug`, `connectedDebugAndroidTest` conforme disponibilidade; revisar diff e atualizar `STATUS.md`, `VALIDATION.md`, `docs/plans/progress.md` e `docs/plans/VALIDATION.md`.
- **Aceite:** comandos e evidências registrados; RF-004/RF-007 rastreáveis; nenhum item excluído implementado; Fase 06 permanece não iniciada.

## Decisões pendentes antes da execução
- Autorização explícita da Fase 05.
- Confirmação do dispositivo/emulador disponível para testes instrumentados.
- Se o estado atual não tiver ViewModel/telas suficientes, registrar a lacuna antes de criar componentes, sem antecipar o resumo da Fase 06.

