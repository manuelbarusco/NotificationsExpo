package com.android.NotificationsExpo

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.android.NotificationsExpo.database.NotificationExpoRepository
import com.android.NotificationsExpo.database.entities.Messaggio
import com.android.NotificationsExpo.database.entities.Utente
import java.util.concurrent.Executors
import kotlin.random.Random

// Questo BroadcastReceiver entrerà in funzione anche quando l'app non è in eseguzione in quanto viene
// registrato direttamente nel manifest

// Il compito di questo BroadcastReceiver è:
// - Inserire il messaggio del Database
// - Visualizzare la relativa notifica (a meno che prima non sia intervenuto il BoradcastReceiver
//   dell'app in foreground per chiedere che questa operazione non venga eseguita; questo comportamento
//   è possibile in quanto questo è un Ordered Broadcast e quindi, se è presente il BroadcastReceiver
//   dell'app in foreground, viene garantita l'eseguzione di quel BoradcastReceiver prima di questo)

class AlarmManagerReceiverAlwaysOn: BroadcastReceiver() {
    private lateinit var repository: NotificationExpoRepository
    private var executor = Executors.newSingleThreadExecutor()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyReceiver_ALWAYSON","${System.currentTimeMillis().toString()} ${intent?.action}") //Mostriamo una riga con un testo sempre nuovo (altrimenti Logcat scrive che ci sono altre n righe simili)
        repository= NotificationExpoRepository.get(context)

        //ottengo l'utente che sta usando l'app
        val preferences= context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val user= preferences.getString(ItemListActivity.KEY_USER, "")

        //ottengo id chat
        val chat_id= intent.getIntExtra(ItemDetailFragment.CHAT_ID,-1)

        //ottengo nome della chat
        val chat_name= intent.getStringExtra(ItemDetailFragment.CHAT_NAME)

        //ottengo immagine della chat
        val chat_img= intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1)

        //recupero i messaggi "standard" e ne sceglo uno a caso
        val messages: Array<String> = context.getResources().getStringArray(R.array.dummy_messages)
        val iM= Random.nextInt(0, messages.size-1)

        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user as String)

        Log.d("MyReceiver_ALWAYSON","sono qui")
        var mittente=""
        //sceglo a caso un utente (escluso utente predefinito) come mittente del messaggio in caso di chat di gruppo
        if(utenti.size>1){
            val i= Random.nextInt(0, utenti.size-1)
            mittente=utenti[i].nickname
        } else
            mittente=utenti[0].nickname

        //genero il messaggio e lo aggiungo al database
        val m= Messaggio(testo= messages[iM], chat = chat_id, media = null, mittente = mittente)
        repository.addMessage(m)

        // Devo generare uno o più messaggi sulla base del tipo di notifica associata alla chat (ad
        // esempio per una notifica conversation genererò 10 messaggi)
        val notificationType:String = intent.getStringExtra(ItemDetailFragment.NOTIFICATION) as String
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

        //TODO: selezionare tipo di notifica

        //******************* CHAT BUBBLE E CONVERSATION *************
        val target = Intent(context, ItemDetailActivity::class.java)
            .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
            .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
            .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
            .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
            .setAction("bubble")
        val bubbleIntent = PendingIntent.getActivity(context, 0, target, 0)

        /*Oggetto Person può essere riutilizzato su altre API per una migliore integrazione
        * setImortant per indicare gli utenti che interagiscono frequentemente con l'utente*/
        val person = Person.Builder()
                .setName(chat_name)
                .setImportant(true)
                .build()

        /*Creo un conversation shortcut.
        * Per lanciare una conversation notification e/o una chat bubble è necessario assiociare alla notifica un
        * conversation shortcut che è uno shortcut (https://developer.android.com/guide/topics/ui/shortcuts) con
        * particolari metadati e proprietà che servono per scatenare le conversation notification e le chat bubbles.
        * Ad uno shortcut "normale" bisogna aggiungere un Icon, un intent (che lancia una activity quando si usa lo
        * shortcut) e deve essere LongLived (così il S.O. lo memorizza nella cache per poterlo usare anche per altre
        * features delle conversazioni). Inoltre è consigliato (non obbligatorio) impostare anche un oggetto Person
        * che serve al S.O. per "conoscere" di più del contesto della conversazione ed utile ad es. per classificare
        * gli shortcut e per fornire suggerimenti per la condivisione.*/
        val shortcut = ShortcutInfo.Builder(context, chat_id.toString())
                .setLongLived(true)
                .setIcon(Icon.createWithResource(context,chat_img))
                .setShortLabel(chat_name as CharSequence)
                .setLongLabel(chat_name as CharSequence)
                .setIntent(target)
                .setPerson(person)
                .build()

        /*pubblico lo shortcut come dinamico tramite pushDynamicShortcut che in automatico gestisce il limite
        * imposto da android degli shortcuts che si possono pubblicare e permette aggiornare uno shortcut se ha lo stesso id*/
        val shortcutManager: ShortcutManager = context.getSystemService(AppCompatActivity.SHORTCUT_SERVICE) as ShortcutManager
        shortcutManager.pushDynamicShortcut(shortcut)

        //Invece di fare come sopra (commento) posso riutilizzare i dati dello shortcut passando il suo id
        /*setDesiredHeight(600) --> imposta la lunghezza della bubble espansa (in dp)
        * setAutoExpandBubble(true) --> bubble espansa in automatico (solo se l'app è in foreground) default = false
        * .setSuppressNotification()--> imposta se la bubble verrà pubblicato senza la notifica associata nell'area apposita default = false.*/

        val bubbleData = Notification.BubbleMetadata.Builder(chat_id.toString())
                .setDesiredHeight(600)
                .setAutoExpandBubble(false)
                .setSuppressNotification(false)
                .build()

        /*NOTA setSuppressBubble -->Indicates whether the bubble should be visually suppressed from the bubble stack if the user is viewing
         the same content outside of the bubble. For example, the user has a bubble with Alice and then opens up the main
         app and navigates to Alice's page. To match the activity and the bubble notification, the bubble notification
         should have a locus id set that matches a locus id set on the */

        //Secondo parametro è di tipo Long e serve per la data del messaggio nella notifica
        var message1 = Notification.MessagingStyle.Message(messages[iM],
                System.currentTimeMillis(),
                person)

        /*Per fare in modo che una notifica sia una notifica conversation serve impostare anche un MessagingSyle*/
        val notification = Notification.Builder(context, ItemListActivity.BUBBLES)
                .setContentTitle("Nuovo messaggio da ${person.name.toString()}")   //probabilmete non serve a niente
                .setBubbleMetadata(bubbleData)
                .addPerson(person)
                .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: mettere immagine migliore
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setShortcutId(chat_id.toString())
                .build()


        val notificationManager: NotificationManager? = context.getSystemService()

        if (notificationManager != null) {
            notificationManager.notify(chat_id,notification)
        }
    }

}