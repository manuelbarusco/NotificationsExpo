package com.android.notificationexpo.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.notificationexpo.database.entities.Messaggio
import com.android.notificationexpo.database.entities.Utente
import com.android.notificationexpo.ItemDetailFragment
import com.android.notificationexpo.ItemListActivity
import com.android.notificationexpo.MessageGenerator
import com.android.notificationexpo.NotificationLauncher

// Questo BroadcastReceiver entrerà in funzione anche quando l'app non è in eseguzione in quanto viene
// registrato direttamente nel manifest

// Il compito di questo BroadcastReceiver è:
// - Inserire il messaggio del Database
// - Visualizzare la relativa notifica (a meno che prima non sia intervenuto il BoradcastReceiver
//   dell'app in foreground per chiedere che questa operazione non venga eseguita; questo comportamento
//   è possibile in quanto questo è un Ordered Broadcast e quindi, se è presente il BroadcastReceiver
//   dell'app in foreground, viene garantita l'eseguzione di quel BoradcastReceiver prima di questo)

class AlarmManagerReceiverAlwaysOn: BroadcastReceiver() {
    private lateinit var user: String
    private var chat_id: Long=-1
    private lateinit var chat_name: String
    private var chat_img: Int=-1
    private lateinit var notificationType: String
    private var twopane: Boolean = false
    private val messagesToSend: MutableList<MittenteMessaggio> = mutableListOf() //lista che contiene i messaggi creati e da inviare

    //classe che incapsula il messaggio appena creato (in modo simulato) e mittente del messaggio
    data class MittenteMessaggio(val mittente: Utente, val messaggio: Messaggio)

    override fun onReceive(context: Context, intent: Intent) {


        //ottengo l'utente che sta usando l'app
        val preferences= context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user= preferences.getString(ItemListActivity.KEY_USER, "") as String

        //ottengo id chat
        chat_id= intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1)

        //ottengo nome della chat
        chat_name= intent.getStringExtra(ItemDetailFragment.CHAT_NAME) as String

        //ottengo immagine della chat
        chat_img= intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1)

        //ottengo variabile twopane
        twopane = intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,true)

        // Devo generare uno o più messaggi sulla base del tipo di notifica associata alla chat (ad
        // esempio per una notifica conversation genererò più messaggi)
        notificationType= intent.getStringExtra(ItemDetailFragment.NOTIFICATION) as String
        Log.d("MyR_Tipo di notifica: ", notificationType)

        //delego la creazione dei messaggi al MessageGenerator
        val messageGenerator = MessageGenerator(user, chat_id, messagesToSend, context)

        //scelgo quale messaggio debba essere creato in base al tipo di notifica
        when(notificationType){
            "Notifica immagine" -> messageGenerator.generateImageMessage()
            "Notifica espandibile" -> messageGenerator.generateSingleMessage(true)
            "Notifica chat bubble" -> messageGenerator.generateSingleMessage(false)
            "Notifiche multiple" -> messageGenerator.generateMultipleMessages()
            "Notifica custom template" -> messageGenerator.generateSingleMessage(false)
            "Notifica conversation"-> messageGenerator.generateSingleMessage(false)
            "Notifica media control" -> messageGenerator.generateMediaControlMessage()
            "Notifica processo in background" -> messageGenerator.generateBackgroundMessage()
            "Notifica quick actions" -> messageGenerator.generateSingleMessage(false)
        }

        // Se è intervenuto prima l'altro BroadcastReceiver non mostro nemmeno la notifica
        if (resultCode != Activity.RESULT_OK){
            // Vuol dire che un activity è in foreground. Non mostrare le notifiche
            return
        }

        //delego il lancio della notifica al NotificationLauncher
        val notificationLauncher = NotificationLauncher(user, chat_id, chat_name, chat_img, messagesToSend, notificationType, twopane, context)

        //scelgo quale notifica debba essere inviata
        when(notificationType){
            "Notifica espandibile" -> notificationLauncher.launchExpandableNotification()
            "Notifica immagine" -> notificationLauncher.launchImageNotification()
            "Notifica chat bubble" -> notificationLauncher.launchBubbleNotification()
            "Notifiche multiple" -> notificationLauncher.launchMultipleNotifications()
            "Notifica custom template" -> notificationLauncher.launchCustomNotifications()
            "Notifica conversation"-> notificationLauncher.launchConversationNotifications()
            "Notifica media control" -> notificationLauncher.launchMediaControlNotification()
            "Notifica processo in background" -> notificationLauncher.launchBackgroundProcessNotification()
            "Notifica quick actions" -> notificationLauncher.launchQuickActionsNotification()
        }
    }

    companion object{
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val ARRAY_MESSAGES_QA = "array_messages_quick_action"
        const val MESSAGE = "message"
        const val SELECTED_CB = "selected_cb"
        const val UPDATE_FRAGMENT = "update_fragment"
    }
}