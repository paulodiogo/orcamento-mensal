# Status — Fase 11

- **Situação:** implementada e validada.
- **Diagnóstico concluído:** o formulário antigo aceitava texto sem máscara e só convertia ao salvar; orçamento tinha parser próprio. O formato persistido já era `Long` em centavos.
- **Entregue:** formatador/conversor puro, `MoneyTextField`, cadastro/edição e orçamento integrados, cursor/seleção, teclado numérico, mensagens, limite e proteção contra salvamentos repetidos.
- **Regra preservada:** despesa exige valor maior que zero; orçamento aceita zero ou mais; não houve alteração de cálculos, esquema ou dados existentes.
- **Validação:** build/lint e 23 testes JVM/27 instrumentados aprovados; `emulator-5554` mostrou o formato digitado e `inputType=2` (teclado numérico). Evidências completas em `VALIDATION.md`.
- **Global tracking:** `docs/plans/TASKS.md`, `docs/plans/progress.md` e `docs/plans/VALIDATION.md` atualizados após autorização.

## Decisões implementadas

1. Campo de orçamento incluído no mesmo incremento.
2. Máximo de 19 algarismos, validado por `toLongOrNull`; excesso ou valor fora da faixa é rejeitado sem truncar.
3. Colagem remove todos os caracteres fora de `0`–`9`; colagem sem dígitos mostra erro e mantém o último valor válido.
4. Mensagens de entrada vazia/inválida/excessiva e despesa zero adicionadas a `strings.xml`.
5. Campo inicia vazio; zeros digitados tornam-se `0,00`; zeros à esquerda são normalizados.
6. Botão de salvamento fica desabilitado durante operação e ViewModels ignoram chamadas concorrentes enquanto salvam.
