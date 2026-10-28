# Critérios de aceitação — Fase 17

Os critérios abaixo são futuros; nenhum foi executado ou aprovado nesta tarefa de planejamento.

## Backup

- Snapshot SQLite completo independente, sem exportar registros; preserva tabelas, dados, índices, relacionamentos, restrições e identidade Room, com banco vazio ou preenchido. Consultar versão SQLite efetiva e usar `VACUUM INTO` quando disponível e adequado; no fallback comprovar checkpoint, fechamento e cópia, sem assumir `minSdk 26` (configurado 28).
- Escritas/transações concorrentes não produzem estado parcial; demonstrar que todos os commits anteriores ao portão constam no snapshot, inclusive frames WAL, que comandos compostos são drenados e novas escritas aguardam até a geração acabar. No fallback, comprovar resultado do checkpoint `TRUNCATE` e ausência de conexões/escritas concorrentes; fechamento sozinho não basta. Transferência SAF ocorre após liberação da manutenção, sem transação aberta.
- Pasta escolhida via SAF, `Orcamento/Backups` criado ou reutilizado, arquivo `.db` com nome único e sem sobrescrever existente. Sem permissões amplas; cartão SD é testado quando disponível. Depois de validação/read-back de ambos os arquivos, persistir atomicamente apenas os URIs exatos do par concluído, geração, árvore SAF e metadados de conferência; uma operação incompleta nunca substitui a última referência válida.
- Antes de exportar, validar snapshot independente: tamanho positivo, abertura, `integrity_check`, `foreign_key_check`, versão e compatibilidade Room; gerar manifesto JSON v1 com identidade/geração, esquema, data UTC, tamanho, SHA-256 e versão do app. Sem autenticação ou criptografia.
- Por streams, fechar e comparar tamanho/hash enviados, reler ambos os documentos e publicar o manifesto final por último; só informar sucesso após releitura e validação do par final. Provedor sem releitura não comprova persistência e gera falha clara; não classificar cópia incompleta como válida. Falhas de espaço, leitura, gravação, fechamento, autorização, cancelamento e capacidades do provedor têm mensagem e limpeza possível; limitações são reportadas.
- Aviso de que os dados financeiros não são criptografados e exigem armazenamento apropriado.

## Restauração

- A UI apresenta exatamente as duas ações principais; destino SAF só é pedido ao criar backup se ainda não registrado. Restaurar consulta a referência persistida do último par concluído, sem seletor de arquivo; se ausente, inacessível, grant revogado, movido/renomeado ou associação divergente, informar erro e não tocar no banco ativo. Não procurar por nomes parecidos.
- Par registrado é copiado antes de modificar banco atual; manifesto ausente, malformado ou incompatível, geração/tamanho/hash divergente, arquivo vazio, truncado, inválido, corrompido, incompatível ou com violação de FK é recusado sem perda atual. Integridade SQLite e validação de esquema/identidade Room são efetuadas em cópia isolada.
- Mensagem explica substituição integral; cancelamento antes da confirmação deixa dados inalterados. A seleção original nunca é alterada. Nenhuma importação individual de linhas.
- Operações ativas e coletores drenam antes de fechar Room; nenhuma nova leitura/escrita ocorre até reabertura ou recuperação. O banco anterior permanece recuperável, sem WAL antigo associado ao novo.
- Reabertura e testes de leitura, integridade/FKs precedem sucesso; quatro ViewModels, estados e coletores usam a nova instância; UI não apresenta formulário/seleção antigos como se fossem atuais.
- Falhas de troca, abertura, migração, espaço e reinicialização em cada fronteira recuperam o banco anterior, ou bloqueiam gravações preservando seus artefatos e explicando a falha. Não criar banco vazio silenciosamente. Backup original SAF permanece intacto.
- Os estados de criação, publicação e limpeza de backup e as ações da [matriz de recuperação](RECOVERY_PROTOCOL.md) são verificados incluindo marcador inválido, arquivos ausentes ou divergentes, morte do processo e interrupção repetida durante exportação, rollback/limpeza; snapshots incompletos nunca são automaticamente exportados como válidos. Em `COMMITTED` e `RECOVERED`, jamais reverter automaticamente e perder gravações posteriores. Um teste de exceção comum não substitui teste de encerramento de processo.
- Marcador durável, escrita ordenada, sincronização e rename são comprovados nas plataformas suportadas; na ausência de garantia, impedir a troca e manter a pendência aberta.

## Compatibilidade e UX

- Versão atual é aceita somente após validação; versão 1 segue migração 1→2 existente e validada sem perda; esquema futuro, inválido ou sem migração é recusado sem fallback destrutivo e sem mudar dados atuais.
- Entrada acessível **Backup e restauração** integrada à navegação atual, com estados de seleção, progresso, confirmação, sucesso, cancelamento, erro recuperável e incompatibilidade; interações incompatíveis ficam bloqueadas durante exclusividade.
- Critérios de durabilidade e publicação são demonstrados nas plataformas e provedores testados, não prometidos além das garantias observadas; limitações são informadas.
