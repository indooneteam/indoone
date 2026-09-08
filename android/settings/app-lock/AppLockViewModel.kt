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
    )

    enum class Step { NONE, CREATE, CURRENT, NEW, CONFIRM, DISABLE }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun sync(hasPin: Boolean) {
        _state.value = _state.value.copy(hasPin = hasPin, error = "")
    }

    fun startCreate() {
        _state.value = State(step = Step.CREATE)
    }

    fun startChange() {
        _state.value = State(hasPin = true, step = Step.CURRENT)
    }

    fun startDisable() {
        _state.value = State(hasPin = true, step = Step.DISABLE)
    }

    fun appendDigit(digit: Char) {
        val current = _state.value.pin
        if (current.length < 12) _state.value = _state.value.copy(pin = current + digit, error = "")
    }

    fun backspace() {
        _state.value = _state.value.copy(pin = _state.value.pin.dropLast(1), error = "")
    }

    fun clear() {
        _state.value = _state.value.copy(pin = "", error = "")
    }

    fun setStep(step: Step) {
        _state.value = _state.value.copy(step = step, pin = "", error = "")
    }

    fun setNewPin(value: String) {
        _state.value = _state.value.copy(newPin = value)
    }

    fun error(message: String) {
        _state.value = _state.value.copy(error = message)
    }
}
