# Progresso

**Estado:** Fase 08 concluída; Fase 09 parcialmente concluída.

Fases 01–07 permanecem preservadas conforme histórico. A Fase 08 adicionou o fluxo de abertura/edição de mês, sugestão do orçamento anterior, histórico mensal, estado vazio e tema claro/escuro do sistema. Testes instrumentados específicos cobrem isolamento e sugestão de orçamento.

Validação final executada: 15 testes JVM efetivamente executados e 15 testes instrumentados no BUSAO (AVD) - 16, todos sem falhas, erros ou ignorados; `assembleDebug`, `lintDebug`, `assembleRelease` e `git diff --check` passaram. Não existe tarefa `ktlintCheck` configurada.

A release foi compilada como `app-release-unsigned.apk`. Por decisão explícita, não existe configuração de assinatura e nenhuma chave/credencial foi inventada ou alterada. O APK debug foi instalado e iniciado no emulador; a UI exibiu `Lançamentos`, `Relatórios`, `Orçamentos` e `Nenhum lançamento`, sem `FATAL EXCEPTION` do aplicativo. A versão 1.0 está tecnicamente validada localmente, mas não é distribuível como APK assinado.

