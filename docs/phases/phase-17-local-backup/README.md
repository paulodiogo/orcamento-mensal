# Fase 17 — backup e restauração local

## Objetivo e estado

Implementar backup/restauração manual do SQLite local integral, sem conversão de registros; JSON somente para metadados do manifesto. **Implementação parcial; validação funcional incompleta.** Backup/restauração SAF, snapshot real, rollback e recuperação após encerramento inesperado não estão todos demonstrados; veja [status e riscos](STATUS.md).

O arquivo `.db` contém dados financeiros legíveis e não é criptografado; seu manifesto JSON contém metadados e SHA-256, não autentica o par. O usuário deve guardar ambos em local apropriado. O backup não é sincronização nem substitui outros cuidados de segurança.

## Documentos

- [Escopo, evidências e estratégia técnica](SCOPE.md)
- [Protocolo de snapshot e recuperação](RECOVERY_PROTOCOL.md)
- [Tarefas futuras](TASKS.md)
- [Critérios de aceitação](ACCEPTANCE_CRITERIA.md)
- [Plano de validação](VALIDATION.md)
- [Status e pendências](STATUS.md)
