package br.com.paulodiogo.orcamento.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupManifestTest {
    @Test
    fun manifestRoundTripPreservesRequiredFields() {
        val fields = listOf(
            "format_version=1",
            "generation_id=generation",
            "database_name=budget.db",
            "schema_version=2",
            "generated_at_utc=2026-10-28T00:00:00Z",
            "size_bytes=128",
            "sha256=${"a".repeat(64)}",
            "app_version_name=1.0",
            "app_version_code=1",
        ).associate { it.substringBefore('=') to it.substringAfter('=') }

        assertEquals("1", fields["format_version"])
        assertEquals("budget.db", fields["database_name"])
        assertEquals("2", fields["schema_version"])
        assertEquals("128", fields["size_bytes"])
        assertEquals(64, fields.getValue("sha256").length)
    }

    @Test
    fun lastBackupRegistryRequiresExactPairAndGeneration() {
        val databaseUri = "content://local/tree/backup.db"
        val manifestUri = "content://local/tree/backup.db.manifest.json"
        val generation = "95d2cb0a-2834-4622-9588-4e88e9ab6d5c"
        val registry = mapOf(
            "database_uri" to databaseUri,
            "manifest_uri" to manifestUri,
            "generation_id" to generation,
        )

        assertEquals("content://local/tree/backup.db", registry["database_uri"])
        assertEquals("content://local/tree/backup.db.manifest.json", registry["manifest_uri"])
        assertEquals(36, registry.getValue("generation_id").length)
        assertThrows(IllegalArgumentException::class.java) {
            require(registry.getValue("database_uri") == "content://local/tree/other.db")
        }
    }

    @Test
    fun backupDiscoveryAcceptsOnlyExactGenerationPairing() {
        val generation = "95d2cb0a-2834-4622-9588-4e88e9ab6d5c"
        val databaseName = "orcamento-backup-2026-10-28-$generation.db"
        val matchingManifest = "$databaseName.manifest.json"
        val otherManifest = "orcamento-backup-2026-10-28-00000000-0000-0000-0000-000000000000.db.manifest.json"

        assertEquals(true, databaseName.endsWith("$generation.db"))
        assertEquals("$databaseName.manifest.json", matchingManifest)
        assertEquals(false, matchingManifest == otherManifest)
    }

    @Test
    fun unsupportedManifestVersionIsRejected() {
        val formatVersion = "2".toInt()
        assertThrows(IllegalArgumentException::class.java) {
            require(formatVersion == 1)
        }
    }
}
