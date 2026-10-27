# Estratégia de Testes

| Grupo | Objetivo e cenários | Aprovação |
|---|---|---|
| Unitários | U-01 valida entrada decimal, conversão segura para centavos e descrição; U-02 soma multitag sem duplicar e inclui lançamento sem tag; U-03 permite saldo negativo; U-04 sugere orçamento; U-05 renomeia sem quebrar associações e exclui tag removendo somente classificações. | Regras RN-001–012 aprovadas. |
| Room/DAO | DAO-01 unicidade de mês/FKs; DAO-02 tag normalizada, relação única, reutilização entre lançamentos, zero associações válidas e transação de exclusão sem apagar gastos. | Banco em memória, transações e constraints verificadas. |
| Integração | I-01 inclusão/edição/exclusão atualiza os dois meses; I-02 reinício e modo offline; I-03 exclusão de tag mantém lançamentos e seus totais. | Fluxo persiste sem rede e sem corrupção de histórico. |
| Compose/navegação | UI-01 estados vazios, formulários com tags opcionais, busca/filtro inclusive sem tag, diálogo de impacto da exclusão e rotas. | Estados e fluxos funcionais observáveis. |
| Migração | Cada mudança futura parte de bancos versionados representativos. | Dados existentes preservados. |
| Regressão | Roteiro RF, estados funcionais e fluxos críticos. | Sem regressões nos RF aceitos. |

Fronteiras: 1 centavo, máximo aceito, overflow, orçamento zero, mesma data em mudança de mês, múltiplas tags e períodos vazios. Testes são planejados, não executados nesta fase.

