package com.android.notificationexpo

import android.app.Application
import com.android.notificationexpo.database.NotificationExpoRepository

//TODO: ELIMINARE
class NotificationExpoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationExpoRepository.initialize(this)
    }
}