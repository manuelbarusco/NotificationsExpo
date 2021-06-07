package com.android.notificationexpo.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.core.content.getSystemService
import com.android.notificationexpo.database.NotificationExpoRepository
import com.android.notificationexpo.database.entities.Messaggio
import java.util.concurrent.Executors
import kotlin.random.Random
import com.android.notificationexpo.ItemDetailFragment
import com.android.notificationexpo.ItemListActivity
import com.android.notificationexpo.R
import com.android.notificationexpo.ItemDetailActivity

//BroadcastReceiver che permette di gestire le quick action notification
class QuickActionNotificationReceiver: BroadcastReceiver() {

    private lateinit var repository: NotificationExpoRepository
    private var notificationManager: NotificationManager? = null
    private lateinit var user:String
    private lateinit var message: String
    private var chat_id: Long = -1
    private var notification_id: Int = -1
    private var chat_img: Int = -1
    private lateinit var chatName: String
    private var size : Int = 0
    private var i : Int = 0
    private var twopane: Boolean = true
    private lateinit var messaggi: ArrayList<String>

    override fun onReceive(context: Context, intent: Intent) {
        repository = NotificationExpoRepository.get(context)
        notificationManager = context.getSystemService()
        /*Prelevo dall'intent i dati che servono per aggiornare una notifica, in particolare ArrayList di messaggi che
        * a questo punto contiene già un messaggio al suo interno (quello inserito nel NoticationLauncher)*/
        messaggi = intent.getStringArrayListExtra(AlarmManagerReceiverAlwaysOn.ARRAY_MESSAGES_QA) as ArrayList<String>
        chat_id = intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1)
        notification_id =intent.getIntExtra(ItemDetailFragment.NOTIFICATION_ID, -1)
        chat_img = intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1)
        chatName = intent.getStringExtra(ItemDetailFragment.CHAT_NAME).toString()
        /*Prelevo anche la variabile twopane perché serve per creare l'intent (per tablet o smartphone) da specificare quando
        * ricostruisco la notifica*/
        twopane = intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,true)
        //Prelevo dal RemoteInput il messaggio scritto nella notifica e lo aggiungo nell'ArrayList dei messaggi
        message = getMessageText(intent) as String
        messaggi.add(message)
        size = messaggi.size
        //prelevo il nome dell'utente dalle SharedPreferences, serve per salvare correttamente nel DB i messaggi scritti dall'utente
        val preferences= context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user = preferences?.getString(ItemListActivity.KEY_USER,"") as String
        /*Solo quando l'ultimo messaggio nell'ArrayList è un messaggio scritto dall'utente nella notifica viene lanciato un
        *executor che aspetta 2 secondi per aggiornare la notifica con un nuovo messaggio di risposta generato altrimenti
        * aggiorno la UI solo con il messaggio scritto dall' utente nella notifica*/
        val executor = Executors.newSingleThreadExecutor()
        if(size%2==0){
            updateNotification(context,false)
            executor.execute {
                Thread.sleep(2000L)
                updateNotification(context,true)
            }
        }

    }

    //Funzione che preleva dal RemoteInput il messaggio scritto dall'utente
    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent).getCharSequence(AlarmManagerReceiverAlwaysOn.KEY_TEXT_REPLY)
    }
    /*Funzione che genera un nuovo messaggio (se answer è true), lo inserisce nel DB e aggiorna la notifica creandone un'altra.
    * Se answer è false aggiorna la notifica con il messaggio scritto dall'utente*/
    private fun updateNotification(context: Context, answer: Boolean){
        val person = Person.Builder()
                .setName(chatName)
                .setIcon(Icon.createWithResource(context,chat_img))
                .build()
        //oggetto Person che rappresenta l'utente dell'applicazione
        val me = Person.Builder()
                .setName("Io")
                .build()

        val messagingStyle: Notification.MessagingStyle = Notification.MessagingStyle(person)
        if(answer) {
            /*Genero un messaggio per la risposta e lo aggiungo nell ArrayList da passare ad una eventuale nuova istanza di
            * questa classe se dovesse esserci un'altra risposta da parte dell'utente*/
            val t = generateMessage(context)
            messaggi.add(t)
        }
        /*Inserisco i messaggi precedenti e quello appena generato o quello scritto dall'utente nella notifica nel messaging
        * style della notifica alternando il mittente*/
        for (m in messaggi){
            val messaggio: Notification.MessagingStyle.Message
            if(i%2==0){
                messaggio = Notification.MessagingStyle.Message(m, System.currentTimeMillis(), person)
                i++
            }
            else {
                messaggio = Notification.MessagingStyle.Message(m, System.currentTimeMillis(), me)
                i++
            }
            messagingStyle.addMessage(messaggio)

        }
        /*Solo nella prima chiamata di updateNotification aggiungo nel DB il messaggio che viene scritto nella notifica.
        * Il messaggio generato viene inserito nel DB nella seconda chiamata di updateNotification in questo modo gli inserimenti sono
        * per forza separati da circa 2 secondi ciò non provoca problemi di ordinamento nel DB*/
        if(!answer){
            val m = Messaggio(testo = message, chat = chat_id, media = null, mittente = user)
            repository.addMessage(m)
        }
        //Ricreo la notifica
        val replayText: String = context.getString(R.string.reply_text)
        val remoteInput: RemoteInput = RemoteInput.Builder(AlarmManagerReceiverAlwaysOn.KEY_TEXT_REPLY)
                .setLabel(replayText)
                .build()
        //Specifico ancora il pendingIntent verso QuickActionNotificationReceiver per gestire eventuali altri risposte da parte dell'utente
        val replyIntent = Intent(context, QuickActionNotificationReceiver::class.java)
                .putExtra(AlarmManagerReceiverAlwaysOn.ARRAY_MESSAGES_QA,messaggi) //per messaggi
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                .putExtra(ItemDetailFragment.CHAT_NAME, chatName)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.TWO_PANE,twopane)
        val replyPendingIntent = PendingIntent.getBroadcast(context,0,replyIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        //Inten per tocco su notifica differenziato se si è su smartphone o tablet
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chatName)
                    .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, "Notifica quick actions")

             pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chatName)
                    .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, "Notifica quick actions")
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val action: Notification.Action=
                Notification.Action.Builder(Icon.createWithResource(context,chat_img),context.getString(R.string.reply_button_text),replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()


        val notification = Notification.Builder(context, ItemListActivity.CUSTOM)
                .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: mettere immagine migliore
                .setStyle(messagingStyle)
                .addAction(action)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setColor(Color.GREEN)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .build()

        notificationManager?.notify(notification_id,notification)

    }
    //Funzione che preleva un messaggio dall'array di dummy messages e lo inserisce nel DB
    private  fun generateMessage(context: Context): String{
        val messages: Array<String> = context.resources.getStringArray(R.array.dummy_messages)

        val index = Random.nextInt(0, messages.size - 1)

        //genero il messaggio e lo aggiungo al database
        val mex= Messaggio(testo= messages[index], chat = chat_id, media = null, mittente = chatName)
        repository.addMessage(mex)

        return messages[index]
    }

}