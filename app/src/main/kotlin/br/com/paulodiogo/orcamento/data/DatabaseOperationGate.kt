package br.com.paulodiogo.orcamento.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

object DatabaseOperationGate {
    private class Lease : CoroutineContext.Element {
        override val key: CoroutineContext.Key<*> get() = LeaseKey
    }

    private object LeaseKey : CoroutineContext.Key<Lease>

    private val stateMutex = Mutex()
    private val maintenanceMutex = Mutex()
    private var maintenanceActive = false
    private var permanentlyBlocked = false
    private var activeOperations = 0
    private var operationsDrained = CompletableDeferred(Unit)
    private var maintenanceFinished = CompletableDeferred(Unit)
    private var maintenanceDrained = CompletableDeferred(Unit)

    suspend fun <T> write(block: suspend () -> T): T = operation(block)

    suspend fun <T> read(block: suspend () -> T): T = operation(block)

    suspend fun <T> maintenance(block: suspend () -> T): T = maintenanceMutex.withLock {
        val drained = stateMutex.withLock {
            check(!permanentlyBlocked) { "A recuperação do banco está pendente" }
            maintenanceActive = true
            maintenanceFinished = CompletableDeferred()
            maintenanceDrained = CompletableDeferred()
            if (activeOperations == 0) maintenanceDrained.complete(Unit)
            maintenanceDrained
        }
        try {
            drained.await()
            withContext(Lease()) { block() }
        } finally {
            stateMutex.withLock {
                if (!permanentlyBlocked) {
                    maintenanceActive = false
                    maintenanceFinished.complete(Unit)
                }
            }
        }
    }

    private suspend fun <T> operation(block: suspend () -> T): T {
        if (currentCoroutineContext()[LeaseKey] != null) return block()
        while (true) {
            var waitForMaintenance: CompletableDeferred<Unit>? = null
            var accepted = false
            stateMutex.withLock {
                check(!permanentlyBlocked) { "A recuperação do banco está pendente" }
                if (maintenanceActive) {
                    waitForMaintenance = maintenanceFinished
                } else {
                    if (activeOperations == 0) operationsDrained = CompletableDeferred()
                    activeOperations++
                    accepted = true
                }
            }
            if (accepted) break
            waitForMaintenance!!.await()
        }
        try {
            return withContext(Lease()) { block() }
        } finally {
            stateMutex.withLock {
                activeOperations--
                if (activeOperations == 0) {
                    operationsDrained.complete(Unit)
                    if (maintenanceActive) maintenanceDrained.complete(Unit)
                }
            }
        }
    }

    suspend fun flow(block: suspend () -> Unit) = block()

    fun blockPermanently() {
        permanentlyBlocked = true
        maintenanceActive = true
    }
}
