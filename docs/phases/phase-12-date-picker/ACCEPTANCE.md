# Critérios de aceitação — Fase 12

A fase só poderá ser concluída após implementação autorizada e evidências. Nesta etapa de planejamento, todos permanecem não verificados.

- [x] Novo lançamento abre com a data local atual, sem data fixa de compilação.
- [x] Edição mostra e preserva a data original persistida.
- [ ] O calendário abre inicialmente na data atualmente selecionada.
- [ ] Toque no campo ou controle claramente identificável abre o DatePicker.
- [ ] Confirmar outra data atualiza o campo imediatamente.
- [ ] Cancelar ou fechar o calendário sem confirmação mantém o valor anterior.
- [ ] O usuário pode alterar a data várias vezes antes de salvar.
- [x] A apresentação visual é legível em padrão brasileiro, enquanto o domínio continua em `yyyy-MM-dd`.
- [x] A data confirmada é persistida corretamente e continua válida após reabertura.
- [ ] Não há deslocamento de dia por conversão de fuso horário.
- [x] Filtros vazios continuam sem restringir a consulta; intervalos e ordem permanecem válidos.
- [ ] Seletores de mês mantêm semântica `YYYY-MM` e não são convertidos indevidamente em datas diárias.
- [ ] Virada de mês e ano mantém a data correta em criação, edição, filtros e persistência.
- [ ] Regras de validação, isolamento mensal, relatórios e esquema Room permanecem inalterados.
- [ ] Testes existentes continuam passando; novos testes cobrem os fluxos descritos em `VALIDATION.md`.
- [ ] Não foram adicionadas dependências, versões, migrações, commits ou alterações fora do escopo.
