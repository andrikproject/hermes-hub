package com.hermeshub

import android.app.Application
import com.hermeshub.data.local.HermesDatabase

class HermesHubApp : Application() {
    val database by lazy { HermesDatabase.getDatabase(this) }
}
