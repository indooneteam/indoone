package com.indoone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.indoone.settings.autolock.AutoLockController

class IndooneApplication : Application() {
    private val controllers = mutableMapOf<Activity, AutoLockController>()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is ComponentActivity) {
                    controllers[activity] = AutoLockController(activity.applicationContext)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                controllers[activity]?.onResumed(activity as? ComponentActivity ?: return)
            }

            override fun onActivityPaused(activity: Activity) {
                controllers[activity]?.onPaused()
            }

            override fun onActivityDestroyed(activity: Activity) {
                controllers.remove(activity)?.onDestroyed()
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }
}
