# Tarefas futuras — Fase 16

O escopo foi autorizado em 2026-10-27; tarefas em execução.

## F16-01 — Aplicar contrato funcional aprovado

- Usar o orçamento atualmente configurado para a tag no mês atual local.
- Restringir os cálculos ao mês atual do dispositivo; não adicionar seletor à Home nem novos filtros no relatório.
- Usar os estados `Sem orçamento`, `Indisponível` e `Acima do orçamento` conforme `ACCEPTANCE_CRITERIA.md`.

**Dependência:** nenhuma; decisões confirmadas pelo usuário.

## F16-02 — Criar modelo e cálculo compartilhado

- Criar modelo de domínio para gasto, orçamento opcional, percentual e estado de consumo da tag.
- Reutilizar `BigDecimal`, `RoundingMode.HALF_UP` e `Long` em centavos.
- Cobrir ausência, zero, gasto zero, igualdade, estouro e arredondamento.

**Dependência:** F16-01.

## F16-03 — Criar projeção e consulta Room reativa

- Criar projeção que combine tag, orçamento opcional e gasto no mês.
- Usar `LEFT JOIN` para incluir tags orçadas sem lançamentos.
- Expor `Flow` que seja invalidado por mudanças em `tag`, `expense_tag` e `expense`.
- Preservar o valor integral em cada associação de despesas com múltiplas tags.

**Dependência:** F16-02.

## F16-04 — Adaptar estado e ViewModel do relatório

- Substituir ou complementar `tagTotals` por execuções de orçamento de tag.
- Reutilizar a consulta e o cálculo compartilhados.
- Manter cálculo e visualização do orçamento global inalterados.

**Dependência:** F16-03.

## F16-05 — Atualizar Relatórios e Home

- Trocar a barra atual de participação no gasto global por consumo do orçamento da tag em Relatórios.
- Adicionar seção compacta de orçamento por tag na Home.
- Adicionar seletor mensal na Home somente após aprovação de F16-01.
- Preservar percentual textual real e limitar apenas a barra a 100%.

**Dependência:** F16-04.

## F16-06 — Cobrir domínio, Room e Compose

- Criar testes unitários de cálculo.
- Cobrir a consulta Room, tags sem lançamentos, `NULL`, zero e despesas multi-tag.
- Cobrir estados visíveis de Home e Relatórios e a consistência entre ambas.

**Dependência:** F16-05.

## F16-07 — Documentar e validar

- Atualizar documentos globais relacionados à regra aprovada.
- Executar as validações previstas e registrar apenas evidências reais.
- Revisar o diff quanto a escopo, versões, dependências, segredos e regressões.

**Dependência:** F16-06.
