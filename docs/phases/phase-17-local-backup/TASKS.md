# Tarefas futuras — Fase 17

Estado de execução após autorização de 2026-10-28: etapas parcialmente implementadas; tarefas abaixo preservam a ordem e identificam cobertura remanescente. Não há IDs de Fase 17 preexistentes nos registros globais.

1. **Comprovar viabilidade técnica (parcial)**: a compilação confirmou APIs usadas, mas não há execução em dispositivo dos mecanismos, WAL ou provedores SAF. Falta testar `sqlite_version()`, `VACUUM INTO`, driver Room, alternativa, sync/rename e recuperação por reinício conforme [protocolo](RECOVERY_PROTOCOL.md).
2. **Coordenar ciclo de vida do banco** (depende de 1): barreira de acesso a operações completas, drenagem, suspensão de coletores, fechamento e substituição das referências e ViewModels; recuperar marcador antes de qualquer abertura.
3. **Snapshot/publicação (parcial)**: `VACUUM INTO`, validação, manifesto, SHA-256, diretórios SAF, streams e read-back implementados. Fallback SQLite <3.27 ausente; fluxo real de provedor SAF sem teste instrumentado. Nunca declarar backup se `VACUUM INTO` indisponível.
4. **Validar e preparar restauração** (depende de 1): selecionar par SAF, verificar manifesto, tamanho e hash antes de validar SQLite, integridade, FKs, versão e Room em cópia isolada; migrar apenas por caminhos existentes, sem fallback destrutivo.
5. **Instalar/recuperar (parcial)**: confirmação, marcador, cópia anterior, troca e rollback tentados; teste unitário/instrumentado de falhas em rename, abertura e morte de processo permanece pendente. Nunca considerar a recuperação após reinício validada apenas pelos testes atuais.
6. **Localizar pasta/selecionar par (parcialmente implementado)**: persistência URI da árvore, re-seleção, descoberta validada por geração e escolha múltipla implementadas. Teste SAF instrumentado de URI/revogação/arquivos movidos e confirmação com par selecionado ainda pendente.
7. **Teste e fechamento pendentes**: acrescentar testes de snapshot/SAF/rollback/recovery, executar teste de UI e injeção de falhas/reinício real; atualizar resultados somente conforme evidência.
