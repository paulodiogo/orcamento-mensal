# Regras de Negócio

| ID | Regra |
|---|---|
| RN-001 | `mesReferencia` é `YYYY-MM` e é único em orçamento mensal. |
| RN-002 | Gastos pertencem ao mês da data de ocorrência, em fuso local do dispositivo. |
| RN-003 | `totalGasto(mês)` é a soma de cada lançamento do mês exatamente uma vez. |
| RN-004 | `saldo = orçamentoCentavos - totalGasto`; saldo não é transferido. |
| RN-005 | `percentual = totalGasto / orçamento × 100`; com orçamento zero, mostrar indisponível. |
| RN-006 | Valores são `Long` em centavos: orçamento aceita zero ou mais; lançamento exige valor maior que zero; descrição não pode ficar vazia após normalização. A conversão da entrada decimal e a formatação de saída não usam ponto flutuante. |
| RN-007 | Um lançamento pode ter zero, uma ou mais tags; associação `(lancamentoId, tagId)` é única e nunca é pré-requisito para salvar o lançamento. |
| RN-008 | Agregação por tag usa o valor integral por associação, é apenas classificatória e não altera RN-003; lançamentos sem tag não entram em grupos de tags, mas continuam no total mensal. |
| RN-009 | A chave normalizada de tag é global e única sem distinção de caixa; não existem hierarquias, categorias obrigatórias, limites ou metas por tag. |
| RN-010 | Renomear uma tag preserva associações e histórico; conflito de chave normalizada é recusado. Excluir tag remove apenas suas associações em transação, preservando todos os lançamentos. |
| RN-011 | Criar/editar/excluir lançamento, criar/associar tags e excluir tag com suas associações ocorrem atomicamente nas respectivas operações. |
| RN-012 | Configurar/editar orçamento não modifica lançamentos nem os bloqueia por ultrapassagem. |
| RN-013 | Orçamento por tag é opcional: ausência é `NULL`, enquanto zero explícito é `0` em centavos; sua edição não altera nome, identidade ou associações da tag. |
| RN-014 | Consumo do orçamento por tag é `gasto mensal associado / orçamento atual da tag × 100`; `NULL` não tem percentual, zero é indisponível e estouro é comparado em centavos. A exibição não altera orçamento ou saldo global. |
| RN-015 | Gastos por tag são filtrados pelo mês atual local. A despesa associada a várias tags conta integralmente em cada tag; tags orçadas sem despesas contam como gasto zero. O orçamento por tag não tem histórico mensal versionado. |

Possibilidade futura fora de escopo: rateio explícito por tag, que exigiria nova regra de soma e migração.

