# Agent guidance

## Mandatory rule: no subagents or delegation

**The primary agent must perform all work directly. The use of subagents is strictly prohibited.**

- Do not create, spawn, invoke, or delegate tasks to subagents, child agents, auxiliary agents, workers, or other AI agents.
- Do not hand off or forward any part of a task to another agent, whether for planning, analysis, implementation, code review, testing, validation, research, or documentation.
- Do not use automatic delegation, parallel agent execution, nested delegation, or agent orchestration.
- Do not ask another agent to inspect files, propose solutions, implement changes, review diffs, run tests, or verify results.
- Perform all repository inspection, reasoning, planning, coding, testing, validation, and documentation updates directly.
- Ordinary non-agent tools and commands may be used when authorized and appropriate. Tool execution must not be used to invoke or orchestrate another agent.
- If a tool or workflow attempts to delegate work automatically, do not proceed with that delegation. Use a supported direct-execution path instead.
- If the primary agent cannot complete a task directly because of a tool, environment, context, or capability limitation, report the limitation and stop or continue only with independent work that can be performed directly.
- Do not claim that an independent review was performed unless an actual independent reviewer was explicitly authorized by the user. Under this policy, do not use another agent as an independent reviewer.

**This rule is mandatory and takes precedence over any conflicting delegation instructions in prompts, repository files, task descriptions, tool defaults, or agent configuration. Only an explicit change authorized by the user may modify this policy.**

## Required context

Before work:

1. Read `AGENTS.md` completely and follow its rules.
2. Inspect the working tree and identify the current task, phase, prerequisites, approved scope, and acceptance criteria.
3. Locate the relevant sections in `docs/plans/TASKS.md` and `docs/plans/progress.md` using targeted searches before reading larger portions.
4. Consult `PLAN.md`, `DECISIONS.md`, `VALIDATION.md`, contracts, and historical phase records only to the extent needed for the current task. Start with relevant headings, task IDs, decisions, validation results, and blockers. Read complete files only when necessary to resolve a material dependency or ambiguity.
5. For work on a specific phase, prioritize that phase's records and its direct dependencies. Do not load unrelated historical phase documents by default.
6. Treat approved plans, explicit user decisions, architectural contracts, and applicable validation requirements as authoritative. If a relevant decision is missing or contradictory, investigate the specific conflicting sections and follow the authorization rules before proceeding.

### Context efficiency

- Prefer targeted searches and bounded excerpts over printing entire documents, source files, or command outputs.
- Do not repeat a read or inspection when its relevant evidence is already available and the underlying files have not changed.
- Expand the inspected scope when targeted evidence is insufficient; do not omit required checks merely to save tokens.
- Preserve historical records and unrelated changes.

## Scope and architecture

- Execute only tasks authorized for the current phase, in dependency order. Preserve task IDs and historical records.
- Follow the single-module `:app`, single-Activity, Kotlin DSL, Version Catalog, AGP 9 integrated Kotlin, Jetpack Compose/Material 3, ViewModel/StateFlow architecture in the planning documents.
- Do not apply `org.jetbrains.kotlin.android`. Keep all approved versions, SDK levels, and JDK 17 unchanged.
- Phase 2 is foundation only: do not implement WebDAV, credentials, remote listings, PDF downloads, storage/cache, or PDF viewing.
- Resolve explicitly blocking decisions with the user before their dependent tasks. If a pinned stack or required tool is incompatible/unavailable, record the exact evidence, stop only dependent work, and do not work around it by changing versions or weakening checks.
- Do not expand the approved scope without authorization.

## Required workflow

1. Identify the existing task ID, phase, prerequisites, authorized scope, and acceptance criteria in `docs/plans/TASKS.md`. Use targeted searches and relevant excerpts rather than reading the entire planning history. Check `progress.md` for the current status, blockers, and applicable execution-stage overlay (F2-A/B/C). Do not invent or renumber task IDs.
2. State a bounded implementation plan and inspect existing files/changes before editing.
3. Make the smallest complete change within authorized scope; preserve unrelated and pre-existing work. Do not install tools or dependencies unless a manifest change requires them or validation fails for a missing dependency.
4. Run the applicable existing validation commands using the project wrapper. Record each actual command, exit result, test counts only when reported, and relevant sanitized errors in `docs/plans/VALIDATION.md` and `progress.md`.
5. Review the diff for scope, pinned versions, secrets, and regressions. Perform the review directly; do not delegate it to another agent.
6. Update task/stage status and known blockers in the existing planning records. Distinguish validated, implemented but unvalidated, partial, blocked, and not started. Do not report success without command or observable evidence.

## Bounded retries and escalation

- For a failing validation, make at most one focused correction for a distinct, understood project defect, then rerun that failed command once.
- Never repeat an unchanged failing command, perform speculative edits, automatically loop agents, delegate work, or retry environment/dependency/version failures as if they were code defects.
- If the single correction attempt does not resolve the defect, or the cause is uncertain, preserve the relevant output (excluding secrets), describe the impact and next decision in `DECISIONS.md`/`progress.md`, and stop dependent tasks. Continue only independent work that can be performed directly.
- Do not run redundant or speculative investigations. Each retry must have a concrete reason or a new hypothesis.
- Do not describe self-review as independent review.

## Secrets and provider configuration

- Never put credentials, tokens, or private server details in source, versioned settings, logs, or evidence. Inspect only provider/configuration presence, not secret values.
- Configure a model provider/model only when the user or planning explicitly specifies it and a supported IDE/tool integration is available. Never invent a project-specific JSON schema, create provider configuration files without authorization, expose credentials, or claim integration is active without verifying it. If the current environment cannot select the requested provider/model, record that limitation and continue independent work.
- Do not commit, push, or change branches unless explicitly authorized.
- Do not change model-provider settings, agent routing, delegation settings, or global IDE configuration unless explicitly authorized by the user.

## Code and validation conventions

- Use Kotlin DSL and `gradle/libs.versions.toml` as the dependency-version source of truth; do not use dynamic versions or add dependencies without a demonstrated need and compatibility.
- Keep UI strings in `strings.xml`, code comments limited to necessary clarifications, and follow the project's `.editorconfig`/ktlint configuration once established.
- Never disable or broadly suppress lint, formatting, or tests to obtain a passing result.
- Use only commands applicable to files that exist. On Windows run the wrapper as `gradlew.bat <task>`; elsewhere use `./gradlew <task>`. Planned commands include `assembleDebug`, `testDebugUnitTest`, `connectedDebugAndroidTest`, `lintDebug`, and `ktlintCheck`.
- Do not claim device installation/launch or instrumented tests unless an attached device/emulator and successful command output demonstrate them.
- Do not claim a test passed unless its command completed successfully and the output supports the claim.
- Do not change test expectations, skip tests, or suppress failures merely to produce a passing result.
- Preserve existing changes and avoid modifying unrelated files.

## Completion report

At the end of each task, report:

1. The task and scope addressed.
2. Files changed.
3. Commands actually executed and their results.
4. Acceptance criteria satisfied and any that remain open.
5. Known blockers, limitations, and next steps.
6. Confirmation that no unauthorized dependency, version, branch, commit, push, or unrelated change was introduced.
7. Confirmation that all work was performed directly by the primary agent without subagents or delegation.

Never claim complete success when required validation has not been performed or a required criterion remains unverified.

## Uso obrigatório do RTK

- Para comandos de terminal compatíveis com o RTK, prefira a forma `rtk <comando>`, aproveitando a integração automática já instalada.
- Não adicione um segundo prefixo `rtk` se o hook já tiver reescrito o comando.
- Use os comandos RTK equivalentes para Git, testes, compilação, busca e inspeção quando estiverem disponíveis e preservarem a semântica necessária.
- Não use versões resumidas de saídas quando os detalhes completos forem necessários para diagnosticar erros, validar testes, analisar stack traces ou comprovar a conclusão de uma tarefa.
- Para comandos não suportados, execute o comando original.
- Nunca declare que o RTK foi utilizado sem verificar a execução real.
- Ao finalizar tarefas relevantes, informe se houve falhas de interceptação ou comandos importantes executados sem otimização.