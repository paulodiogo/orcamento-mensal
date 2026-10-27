# Critérios de aceitação — Fase 14

## Diagnóstico

- [x] Cada superfície do `SCOPE.md` foi inspecionada no código e explicitamente diferenciada entre tela e modal.
- [x] A matriz registra arquivo, componente, contêiner, inset, cabeçalho, origem do topo, sobreposição e justificativa.
- [x] A causa foi demonstrada por código e pela inspeção das capturas válidas; nenhuma causa foi presumida.

## Contrato visual e técnico

- [x] O inset superior do sistema tem um responsável único por tela.
- [x] O primeiro elemento visual recebe uma regra de espaçamento visual única após a área segura.
- [x] Cabeçalhos equivalentes começam no mesmo alinhamento vertical.
- [x] Telas sem cabeçalho têm regra documentada e coerente.
- [x] Não existe dupla aplicação de `innerPadding`, `statusBarsPadding()` ou `safeDrawingPadding()` no código alterado.
- [x] O conteúdo permanece protegido da barra inferior e do FAB.
- [x] Exceções de formulários e modais usam insets próprios sem herdar padding de tela indevido.

## Regressão e preservação

- [x] Navegação normal, filtros, formulários, relatórios, orçamento, tags e modais continuam funcionando nos fluxos verificados.
- [x] Espaçamentos internos não relacionados ao topo permanecem preservados.
- [ ] Temas claro/escuro e tamanhos de tela adicionais não foram executados; a evidência cobre o tema e o AVD padrão disponíveis.
- [x] Testes, build e lint pertinentes não apresentam regressões; `ktlintCheck` não existe no projeto.

## Comparação visual reproduzível

Usar o mesmo emulador, resolução, densidade, escala de fonte, orientação, tema, estado do banco e rota. Capturar a tela após o mesmo evento de estabilização, sem teclado aberto salvo quando o cenário o exigir. Comparar pares antes/depois lado a lado e em sobreposição alinhada pelo limite da janela; registrar a observação qualitativa de alinhamento e toda diferença intencional. Não adotar tolerância numérica arbitrária: se uma métrica for necessária, ela deve ser derivada da geometria do componente Material/Design já adotado e justificada na evidência.
