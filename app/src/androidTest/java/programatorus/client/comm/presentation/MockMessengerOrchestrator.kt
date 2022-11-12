package programatorus.client.comm.presentation

import programatorus.client.comm.presentation.mock.MockMessenger
import programus.proto.Protocol.GenericMessage
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

class MockMessengerOrchestrator {

    private val mSessionLock = ReentrantReadWriteLock()
    private val mSessions = mutableListOf<MockMessenger>()

    fun register(messenger: MockMessenger) {
        mSessionLock.writeLock().withLock {
            mSessions.add(messenger)
        }
    }

    fun mockError() {
        mSessionLock.readLock().withLock {
            mSessions.forEach { it.mockError() }
        }
    }

    fun mockMessage(message: GenericMessage) {
        mSessionLock.readLock().withLock {
            mSessions.forEach { it.mockMessage(message) }
        }
    }

}