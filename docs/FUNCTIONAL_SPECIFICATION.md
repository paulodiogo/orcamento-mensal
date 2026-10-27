# Especificação Funcional Consolidada

## Orçamento e meses
Um mês é identificado por `YYYY-MM` local. Há no máximo um orçamento por mês. No primeiro acesso ou num mês sem orçamento, o resumo informa a ausência e oferece a configuração explícita. Ao abrir mês novo, propõe o valor do orçamento imediatamente anterior existente; sem anterior, apresenta valor vazio. O usuário pode editar antes de confirmar. Reconfigurar mês existente abre edição, não cria duplicata. Alterar orçamento após lançamentos preserva os gastos e recalcula saldo/percentual. Saldo negativo indica ultrapassagem; lançamentos válidos nunca são bloqueados.

## Lançamentos
Um lançamento contém identificador, descrição obrigatória normalizada, valor inteiro positivo em centavos, data local, timestamps e zero, uma ou mais tags opcionais. Salvar sem tag é válido e o lançamento permanece na lista geral, no total mensal e no saldo. Inclusão retroativa entra no mês da data, não no mês visualizado. Edição pode transferir o lançamento entre meses e atualiza ambos os resumos. Exclusão requer confirmação e atualiza cálculos. Pesquisa considera descrição; filtros combináveis incluem mês, período no mês e tags. A entrada decimal é convertida para `Long` em centavos com validação, sem `Float`/`Double`; a exibição usa formatação monetária da localidade.

## Tags
Tags globais são rótulos planos, não obrigatórios e sem hierarquia. Ao informar tag inexistente durante cadastro/edição, cria-se a tag e a associação na mesma operação; existentes são reutilizadas. Espaços externos são removidos, sequências internas viram um espaço e a comparação ignora maiúsculas/minúsculas: `Mercado` e `mercado` são a mesma tag. Renomear altera globalmente o nome exibido e preserva associações históricas; se a nova chave normalizada já existir, a alteração é recusada. Excluir uma tag informa quantas classificações serão removidas e, após confirmação, remove apenas a tag e suas associações em transação: lançamentos, valores, datas e descrições permanecem intactos e podem ficar sem tags. “Sem tag” é filtro/visualização transitória, não uma tag persistida.

## Resumo e relatórios
Para o mês selecionado: orçamento, soma única dos lançamentos, saldo `orçamento − gasto` e percentual `gasto / orçamento × 100`. Se orçamento for zero, percentual é indisponível; valores são formatados em moeda local. Indicadores visuais sugeridos: abaixo de 80%, 80% a menor que 100%, 100% ou mais; são informativos apenas no orçamento mensal. O agrupamento por tag conta o valor integral para cada tag: suas linhas podem somar mais que o gasto mensal. Não há rateio automático.

## Persistência e reinício
Dados salvos devem reaparecer após reinício. O aplicativo funciona sem internet. Sem backup próprio, perda/desinstalação/dano do armazenamento pode ser irrecuperável.

