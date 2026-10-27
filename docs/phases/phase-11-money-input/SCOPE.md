# Escopo, diagnóstico e solução — Fase 11

## Diagnóstico do comportamento atual

### Campos monetários identificados

1. **Despesa — cadastro:** `CreateExpenseDialog` em `app/src/main/kotlin/br/com/paulodiogo/orcamento/ExpenseForm.kt`, usando `OutlinedTextField` sem `KeyboardOptions`, máscara ou transformação. O estado local `amount` é `String`.
2. **Despesa — edição:** o mesmo diálogo é reutilizado por `AppShell.kt` e `MainActivity.kt`; o valor persistido em centavos é convertido para texto decimal simples com `BigDecimal.valueOf(amountCents, 2).toPlainString()`.
3. **Orçamento mensal:** `BudgetScreen.kt`/`BudgetViewModel.kt` possuem outro campo editável; ele foi incluído no mesmo incremento e agora usa o formatador/conversor compartilhado.
4. **Exibição:** `DesignSystem.formatBrazilianCents`, `BudgetScreen`, `ReportScreen` e `AppShell` formatam valores para leitura com `NumberFormat` brasileiro. Não são campos editáveis.

Campos numéricos não monetários, como mês/data, identificadores, filtros e percentuais, não devem receber a máscara monetária.

### Fluxo atual de conversão e persistência

- `ExpenseListViewModel.create/updateExpense` chama `parseAmountCents` antes de `ExpenseOperations`.
- `DomainValidation.parseAmountCents` aceita texto decimal com ponto ou vírgula, exige escala máxima de duas casas via `BigDecimal` e usa `longValueExact`.
- `ExpenseOperations` valida novamente `amountCents` com `validateExpenseAmount`, que rejeita `<= 0`, e persiste `ExpenseEntity.amountCents` como `Long`.
- Cálculos em `FinancialCalculations` usam `Long`/`BigDecimal`; não se deve introduzir `Float` ou `Double`.

### Problemas a resolver

- O campo aceita letras, separadores e formas textuais inconsistentes até a conversão no salvamento.
- Não há formatação durante a digitação, teclado numérico, limite explícito, política de colagem ou estratégia de cursor.
- Cadastro e edição exibem formatos diferentes do comportamento desejado; recomposição e reabertura precisam preservar o estado.
- O orçamento mantém parser próprio, criando risco de regras divergentes.

## Comportamento funcional implementado

### Modelo de entrada

A fonte de verdade editável será uma sequência de algarismos ASCII (`0`–`9`) representando centavos implícitos. A conversão será `Long` com dois últimos dígitos como centavos: `1` → `0,01`, `12` → `0,12`, `123` → `1,23`, `12345` → `123,45` e `123456` → `1.234,56`.

O texto visível será produzido por função pura: dividir em parte inteira e dois centavos, aplicar ponto de milhares e vírgula decimal. O prefixo visual opcional `R$` deve ficar fora do conteúdo editável, por exemplo em `prefix`, para não contaminar seleção, colagem ou conversão.

- Vazio: conteúdo editável vazio, sem inventar `0,00`.
- Zeros à esquerda são normalizados sem mudar o valor; um primeiro `0` exibe `0,00`. Antes da primeira entrada, o campo permanece vazio.
- Valor zero é representado por `0,00`; despesas rejeitam zero na validação do formulário e novamente no domínio. O orçamento aceita zero pela regra já existente.

### Filtragem e colagem

- Digitação, colagem e composição do IME serão sanitizadas para manter apenas algarismos; espaços, letras, ponto, vírgula, `R$` e outros símbolos são descartados, não reinterpretados.
- Caracteres que não forem algarismos ASCII são descartados em digitação/colagem; se nenhum algarismo restar, o campo informa erro e preserva o último valor válido.
- O sinal negativo não pode ser digitado no teclado numérico. Em texto colado, o sinal é descartado e os dígitos restantes são usados, conforme a decisão do usuário; os validadores de domínio continuam proibindo valores negativos.

### Limites e overflow

O limite é de no máximo 19 algarismos ASCII, condicionado também à faixa numérica de `Long`: a sequência é convertida com `toLongOrNull`, sem multiplicação intermediária, e números acima de `Long.MAX_VALUE` são rejeitados. Mais de 19 algarismos também são rejeitados. Não há truncamento silencioso; a UI preserva o valor anterior e mostra erro.

### Cursor, exclusão e foco

`MoneyTextField` usa `TextFieldValue` e traduz seleção/offsets visuais para índices de dígitos. Separadores são recalculados após edição; backspace e inserção no meio atuam previsivelmente; seleção permanece selecionável e cursor volta à posição lógica após alteração. Campo vazio permanece vazio. Foi aplicado `KeyboardOptions(keyboardType = KeyboardType.Number)` e single line.

Ao entrar/sair do foco, o texto não é convertido para outro formato nem limpo. `rememberSaveable` mantém o valor de edição, e a tela de edição inicializa com os centavos persistidos usando o mesmo formatador do cadastro.

## Arquitetura proposta

Foram criadas o componente reutilizável `MoneyTextField` e funções puras em `domain/MoneyFormatting.kt`:

- `sanitizeMoneyDigits(text: String): String`;
- `formatMoneyDigits(digits: String): String`;
- `parseMoneyDigitsToCents(digits: String): Long`;
- `moneyDigitsFromCents(cents: Long): String`;
- mapeamento de offsets/seleção, se necessário, separado da conversão.

`MoneyTextField` concentra edição, cursor, teclado e erro visual. Não persiste dados, não conhece Room e não decide regras de negócio. Os ViewModels recebem `Long` em centavos, sem converter novamente texto formatado; `ExpenseOperations` e `BudgetOperations` continuam validando as regras próprias. O parser decimal legado permanece para compatibilidade, mas a UI nova envia a representação centesimal unívoca.

O campo de orçamento reutiliza as mesmas funções puras e o mesmo componente, sem compartilhar as regras de aceitação específicas: orçamento aceita zero; despesa requer valor positivo.

## Arquivos previstos para alteração

- `app/src/main/kotlin/br/com/paulodiogo/orcamento/ExpenseForm.kt`: substituir o campo de valor por `MoneyTextField` e conectar erro/estado.
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/MoneyTextField.kt`: edição, teclado, cursor, prefixo, erros e estado saveable.
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/AppShell.kt` e `MainActivity.kt`: fornecer valor inicial em centavos/representação comum e preservar edição.
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/MoneyFormatting.kt`: sanitização, formatação, limite e conversão de/para centavos.
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/domain/ExpenseListViewModel.kt`: recebimento de centavos e proteção contra salvamento repetido.
- `app/src/main/kotlin/br/com/paulodiogo/orcamento/BudgetScreen.kt` e `domain/BudgetViewModel.kt`: integração do orçamento com formatador compartilhado e estado de salvamento.
- `app/src/main/res/values/strings.xml`: rótulos e mensagens novos.
- Testes: `app/src/test/.../MoneyFormattingTest.kt`, `app/src/androidTest/.../MainActivityComposeTest.kt`, `BudgetScreenTest.kt`, `ExpenseOperationsTest.kt` e `BudgetOperationsTest.kt`.

Não previstos: entidades/DAOs/migrações, dependências, versões, cálculos financeiros ou outras funcionalidades.

## Requisitos de validação

- Campo vazio: não salvar despesa; mostrar erro existente/adequado.
- Zero: exibir corretamente; rejeitar despesa no domínio, aceitar orçamento conforme regra atual.
- Um dígito e centavos: preservar `1` como `0,01` e `123` como `1,23`.
- Milhares: inserir `123456` como `1.234,56`.
- Limite: rejeitar excesso e overflow sem persistir parcialmente.
- Colagem inválida/mista: aplicar a política de sanitização; vazio ou excesso produz erro.
- Correção após erro: nova entrada válida limpa o erro e permite salvar.
- Reabertura: valor armazenado reaparece com a mesma formatação e converte ao mesmo `Long`.
- Salvamento repetido/interação rápida: impedir dupla ação no formulário ou garantir idempotência de estado; não criar duplicatas por cliques repetidos.

## Riscos

- O mapeamento de cursor com separadores e seleção é a parte mais sensível; requer testes de edição, não apenas snapshots.
- Formatos legados decimais podem aparecer em chamadas existentes; a migração do comportamento visual não deve alterar valores já armazenados.
- Colagem mista sempre descarta caracteres inválidos; quando não restam dígitos, a mensagem é exibida e o último valor válido é preservado.
