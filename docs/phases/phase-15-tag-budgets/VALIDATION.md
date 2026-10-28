# Estratégia de validação — Fase 15

## Estado

Validação concluída. O teste dedicado de migração, os testes de independência, os testes instrumentados semânticos de CRUD e a suíte automatizada foram aprovados.

## Unitários

- Parsing da máscara brasileira e conversão de dígitos com centavos implícitos para `Long`.
- Campo vazio convertido em `null`; zero digitado convertido em `0L`.
- Rejeição de sinal negativo, caracteres inválidos, entrada não numérica, mais de 19 dígitos e overflow.
- Limite mínimo de 1 centavo, zero explícito e máximo permitido.
- Criação sem/com orçamento, edição do valor, inclusão em tag sem valor e remoção para `null`.
- Garantia de que edição do orçamento não modifica nome normalizado, ID ou associações.
- Criação idempotente de tag já existente não sobrescreve orçamento sem edição explícita.

## Room e migração

- Criar banco na versão 1 com tags, despesas e associações representativas; migrar para 2.
- Confirmar que `budget_amount_cents` de tags existentes fica `NULL`.
- Confirmar round-trip de `NULL`, zero e valores positivos.
- Confirmar que IDs, nomes, timestamps, despesas e todas as linhas `expense_tag` permanecem iguais.
- Confirmar que banco já atualizado abre normalmente após fechar e reabrir o app.

## Instrumentados/Compose

- Abrir editor de nova tag, preencher somente nome, salvar e reabrir.
- Preencher orçamento, verificar máscara/prefixo/teclado, salvar e confirmar no editor de edição.
- Editar, incluir e remover orçamento.
- Forçar entradas inválidas e confirmar erro inline, diálogo aberto e nenhum salvamento parcial.
- Cancelar e dispensar formulário, confirmando que nome e orçamento não mudam.
- Excluir tag e confirmar que associações são removidas conforme regra existente, despesas permanecem e não há alteração de outras tags.

## Validação manual

No emulador padrão do projeto, repetir criação/edição com valores de 1 centavo, zero, máximo, acima do máximo, vazio e tentativa negativa. Fechar e reabrir o aplicativo e confirmar persistência. Não inspecionar nem declarar orçamento em Home, histórico, relatórios ou cartões como funcionalidade da fase.

## Resultados executados

- `rtk .\gradlew.bat connectedDebugAndroidTest` — primeira execução após o teste de migração revelou uma expectativa incorreta de quantidade de índices; corrigida para refletir o índice da chave primária e o índice explícito. Nova execução: sucesso no `BUSAO(AVD) - 16`, 33 testes, sem falhas.
- `rtk .\gradlew.bat testDebugUnitTest` — sucesso.
- `rtk .\gradlew.bat assembleDebug lintDebug` — sucesso.
- `rtk git diff --check` — sucesso.
- APK instalado com `adb install -r`; Activity iniciada; `uiautomator dump` confirmou a tela Tags e o editor com `Nome`, `Orçamento da tag (opcional)`, ajuda, `Salvar` e `Cancelar`.
- A interação manual CRUD completa não foi declarada executada: a tentativa de preencher/salvar não produziu evidência confiável da tag criada. A investigação adicionou apenas seletores `testTag` mínimos (`createTagButton`, `tagEditor`, `tagNameField`, `tagBudgetField`) para viabilizar interação sem coordenadas em uma execução posterior. Não houve limpeza de dados pessoais, desinstalação ou alteração da configuração do emulador.

## Comandos futuros

Não há comandos obrigatórios adicionais para a implementação atual. Validações manuais completas podem ser repetidas em uma sessão interativa dedicada.

- `rtk .\gradlew.bat testDebugUnitTest`
- `rtk .\gradlew.bat assembleDebug lintDebug`
- `rtk .\gradlew.bat connectedDebugAndroidTest`
- `rtk git diff --check`

Registrar somente resultados realmente obtidos, sem declarar testes executados nesta etapa de planejamento.
