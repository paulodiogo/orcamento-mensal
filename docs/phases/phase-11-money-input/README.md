# Fase 11 — Planejamento de entrada monetária

**Estado:** Implementação concluída e validada localmente.

Esta fase implementa a melhoria dos campos monetários no cadastro/edição de despesas e orçamento mensal, sem alterar o esquema Room ou as regras financeiras.

## Documentos

- [Escopo, diagnóstico e solução](SCOPE.md)
- [Tarefas planejadas](TASKS.md)
- [Critérios de aceitação](ACCEPTANCE.md)
- [Status e decisões pendentes](STATUS.md)
- [Estratégia de validação](VALIDATION.md)

## Princípios

- A representação de domínio continua sendo `Long` em centavos.
- A representação textual formatada permanece responsabilidade da UI/formatador, não da persistência.
- A regra atual de lançamento exige valor maior que zero; orçamento continua aceitando zero ou mais.
- Nenhuma dependência, versão, migração, entidade ou contrato existente é alterado nesta etapa.
- Resultados de build, lint, testes e inspeção do emulador estão registrados em [validação](VALIDATION.md).
