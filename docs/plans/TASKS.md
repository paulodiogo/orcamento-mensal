# Tarefas de Planejamento

- **Fases atuais:** Fase 08 concluída; Fase 09 parcialmente concluída; Fase 10 parcialmente concluída; Fase 11 concluída e validada localmente; Fase 12 parcialmente implementada e validada tecnicamente.
- **Tarefas concluídas:** F6-B/F6-C/F6-D/F6-E, F7-A/F7-B/F7-C/F7-D/F7-E/F7-F, F8-A/F8-B/F8-C, F9-A/F9-B/F9-C e F11-01 a F11-06.
- **Evidências:** Fase 11: 23 testes JVM, 27 instrumentados, debug build, lint e inspeção de entrada no emulador aprovados; detalhes em `docs/phases/phase-11-money-input/VALIDATION.md`.
- **Limitações conhecidas:** release unsigned conforme decisão explícita anterior; inspeção manual do teclado mostrou `inputType=2` e IME solicitado, mas o keypad visual não apareceu devido à configuração do AVD.
- **Estado atual:** Fase 14 — padronização do espaçamento superior — concluída e validada; evidências em `docs/phases/phase-14-top-spacing/`.
- **Estado da Fase 15:** concluída; migração, independência, CRUD instrumentado semântico e validações técnicas aprovados; documentos em `docs/phases/phase-15-tag-budgets/`.
- **Fase 16:** acompanhamento mensal do orçamento por tag em Home e Relatórios; implementação e validações técnicas concluídas, sem seletor na Home ou histórico versionado; documentos em `docs/phases/phase-16-tag-budget-usage/`.
- **Fase 17:** implementação parcial de backup/restauração local com `VACUUM INTO`, manifesto SHA-256, SAF, recuperação e menu de acesso; detalhes, testes e critérios ainda pendentes no [status](../phases/phase-17-local-backup/STATUS.md). Os testes gerais aprovados não comprovam execução real SAF, restauração nem recuperação após morte de processo. Tarefas e critérios em [phase-17-local-backup](../phases/phase-17-local-backup/README.md); não havia IDs preexistentes para a fase.
