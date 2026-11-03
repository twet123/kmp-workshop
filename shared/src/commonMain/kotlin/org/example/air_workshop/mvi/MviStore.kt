package org.example.air_workshop.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base MVI Store that connects immutable [State], sealed [Intent] actions,
 * and single-shot [Effect] side effects.
 *
 * Subclasses must provide:
 * - An initial [State] via [initialState].
 * - A [reduce] implementation that handles each [Intent] and produces the next [State].
 *
 * @param State The immutable UI state exposed to the View via [stateFlow].
 * @param Intent Sealed interface representing discrete user or lifecycle actions.
 * @param Effect Single-shot side effects (navigation, snackbars, file saving, etc.).
 */
abstract class MviStore<State : Any, Intent : Any, Effect : Any> {

    /**
     * Coroutine scope for all store-side async operations.
     * Uses [SupervisorJob] so that a failure in one child does not cancel others.
     */
    protected val coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    /** Exposes the current immutable [State] to the View. */
    val stateFlow: StateFlow<State>
        get() = _stateFlow

    private val _stateFlow: MutableStateFlow<State>

    private val _effects: Channel<Effect> = Channel(Channel.UNLIMITED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    init {
        _stateFlow = MutableStateFlow(initialState())
        handleStart()
    }

    /**
     * Called when the store is created. Override to perform initialization
     * logic such as loading initial data from repositories.
     */
    protected open fun handleStart() {}

    /**
     * Entry point for the View to dispatch an [Intent].
     * Routes the intent to [reduce] and collects the resulting state update.
     */
    fun onIntent(intent: Intent) {
        val nextState = reduce(intent)
        if (nextState != null) {
            _stateFlow.value = nextState
        }
    }

    /**
     * Reduces the current state and the given [intent] into a new state.
     * Return the new [State] or `null` to keep the current state unchanged.
     */
    protected abstract fun reduce(intent: Intent): State?

    /**
     * Returns the initial state when the store is first created.
     */
    protected abstract fun initialState(): State

    /**
     * Sends a single-shot [Effect] event downstream to the View.
     * Launches a coroutine on [coroutineScope] so it can be called from non-suspend contexts.
     */
    protected fun sendEffect(effect: Effect) {
        coroutineScope.launch {
            _effects.send(effect)
        }
    }

    /**
     * Updates the current state directly from async operations (e.g. repository callbacks).
     * Subclasses use this to push state changes that originate outside of [onIntent].
     */
    protected fun setState(reducer: State.() -> State) {
        _stateFlow.value = _stateFlow.value.reducer()
    }
}
