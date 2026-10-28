# Critérios de aceitação — Fase 18

**Status final: atendidos.** Os testes automatizados foram registrados em `VALIDATION.md`; as verificações visuais, de responsividade, tema e acessibilidade foram aprovadas manualmente pelo usuário.

## Layout e dados

- [x] Cada cartão mostra `occurredOn` somente como dia e mês abreviado em português, sem ano.
- [x] A data fica à esquerda e tem largura estável suficiente para dia e mês.
- [x] A descrição e uma tag compartilham linha quando houver largura disponível.
- [x] Chips de tag são compactos; tags longas e múltiplas tags não sobrepõem outros elementos.
- [x] Lançamento sem tag não mostra chip vazio nem espaço reservado indevido.
- [x] Valor formatado por `formatBrazilianCents` aparece abaixo da descrição/tags.
- [x] A lista mantém ordenação por `occurredOn DESC, id DESC`.
- [x] O carregamento de tags para a lista não executa consulta individual por lançamento.

## Interação e preservação

- [x] Editar e excluir ficam visualmente à direita e possuem alvos independentes.
- [x] Excluir permanece visualmente distinguível com cor semântica de erro e continua exigindo confirmação.
- [x] Criar, editar e excluir preservam os contratos de `ExpenseOperations` e de `CreateExpenseDialog`.
- [x] Pesquisa por descrição continua case-insensitive.
- [x] Filtros “Sem tag”, “Alimentação”, “Mercado” e demais tags continuam funcionando e reativos.
- [x] FAB de criação, navegação inferior e destino Despesas permanecem funcionais.
- [x] Tema claro/escuro continua íntegro e não há cores fixas fora do tema.

## Responsividade e acessibilidade

- [x] Descrições longas não invadem data nem ações.
- [x] Tags extensas e múltiplas ocupam linhas adicionais ou truncam de forma legível, sem clipping ou sobreposição.
- [x] Em largura reduzida e fonte ampliada, o cartão cresce verticalmente e preserva alvos de toque adequados.
- [x] Ações possuem descrições semânticas inequívocas e ordem de leitura lógica.

## Limites técnicos

- [x] Não há mudança de entidades, schema Room, migração, dependência, Kotlin, Gradle, AGP ou SDK sem necessidade comprovada e aprovação explícita.
- [x] Nenhuma regra de receita, rateio ou associação de tags é introduzida pelo redesign visual.
