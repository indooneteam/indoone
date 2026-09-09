package com.indoone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.indoone.settings.autolock.AutoLockController

class IndooneApplication : Application() {
    private val controllers = mutableMapOf<Activity, AutoLockController>()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is ComponentActivity) {
                    controllers[activity] = AutoLockController(activity.applicationContext)
                    installStatusBarInset(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                controllers[activity]?.onResumed(activity as? ComponentActivity ?: return)
                if (activity is ComponentActivity) installStatusBarInset(activity)
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

    private fun installStatusBarInset(activity: ComponentActivity) {
        val content = activity.findViewById<View>(android.R.id.content) ?: return
        val initialLeft = content.paddingLeft
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(initialLeft, topInset, initialRight, initialBottom)
            insets
        }
        ViewCompat.requestApplyInsets(content)
    }
}
