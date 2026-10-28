# Critérios de aceitação — Fase 16

> Critérios de aceitação da implementação autorizada em 2026-10-27.

## Cálculo compartilhado

- [ ] Orçamento de R$ 500,00 e gasto de R$ 200,00 resultam em `40,00%`.
- [ ] Orçamento de R$ 500,00 e gasto de R$ 650,00 resultam em `130,00%`.
- [ ] Gasto igual ao orçamento resulta em `100,00%`.
- [ ] Orçamento positivo com gasto zero resulta em `0,00%`.
- [ ] Orçamento `NULL` não produz percentual nem representação que sugira limite configurado.
- [ ] Orçamento igual a zero não causa divisão por zero; gasto positivo recebe estado de estouro sem percentual numérico.
- [ ] Percentuais são arredondados para duas casas com `HALF_UP` exclusivamente para exibição.
- [ ] Barras são visualmente limitadas a 100%, mas o texto conserva o valor real acima de 100%.

## Dados e reatividade

- [ ] Tags com orçamento e sem lançamentos aparecem com gasto zero.
- [ ] Tags sem orçamento e com gasto aparecem como `Sem orçamento`.
- [ ] Inclusão, edição e exclusão de despesas atualizam o consumo da tag afetada.
- [ ] Alterações das associações de tags atualizam o consumo das tags afetadas.
- [ ] Alteração do orçamento de uma tag atualiza Home e Relatórios.
- [ ] Despesas associadas a múltiplas tags permanecem integralmente contabilizadas em cada tag.
- [ ] Orçamento global, saldo global e regras existentes não são alterados.

## Home e relatório

- [ ] Home e Relatórios mostram o mesmo gasto, orçamento, percentual e estado para a mesma tag e mês.
- [ ] A Home usa o mês atual local, sem seletor de mês nesta fase.
- [ ] O Relatório permanece mensal; não compara orçamento mensal diretamente com gastos de intervalos de vários meses.
- [ ] Tags acima do orçamento possuem destaque visual acessível e percentual real acima de 100%.
- [ ] Tags sem orçamento não exibem barra de consumo nem percentual enganoso.

## Compatibilidade

- [ ] Não há migração, tabela, dependência, versão ou alteração não autorizada no CRUD de tags.
- [ ] Dados existentes e comportamento atual do orçamento mensal permanecem preservados.
