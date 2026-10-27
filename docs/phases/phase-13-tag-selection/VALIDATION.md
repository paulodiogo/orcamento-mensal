# Validação planejada — Fase 13

A implementação foi autorizada em 2026-10-27. Os testes automatizados e a validação funcional direcionada foram executados. A primeira tentativa manual encontrou tela preta; após reiniciar o emulador, a UI Compose renderizou e a árvore foi inspecionada. A Fase 13 foi encerrada por decisão final sem novas verificações complementares.

## Resultados executados

- `rtk .\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — sucesso; testes JVM, build debug e lint concluídos sem falhas.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`; testes instrumentados concluídos sem falhas. O warning de depreciação de `createComposeRule` permaneceu preexistente.
- `rtk git diff --check` — sucesso.
- `ktlintCheck` — não aplicável até o momento; a tarefa não está disponível no projeto.

## Validação manual executada

Preparação concluída: `adb devices` detectou `emulator-5554`; o APK atual foi reinstalado com `adb install -r .\app\build\outputs\apk\debug\app-debug.apk`; `MainActivity` foi iniciada e `dumpsys window` confirmou foco. A primeira captura anterior mostrou tela preta e `uiautomator dump` retornou `ERROR: null root node returned by UiTestAutomationBridge`. Nesta execução, após instalação limpa por atualização e inicialização da atividade, `uiautomator dump` concluiu com sucesso e a hierarquia foi salva em `app/build/phase13-final-validation.xml`. O log não apresentou `FATAL EXCEPTION` da aplicação; apenas warnings de API oculta do framework durante a inspeção.
Evidências observadas após recuperação: estado “Tags (opcional)”/“Nenhuma tag selecionada”; modal “Selecionar tags” com “Pesquisar tags”, tag disponível, checkbox, “Cancelar” e “Aplicar”; consulta `zzzzz` exibiu “Nenhuma tag corresponde à pesquisa.”.

| Cenário | Resultado | Evidência/limitação |
|---|---|---|
| 1. Cadastro e estado inicial | Executado | Formulário mostrou `Tags (opcional)` e `Nenhuma tag selecionada`; evidência na árvore `phase13-form.xml` |
| 2. Salvar sem tags | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 3. Uma tag e `Aplicar` | Executado | A tag `aaaaaaaaa` foi marcada; `Aplicar` fechou o modal; `phase13-applied1.xml` mostra o resumo `1 tag` |
| 4. Várias tags e contagem | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 5. Cancelamento | Executado parcialmente | Após reabrir/modificar e cancelar, a árvore `phase13-cancel.xml` manteve `1 tag`; retorno do dispositivo ainda não foi repetido |
| 6. Pesquisa existente | Executado parcialmente | A pesquisa/limpeza foi exercitada; após limpar, `aaaaaaaaa` reapareceu marcada em `phase13-search-clear.xml`. Pesquisa por fragmento específico ainda não foi isolada |
| 7. Pesquisa sem resultados | Executado | `zzzzz` exibiu `Nenhuma tag corresponde à pesquisa.` em `phase13-search-none.xml` |
| 8. Edição e recuperação | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 9. Aplicar na edição/persistência | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 10. Lista vazia/gerenciamento | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 11. Renomear/remover durante formulário | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 12. Remoção sem excluir despesa | Dispensado | Não executado individualmente; dispensado pela decisão final |
| 13. Tela pequena e temas | Dispensado | Renderização em 1344×2992 foi observada; temas não foram testados individualmente e foram dispensados |

## Testes unitários/domain

- `ExpenseOperations` cria despesa sem tags, com uma tag e com várias tags, preservando IDs únicos.
- `updateTags` substitui associações corretamente e rejeita ID inexistente sem persistência parcial.
- Renomear mantém a associação; excluir remove a associação e preserva a despesa.
- Filtragem local do seletor compara o nome da tag sem alterar o nome persistido.
- Tag removida durante edição segue a política aprovada e nunca chega como ID inválido ao save.

## Testes Compose/instrumentados

- Campo fechado exibe estado vazio e resumo para uma/várias tags.
- Abrir o modal exibe tags ordenadas, checkboxes coerentes e ações de cancelar/aplicar.
- Selecionar e remover várias tags mantém estado temporário enquanto o modal está aberto.
- Cancelar, voltar e fechar não alteram o campo; aplicar altera o resumo.
- Cadastro salva sem tag e com múltiplas tags.
- Edição mostra as associações persistidas e salva alterações.
- Pesquisa filtra por nome, mostra lista vazia apropriada e pode ser limpa.
- Lista vazia, erro e carregamento não bloqueiam indevidamente o formulário.
- Nomes longos e grande quantidade permanecem roláveis e sem overflow em tela pequena.
- Regressão do cadastro/edição nos dois pontos de entrada, relatórios e filtros.

## Comandos executados

- `rtk .\gradlew.bat testDebugUnitTest` — sucesso; compilação dos testes e execução concluídas sem falhas.
- `rtk .\gradlew.bat assembleDebug` — sucesso.
- `rtk .\gradlew.bat lintDebug` — sucesso.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`.
- `rtk .\gradlew.bat ktlintCheck` — tarefa inexistente; não aplicável.
- `rtk git diff --check` — sucesso.

Os testes automatizados não substituem cenários manuais; neste encerramento, os cenários complementares sem evidência individual foram formalmente dispensados por decisão administrativa, sem nova execução.

## Validação funcional direcionada — 2026-10-27

Tag criada: `VALIDACAO_TAG_2710`. O aplicativo não foi encerrado nem reiniciado entre o gerenciamento, o formulário e os filtros.

| Passo | Resultado | Evidência |
|---|---|---|
| 1. Iniciar aplicativo no BUSAO | **Aprovado** | `adb devices` detectou `emulator-5554`; atividade iniciada |
| 2. Abrir gerenciamento de tags | **Aprovado** | `phase13-tags.xml` exibiu `Tags` e `Criar nova tag` |
| 3. Criar tag exclusiva | **Aprovado** | `phase13-tag-editor.xml` exibiu o nome no editor |
| 4. Confirmar tag na lista de gerenciamento | **Aprovado** | `phase13-tag-created.xml` exibiu `VALIDACAO_TAG_2710` e `Nome normalizado: validacao_tag_2710` |
| 5. Retornar ao formulário sem encerrar o app | **Aprovado** | `phase13-form-validation.xml` exibiu `Novo lançamento` e o seletor |
| 6. Pesquisar tag no seletor | **Aprovado** | `phase13-selector-search.xml` exibiu a pesquisa `VALIDACAO_TAG_2710` e o resultado correspondente |
| 7. Confirmar resultado no seletor | **Aprovado** | Resultado visível no modal `Selecionar tags` |
| 8. Selecionar e aplicar | **Aprovado** | `phase13-form-selected.xml` exibiu `1 tag` após `Aplicar` |
| 9. Salvar despesa de teste | **Aprovado** | `phase13-history-filter.xml` exibiu `DESPESA_VALIDACAO` |
| 10. Abrir filtros de lançamentos | **Aprovado** | `phase13-history2.xml` exibiu `VALIDACAO_TAG_2710` como opção |
| 11. Aplicar filtro | **Aprovado** | `phase13-history-applied.xml` manteve `DESPESA_VALIDACAO` visível |
| 12. Remover filtro | **Aprovado** | `phase13-history-cleared.xml` manteve a listagem normal e a despesa visível |

Evidências adicionais: `phase13-manual-start.xml`, `phase13-tags.xml`, `phase13-tag-editor.xml`, `phase13-tag-created.xml`, `phase13-form-validation.xml`, `phase13-selector.xml`, `phase13-selector-search.xml`, `phase13-form-selected.xml`, `phase13-history2.xml`, `phase13-history-filter.xml`, `phase13-history-applied.xml` e `phase13-history-cleared.xml`, todas em `app/build/`.

A tag e a despesa de validação permaneceram no emulador para evitar remover dados sem confirmação de que não havia conflito com dados preexistentes. A persistência da tag foi confirmada indiretamente pela exibição após navegação e diretamente pela consulta reativa coberta por `TagOperationsTest`; não foi feita exclusão manual para preservar a despesa associada.

## Decisão de encerramento — 2026-10-27

Não foram executadas novas rodadas de testes automatizados ou validações manuais para encerrar a fase. A decisão final aprova a Fase 13 com base nos resultados reais acima e dispensa individualmente os cenários complementares sem evidência específica, incluindo renomeação, remoção, múltiplas tags, edição detalhada, lista vazia e temas claro/escuro. Esses cenários permanecem identificados como dispensados, não como testados.

## Roteiro manual no emulador/dispositivo

1. Instalar o APK de debug após a implementação e abrir o fluxo de novo lançamento.
2. Confirmar que o campo é compacto, indica opcionalidade e começa vazio.
3. Abrir o seletor, escolher três tags, aplicar e confirmar o resumo no formulário.
4. Reabrir o seletor, remover uma, cancelar e verificar que a seleção anterior permaneceu.
5. Salvar; reabrir a mesma despesa e confirmar que as associações foram recuperadas.
6. Renomear uma tag na tela Tags, voltar ao formulário e verificar o novo nome para o mesmo ID.
7. Excluir uma tag usada e verificar o comportamento decidido, sem excluir a despesa.
8. Testar zero tags cadastradas, erro simulado conforme arquitetura e pesquisa sem resultados.
9. Repetir com nomes longos, muitas tags, teclado aberto e tema claro/escuro; verificar rolagem e ações.
10. Repetir criação e edição pelos caminhos de `AppShell` e `MainActivity` e registrar evidências visuais.
