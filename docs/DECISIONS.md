# Registro de Decisões

## Decisões de produto confirmadas pelo responsável
- **D-001:** Android nativo, Kotlin, Jetpack Compose Material 3, Room/SQLite, ViewModel e StateFlow; operação offline, armazenamento local, sem conta ou servidor.
- **D-002:** Há um único orçamento total por mês; o saldo não é acumulado. Abrir um novo mês sugere o orçamento anterior e exige confirmação após edição opcional.
- **D-003:** Tags são globais, livres, reutilizáveis, planas e opcionais. Um lançamento pode ter zero, uma ou várias tags; não há categorias obrigatórias, tags-pai ou tags-filhas.
- **D-004:** Tags classificam, filtram, pesquisam e agrupam gastos, sem alterar o valor do lançamento, o total mensal ou o saldo.
- **D-005:** Não há limites, metas, orçamentos individuais, nem alertas financeiros por tag. Não existe rateio automático.
- **D-006:** Ao excluir uma tag, remover suas classificações relacionadas sem excluir ou invalidar lançamentos e sem alterar valor, data ou descrição; informar previamente essa consequência.
- **D-007:** Sem backup próprio na v1; a implementação somente começa após aprovação explícita do planejamento. O planejamento posterior da Fase 17 foi autorizado separadamente e não autoriza sua implementação.

## Decisões técnicas consolidadas
- **T-001 — moeda:** valores monetários são `Long` em centavos. A entrada decimal digitada é validada e convertida para centavos sem usar ponto flutuante; o valor persistido permanece inteiro e a exibição é formatada na moeda/localidade do dispositivo. Valores devem ser não negativos para orçamento, positivos para lançamento e rejeitados quando excederem limites que possam causar overflow em conversões ou somas.
- **T-002 — identidade de tags:** a tag possui nome de apresentação e chave global `normalized_name`, única e sem distinção entre maiúsculas/minúsculas. A normalização remove espaços nas extremidades, reduz sequências internas a um espaço e compara o resultado sem diferença de caixa. Não há estrutura de hierarquia.
- **T-003 — classificação:** `expense_tag` é uma relação muitos-para-muitos opcional: um lançamento pode não ter nenhuma linha de associação. A criação de uma tag inexistente durante cadastro/edição e a associação são transacionais; uma tag existente com a mesma chave normalizada é reutilizada.
- **T-004 — renomeação:** renomear atualiza somente a tag global, preservando todas as associações e os valores financeiros. Se o novo nome normalizar para a chave de outra tag, a renomeação é recusada e a interface solicita outro nome; não há fusão implícita.
- **T-005 — exclusão:** remover a tag e suas linhas em `expense_tag` em uma transação, mantendo os lançamentos. A UI exibe a quantidade de classificações que serão removidas e solicita confirmação. O histórico financeiro permanece intacto; somente o agrupamento daquela tag deixa de existir.
- **T-006 — composição técnica:** recomenda-se um único módulo `:app` e injeção manual inicial. Essa recomendação não altera requisitos de produto e será reavaliada na implementação aprovada.

## Planejamento da Fase 17 — backup manual local

Decisão posterior: snapshot SQLite independente, preferencialmente `VACUUM INTO` quando disponível e adequado, alternativa segura, manifesto JSON auxiliar com SHA-256, armazenamento SAF local e restauração com recuperação. Substitui a preferência inicial por cópia física como mecanismo único e a ausência de manifesto; não converte registros nem dados SQLite. Implementação autorizada em 2026-10-28; pendências e limitações efetivas em [status](phases/phase-17-local-backup/STATUS.md) e [escopo](phases/phase-17-local-backup/SCOPE.md).

## Planejamento da Fase 15 — orçamento associado a tags

- **D-007:** O orçamento associado à tag será opcional e persistido como `Long?` em centavos; `NULL` significa ausência e `0L` significa zero explicitamente definido. A migração Room 1→2 adicionará uma coluna nullable sem apagar dados ou associações.
- **D-008:** A entrada seguirá `MoneyTextField` e a convenção brasileira existente. Valores negativos, inválidos e fora do intervalo de `Long` serão rejeitados. A criação idempotente de uma tag já existente não sobrescreverá seu orçamento; somente a edição explícita poderá alterá-lo ou removê-lo.
- **D-009:** Esta decisão autoriza apenas cadastro, edição e persistência no formulário de tags. Não autoriza exibição, cálculo, comparação, acompanhamento, alertas ou relatórios por tag.

## Pendências reais
- **P-001:** não há pendências de produto para este planejamento. A autorização da Fase 01 foi recebida.
- **P-002:** AGP `9.4.1` foi confirmado pelo responsável e está configurado como versão oficial do projeto. Gradle `9.6.0` e JDK 17 permanecem oficiais.
- **P-003:** O responsável autorizou uma versão Kotlin compatível com KSP/Room estáveis. Foi aplicada Kotlin `2.2.10` com KSP `2.3.12`, Room `2.8.5`, mantendo built-in Kotlin do AGP 9 e sem `org.jetbrains.kotlin.android`. A combinação compilou e processou as entidades; testes instrumentados ainda dependem de dispositivo conectado.

