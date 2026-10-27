# Validação planejada — Fase 12

A implementação foi autorizada posteriormente ao planejamento. Os resultados executados estão registrados abaixo; a validação manual interativa completa permanece pendente.

## Resultados executados

- `rtk .\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — sucesso; os testes JVM e a compilação/lint concluíram sem falhas.
- `rtk .\gradlew.bat connectedDebugAndroidTest` — sucesso; relatório do emulador `BUSAO(AVD) - 16` registra 27 testes, 0 falhas, 0 erros e 0 ignorados.
- `rtk adb install -r .\app\build\outputs\apk\debug\app-debug.apk` — sucesso.
- `rtk adb shell am start -n br.com.paulodiogo.orcamento/.MainActivity` — atividade iniciada.
- `rtk git diff --check` — sucesso.
- A primeira execução instrumentada falhou em duas expectativas antigas de rótulo/data; asserções foram ajustadas ao novo formato e a execução seguinte passou. Nenhum teste foi removido ou enfraquecido.

## Testes unitários planejados

- Função de data local para novo lançamento usa a data fornecida pelo relógio/abstração local, sem `Instant` ou UTC.
- Conversão `LocalDate` ↔ `yyyy-MM-dd` é round-trip e rejeita valores inválidos.
- Apresentação `dd/MM/yyyy` não altera o valor ISO persistido.
- Data original é mantida quando o formulário é criado em modo de edição.
- Limites de filtro continuam inclusivos, aceitam vazio como `null` e rejeitam início posterior ao fim.
- Casos de 28/29/30/31 dias, virada de mês e virada de ano.

## Testes Compose/instrumentados planejados

- Novo cadastro mostra a data local atual.
- Edição mostra a data existente, inclusive retroativa.
- Toque no campo/ícone abre o calendário na data selecionada.
- Confirmar uma data diferente atualiza o campo.
- Cancelar e fechar sem confirmação não alteram o campo.
- Alterar a data duas ou mais vezes antes de salvar mantém a última seleção.
- Salvar e reabrir recupera a data correta.
- Filtros de intervalo continuam inicialmente vazios e aplicam `De`/`Até` sem alterar outros filtros.
- Seletores de mês/relatório continuam operando em `YYYY-MM`.
- Regressão dos testes existentes de despesas, filtros, orçamento e relatórios.

## Validação manual futura

1. Instalar o APK de debug em emulador/dispositivo com relógio local controlado.
2. Abrir novo lançamento, comparar a sugestão com a data local e alterar pelo calendário.
3. Confirmar, voltar à lista e editar o lançamento; verificar preservação e nova alteração.
4. Abrir o calendário, cancelar e fechar por voltar/toque fora; confirmar que o valor não mudou.
5. Repetir nos últimos dias de mês e ano e conferir a data mostrada e persistida.
6. Abrir filtros: confirmar que `De`/`Até` começam vazios, selecionar limites, limpar e verificar a lista.
7. Abrir orçamento e relatório: confirmar que o seletor mensal não foi tratado como data diária e que o período consultado não mudou sem ação.
8. Inspecionar logs apenas para confirmar ausência de falhas; não declarar sucesso sem registrar comando/evidência.

## Comandos a executar somente após implementação autorizada

- `rtk .\\gradlew.bat testDebugUnitTest`
- `rtk .\\gradlew.bat connectedDebugAndroidTest`
- `rtk .\\gradlew.bat assembleDebug lintDebug`
- `rtk git diff --check`

Os resultados, quantidade de testes e eventuais correções deverão ser registrados aqui sem remover ou enfraquecer testes existentes.
