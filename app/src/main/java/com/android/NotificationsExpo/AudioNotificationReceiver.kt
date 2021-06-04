package com.android.NotificationsExpo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast


class AudioNotificationReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (intent.action != null) {
            if (intent.action == RESUME_ACTION){
                var playerIntent = Intent(context, PlayerService::class.java) // intent esplicito
                playerIntent.putExtra(PlayerService.PLAY_PAUSE,true)
                context?.startService(playerIntent)
            }
            else if (intent.action == STOP_ACTION){
                var playerIntent = Intent(context, PlayerService::class.java) // intent esplicito
                playerIntent.putExtra(PlayerService.PLAY_STOP,true)
                context?.startService(playerIntent)
            }
        }
    }

    companion object {
        var REQUEST_CODE_NOTIFICATION = 1212
        var REQUEST_CODE = 10
        const val RESUME_ACTION = "RESUME_ACTION"
        const val STOP_ACTION = "STOP_ACTION"
        const val CANCEL_ACTION = "CANCEL_ACTION"
    }
}