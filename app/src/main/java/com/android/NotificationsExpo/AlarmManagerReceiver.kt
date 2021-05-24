package com.android.NotificationsExpo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

//Va dichiarato nel manifest per ricevere l'intent
// con <receiver android:name=".MyReceiver"/>
class AlarmManagerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //Lancio la notifica e faccio altro
        val action = intent?.action
        val text = intent?.getStringExtra("Test")
        Log.d("MyReceiver","$text")
    }
    //service
}