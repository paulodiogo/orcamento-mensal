# Critérios de Aceitação e Rastreabilidade

| RF | Critério verificável | Regras | Dados | Tela | Teste | Fase |
|---|---|---|---|---|---|---|
| RF-001 | Dado mês sem orçamento, quando acessar resumo, então vê CTA de configuração. | RN-001 | monthly_budget | Resumo | UI-01 | 6 |
| RF-002 | Dado orçamento, quando salvar valor válido, então existe um único registro do mês. | RN-001,12 | monthly_budget | Orçamento | DAO-01 | 2 |
| RF-003 | Dado mês anterior, quando abrir novo mês, então valor é sugerido e editável. | RN-004,12 | monthly_budget | Abrir mês | U-04 | 7 |
| RF-004 | Dado lançamento válido sem tags, quando salvar, então ele pertence ao mês de sua data, aparece na lista geral e é incluído no total mensal. | RN-002,6,7 | expense | Lançamento | U-01 | 5 |
| RF-005 | Dado lançamento com uma ou várias tags, quando resumir mês, então seu valor conta uma vez no total e cada classificação usa o valor integral. | RN-003,7,8 | expense_tag | Resumo | U-02 | 6 |
| RF-006 | Dada tag inexistente, quando confirmada durante o lançamento, então é criada; dada chave normalizada existente, então a tag é reutilizada em diferentes lançamentos. | RN-007,9,11 | tag/expense_tag | Lançamento | DAO-02 | 4 |
| RF-007 | Dado lançamento editado/excluído, quando confirmar, então totais afetados são atualizados sem depender de tags. | RN-011 | expense | Lista | I-01 | 5 |
| RF-008 | Dado gasto acima do orçamento, quando salvar, então persiste e saldo é negativo. | RN-004,12 | expense | Resumo | U-03 | 6 |
| RF-009 | Dada tag associada, quando excluir e confirmar o impacto, então somente suas classificações são removidas; lançamentos e valores permanecem. | RN-010,11 | tag/expense_tag/expense | Detalhe tag | U-05 | 4 |
| RF-010 | Dado reinício sem rede, quando abrir aplicativo, então dados persistidos são exibidos. | RNF-001,002 | todas | Resumo | I-02 | 9 |

IDs de teste são definidos em [TEST_STRATEGY.md](TEST_STRATEGY.md). Todos os RF têm aceite observável; não existem requisitos para limite por tag.

