# Validação planejada — Fase 13

A implementação foi autorizada em 2026-10-27. Os testes automatizados foram executados. A primeira tentativa manual encontrou tela preta; após reiniciar o emulador, a UI Compose renderizou e a árvore foi inspecionada. A validação manual completa ainda está em andamento.

## Resultados executados

- `rtk .\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — sucesso; testes JVM, build debug e lint concluídos sem falhas.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`; testes instrumentados concluídos sem falhas. Warnings de depreciação de `createComposeRule` permaneceram preexistentes.
- `rtk git diff --check` — sucesso.
- `ktlintCheck` — não aplicável até o momento; a tarefa não está disponível no projeto.

## Validação manual executada

Preparação concluída: `adb devices` detectou `emulator-5554`; o APK atual foi reinstalado com `adb install -r .\app\build\outputs\apk\debug\app-debug.apk`; `MainActivity` foi iniciada e `dumpsys window` confirmou foco. A primeira captura mostrou tela preta e `uiautomator dump` retornou `ERROR: null root node returned by UiTestAutomationBridge`. Após `adb reboot`, sem limpar dados, a atividade foi iniciada novamente: a captura transferida diretamente do dispositivo mostrou a tela; `uiautomator dump` identificou `ComposeView` e textos da tela inicial/formulário/modal. A causa da primeira tela preta não foi uma exceção do aplicativo; o log não apresentou `FATAL EXCEPTION`, `AndroidRuntime` da aplicação, erro Compose, Room ou SQLite. O reinício do emulador recuperou a renderização, portanto a hipótese atual é falha transitória do ambiente gráfico/SurfaceFlinger/UiAutomation, não defeito confirmado no código.

Evidências observadas após recuperação: estado “Tags (opcional)”/“Nenhuma tag selecionada”; modal “Selecionar tags” com “Pesquisar tags”, tag disponível, checkbox, “Cancelar” e “Aplicar”; consulta `zzzzz` exibiu “Nenhuma tag corresponde à pesquisa.”.

| Cenário | Resultado | Evidência/limitação |
|---|---|---|
| 1. Cadastro e estado inicial | Executado | Formulário mostrou `Tags (opcional)` e `Nenhuma tag selecionada`; evidência na árvore `phase13-form.xml` |
| 2. Salvar sem tags | Pendente | Ainda não preenchido/salvo manualmente |
| 3. Uma tag e `Aplicar` | Executado | A tag `aaaaaaaaa` foi marcada; `Aplicar` fechou o modal; `phase13-applied1.xml` mostra o resumo `1 tag` |
| 4. Várias tags e contagem | Pendente | Não executado |
| 5. Cancelamento | Executado parcialmente | Após reabrir/modificar e cancelar, a árvore `phase13-cancel.xml` manteve `1 tag`; retorno do dispositivo ainda não foi repetido |
| 6. Pesquisa existente | Executado parcialmente | A pesquisa/limpeza foi exercitada; após limpar, `aaaaaaaaa` reapareceu marcada em `phase13-search-clear.xml`. Pesquisa por fragmento específico ainda não foi isolada |
| 7. Pesquisa sem resultados | Executado | `zzzzz` exibiu `Nenhuma tag corresponde à pesquisa.` em `phase13-search-none.xml` |
| 8. Edição e recuperação | Pendente | Não executado |
| 9. Aplicar na edição/persistência | Pendente | Não executado |
| 10. Lista vazia/gerenciamento | Pendente | Não executado com banco sem tags |
| 11. Renomear/remover durante formulário | Pendente | Não executado |
| 12. Remoção sem excluir despesa | Pendente | Não executado |
| 13. Tela pequena e temas | Parcialmente executado | Resolução 1344×2992, modal e formulário renderizados; temas claro/escuro ainda pendentes |

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

- `rtk .\gradlew.bat testDebugUnitTest` — sucesso.
- `rtk .\gradlew.bat assembleDebug` — sucesso.
- `rtk .\gradlew.bat lintDebug` — sucesso.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`.
- `rtk .\gradlew.bat ktlintCheck` — tarefa inexistente; não aplicável.
- `rtk git diff --check` — sucesso.

Os testes automatizados não substituem os cenários manuais pendentes.

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
