# Estratégia de validação — Fase 18

## Estado final

**Validação concluída e registrada.** Os comandos automatizados abaixo foram executados anteriormente e seus resultados permanecem consistentes. As verificações visuais, de responsividade, tema e acessibilidade foram aprovadas manualmente pelo usuário; essa aprovação é distinta dos testes automatizados e não foi repetida nesta etapa de encerramento.

## Resultados executados

- `rtk .\\gradlew.bat testDebugUnitTest` — sucesso após correção do formatador; 40 testes concluídos.
- `rtk .\\gradlew.bat assembleDebug lintDebug` — sucesso.
- `rtk .\\gradlew.bat connectedDebugAndroidTest` — sucesso no dispositivo `BUSAO(AVD) - 16`; a saída resumida do Gradle não reportou contagem, falhas ou ignorados.
- `problems` nos arquivos Kotlin alterados — nenhum erro reportado.
- `rtk git diff --check` — sucesso.
- `rtk git status --short --branch` — branch `master` sem troca; alterações da Fase 18 e documentos/referência não rastreados listados.
- Após o ajuste fino dos chips, `rtk .\\gradlew.bat testDebugUnitTest` — sucesso; `rtk .\\gradlew.bat assembleDebug lintDebug` — sucesso; `problems` — sem erros no arquivo alterado; `rtk git diff --check` — sucesso.

## Testes unitários

- Formatador de data do histórico: `2026-10-15` resulta em `15 out`; cobrir meses distintos e valor ISO inválido/vazio.
- Agrupamento de projeção: lançamento sem tag, uma tag e múltiplas tags preservam IDs e nomes.
- Filtros: pesquisa por descrição, tag específica, “Sem tag”, mês/período e combinações preservam a semântica de `filterExpenses` após o novo modelo de apresentação.
- Formatação monetária: manter cenários existentes de `formatBrazilianCents` e `MoneyFormattingTest`.

## Testes Room/instrumentados

- Consulta reativa única inclui lançamentos sem tag por `LEFT JOIN`.
- Consulta retorna uma ou várias tags associadas por lançamento, com nomes corretos.
- Inserção, edição, exclusão e alteração de associações atualizam a emissão da lista.
- Ordenação por data e ID continua idêntica à atual.
- Teste deve exercer a projeção única, sem chamar `findTagIdsForExpense` uma vez por lançamento durante a montagem da lista.

## Testes Compose/instrumentados

- Cartão mostra data com dia e mês, sem ano.
- Descrição, chip e valor aparecem na hierarquia correta; lançamento sem tag não apresenta chip.
- Tag aparece ao lado da descrição em largura suficiente.
- Descrição longa, tag longa e múltiplas tags não produzem sobreposição em largura reduzida.
- Editar chama exclusivamente o callback de edição; excluir abre exclusivamente a confirmação e sua confirmação chama a exclusão.
- Pesquisa e filtros “Sem tag”, “Alimentação” e “Mercado” continuam atualizando a lista.
- FAB e navegação inferior continuam acessíveis.

## Validação manual

**Aprovada manualmente pelo usuário.**

- Comparação do cartão com [`docs/references/lancamentos_novo.png`](../../references/lancamentos_novo.png), incluindo temas claro e escuro.
- Verificação de largura reduzida, fonte ampliada, rolagem, quebra de textos/chips e ausência de clipping.
- Verificação de acessibilidade: data, descrição, tags, valor e rótulos de ações em ordem compreensível, incluindo TalkBack quando disponível.
- Criação, edição, exclusão e cancelamento de exclusão em lançamentos com e sem tags.

## Comandos previstos

```powershell
rtk .\gradlew.bat testDebugUnitTest
```

```powershell
rtk .\gradlew.bat assembleDebug lintDebug
```

```powershell
rtk .\gradlew.bat connectedDebugAndroidTest
```

```powershell
rtk git diff --check
```

Os resultados acima refletem execuções anteriores; não foram repetidos nesta etapa de encerramento, conforme solicitado. Nenhum número adicional de testes foi inventado.
