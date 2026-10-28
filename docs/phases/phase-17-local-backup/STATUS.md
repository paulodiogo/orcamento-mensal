# Status — Fase 17

**Implementação da Fase 17 realizada; validação funcional ainda parcial.** Testes instrumentados gerais passaram, mas os fluxos reais SAF de backup/restauração e testes de encerramento de processo/reinicialização ainda não foram executados. Portanto, consistência `VACUUM INTO`/WAL, rollback sob falhas e recuperação determinística não estão declarados integralmente validados. A Fase 16 permanece concluída.

## Correção SAF — URI de árvore

A causa do erro era o tratamento do URI selecionado como uma referência documental genérica durante a validação/listagem. O fluxo agora valida explicitamente `content://` com `DocumentsContract.isTreeUri`, abre a raiz com `DocumentFile.fromTreeUri`, abre a subpasta resolvida como documento e lista seus filhos por `DocumentFile.listFiles()`. Os arquivos continuam sendo lidos e gravados exclusivamente por `ContentResolver.openInputStream/openOutputStream` nos URIs de documento; nenhum URI é convertido em `File`.

A pasta registrada é reutilizada na restauração após validação do grant persistente e existência da árvore. URI inválido, permissão revogada ou árvore inacessível falham antes de copiar ou substituir o banco, mantendo o fluxo de seleção novamente pela UI.

Validação executada após a correção: `testDebugUnitTest`, `assembleDebug`, `lintDebug` e `connectedDebugAndroidTest` passaram; o teste instrumentado geral concluiu no `BUSAO(AVD) - 16`. A execução ainda não automatiza um provedor SAF real com arquivo de backup pré-existente.

## Implementado

- Gerenciador local serializado para snapshot SQLite, validação SQLite/Room, manifesto JSON v1 e exportação/verificação via SAF.
- `VACUUM INTO` selecionado após consultar `sqlite_version()`; WAL é verificado e checkpointado para o ciclo de restauração. Ambos os fluxos suspendem e recriam os observadores do Room, mas o procedimento não foi exercitado com backup/restauração reais em dispositivo. Fallback de geração para SQLite anterior a 3.27.0 **não** foi implementado; a operação falha sem exportar se `VACUUM INTO` não existir.
- Restauração verifica manifesto, tamanho, hash, integridade, FKs e versão; prepara cópia compatível com Room, preserva banco anterior, registra estados e tenta rollback; recuperação inicial ocorre antes da construção dos ViewModels.
- UI acessível pelo menu de três pontos no cabeçalho da Home, sem novo destino na barra inferior; tela apresenta Criar backup e Restaurar backup. Criar registra atomicamente a referência ao último par concluído; restauração consulta automaticamente a dupla, valida referência/grant/manifesto e mantém confirmação de substituição.
- Operações de negócio e observadores principais coordenados pelo `DatabaseOperationGate`; ViewModels recebem a nova instância depois da substituição ou rollback.

## Decisões incorporadas

SQLite completo sem conversão dos registros ou criptografia, manifesto JSON v1 com SHA-256 para integridade acidental, SAF local, confirmação e preservação do banco anterior. Os pormenores e limitações estão em [SCOPE.md](SCOPE.md) e [RECOVERY_PROTOCOL.md](RECOVERY_PROTOCOL.md).

## Pendências e riscos técnicos

- O URI da árvore SAF é persistido separadamente com `AtomicFile` em `noBackupFilesDir` e grant persistente; ao restaurar, o app reutiliza e verifica a árvore ou permite selecioná-la novamente. Descoberta percorre `Orcamento/Backups` e aceita apenas `.db` mais `.manifest.json` com nome e generationId exatamente pareados, metadados/hash/integridade/esquema válidos; com vários pares, o usuário escolhe. Sem correspondência válida, a substituição não começa. SAF real, persistência de grants, revogação e arquivos movidos não foram testados em provedor.
- O fallback consistente para versões sem `VACUUM INTO` não está implementado; nessas versões o backup falha explicitamente sem exportar.
- `VACUUM INTO` foi compilado, mas não foi exercitado no SQLite real de dispositivo nem provada a versão efetiva; os testes instrumentados gerais não verificam exportação SAF/manifesto.
- Não há teste de morte real do processo/dispositivo em cada etapa; persistência de marcador e rename não tem garantia validada contra perda de energia.
- A limpeza dos arquivos auxiliares e rollback precisa de teste de falhas/injeção; provedores SAF e localização física variam. Não foi testado cartão SD nem provedor sem read-back.
- `connectedDebugAndroidTest` passou com 41 testes, 0 falhas e 0 ignorados, mas cobre o conjunto existente; não é evidência dos novos fluxos SAF. Falta teste instrumentado específico de backup/restauração e UI do menu.
- Configuração mantém minSdk 28; API 26 não foi habilitada. Nenhuma migração ou mudança de esquema foi feita.
