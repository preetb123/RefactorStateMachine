// © 2024 Sony Corporation

package org.mycmpapp

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import ru.nsk.kstatemachine.StateMachine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MviModel<State, Effect>(val scope: CoroutineScope, initialState: State) {
  private val _stateFlow = MutableStateFlow(initialState)
  val stateFlow = _stateFlow.asStateFlow()

  private val _effectChannel = Channel<Effect>()
  val effectFlow = _effectChannel.receiveAsFlow()

  suspend fun sendEffect(effect: Effect) = _effectChannel.send(effect)

  fun state(block: State.() -> State) {
    _stateFlow.value = _stateFlow.value.block()
  }
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
val singleThread = newSingleThreadContext("mvi")

@OptIn(ExperimentalCoroutinesApi::class)
val machineScope = CoroutineScope(singleThread)

/**
 * Typically a ViewModel implements this interface
 */
interface MviModelHost<State, Effect> {
  val model: MviModel<State, Effect>

  fun <State, Effect> MviModelHost<State, Effect>.model(scope: CoroutineScope, initialState: State) =
    MviModel<State, Effect>(scope, initialState)

  /**
   * This block is used to change model state and emit effects
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun intent(context: CoroutineContext = EmptyCoroutineContext, block: suspend MviModel<State, Effect>.() -> Unit) {
    model.scope.launch(singleThread) { model.block() }
  }

  /**
   * Blocking version of [intent] to change model state and emit effects
   *
   * @param block
   */
  fun intentBlocking(block: MviModel<State, Effect>.() -> Unit) {
    model.block()
  }

  val state: State get() = model.stateFlow.value
}

object AppModel : MviModelHost<AppData, ModelEffect> {
  private const val TAG = "Mvi"
  override val model = model(machineScope, AppData())
  fun sendEvent(event: AppEvent): Unit = intentBlocking {
    machineScope.launch {
      sendEffect(ModelEffect.ControlEventSent(event))
      val machine: StateMachine = MyStateMachine.get()
      Log.d(TAG,"processing event : ${event::class.simpleName}")
      machine.processEvent(event)
    }
  }

  fun sendEvent(event: AppEvent, args: Any): Unit = intentBlocking {
    machineScope.launch {
      sendEffect(ModelEffect.ControlEventSent(event))
      val machine: StateMachine = MyStateMachine.get()
      machine.processEvent(event, args)
    }
  }
}


fun <State, Effect> MviModelHost<State, Effect>.observe(
  lifecycleOwner: LifecycleOwner,
  onState: ((State) -> Unit)?,
  onEffect: ((Effect) -> Unit)?,
  lifecycleState: Lifecycle.State = Lifecycle.State.STARTED
) {
  machineScope.launch {
    lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
      onState?.let { launch { model.stateFlow.collect { onState(it) } } }
      onEffect?.let { launch { model.effectFlow.collect { onEffect(it) } } }
    }
  }
}