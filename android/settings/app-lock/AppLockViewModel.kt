package com.indoone.settings.applock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLockViewModel : ViewModel() {
    data class State(
        val hasPin: Boolean = false,
        val step: Step = Step.NONE,
        val pin: String = "",
        val newPin: String = "",
        val error: String = "",
        val completed: Boolean = false,
    )

    enum class Step { NONE, CREATE, CURRENT, NEW, CONFIRM, UNLOCK }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun sync(hasPin: Boolean) {
        _state.value = _state.value.copy(hasPin = hasPin, error = "", completed = false)
    }

    fun startCreate() {
        _state.value = _state.value.copy(step = Step.CREATE, pin = "", newPin = "", error = "", completed = false)
    }

    fun startChange() {
        _state.value = _state.value.copy(step = Step.CURRENT, pin = "", newPin = "", error = "", completed = false)
    }

    fun startDisable() {
        _state.value = _state.value.copy(step = Step.CURRENT, pin = "", newPin = "", error = "", completed = false)
    }

    fun startUnlock() {
        _state.value = _state.value.copy(step = Step.UNLOCK, pin = "", newPin = "", error = "", completed = false)
    }

    fun appendDigit(digit: Char) {
        val current = _state.value.pin
        if (current.length >= 12) return
        _state.value = _state.value.copy(pin = current + digit, error = "")
    }

    fun backspace() {
        _state.value = _state.value.copy(pin = _state.value.pin.dropLast(1), error = "")
    }

    fun clear() {
        _state.value = _state.value.copy(pin = "", error = "")
    }

    fun setNewPin(value: String) {
        _state.value = _state.value.copy(newPin = value, error = "")
    }

    fun error(message: String) {
        _state.value = _state.value.copy(error = message)
    }

    fun resetInput() {
        _state.value = _state.value.copy(pin = "", error = "")
    }

    fun markCompleted() {
        _state.value = _state.value.copy(completed = true, error = "")
    }
}
