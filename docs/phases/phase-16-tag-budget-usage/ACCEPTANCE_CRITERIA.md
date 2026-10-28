# Critérios de aceitação — Fase 16

## Cálculo compartilhado

- [x] Orçamento de R$ 500,00 e gasto de R$ 200,00 resultam em `40,00%`.
- [x] Orçamento de R$ 500,00 e gasto de R$ 650,00 resultam em `130,00%`.
- [x] Gasto igual ao orçamento resulta em `100,00%`.
- [x] Orçamento positivo com gasto zero resulta em `0,00%`.
- [x] Orçamento `NULL` não produz percentual nem representação que sugira limite configurado.
- [x] Orçamento igual a zero não causa divisão por zero; gasto positivo recebe estado de estouro sem percentual numérico.
- [x] Percentuais são arredondados para duas casas com `HALF_UP` exclusivamente para exibição.
- [x] Barras são visualmente limitadas a 100%, mas o texto conserva o valor real acima de 100%.

## Dados e reatividade

- [x] Tags com orçamento e sem lançamentos aparecem com gasto zero.
- [x] Tags sem orçamento e com gasto aparecem como `Sem orçamento`.
- [x] Inclusão, edição e exclusão de despesas atualizam o consumo da tag afetada via fluxo observado pelo Room.
- [x] Alterações das associações de tags atualizam o consumo das tags afetadas via fluxo observado pelo Room.
- [x] Alteração do orçamento de uma tag atualiza Home e Relatórios via fluxo de tags.
- [x] Despesas associadas a múltiplas tags permanecem integralmente contabilizadas em cada tag.
- [x] Orçamento global, saldo global e regras existentes não são alterados.

## Home e relatório

- [x] Home e Relatórios mostram o mesmo gasto, orçamento, percentual e estado para a mesma tag e mês.
- [x] A Home usa o mês atual local, sem seletor de mês nesta fase.
- [x] O Relatório permanece mensal; não compara orçamento mensal diretamente com gastos de intervalos de vários meses.
- [x] Tags acima do orçamento possuem destaque visual e percentual real acima de 100%.
- [x] Tags sem orçamento não exibem barra de consumo nem percentual enganoso.

## Compatibilidade

- [x] Não há migração, tabela, dependência, versão ou alteração não autorizada no CRUD de tags.
- [x] Dados existentes e comportamento atual do orçamento mensal permanecem preservados.
