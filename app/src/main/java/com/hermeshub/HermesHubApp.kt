package com.hermeshub

import android.app.Application
import com.hermeshub.data.local.HermesDatabase

class HermesHubApplication : Application() {
    val database by lazy { HermesDatabase.getDatabase(this) }
}
