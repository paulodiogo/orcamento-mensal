# Requisitos

A fonte detalhada é [FUNCTIONAL_SPECIFICATION.md](FUNCTIONAL_SPECIFICATION.md). A rastreabilidade e aceitação estão em [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md).

## Não funcionais
| ID | Requisito verificável |
|---|---|
| RNF-001 | Operar sem rede para todas as funções. |
| RNF-002 | Persistir localmente com Room sobre SQLite. |
| RNF-003 | Representar valores em centavos inteiros, sem `Float`/`Double` em cálculos. |
| RNF-004 | Preservar integridade por chaves estrangeiras, unicidades e transações. |
| RNF-005 | Suportar ao menos 10.000 lançamentos com paginação/consultas indexadas sem bloqueio perceptível da interface. |
| RNF-006 | Suportar tema claro/escuro e estados funcionais consistentes da interface. |
| RNF-007 | Não transmitir dados financeiros; não declarar permissões de rede. |
| RNF-008 | Possuir estados de carregamento, vazio, erro recuperável e sucesso. |
| RNF-009 | Prever migrações Room versionadas e testadas antes de evolução de esquema. |
| RNF-010 | Separar UI, estado, regras e persistência para permitir testes. |

