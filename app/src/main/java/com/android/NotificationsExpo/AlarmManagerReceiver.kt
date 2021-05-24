package com.android.NotificationsExpo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log

// Questo BroadcastReceiver entrerà in funzione anche quando l'app non è in eseguzione in quanto viene
// registrato direttamente nel manifest
// Il compito di questo BroadcastReceiver è far partire un OrderedBroadcast

class AlarmManagerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("MyReceiver_BASE","${System.currentTimeMillis().toString()} ${intent.action}") //Mostriamo una riga con un testo sempre nuovo (altrimenti Logcat scrive che ci sono altre n righe simili)


        // Invio il broadcast per mostrare la notifica (a meno che l'app non sia in background)
        val myIntent = Intent(ACTION_SHOW_NOTIFICATION) // Qui se vuoi puo aggiungere dei putExtra
        myIntent.putExtra("notificationType",intent.getStringExtra("notificationType"))
        // Ritrasmetto (anche questa volta) il tipo di notifica associato alla chat
        context.sendOrderedBroadcast(myIntent,PERM_PRIVATE)
        // Creiamo un intent implicito e lo mandiamo come Ordered Broadcast

        // Il metodo orderedBroadcast funziona come sendBroadcast con l'ulteriore garanzia che i
        // broadcast saranno recapitati a un receiver alla volta

        // Al momento dell'invio di un oredered broadcast il resultCode verrà impostato automaticamente
        // a RESULT_OK. Se avremo un activity in foreground, il receiver relativo cambierà tale
        // resultCode per segnalare il fatto che le motifiche non vanno mostrate (in quanto non
        // vogliamo la visualizzazione delle notifiche ad app aperta)

        // L'ordine con cui saranno chiamati i receiver è il seguente:
        // 1) Verrà chiamato il recyver relativo all'activity, se l'activity è in foreground
        // 2) Verrà chiamatp AlarmManagerReciverAlwaysOn
        // L'ordine è garantito dal fatto che nel manifest il receiver AlarmmangerAlwaysOn ha il campo
        // priority impostato su -999 (questo è il valore di priorità più basso inseribile, i numeiri
        // al di sotto sono riservati)

        // Nota sulla sicurezza:
        // 1) Essendo broadcast impliciti devo sfruttare il parametro receiverPermission del metodo
        //    sendBroadcast. Usando questo parametro evitiamo che altre app non sviluppate da noi
        //    (quindi con un certificato sviluppatore diverso sal nostro) leggano i dati degli intent.
        //    Vedere nel manifest la dichiarazione del protectionLevel impostato su signature.
        // 2) Poichè AlarmManagerReceiverAlwaysOn è dichiarato nel manifest con exported = false,
        //    esso riceverà solo gli intent provenienti da questa app
        // https://developer.android.com/guide/components/broadcasts#security-and-best-practices
    }


    companion object {

        // Stringa che vien usata come action per i Broadcast
        const val ACTION_SHOW_NOTIFICATION = "com.android.NotificationsExpo.SHOW_NOTIFICATION"

        // Per l'autorizzazione
        const val PERM_PRIVATE = "com.android.NotificationsExpo.PRIVATE"
    }
}