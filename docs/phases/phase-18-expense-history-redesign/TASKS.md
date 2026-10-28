# Tarefas propostas — Fase 18

## Estado

**Fase concluída e encerrada documentalmente em 2026-10-28.** F18-01 a F18-06 foram implementadas, validadas e registradas. As verificações visuais, de responsividade, tema e acessibilidade foram aprovadas manualmente conforme confirmação do usuário.

## F18-01 — Confirmar escopo e baseline

- Confirmar aprovação do redesign e que a referência visual continua disponível em [`docs/references/lancamentos_novo.png`](../../references/lancamentos_novo.png).
- Registrar a árvore de trabalho e preservar alterações preexistentes.
- Confirmar que nenhum requisito de negócio, schema, dependência ou versão faz parte do escopo.

**Aceite:** escopo visual e exclusões confirmados antes de editar produção. **Concluída.**

## F18-02 — Criar leitura única de lançamentos com tags

- Criar projeção Room com lançamento e tag opcional.
- Adicionar consulta reativa única baseada em `LEFT JOIN` que inclua lançamentos sem tag.
- Agrupar resultados no ViewModel sem consulta de tags por lançamento.
- Preservar ordenação, reatividade e dados necessários aos filtros.

**Dependência:** F18-01.

**Aceite:** lista enriquecida contém data, descrição, valor e zero/uma/múltiplas tags; não há padrão de consulta individual por lançamento. **Concluída.**

## F18-03 — Adaptar estado, filtros e formatação de data

- Ajustar `ExpenseListState`/ViewModel para expor o modelo enriquecido sem alterar as operações de CRUD.
- Preservar `filterExpenses` para pesquisa, mês, intervalo, tag e “Sem tag”.
- Adicionar formatador de leitura `dia mês` em português, sem ano, exclusivamente para o cartão do histórico.

**Dependência:** F18-02.

**Aceite:** os mesmos filtros retornam os mesmos lançamentos; data ISO válida produz formato como `15 out`; data inválida não gera representação enganosa. **Concluída.**

## F18-04 — Implementar cartão de histórico

- Criar componente Compose específico para um lançamento.
- Posicionar data à esquerda, descrição e chips em fluxo central, valor abaixo e ações à direita.
- Aplicar cores, tipografia e superfícies de `MaterialTheme`.
- Manter callbacks independentes de editar e excluir e o diálogo de confirmação já existente.

**Dependência:** F18-03.

**Aceite:** o componente apresenta a referência visual sem modificar `SummaryCard` compartilhado nem a navegação/FAB. **Concluída.**

## F18-05 — Cobrir acessibilidade e responsividade

- Garantir alvos de toque adequados e descrições semânticas para editar/excluir.
- Testar largura reduzida, fonte ampliada, descrição longa, tags longas, múltiplas tags e ausência de tags.
- Revisar tema claro/escuro sem cores fixas incompatíveis.

**Dependência:** F18-04.

**Aceite:** não há sobreposição, clipping nem ação ambígua nos cenários definidos. **Concluída por testes e validação manual.**

## F18-06 — Validar e registrar evidências

- Executar testes unitários, Room/instrumentados e Compose definidos em [VALIDATION.md](VALIDATION.md).
- Executar `rtk .\gradlew.bat testDebugUnitTest`, `rtk .\gradlew.bat assembleDebug`, `rtk .\gradlew.bat lintDebug` e `rtk git diff --check`.
- Executar `rtk .\gradlew.bat connectedDebugAndroidTest` apenas se houver dispositivo/emulador disponível.
- Registrar somente comandos e resultados realmente obtidos.
- Fazer inspeção manual de acessibilidade e responsividade.

**Dependência:** F18-05.

**Aceite:** critérios de [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md) são evidenciados sem alterar versões, dependências ou regras de negócio. **Concluída.**
