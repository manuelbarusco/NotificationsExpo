package com.android.NotificationsExpo

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// Questo BroadcastReceiver entrerà in funzione anche quando l'app non è in eseguzione in quanto viene
// registrato direttamente nel manifest

// Il compito di questo BroadcastReceiver è:
// - Inserire il messaggio del Database
// - Visualizzare la relativa notifica (a meno che prima non sia intervenuto il BoradcastReceiver
//   dell'app in foreground per chiedere che questa operazione non venga eseguita; questo comportamento
//   è possibile in quanto questo è un Ordered Broadcast e quindi, se è presente il BroadcastReceiver
//   dell'app in foreground, viene garantita l'eseguzione di quel BoradcastReceiver prima di questo)

class AlarmManagerReceiverAlwaysOn: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("MyReceiver_ALWAYSON","${System.currentTimeMillis().toString()} ${intent?.action}") //Mostriamo una riga con un testo sempre nuovo (altrimenti Logcat scrive che ci sono altre n righe simili)



        // TODO Generare un messaggio e scriverlo nel Database (l'UI verrà aggiornata tramite LiveData)
        // Devo generare uno o più messaggi sulla base del tipo di notifica associata alla chat (ad
        // esempio per una notifica conversation genererò 10 messaggi)
        val notificationType:String = intent.getStringExtra("notificationType") as String
        Log.d("MyR_Tipo di notifica: ", notificationType)




        // Se è intervenuto prima l'altro BroadcastReceiver non mostro nemmeno la notifica
        if (resultCode != Activity.RESULT_OK){
            // Vuol dire che un activity è in foreground. Non mostrare le neotifiche
            Log.d("MyR", "Eravamo in foreground")
            return
        }
        Log.d("MyR", "Eravamo in background")
         // ottengo eventuali valori dall'intent extra

        // TODO Mostro la notifica

    }
}