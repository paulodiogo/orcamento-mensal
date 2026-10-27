# Riscos

| ID | Risco | Impacto | Mitigação |
|---|---|---|---|
| RK-001 | Desinstalação/falha do dispositivo perde dados sem backup. | Alto | Comunicar limitação; não prometer recuperação; avaliar exportação só em versão futura aprovada. |
| RK-002 | Soma por tag ser interpretada como total mensal. | Médio | Rotular que multitag pode exceder o total e testar RN-008. |
| RK-003 | Valores em ponto flutuante causarem discrepâncias. | Alto | Usar centavos inteiros e testar fronteiras. |
| RK-004 | Alteração de esquema perder dados. | Alto | Migrações versionadas e testes de migração. |
| RK-005 | Tags semelhantes fragmentarem análises. | Médio | Normalização e unicidade case-insensitive. |
| RK-006 | Requisitos pendentes virarem implementação prematura. | Alto | Aprovação por fase e status explícito. |

