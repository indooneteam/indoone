package com.indoone.notifications

import java.util.concurrent.atomic.AtomicInteger

object IndooneAppState {
    private val startedActivities = AtomicInteger(0)

    val isForeground: Boolean
        get() = startedActivities.get() > 0

    fun onActivityStarted() {
        startedActivities.incrementAndGet()
    }

    fun onActivityStopped() {
        startedActivities.updateAndGet { count -> (count - 1).coerceAtLeast(0) }
    }
}
