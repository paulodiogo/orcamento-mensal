# Plano — Fase 07: regressão, qualidade e preparação da versão 1.0

## Escopo
Executar a regressão final autorizada das funcionalidades das Fases 01–06, verificar critérios de aceitação da versão 1.0, corrigir somente defeitos reproduzíveis dentro do escopo e verificar builds debug/release. Não inclui publicação, sincronização, login, backup, exportação ou alteração de stack.

## Dependências
- Código das Fases 01–06 presente no checkout.
- JDK 17, Gradle Wrapper e Android SDK configurados.
- Emulador BUSAO (AVD) - 16 disponível para testes instrumentados.
- Pendências conhecidas: validação manual completa da Fase 05 e cobertura de UI complementar.

## Riscos
- Testes instrumentados podem depender do estado do emulador.
- A configuração release pode produzir somente APK não assinado.
- Fluxos manuais não observados não serão declarados aprovados.

## Sequência
1. Inspecionar estado, histórico, critérios e pendências das Fases 05/06.
2. Executar testes JVM e confirmar métodos e contagens nos XML.
3. Executar testes instrumentados no dispositivo disponível.
4. Executar assembleDebug, lintDebug, assembleRelease, diff check e verificar ktlint.
5. Inspecionar assinatura release e artefatos sem expor segredos.
6. Registrar evidências, critérios, limitações e estado final nos documentos globais e desta fase.

## Critério objetivo
A fase só é concluída se todos os critérios obrigatórios tiverem evidência. Ausência de validação manual, cobertura incompleta ou release não assinado mantém o resultado parcial ou bloqueado, conforme o impacto.