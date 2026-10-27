# Validação planejada — Fase 14

## Estado

A validação visual foi concluída. A captura inicialmente inválida não era tela preta do aplicativo: o redirecionamento PowerShell acrescentou bytes UTF-16 ao PNG (`FF FE FD FF`), corrompendo o arquivo. O processo e a Activity estavam ativos, sem exceções relevantes. As capturas válidas foram geradas com redirecionamento nativo do `cmd.exe`, preservando os dados do AVD.

## Resultados executados

- `rtk .\gradlew.bat testDebugUnitTest` — sucesso.
- `rtk .\gradlew.bat assembleDebug lintDebug` — sucesso.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`; warnings de depreciação preexistentes em `createComposeRule`.
- `rtk .\gradlew.bat ktlintCheck` — falhou porque a tarefa não existe no projeto; não aplicável.
- `rtk git diff --check` — sucesso.
- `rtk adb install -r .\app\build\outputs\apk\debug\app-debug.apk` — sucesso.
- `rtk adb shell am start -n br.com.paulodiogo.orcamento/.MainActivity` — atividade iniciada.
- Redirecionamento PowerShell de `adb exec-out screencap -p` — arquivo inválido; inspeção mostrou prefixo UTF-16 `FF FE FD FF` antes da assinatura PNG.
- `cmd.exe /c "adb exec-out screencap -p > ...png"` — sucesso; PNGs válidos em `app/build/phase14-*-final.png` e `phase14-form-valid.png`.
- `adb shell uiautomator dump ...` — sucesso; árvores XML correspondentes salvas em `app/build/phase14-*.xml`.
- Diagnóstico: `emulator-5554` ficou `device`, a Activity apareceu como `topResumedActivity`, não houve `FATAL EXCEPTION`/falha de renderização relevante, e a tela válida mostrou a aplicação normalmente.

## Evidências visuais

- [Home](../../../app/build/phase14-home-final.png): título, conteúdo, FAB e barra inferior respeitam a janela.
- [Histórico](../../../app/build/phase14-expenses-final.png): cabeçalho e primeiro filtro seguem o mesmo topo visual.
- [Relatórios](../../../app/build/phase14-reports-final.png): título e seletor mensal alinhados ao contêiner comum.
- [Orçamento](../../../app/build/phase14-budget-final.png): título, seletor e CTA respeitam o contêiner comum.
- [Tags](../../../app/build/phase14-tags-final.png): título e CTA seguem o mesmo início vertical.
- [Formulário](../../../app/build/phase14-form-valid.png): modal Material 3 preserva seus próprios insets, sem herdar o padding da tela.

A inspeção lado a lado confirmou alinhamento consistente dos cabeçalhos equivalentes e ausência de sobreposição visível com a barra inferior/FAB. Não foram observados defeitos de espaçamento que justificassem nova alteração de código.

## Comandos futuros

Executar após autorização e implementação, usando o wrapper do projeto:

- `rtk .\gradlew.bat testDebugUnitTest`
- `rtk .\gradlew.bat connectedDebugAndroidTest`
- `rtk .\gradlew.bat assembleDebug lintDebug`
- `rtk git diff --check`

Registrar o resultado real, quantidade de testes quando informada e eventuais warnings/erros sanitizados. Não declarar sucesso com base no planejamento.

## Roteiro visual futuro

1. Fixar emulador, resolução, densidade, escala de fonte, orientação, tema e dataset.
2. Capturar baseline de Home, histórico, Relatórios, Orçamento e Tags.
3. Abrir novo/editar lançamento, seletor de tags, abertura de mês, editor/exclusão de tags e filtros; capturar somente os modais que apresentarem alinhamento superior comparável.
4. Repetir exatamente as rotas após a implementação, sem reiniciar condições entre pares além do procedimento documentado.
5. Comparar capturas em contato e sobreposição, anotando cabeçalho, primeiro campo, inset superior e barra inferior.
6. Repetir em tema claro/escuro e ao menos nos tamanhos de tela disponíveis no ambiente; registrar limitações se um dispositivo não estiver disponível.
7. Confirmar que nenhuma tela foi coberta pelo FAB/barra inferior e que o formulário não perdeu seus espaçamentos internos.
