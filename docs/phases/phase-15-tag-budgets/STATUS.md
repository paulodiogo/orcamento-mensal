# Status — Fase 15

## Situação

**Fase concluída e validada.**

A entidade, migração Room, operações, ViewModel, formulário e testes foram atualizados. O orçamento da tag permanece independente do orçamento mensal global e não é exibido fora do formulário de tags.

## Decisões do planejamento

1. Orçamento por tag é opcional.
2. `NULL` representa ausência; `0L` representa orçamento explicitamente igual a zero.
3. O armazenamento recomendado é `Long?` em centavos em coluna SQLite `INTEGER` nullable.
4. O limite recomendado é `Long.MAX_VALUE` centavos (R$ 92.233.720.368.547.758,07); 19 dígitos são apenas o teto textual técnico atual, e valores negativos, inválidos e overflow são rejeitados.
5. A migração deve adicionar a coluna nullable sem default zero, mantendo registros antigos como `NULL`.
6. A criação/reutilização de tag existente não deve sobrescrever orçamento sem edição explícita.
7. Exclusão de tag mantém a regra existente: remove a tag e suas associações, e o orçamento desaparece junto com a tag; despesas permanecem intactas. Não há ação de apagar orçamento independente no escopo.

## Evidências

- `testDebugUnitTest` — sucesso.
- `assembleDebug lintDebug` — sucesso.
- `connectedDebugAndroidTest` — sucesso no `BUSAO(AVD) - 16`.
- `git diff --check` — sucesso.

A migração histórica 1→2 foi validada por teste dedicado com o esquema real da versão 1, preservando dados, relações, índices e orçamento mensal. Os testes de independência foram aprovados. A validação instrumentada reproduzível em `BUSAO(AVD) - 16` aprovou criação sem orçamento, criação com orçamento, edição, remoção, cancelamento, duplicidade, nome vazio e persistência no banco de teste. Os seletores semânticos foram usados sem coordenadas arbitrárias. A fase está concluída.
