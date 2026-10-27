# Critérios de aceitação — Fase 13

A implementação foi autorizada em 2026-10-27. Os critérios marcados abaixo foram verificados pelos testes automatizados ou pela inspeção do código. Os critérios dependentes de interação visual permanecem pendentes porque o emulador não renderizou a UI.

## Comportamento funcional

- [x] O formulário indica claramente que tags são opcionais.
- [x] O campo fechado mostra “Nenhuma tag selecionada” sem associações.
- [x] O campo fechado resume uma ou várias tags sem ocupar o formulário com chips excessivos.
- [x] Abrir o seletor mostra todas as tags disponíveis em lista rolável.
- [x] O modal permite selecionar e remover várias tags sem fechar a cada toque.
- [x] `Aplicar` confirma alterações; `Cancelar`, voltar e fechar descartam alterações temporárias.
- [x] Cadastro começa sem tags e permite salvar sem associação.
- [x] Edição recupera todas as associações persistidas e exibe seus nomes atuais.
- [ ] Renomear uma tag atualiza o nome exibido sem trocar a associação.
- [x] Lista vazia informa que não há tags cadastradas e aponta para o gerenciamento de tags, sem bloquear salvar.
- [x] Falha de carregamento oferece mensagem coerente sem inventar opções.
- [x] Pesquisa filtra por nome e informa quando não há correspondência.
- [x] Nomes longos não causam corte ilegível, overflow ou perda da ação de seleção.
- [x] Uma tag removida não deixa uma associação inválida nem causa crash; a atualização remove o ID obsoleto.

## Regra e regressão

- [ ] Zero, uma e várias tags continuam válidos conforme RN-007.
- [ ] IDs selecionados são persistidos em `expense_tag`, sem duplicatas.
- [ ] Edição atualiza associações sem alterar descrição, valor ou data.
- [ ] Exclusão de tag continua preservando a despesa.
- [ ] Cadastro e edição funcionam nos caminhos de `AppShell` e `MainActivity`.
- [ ] Nenhum esquema, entidade, repositório, regra, dependência ou versão é alterado.

## Qualidade visual

- [ ] O componente segue Material 3, cabe em tela pequena e mantém ações compreensíveis.
- [ ] O campo possui indicação visual de abertura e foco/semântica testável.
- [ ] O modal tem contraste, espaçamento e hierarquia consistentes com o app claro/escuro.
