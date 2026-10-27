# Roteiro de Implementação

| Fase | Nome | Depende de | Saída / condição de conclusão |
|---|---|---|---|
| 00 | Planejamento | — | Documentos revisados e aprovados. |
| 01 | Fundações | 00 aprovada | Projeto Compose/Room preparado e validado. |
| 02 | Dados e persistência | 01 | Esquema, DAOs, migração inicial e testes Room. |
| 03 | Regras de negócio | 02 | Casos de uso e testes unitários RN. |
| 04 | Tags | 03 | Fluxo/consultas de tags e testes. |
| 05 | Lançamentos | 04 | CRUD, filtros e testes integrados. |
| 06 | Resumo e relatórios | 05 | Totais únicos e agrupamentos por tag testados. |
| 07 | Meses e histórico | 06 | Abertura explícita, sugestão/histórico testados. |
| 08 | Qualidade UX | 07 | Acessibilidade, erros e refinamento validados. |
| 09 | Validação final | 08 | Suite prevista, revisão e preparação de distribuição. |

Cada fase requer autorização explícita após sua conclusão. Consulte os documentos em `phases/` para escopo, tarefas, aceite e estado.

