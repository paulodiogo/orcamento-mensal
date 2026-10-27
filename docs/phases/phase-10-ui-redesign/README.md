# Fase 10 — Redesign incremental da interface

**Estado:** Proposta de planejamento; implementação não iniciada.

Esta fase foi criada a partir do diagnóstico solicitado para modernizar a interface sem alterar o modelo de dados, as regras de negócio ou os contratos dos ViewModels. A execução requer autorização explícita após a revisão deste plano.

## Documentos

- [Escopo e diagnóstico](SCOPE.md)
- [Tarefas](TASKS.md)
- [Critérios de aceitação](ACCEPTANCE.md)
- [Status](STATUS.md)

## Princípios

- Preservar Room, centavos em `Long`, datas civis `YYYY-MM-DD` e as agregações existentes.
- Implementar em etapas pequenas, validando cada tela antes da próxima.
- Usar Compose/Material 3 e as dependências já existentes.
- Não criar destinos de navegação sem funcionalidade real.
- Não modificar esquema, regras de cálculo ou contratos de estado para facilitar o visual.
