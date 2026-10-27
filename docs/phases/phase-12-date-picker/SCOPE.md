# Escopo — Fase 12

## Campos identificados

| Campo/componente | Tipo atual | Valor inicial e comportamento atual | Proposta | DatePicker |
|---|---|---|---|---|
| `CreateExpenseDialog.occurredOn` em `app/src/main/kotlin/br/com/paulodiogo/orcamento/ExpenseForm.kt` | `String` ISO `yyyy-MM-dd` | `rememberSaveable(initialOccurredOn)`; cadastro vazio, edição recebe a data persistida; `OutlinedTextField` editável com rótulo `Data (AAAA-MM-DD)` | Cadastro: data local de hoje. Edição: preservar `initialOccurredOn`. Não atualizar ao mudar o dia enquanto o diálogo está aberto | Sim; toque no campo ou ícone de calendário abre o calendário na data selecionada |
| Filtro de mês em `MainActivity.kt` / `ExpenseListScreen` | `String` `YYYY-MM` | Vazio; filtra somente quando informado e pode ser limpo | Preservar vazio e semântica atual. Não preencher com hoje automaticamente, pois isso mudaria a lista inicial | Não é DatePicker diário; se futuramente houver seletor mensal, usar componente de mês explicitamente aprovado |
| Filtro `rangeStart` em `MainActivity.kt` | `String?`/texto UI | Vazio; enviado como limite inclusivo quando preenchido | Preservar vazio para não restringir a consulta inicial | Sim, DatePicker diário, somente quando o usuário abrir o filtro |
| Filtro `rangeEnd` em `MainActivity.kt` | `String?`/texto UI | Vazio; enviado como limite inclusivo quando preenchido | Preservar vazio para não restringir a consulta inicial | Sim, DatePicker diário, somente quando o usuário abrir o filtro |
| `BudgetScreen.month` | `String` `YYYY-MM` | Fixo em `2026-10`; `openMonth(month)` valida o mês | Substituir o valor fixo por uma inicialização local coerente com o mês corrente somente após decisão/autorização, mantendo edição e consulta explícitas | Não é DatePicker diário; requer seletor mensal, não deve ser tratado como data de lançamento |
| `ReportScreen.month` | `String` `YYYY-MM` | Fixo em `2026-10`; recarrega relatório em `LaunchedEffect(month)` | Preservar semântica de período e eliminar valor fixo em etapa própria; não aplicar DatePicker diário | Não; eventual seletor mensal separado |
| `AppShell.todayMonth`/`HomeScreen` | `String` `YYYY-MM` derivada de `YearMonth.now()` | Mês local atual, calculado uma vez por composição salva; usado no resumo | Preservar como mês de referência do resumo; avaliar atualização apenas ao recriar a tela, nunca sobrescrever seleção do usuário | Não |
| Datas exibidas na lista/cartões (`expense.occurredOn`) | `String` | Somente leitura, mostradas em ISO | Manter valor de domínio; eventual apresentação brasileira é decisão de UI, sem alterar persistência | Não |

## Cadastro, edição e persistência

O fluxo de criação deve receber uma data inicial calculada com `LocalDate.now()` no dispositivo no instante de abertura. O formulário mantém a data em estado local e só grava após confirmação. A edição deve receber a data da entidade, inclusive para despesas antigas, retroativas ou de outro mês. O callback existente continua entregando `String` ao ViewModel e `ExpenseOperations`, que já valida `yyyy-MM-dd` e persiste `occurredOn`.

A seleção deve ser apresentada como `dd/MM/yyyy` para o usuário, com conversão estrita e local para `yyyy-MM-dd` na borda. Não usar epoch/UTC para evitar deslocamento de dia. Cancelamento do diálogo do calendário deve descartar apenas o estado temporário do picker.

## Exclusões

- Nenhuma mudança no esquema Room, entidades, DAOs, migrações ou formato persistido.
- Nenhuma alteração em regras financeiras, isolamento mensal, tags ou filtros de conteúdo.
- Nenhuma dependência ou atualização de versão.
- Nenhum calendário para campos que são meses (`YYYY-MM`) sem uma decisão específica sobre seletor mensal.
- Nenhuma auditoria formal de acessibilidade; serão considerados estados de foco, rótulos e tamanho de tela como parte da implementação normal.
