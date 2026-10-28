# Critérios de aceitação do planejamento — Fase 15

## Dados e compatibilidade

- [ ] `TagEntity` terá coluna nullable `budget_amount_cents` (`Long?`), sem ponto flutuante.
- [ ] A ausência será `NULL`; zero explícito permanecerá `0`.
- [ ] A migração 1→2 será incremental, preservará tags, despesas e todas as linhas `expense_tag`, e será registrada no Room.
- [ ] O limite funcional será `Long.MAX_VALUE` centavos (R$ 92.233.720.368.547.758,07), sujeito à confirmação na implementação; overflow e valores negativos serão rejeitados.
- [ ] Nenhum campo monetário existente será convertido nem nenhuma relação será recriada.

## Comportamento

- [ ] Criar tag permite nome sem orçamento e nome com orçamento válido.
- [ ] Editar tag mostra o valor atual quando houver, permite alterar, incluir ou remover o orçamento.
- [ ] Editar orçamento não altera nome, ID, timestamps não relacionados ou associações com despesas.
- [ ] Tag reutilizada por nome normalizado não terá seu orçamento sobrescrito silenciosamente pela criação; edição explícita será necessária.
- [ ] Cancelar ou dispensar o formulário não persiste mudanças.

## Interface

- [ ] O campo aparece apenas no editor de tags, abaixo do nome, usando Material 3 e `MoneyTextField`.
- [ ] Campo vazio é apresentado como sem orçamento e salvo como `NULL`, nunca convertido implicitamente em zero.
- [ ] Erros de formato, negativos, limite e overflow aparecem junto ao campo; o diálogo permanece aberto.
- [ ] O valor usa a mesma máscara/prefixo/teclado do restante do app.
- [ ] Nenhuma outra tela passa a exibir ou utilizar orçamento por tag nesta fase.

## Testes planejados

- [ ] Unitários cobrem parsing, limite, ausência/zero e operações de tag.
- [ ] Room cobre migração, round-trip nullable e preservação de relações.
- [ ] Instrumentados cobrem criação, edição, remoção, cancelamento e validação.
- [ ] Manual cobre padrão monetário e persistência após fechar/reabrir.

Os itens estão planejados, não executados nesta fase.
