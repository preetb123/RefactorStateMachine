package org.mycmpapp

import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.FinalState

data class AppData(val activeStates: List<AppState> = listOf()) {
  companion object {
    fun getActiveStates(): List<AppState> {
      return AppModel.state.activeStates
    }
  }
}

sealed interface AppEvent : Event {
  data object ExitAppEvent : AppEvent
}

sealed class AppState : DefaultState() {
  object Online : AppState()
  object Offline : AppState()
  object GroupCall : AppState()
  object PersonalCall : AppState()
  object ExitApp : AppState(), FinalState
}

sealed interface ModelEffect {
  class StateEntered(val state: AppState) : ModelEffect
  class ControlEventSent(val event: AppEvent) : ModelEffect
}


fun AppEvent.send() = AppModel.sendEvent(this)
fun AppEvent.send(args: Any) = AppModel.sendEvent(this, args)

inline fun <reified S : AppState> List<AppState>.hasState() = filterIsInstance<S>().isNotEmpty()