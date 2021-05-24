package com.android.NotificationsExpo

import android.app.Application
import com.android.NotificationsExpo.database.NotificationExpoRepository

class NotificationExpoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationExpoRepository.initialize(this)
    }
}