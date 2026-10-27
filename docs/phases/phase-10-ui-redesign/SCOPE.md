# Escopo e diagnóstico — Fase 10

## Diagnóstico atual

A implementação atual concentra o fluxo em `MainActivity.kt`, alternando telas com estado local e ações textuais. A tela principal apresenta o histórico e filtros, mas não oferece uma visão imediata de orçamento, saldo, receitas/despesas ou consumo mensal. Relatórios e orçamentos são acessados por botões no cabeçalho, sem navegação inferior persistente.

O cadastro usa um `AlertDialog` com descrição, valor, data e tags. Isso reduz a área disponível, não distingue explicitamente despesa de receita e não fornece um fluxo dedicado para validação, teclado monetário ou retorno pós-salvamento. O histórico já possui busca, período, tag, edição e exclusão e deve ser preservado.

Relatórios já exibem orçamento, gasto, saldo, percentual e totais por tag, porém em uma lista textual. Orçamentos já suportam abertura, sugestão do mês anterior, edição e histórico isolado; a interface deve ser melhorada sem transformar sugestões em alterações automáticas.

## Incluído

1. Sistema visual compartilhado em Compose/Material 3: cores, tipografia, espaçamento, estados vazios e componentes de resumo.
2. Navegação principal baseada apenas em destinos funcionais existentes, com acesso destacado para novo lançamento.
3. Tela inicial mensal com seletor de mês, orçamento, saldo, receitas/despesas disponíveis, consumo, tags e últimos lançamentos.
4. Fluxo dedicado de cadastro/edição, preservando campos e contratos atuais.
5. Redesign do histórico, tags/orçamentos e relatórios usando os dados e estados já existentes.
6. Validação incremental por compilação, lint, testes existentes e inspeção visual no emulador quando disponível.

## Excluído

- Alterações no esquema Room, entidades, DAOs, regras financeiras ou contratos dos ViewModels sem autorização específica.
- WebDAV, servidor, autenticação, sincronização, novas dependências ou novas métricas não calculáveis.
- Auditoria de acessibilidade, publicação, assinatura, commit, push ou mudança de branch.

## Ordem proposta

- **F10-A — Diagnóstico e tokens visuais:** definir componentes e estados sem alterar comportamento.
- **F10-B — Navegação e tela inicial:** introduzir navegação funcional e resumo mensal.
- **F10-C — Cadastro:** substituir o diálogo por fluxo dedicado, mantendo validações e persistência.
- **F10-D — Histórico e tags:** melhorar busca, agrupamento, edição, exclusão e limites.
- **F10-E — Relatórios e orçamento:** organizar comparativos, meses e histórico.
- **F10-F — Validação integrada:** executar a matriz de testes e revisar regressões.
