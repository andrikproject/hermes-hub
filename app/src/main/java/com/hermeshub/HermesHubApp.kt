package com.hermeshub

import android.app.Application
import com.hermeshub.data.local.HermesDatabase
import com.hermeshub.util.NotificationHelper

class HermesHubApplication : Application() {
    val database by lazy { HermesDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
