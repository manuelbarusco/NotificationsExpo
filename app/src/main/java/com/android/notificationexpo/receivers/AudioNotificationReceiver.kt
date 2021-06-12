package com.android.notificationexpo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.notificationexpo.PlayerService

// Lo scopo di questo Broadcast Receiver è ricevere gli intent provenienti dai pulsanti della
// notifica Media Control (pausa/play e stop)

class AudioNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (intent.action != null) {
            if (intent.action == RESUME_ACTION){
                // Indichiamo al service che riproduce la musica di andare in pausa/play (a seconda dello stato della riproduzione)
                var playerIntent = Intent(context, PlayerService::class.java) // intent esplicito
                playerIntent.putExtra(PlayerService.PLAY_PAUSE,true)
                context?.startService(playerIntent)
            }
            else if (intent.action == STOP_ACTION){
                // Indichiamo al service che riproduce la musica di terminare
                var playerIntent = Intent(context, PlayerService::class.java) // intent esplicito
                playerIntent.putExtra(PlayerService.PLAY_STOP,true)
                context?.startService(playerIntent)
            }
        }
    }

    companion object {
        const val REQUEST_CODE_NOTIFICATION = 1212
        const val RESUME_ACTION = "RESUME_ACTION"
        const val STOP_ACTION = "STOP_ACTION"
    }
}