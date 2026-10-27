# Status — Fase 12

- **Situação:** implementação autorizada, parcial e validada tecnicamente; fechamento da fase pendente de validação manual completa.
- **Levantamento:** concluído no código atual para cadastro/edição de despesas, filtros de intervalo, filtro de mês, orçamento, relatório, resumo mensal e exibição de datas.
- **Persistência:** `ExpenseEntity.occurredOn` permanece `String` ISO `yyyy-MM-dd`; não há necessidade comprovada de migração Room.
- **Proposta principal:** `LocalDate.now()` apenas para novo lançamento; edição usa a data persistida; filtros continuam vazios; meses usam semântica mensal própria.
- **Componentes:** Material 3 `DatePicker` é a opção preferencial, sujeito à confirmação de compatibilidade com as versões já fixadas. Não foi adicionada dependência.
- **Validação:** `testDebugUnitTest assembleDebug lintDebug` passou; `connectedDebugAndroidTest` passou com 27 testes; APK debug instalado e atividade iniciada no emulador. A validação manual interativa dos calendários ainda não foi concluída.

## Decisões pendentes

1. Autorizar a implementação da fase e das tarefas F12-01 a F12-06.
2. Confirmar se mês de orçamento e relatório devem iniciar no mês local atual, permanecer no valor atual até correção específica, ou receber seletor mensal.
3. Confirmar se os filtros `De`/`Até` devem receber DatePicker mantendo valor vazio inicial (proposta) e qual apresentação brasileira será adotada.
4. Confirmar se o formato visual `dd/MM/yyyy` deve ser aplicado também às datas somente leitura da lista/cartões ou apenas aos campos editáveis.

Nenhuma decisão pendente bloqueia o diagnóstico; todas as decisões de implementação dependem de autorização antes de editar produção.
