package com.fitpulse.app.core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface providing standard coroutine dispatchers to allow seamless swapping with
 * TestDispatchers during unit tests.
 */
interface CoroutineDispatchersProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

/**
 * Production implementation of [CoroutineDispatchersProvider] utilizing standard [Dispatchers].
 */
class DefaultCoroutineDispatchersProvider : CoroutineDispatchersProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}
