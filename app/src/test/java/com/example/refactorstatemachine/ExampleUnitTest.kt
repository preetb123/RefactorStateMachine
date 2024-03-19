package com.example.refactorstatemachine

import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mycmpapp.MyStateMachine


import ru.nsk.kstatemachine.IState
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.activeStates
import ru.nsk.kstatemachine.stopBlocking

internal fun StateMachine.containsActiveState(state: IState): Boolean {
    return activeStates(false).any { it::class.simpleName == state::class.simpleName }
}

internal fun StateMachine.containsActiveStates(states: List<IState>): Boolean {
    val activeStates = activeStates(false).map { it::class.simpleName }
    val actualStates = states.map { it::class.simpleName }

    return activeStates.containsAll(actualStates)
}

class MyStateMachineTest {

    private lateinit var stateMachine: StateMachine

    @Before
    fun setupStateMachine(){
        MyStateMachine.getInstance()
        stateMachine = MyStateMachine.get()
        MyStateMachine.start()
    }

    @After
    fun cleanUp() {
        MyStateMachine.get().stopBlocking()
        MyStateMachine.destroy()
    }

    @Test
    fun `check if state machine is active and running after starting`() {
        assertTrue(stateMachine.isActive && stateMachine.isRunning)
    }

    @Test
    fun `new test`() {
        assertTrue(stateMachine.activeStates().isNotEmpty())
    }
}