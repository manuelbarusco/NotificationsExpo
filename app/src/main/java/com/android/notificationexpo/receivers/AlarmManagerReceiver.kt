package com.android.notificationexpo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.notificationexpo.ItemDetailFragment

// Questo BroadcastReceiver entrerà in funzione anche quando l'app non è in eseguzione in quanto viene
// registrato direttamente nel manifest
// Il compito di questo BroadcastReceiver è far partire un OrderedBroadcast

class AlarmManagerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("MyReceiver_BASE","${System.currentTimeMillis().toString()} ${intent.action}") //Mostriamo una riga con un testo sempre nuovo (altrimenti Logcat scrive che ci sono altre n righe simili)

        Log.d("MyReceiver_BASE", ""+intent.getStringExtra(ItemDetailFragment.NOTIFICATION))
        Log.d("MyReceiver_BASE", ""+intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1))
        Log.d("MyReceiver_BASE", ""+intent.getStringExtra(ItemDetailFragment.CHAT_NAME))

        // Invio il broadcast per mostrare la notifica (a meno che l'app non sia in background)
        val myIntent = Intent(ACTION_SHOW_NOTIFICATION) // Qui se vuoi puo aggiungere dei putExtra
        myIntent.putExtra(ItemDetailFragment.NOTIFICATION,intent.getStringExtra(ItemDetailFragment.NOTIFICATION))
        myIntent.putExtra(ItemDetailFragment.CHAT_ID,intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1))
        myIntent.putExtra(ItemDetailFragment.CHAT_NAME,intent.getStringExtra(ItemDetailFragment.CHAT_NAME))
        myIntent.putExtra(ItemDetailFragment.CHAT_IMG,intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1))
        myIntent.putExtra(ItemDetailFragment.TWO_PANE,intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,false))

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
        const val ACTION_SHOW_NOTIFICATION = "com.android.notificationexpo.SHOW_NOTIFICATION"

        // Per l'autorizzazione
        const val PERM_PRIVATE = "com.android.notificationexpo.PRIVATE"
    }
}