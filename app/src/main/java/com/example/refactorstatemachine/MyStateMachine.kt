package org.mycmpapp

import android.util.Log
import kotlinx.coroutines.launch
import ru.nsk.kstatemachine.ChildMode
import ru.nsk.kstatemachine.IState
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.createStdLibStateMachine
import ru.nsk.kstatemachine.destroyBlocking
import ru.nsk.kstatemachine.finalState
import ru.nsk.kstatemachine.initialState
import ru.nsk.kstatemachine.onEntry
import ru.nsk.kstatemachine.onExit
import ru.nsk.kstatemachine.startBlocking
import ru.nsk.kstatemachine.state
import ru.nsk.kstatemachine.transition
import ru.nsk.kstatemachine.visitors.exportToMermaidBlocking
import ru.nsk.kstatemachine.visitors.exportToPlantUmlBlocking
import kotlin.concurrent.Volatile

class MyStateMachine {
  companion object {
    private val TAG = "MyStateMachine"
    @Volatile
    private var instance: MyStateMachine? = null
    private lateinit var machine: StateMachine
    fun getInstance(): MyStateMachine {
      if (instance == null) {
        instance = MyStateMachine()
        init()
      }
      return instance as MyStateMachine
    }

    fun init() {
      machine = createMachine()
      printStateMachine()
    }

    fun get(): StateMachine {
      return machine
    }

    fun start() {
      if (!machine.isRunning) {
          val start = System.currentTimeMillis()
          machine.startBlocking(null)
          val end = System.currentTimeMillis()
          Log.d(TAG, "state machine took ${end - start}ms to start" )
      } else {
        Log.d(TAG, "machine is already running" )
      }
    }

    fun destroy() {
      machine.destroyBlocking(true)
      instance = null
    }

    fun createMachine(): StateMachine {
      return createStdLibStateMachine("MyStateMachine", ChildMode.EXCLUSIVE, start = false ) {
        logger = StateMachine.Logger {
          Log.d(TAG, "*=> $it, activeStates = ${AppData.getActiveStates()}")
        }
        state("AppState"){
          initialState("Online"){
            initialState("GroupCall"){
              initialState("JoinState"){
                onEntry {
                  // send JOIN message
                  // check all group users status & send event for next state
                }
              }
              state("GroupCallHomeState", childMode = ChildMode.PARALLEL){
                state("SuspendState"){

                }
                state("ActiveState"){

                }
                state("RemoconState"){
                  onEntry {
                    // 1. timer started
                    // 2. selectionIndex = 0
                  }
                  state("GroupSelection"){

                  }
                  state("UserSelection"){

                  }
                  onExit {
                    // 1. timer canceled
                    // 2. selectionIndex = 0
                  }
                }
                state("IncomingCallState"){

                }
              }
              finalState("LeaveState") {
                onEntry {
                  // send LEAVE message
                }
              }
            }
            state("PersonalCall"){

            }
          }
          state("Offline"){

          }
          finalState("Exit"){

          }
        }
      }
    }

    private fun printStateMachine() {
      var plantUmlDiagramString = machine.exportToPlantUmlBlocking()
      plantUmlDiagramString = generatePlantUmlString(plantUmlDiagramString, machine?.states!!)
      println(plantUmlDiagramString)
      var other = machine.exportToMermaidBlocking()
      println(other)
    }

    // FIXME add cases for conditional transition & guards
    fun generatePlantUmlString(plantUmlStr: String, states: Set<IState>): String {
      var str = plantUmlStr
      for (state in states) {
        if (state.states.isNotEmpty()) {
          str = generatePlantUmlString(str, state.states)
        }
        val stateName = state::class.simpleName
        val hashCode = state.hashCode()
        str = str.replace("State$hashCode", stateName!!)
      }
      return str
    }
  }
}