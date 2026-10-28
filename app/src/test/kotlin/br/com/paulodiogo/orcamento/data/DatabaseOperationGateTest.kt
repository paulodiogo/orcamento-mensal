package br.com.paulodiogo.orcamento.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseOperationGateTest {
    @Test
    fun maintenanceWaitsForAdmittedOperationsAndBlocksNewOnes() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val write = async {
            DatabaseOperationGate.write {
                entered.complete(Unit)
                release.await()
            }
        }
        entered.await()

        val maintenance = async {
            DatabaseOperationGate.maintenance { "maintenance" }
        }
        val queuedWrite = async {
            DatabaseOperationGate.write { "write" }
        }
        release.complete(Unit)

        assertEquals("maintenance", withTimeout(2_000) { maintenance.await() })
        assertEquals("write", withTimeout(2_000) { queuedWrite.await() })
        write.await()
    }
}
