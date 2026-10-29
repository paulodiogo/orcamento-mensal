package br.com.paulodiogo.orcamento.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.DocumentsContract
import android.util.AtomicFile
import androidx.room.Room
import androidx.documentfile.provider.DocumentFile
import android.os.Build
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AvailableBackup(val databaseUri: Uri, val manifestUri: Uri, val displayName: String, val generationId: String)

class LocalBackupManager(context: Context) {
    private val appContext = context.applicationContext
    private val operationMutex = Mutex()
    private val databaseFile = appContext.getDatabasePath("budget.db")
    private val stateFile = AtomicFile(File(databaseFile.parentFile, "budget-restore.state"))
    private val lastBackupFile = AtomicFile(File(appContext.noBackupFilesDir, "last-local-backup.json"))
    private val backupFolderFile = AtomicFile(File(appContext.noBackupFilesDir, "backup-folder.json"))

    suspend fun createBackup(
        treeUri: Uri,
        pauseObservers: suspend () -> Unit = {},
        onDatabaseReady: () -> Unit = {},
    ) = operationMutex.withLock {
        DatabaseOperationGate.maintenance {
            validateNoUncommittedRestore()
            pauseObservers()
            try {
                withContext(Dispatchers.IO) { createBackupBlocking(treeUri) }
            } finally {
                onDatabaseReady()
            }
        }
    }

    suspend fun lastBackupAvailability(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { readLastBackupPair(); Unit }
    }

    suspend fun rememberBackupFolder(treeUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val persisted = appContext.contentResolver.persistedUriPermissions
            require(persisted.any { it.uri == treeUri && it.isReadPermission }) {
                "A permissão persistente à pasta não está disponível"
            }
            writeAtomicJson(backupFolderFile, JSONObject().put("format_version", 1).put("tree_uri", treeUri.toString()))
        }
    }

    suspend fun registeredBackupFolder(): Result<Uri?> = withContext(Dispatchers.IO) {
        runCatching { readBackupFolder() }
    }

    suspend fun discoverBackups(treeUri: Uri): Result<List<AvailableBackup>> = withContext(Dispatchers.IO) {
        runCatching {
            require(readBackupFolder() == treeUri) { "A pasta selecionada não está registrada ou autorizada" }
            discoverBackupsInTree(treeUri)
        }
    }

    suspend fun discoverRegisteredBackups(): Result<List<AvailableBackup>> = withContext(Dispatchers.IO) {
        runCatching {
            val tree = readBackupFolder() ?: error("Selecione a pasta que contém os backups")
            discoverBackupsInTree(tree)
        }
    }

    suspend fun lastBackupSummary(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val registered = lastBackupFile.openRead().bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
            registered.getString("database_display_name")
        }
    }

    suspend fun restoreSelectedBackup(
        backup: AvailableBackup,
        pauseObservers: suspend () -> Unit = {},
        onDatabaseReplaced: () -> Unit = {},
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(readBackupFolder() != null) { "Selecione novamente a pasta com o backup" }
            restoreBackup(backup.databaseUri, backup.manifestUri, pauseObservers, onDatabaseReplaced)
        }
    }

    suspend fun restoreLastBackup(
        pauseObservers: suspend () -> Unit = {},
        onDatabaseReplaced: () -> Unit = {},
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val (database, manifest) = readLastBackupPair()
            restoreBackup(database, manifest, pauseObservers, onDatabaseReplaced)
        }
    }

    private fun readBackupFolder(): Uri? {
        if (!backupFolderFile.baseFile.isFile) return null
        val json = backupFolderFile.openRead().bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        require(json.getInt("format_version") == 1) { "Registro da pasta inválido" }
        val uri = Uri.parse(json.getString("tree_uri"))
        require(uri.scheme == "content" && DocumentsContract.isTreeUri(uri)) {
            "A referência registrada não é uma árvore SAF válida"
        }
        require(appContext.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }) {
            "A permissão da pasta foi revogada"
        }
        require(DocumentFile.fromTreeUri(appContext, uri)?.exists() == true) {
            "A pasta registrada não está acessível"
        }
        return uri
    }

    private fun persistBackupFolder(treeUri: Uri) {
        writeAtomicJson(backupFolderFile, JSONObject().put("format_version", 1).put("tree_uri", treeUri.toString()))
    }

    private fun writeAtomicJson(file: AtomicFile, json: JSONObject) {
        val stream = file.startWrite()
        try {
            stream.write(json.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (error: Throwable) {
            file.failWrite(stream)
            throw error
        }
    }

    private fun discoverBackupsInTree(treeUri: Uri): List<AvailableBackup> {
        require(treeUri.scheme == "content" && DocumentsContract.isTreeUri(treeUri)) {
            "URI de árvore SAF inválido"
        }
        val tree = DocumentFile.fromTreeUri(appContext, treeUri)
            ?: error("Não foi possível abrir a árvore SAF selecionada")
        require(tree.exists() && tree.isDirectory) { "A pasta selecionada não está acessível" }
        val folder = findBackupDirectory(tree)
        val files = folder.listFiles().associateBy { it.name ?: "" }
            .mapValues { (_, document) -> document.uri }
        require(files.isNotEmpty() || folder.canRead()) { "Não foi possível listar a pasta de backups" }
        val pattern = Regex("orcamento-backup-(\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2})\\.db")
        val found = mutableListOf<AvailableBackup>()
        files.keys.filter { it.matches(pattern) }.forEach { databaseName ->
            val match = pattern.matchEntire(databaseName) ?: return@forEach
            val generation = match.groupValues[1]
            val manifestName = "$databaseName.manifest.json"
            val databaseUri = files[databaseName] ?: return@forEach
            val manifestUri = files[manifestName] ?: return@forEach
            runCatching {
                val databaseTemp = File(appContext.cacheDir, "discover-$generation.db")
                val manifestTemp = File(appContext.cacheDir, "discover-$generation.json")
                try {
                    copyUriToFile(databaseUri, databaseTemp)
                    copyUriToFile(manifestUri, manifestTemp)
                    val metadata = JSONObject(manifestTemp.readText(Charsets.UTF_8))
                    require(metadata.getInt("format_version") == 1)
                    require(metadata.getString("generation_id") == generation)
                    require(metadata.getString("database_name") == "budget.db")
                    require(metadata.getLong("size_bytes") == databaseTemp.length())
                    require(metadata.getString("sha256") == sha256(databaseTemp))
                    validateDatabase(databaseTemp)
                    val schema = readUserVersion(databaseTemp)
                    require(schema == metadata.getInt("schema_version") && schema in 1..BudgetDatabase.SCHEMA_VERSION)
                } finally {
                    databaseTemp.delete()
                    manifestTemp.delete()
                }
            }.onSuccess {
                found += AvailableBackup(databaseUri, manifestUri, databaseName, generation)
            }
        }
        return found.sortedByDescending { it.displayName }
    }

    private fun findBackupDirectory(root: DocumentFile): DocumentFile {
        if (root.name == "Backups" && root.isDirectory) return root
        val orcamento = root.findFile("Orcamento")
        val backups = orcamento?.takeIf { it.isDirectory }?.findFile("Backups")
        return backups?.takeIf { it.isDirectory }
            ?: error("A pasta Orcamento/Backups não existe")
    }

    private fun resolveBackupDirectory(treeUri: Uri): Uri {
        require(treeUri.scheme == "content" && DocumentsContract.isTreeUri(treeUri)) {
            "URI de árvore SAF inválido"
        }
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootId)
        val rootName = runCatching {
            appContext.contentResolver.query(rootUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
        if (rootName == "Backups") return rootUri
        var parentId = rootId
        for (segment in listOf("Orcamento", "Backups")) {
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
            var nextId: String? = null
            appContext.contentResolver.query(
                children,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    if (cursor.getString(1) == segment) {
                        nextId = cursor.getString(0)
                        break
                    }
                }
            } ?: error("Não foi possível ler a pasta selecionada")
            parentId = nextId ?: error("A pasta Orcamento/Backups não existe")
        }
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId)
    }

    private fun readLastBackupPair(): Pair<Uri, Uri> {
        require(lastBackupFile.baseFile.isFile) { "Nenhum backup disponível. Crie um backup primeiro." }
        val registered = lastBackupFile.openRead().bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        require(registered.getInt("format_version") == 1) { "Registro do último backup inválido" }
        val tree = Uri.parse(registered.getString("tree_uri"))
        val database = Uri.parse(registered.getString("database_uri"))
        val manifest = Uri.parse(registered.getString("manifest_uri"))
        val registeredFolder = readBackupFolder()
        require(registeredFolder == tree) { "Selecione novamente a pasta com os backups" }
        val persisted = appContext.contentResolver.persistedUriPermissions
        require(persisted.any { it.uri == tree && it.isReadPermission }) {
            "A permissão à pasta do último backup foi revogada; selecione novamente a pasta"
        }
        val generation = registered.getString("generation_id")
        require(database != manifest && registered.getString("database_name") == "budget.db") {
            "O registro não identifica um par de backup válido"
        }
        verifyDocument(database, registered.getLong("size_bytes"), registered.getString("sha256"))
        val manifestFile = File(appContext.cacheDir, "verify-last-${UUID.randomUUID()}.json")
        try {
            copyUriToFile(manifest, manifestFile)
            val metadata = JSONObject(manifestFile.readText(Charsets.UTF_8))
            require(metadata.getInt("format_version") == 1)
            require(metadata.getString("generation_id") == generation)
            require(metadata.getLong("size_bytes") == registered.getLong("size_bytes"))
            require(metadata.getString("sha256") == registered.getString("sha256"))
            require(metadata.getString("database_name") == "budget.db")
        } finally {
            manifestFile.delete()
        }
        return database to manifest
    }

    suspend fun restoreBackup(
        databaseUri: Uri,
        manifestUri: Uri,
        pauseObservers: suspend () -> Unit = {},
        onDatabaseReplaced: () -> Unit = {},
    ) = operationMutex.withLock {
        withContext(Dispatchers.IO) {
            DatabaseOperationGate.maintenance {
                validateNoUncommittedRestore()
                restoreBackupBlocking(databaseUri, manifestUri, pauseObservers, onDatabaseReplaced)
            }
        }
    }

    private fun createBackupBlocking(treeUri: Uri) {

        val dataHora = LocalDateTime.now()

        val formatador = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        val generation = dataHora.format(formatador)

        val temporaryDirectory = File(appContext.cacheDir, "backup-$generation")
        val snapshot = File(temporaryDirectory, "budget.db")
        val manifest = File(temporaryDirectory, "backup.manifest.json")
        persistBackupState("PREPARING|$generation|${temporaryDirectory.name}")
        check(temporaryDirectory.mkdirs()) { "Não foi possível preparar o backup" }
        try {
            validateNoUncommittedRestore()
            val db = BudgetDatabase.getInstance(appContext)
            val sqliteVersion = db.openHelper.writableDatabase.query("SELECT sqlite_version()").use { cursor ->
                check(cursor.moveToFirst()) { "Versão SQLite indisponível" }
                cursor.getString(0)
            }
            if (supportsVacuumInto(sqliteVersion)) {
                val path = snapshot.absolutePath.replace("'", "''")
                db.openHelper.writableDatabase.execSQL("VACUUM main INTO '$path'")
            } else {
                error("Esta versão do SQLite não oferece um mecanismo de snapshot seguro")
            }
            validateDatabase(snapshot)
            val roomValidationFile = appContext.getDatabasePath("backup-validate-$generation.db")
            validateRoomCandidate(snapshot, roomValidationFile, keepTarget = false)
            val size = snapshot.length()
            val digest = sha256(snapshot)
            val schemaVersion = readUserVersion(snapshot)
            require(schemaVersion == BudgetDatabase.SCHEMA_VERSION) { "O esquema do backup não é compatível com este aplicativo" }
            val manifestJson = JSONObject()
                .put("format_version", 1)
                .put("generation_id", generation)
                .put("database_name", "budget.db")
                .put("schema_version", schemaVersion)
                .put("generated_at_utc", Instant.now().toString())
                .put("size_bytes", size)
                .put("sha256", digest)
                .put("app_version_name", appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName)
                .put("app_version_code", appContext.packageManager.getPackageInfo(appContext.packageName, 0).longVersionCode)
            manifest.writeText(manifestJson.toString(), Charsets.UTF_8)
            persistBackupState("SNAPSHOT_READY|$generation|${temporaryDirectory.name}|$size|$digest")
            val directory = ensureBackupDirectory(treeUri)
            val base = "orcamento-backup-$generation"
            persistBackupState("EXPORTING|$generation|${temporaryDirectory.name}|${directory}|$base")
            val dbUri = writeDocument(directory, "$base.db.partial", snapshot)
            val manifestUri = writeBytesDocument(directory, "$base.manifest.json.partial", manifest.readBytes())
            persistBackupState("PUBLISHING|$generation|${temporaryDirectory.name}|${directory}|$base|$dbUri|$manifestUri")
            verifyDocument(dbUri, size, digest)
            verifyDocument(manifestUri, manifest.length(), sha256(manifest))
            val publishedDatabase = DocumentsContract.renameDocument(appContext.contentResolver, dbUri, "$base.db")
                ?: error("O provedor não permite publicar o banco com segurança")
            val publishedManifest = DocumentsContract.renameDocument(appContext.contentResolver, manifestUri, "$base.db.manifest.json")
                ?: error("O provedor não permite publicar o manifesto com segurança")
            verifyDocument(publishedDatabase, size, digest)
            verifyDocument(publishedManifest, manifest.length(), sha256(manifest))
            persistBackupFolder(treeUri)
            persistLastBackup(
                JSONObject()
                    .put("format_version", 1)
                    .put("generation_id", generation)
                    .put("tree_uri", treeUri.toString())
                    .put("database_uri", publishedDatabase.toString())
                    .put("manifest_uri", publishedManifest.toString())
                    .put("database_display_name", "$base.db")
                    .put("manifest_display_name", "$base.db.manifest.json")
                    .put("database_name", "budget.db")
                    .put("size_bytes", size)
                    .put("sha256", digest)
                    .put("schema_version", schemaVersion),
            )
            persistBackupState("COMPLETED|$generation|${temporaryDirectory.name}|$publishedDatabase|$publishedManifest")
        } finally {
            if (stateFile.baseFile.exists()) persistBackupState("CLEANING|$generation|${temporaryDirectory.name}")
            temporaryDirectory.deleteRecursively()
            if (stateFile.baseFile.exists()) stateFile.delete()
        }
    }

    private suspend fun restoreBackupBlocking(databaseUri: Uri, manifestUri: Uri, pauseObservers: suspend () -> Unit, onDatabaseReplaced: () -> Unit) {
        val generation = UUID.randomUUID().toString()
        val incoming = File(databaseFile.parentFile, "budget-restore-$generation.tmp")
        val candidate = File(databaseFile.parentFile, "budget-candidate-$generation.db")
        val previous = File(databaseFile.parentFile, "budget-previous-$generation.db")
        val rollback = File(databaseFile.parentFile, "budget-rollback-$generation.db")
        val manifestFile = File(databaseFile.parentFile, "budget-manifest-$generation.json")
        val operationId = generation
        var preserveRecoveryArtifacts = false
        try {
            copyUriToFile(databaseUri, incoming)
            copyUriToFile(manifestUri, manifestFile)
            val json = JSONObject(manifestFile.readText(Charsets.UTF_8))
            require(json.getInt("format_version") == 1) { "Formato de backup não suportado" }
            require(json.getString("generation_id").matches(Regex("(\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2})"))) { "Geração de backup inválida" }
            require(json.getString("database_name") == "budget.db") { "Manifesto não corresponde ao banco" }
            require(json.getString("sha256").matches(Regex("[0-9a-f]{64}"))) { "Checksum do manifesto inválido" }
            require(json.getLong("size_bytes") > 0L) { "Tamanho do manifesto inválido" }
            require(runCatching { Instant.parse(json.getString("generated_at_utc")) }.isSuccess) { "Data de geração inválida" }
            require(json.getString("app_version_name").isNotBlank() && json.getLong("app_version_code") > 0L) { "Versão de aplicativo inválida" }
            require(json.getLong("size_bytes") == incoming.length()) { "Tamanho do backup divergente" }
            require(json.getString("sha256") == sha256(incoming)) { "Checksum do backup divergente" }
            validateDatabase(incoming)
            val schemaVersion = readUserVersion(incoming)
            require(schemaVersion == json.getInt("schema_version")) { "Esquema não corresponde ao manifesto" }
            require(schemaVersion in 1..BudgetDatabase.SCHEMA_VERSION) { "Esquema futuro não é compatível" }
            validateRoomCandidate(incoming, candidate, keepTarget = true)
            require(readUserVersion(candidate) == BudgetDatabase.SCHEMA_VERSION) { "Não foi possível migrar o backup ao esquema atual" }
            validateDatabase(candidate)

            validateNoUncommittedRestore()
            persistState("PREPARING|$operationId|${previous.name}|${candidate.name}")
            pauseObservers()
            val activeDatabase = BudgetDatabase.getInstance(appContext)
            val activeSqlite = activeDatabase.openHelper.writableDatabase
            if (activeSqlite.query("PRAGMA journal_mode").use { cursor -> cursor.moveToFirst() && cursor.getString(0).equals("wal", true) }) {
                activeSqlite.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                    require(cursor.moveToFirst() && cursor.getInt(0) == 0) { "Não foi possível consolidar o WAL" }
                    require(cursor.getInt(1) == 0 && cursor.getInt(2) == 0) { "O checkpoint do WAL não foi concluído" }
                }
            }
            BudgetDatabase.closeInstance()
            require(databaseFile.isFile) { "Banco atual não foi inicializado" }
            copyFile(databaseFile, previous)
            validateDatabase(previous)
            persistState("PRESERVED|$operationId|${previous.name}|${candidate.name}")
            copyFile(candidate, rollback)
            persistState("SWAPPING|$operationId|${previous.name}|${rollback.name}")
            removeSidecars(databaseFile)
            check(rollback.renameTo(databaseFile)) { "Não foi possível substituir o banco" }
            persistState("VALIDATING|$operationId|${previous.name}|${candidate.name}")
            val restored = BudgetDatabase.getInstance(appContext)
            validateOpenDatabase(restored)
            persistState("COMMITTED|$operationId|${previous.name}|${candidate.name}")
            onDatabaseReplaced()
            persistState("COMMITTED|$operationId|${previous.name}|${candidate.name}")
            previous.delete()
            candidate.delete()
            persistState(null)
        } catch (error: Throwable) {
            runCatching {
                if (!stateFile.baseFile.exists()) return@runCatching
                val phase = stateFile.openRead().bufferedReader().use { it.readText() }.substringBefore('|')
                if (phase == "PREPARING") {
                    persistState(null)
                    onDatabaseReplaced()
                } else if (previous.isFile) {
                    persistState("RECOVERING|$operationId|${previous.name}|${rollback.name}")
                    BudgetDatabase.closeInstance()
                    rollback.delete()
                    copyFile(previous, rollback)
                    removeSidecars(databaseFile)
                    check(rollback.renameTo(databaseFile))
                    BudgetDatabase.clearInstance()
                    val restored = BudgetDatabase.getInstance(appContext)
                    validateOpenDatabase(restored)
                    persistState("RECOVERED|$operationId|${previous.name}|${candidate.name}")
                    onDatabaseReplaced()
                    persistState(null)
                } else {
                    error("A cópia recuperável do banco anterior não está disponível")
                }
            }.onFailure {
                preserveRecoveryArtifacts = true
                DatabaseOperationGate.blockPermanently()
                error.addSuppressed(it)
            }
            throw error
        } finally {
            incoming.delete()
            manifestFile.delete()
            if (!preserveRecoveryArtifacts && !stateFile.baseFile.exists()) {
                candidate.delete()
                rollback.delete()
                previous.delete()
            }
        }
    }

    private fun validateRoomCandidate(source: File, target: File, keepTarget: Boolean) {
        check(source.copyTo(target, overwrite = false).length() > 0L)
        val testDb = Room.databaseBuilder(appContext, BudgetDatabase::class.java, target.name)
            .addMigrations(BudgetDatabase.MIGRATION_1_2)
            .build()
        try {
            testDb.openHelper.writableDatabase.query("PRAGMA integrity_check").use { cursor ->
                check(cursor.moveToFirst() && cursor.getString(0) == "ok") { "O Room rejeitou o banco" }
            }
        } finally {
            testDb.close()
            if (!keepTarget) {
                target.delete()
                listOf("-wal", "-shm", "-journal").forEach { File(target.parentFile, target.name + it).delete() }
            }
        }
    }

    private fun validateOpenDatabase(database: BudgetDatabase) {
        database.openHelper.writableDatabase.query("PRAGMA integrity_check").use { cursor ->
            require(cursor.moveToFirst() && cursor.getString(0) == "ok") { "O banco não passou na verificação de integridade" }
            while (cursor.moveToNext()) require(cursor.getString(0) == "ok")
        }
        database.openHelper.writableDatabase.query("PRAGMA foreign_key_check").use { cursor ->
            require(!cursor.moveToFirst()) { "O banco contém relações inválidas" }
        }
    }

    private fun validateDatabase(file: File) {
        require(file.isFile && file.length() > 0L) { "Arquivo de banco vazio ou ausente" }
        val sqlite = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            sqlite.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                require(cursor.moveToFirst() && cursor.getString(0) == "ok") { "Integridade SQLite inválida" }
                while (cursor.moveToNext()) require(cursor.getString(0) == "ok") { "Integridade SQLite inválida" }
            }
            sqlite.rawQuery("PRAGMA foreign_key_check", null).use { cursor ->
                require(!cursor.moveToFirst()) { "O banco contém relações inválidas" }
            }
        } finally {
            sqlite.close()
        }
    }

    private fun readUserVersion(file: File): Int = SQLiteDatabase.openDatabase(
        file.absolutePath,
        null,
        SQLiteDatabase.OPEN_READONLY,
    ).use { sqlite ->
        sqlite.rawQuery("PRAGMA user_version", null).use { cursor ->
            check(cursor.moveToFirst())
            cursor.getInt(0)
        }
    }

    private fun ensureBackupDirectory(treeUri: Uri): Uri {
        var parent = DocumentsContract.getTreeDocumentId(treeUri)
        for (segment in listOf("Orcamento", "Backups")) {
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parent)
            var found: Uri? = null
            appContext.contentResolver.query(children, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) if (cursor.getString(1) == segment) {
                    found = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0))
                    break
                }
            }
            val child = found ?: DocumentsContract.createDocument(appContext.contentResolver, DocumentsContract.buildDocumentUriUsingTree(treeUri, parent), DocumentsContract.Document.MIME_TYPE_DIR, segment)
                ?: error("Não foi possível criar a pasta de backup")
            parent = DocumentsContract.getDocumentId(child)
        }
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, parent)
    }

    private fun writeDocument(parent: Uri, name: String, file: File): Uri {
        val uri = DocumentsContract.createDocument(appContext.contentResolver, parent, "application/octet-stream", name)
            ?: error("Não foi possível criar o arquivo de backup")
        appContext.contentResolver.openOutputStream(uri, "wt")?.use { output -> FileInputStream(file).use { it.copyTo(output) } }
            ?: error("Não foi possível gravar o arquivo de backup")
        return uri
    }

    private fun writeBytesDocument(parent: Uri, name: String, bytes: ByteArray): Uri {
        val uri = DocumentsContract.createDocument(appContext.contentResolver, parent, "application/json", name)
            ?: error("Não foi possível criar o manifesto")
        appContext.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
            ?: error("Não foi possível gravar o manifesto")
        return uri
    }

    private fun verifyDocument(uri: Uri, expectedSize: Long, expectedHash: String) {
        val temp = File(appContext.cacheDir, "verify-${UUID.randomUUID()}.tmp")
        try {
            appContext.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(temp).use { input.copyTo(it) } }
                ?: error("O provedor não permite reler o backup")
            check(temp.length() == expectedSize && sha256(temp) == expectedHash) { "A verificação do arquivo exportado falhou" }
        } finally {
            temp.delete()
        }
    }

    private fun copyUriToFile(uri: Uri, file: File) {
        appContext.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { input.copyTo(it) } }
            ?: error("Não foi possível ler o arquivo selecionado")
    }

    private fun copyFile(source: File, target: File) {
        FileInputStream(source).use { input -> FileOutputStream(target).use { output -> input.copyTo(output); output.fd.sync() } }
    }

    private fun removeSidecars(file: File) {
        listOf("-wal", "-shm", "-journal").forEach { suffix ->
            val sidecar = File(file.parentFile, file.name + suffix)
            if (sidecar.exists()) check(sidecar.delete()) { "Não foi possível preparar a substituição do banco" }
        }
    }

    private fun validateNoUncommittedRestore() {
        if (!stateFile.baseFile.exists()) return
        val phase = stateFile.openRead().bufferedReader().use { it.readText() }.substringBefore('|')
        check(phase == "COMMITTED" || phase == "RECOVERED") { "Uma recuperação pendente bloqueia novas operações" }
        stateFile.delete()
    }

    private fun persistLastBackup(value: JSONObject) {
        writeAtomicJson(lastBackupFile, value)
    }

    private fun persistBackupState(value: String) {
        val operationState = AtomicFile(File(databaseFile.parentFile, "budget-backup.state"))
        val stream = operationState.startWrite()
        try {
            stream.write(value.toByteArray(Charsets.UTF_8))
            operationState.finishWrite(stream)
        } catch (error: Throwable) {
            operationState.failWrite(stream)
            throw error
        }
    }

    private fun persistState(value: String?) {
        if (value == null) {
            stateFile.delete()
            return
        }
        val stream = stateFile.startWrite()
        try {
            stream.write(value.toByteArray(Charsets.UTF_8))
            stateFile.finishWrite(stream)
        } catch (error: Throwable) {
            stateFile.failWrite(stream)
            throw error
        }
    }

    private fun supportsVacuumInto(version: String): Boolean {
        val current = version.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val minimum = listOf(3, 27, 0)
        return (0 until minimum.size).firstNotNullOfOrNull { index ->
            when {
                current.getOrElse(index) { 0 } > minimum[index] -> true
                current.getOrElse(index) { 0 } < minimum[index] -> false
                else -> null
            }
        } ?: true
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun recoverBeforeRoom(context: Context) {
            val appContext = context.applicationContext
            val dbFile = appContext.getDatabasePath("budget.db")
            val backupStateFile = AtomicFile(File(dbFile.parentFile, "budget-backup.state"))
            if (backupStateFile.baseFile.exists()) {
                val backupState = backupStateFile.openRead().bufferedReader().use { it.readText() }.split('|')
                require(backupState.size >= 3) { "O backup interrompido precisa de atenção" }
                val temporaryDirectory = File(appContext.cacheDir, backupState[2])
                require(temporaryDirectory.parentFile == appContext.cacheDir && temporaryDirectory.name.startsWith("backup-"))
                if (backupState[0] != "COMPLETED" && backupState[0] != "PREPARING" && backupState[0] != "SNAPSHOT_READY" && backupState.size >= 7) {
                    val resolver = appContext.contentResolver
                    listOf(backupState[5], backupState[6]).forEach { raw ->
                        runCatching { DocumentsContract.deleteDocument(resolver, Uri.parse(raw)) }
                    }
                }
                temporaryDirectory.deleteRecursively()
                backupStateFile.delete()
            }
            val stateFile = AtomicFile(File(dbFile.parentFile, "budget-restore.state"))
            if (!stateFile.baseFile.exists()) return
            val state = stateFile.openRead().bufferedReader().use { it.readText() }.split('|')
            require(state.size == 4) { "A recuperação do banco precisa de atenção" }
            val stateName = state[0]
            val previous = File(dbFile.parentFile, state[2])
            val candidate = File(dbFile.parentFile, state[3])
            when (stateName) {
                "PREPARING" -> {
                    require(dbFile.isFile && dbFile.length() > 0) { "O banco original precisa de recuperação" }
                    SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
                        sqlite.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                            require(cursor.moveToFirst() && cursor.getString(0) == "ok") { "O banco original precisa de recuperação" }
                        }
                    }
                    stateFile.delete()
                    candidate.delete()
                    previous.delete()
                }
                "PRESERVED", "SWAPPING", "VALIDATING", "RECOVERING" -> {
                    require(previous.isFile && previous.length() > 0) { "A cópia anterior não está disponível" }
                    val rollback = File(dbFile.parentFile, "budget-recovery-${state[1]}.tmp")
                    FileInputStream(previous).use { input -> FileOutputStream(rollback).use { output -> input.copyTo(output); output.fd.sync() } }
                    listOf("-wal", "-shm", "-journal").forEach { File(dbFile.parentFile, dbFile.name + it).delete() }
                    check(rollback.renameTo(dbFile)) { "Não foi possível recuperar o banco anterior" }
                    DatabaseOperationGate.blockPermanently()
                    stateFile.delete()
                }
                "COMMITTED", "RECOVERED" -> {
                require(dbFile.isFile && dbFile.length() > 0) { "Banco confirmado não está disponível" }
                previous.delete()
                candidate.delete()
                stateFile.delete()
                }
                else -> error("Estado de recuperação desconhecido")
            }
        }
    }
}
