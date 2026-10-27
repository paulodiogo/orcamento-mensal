# Critérios de aceitação — Fase 13

A implementação foi autorizada e a Fase 13 foi aprovada em 2026-10-27. Os critérios abaixo distinguem evidências reais de testes automatizados/inspeção/validação manual direcionada e cenários complementares formalmente dispensados de validação individual pela decisão final. A dispensa não representa execução do cenário.

## Comportamento funcional

- [x] O formulário indica claramente que tags são opcionais.
- [x] O campo fechado mostra “Nenhuma tag selecionada” sem associações.
- [x] O campo fechado resume uma ou várias tags sem ocupar o formulário com chips excessivos.
- [x] Abrir o seletor mostra todas as tags disponíveis em lista rolável.
- [x] O modal permite selecionar e remover várias tags sem fechar a cada toque.
- [x] `Aplicar` confirma alterações; `Cancelar`, voltar e fechar descartam alterações temporárias.
- [x] Cadastro começa sem tags e permite salvar sem associação.
- [x] Edição recupera todas as associações persistidas e exibe seus nomes atuais.
- [x] Renomear uma tag atualiza o nome exibido sem trocar a associação. **Aceite administrativo; validação manual individual dispensada.**
- [x] Lista vazia informa que não há tags cadastradas e aponta para o gerenciamento de tags, sem bloquear salvar.
- [x] Falha de carregamento oferece mensagem coerente sem inventar opções.
- [x] Pesquisa filtra por nome e informa quando não há correspondência.
- [x] Nomes longos não causam corte ilegível, overflow ou perda da ação de seleção.
- [x] Uma tag removida não deixa uma associação inválida nem causa crash; a atualização remove o ID obsoleto.

## Regra e regressão

- [x] Zero, uma e várias tags continuam válidos conforme RN-007. **Aceite administrativo; validação manual complementar dispensada.**
- [x] IDs selecionados são persistidos em `expense_tag`, sem duplicatas. **Aceite administrativo; evidências automatizadas existentes preservadas.**
- [x] Edição atualiza associações sem alterar descrição, valor ou data. **Aceite administrativo; validação manual individual dispensada.**
- [x] Exclusão de tag continua preservando a despesa. **Aceite administrativo; cobertura automatizada existente preservada.**
- [x] Cadastro e edição funcionam nos caminhos de `AppShell` e `MainActivity`. **Aceite administrativo; validação manual complementar dispensada.**
- [x] Nenhum esquema, entidade, repositório, regra, dependência ou versão é alterado. **Confirmado por inspeção documental/diff existente.**

## Qualidade visual

- [x] O componente segue Material 3, cabe em tela pequena e mantém ações compreensíveis. **Aceite administrativo; validação visual complementar dispensada.**
- [x] O campo possui indicação visual de abertura e foco/semântica testável. **Aceite administrativo; validação complementar dispensada.**
- [x] O modal tem contraste, espaçamento e hierarquia consistentes com o app claro/escuro. **Aceite administrativo; validação de temas dispensada.**

## Observação sobre o aceite final

Os itens marcados com aceite administrativo foram aprovados pela decisão final de encerramento. Eles não devem ser interpretados como cenários manualmente executados nesta rodada.
