# Critérios de aceitação — Fase 11

1. Todos os campos monetários editáveis estão identificados e os campos numéricos não monetários permanecem fora da máscara.
2. O cadastro e a edição de despesas aceitam apenas algarismos no fluxo efetivo e usam teclado numérico.
3. A digitação `1`, `12`, `123`, `1234`, `12345`, `123456` exibe respectivamente `0,01`, `0,12`, `1,23`, `12,34`, `123,45`, `1.234,56`.
4. Vírgula decimal e ponto de milhares são automáticos; o usuário não precisa digitá-los.
5. Cursor, seleção, backspace, zeros à esquerda, vazio, foco e recomposição têm comportamento documentado e coberto por testes.
6. Colagem, sinal negativo, caracteres inválidos, limite e overflow seguem política explícita e não conseguem persistir valor inválido.
7. A conversão visual resulta no mesmo `Long` em centavos, sem `Float`/`Double`, e `12345` resulta em `12345L`.
8. Despesa vazia/zero/inválida é rejeitada no domínio; valor válido é persistido sem alterar o esquema e valores existentes continuam editáveis.
9. Funções puras de formatação/conversão possuem testes para limites, milhares, zeros, vazio, colagem e conversão inversa.
10. Testes Compose/instrumentados cobrem teclado, digitação contínua, exclusão, cursor, colagem, edição existente e persistência após salvar.
11. Build, lint e regressão existentes passam; nenhum teste é removido ou enfraquecido.
12. Nenhuma dependência, versão, migração, regra financeira, branch, commit ou push é introduzido nesta fase de planejamento.
