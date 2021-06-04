package com.android.NotificationsExpo

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import com.android.NotificationsExpo.database.NotificationExpoRepository
import com.android.NotificationsExpo.database.entities.Messaggio
import com.android.NotificationsExpo.database.entities.Utente
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
    private lateinit var user: String
    private var chat_id: Int=-1
    private lateinit var chat_name: String
    private var chat_img: Int=-1
    private lateinit var notificationType: String
    private val messagesToSend: MutableList<MittenteMessaggio> = mutableListOf()

    private data class MittenteMessaggio(val mittente: Utente, val messaggio: Messaggio)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyReceiver_ALWAYSON","${System.currentTimeMillis().toString()} ${intent?.action}") //Mostriamo una riga con un testo sempre nuovo (altrimenti Logcat scrive che ci sono altre n righe simili)
        repository= NotificationExpoRepository.get(context)

        //ottengo l'utente che sta usando l'app
        val preferences= context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user= preferences.getString(ItemListActivity.KEY_USER, "") as String

        //ottengo id chat
        chat_id= intent.getIntExtra(ItemDetailFragment.CHAT_ID,-1)

        //ottengo nome della chat
        chat_name= intent.getStringExtra(ItemDetailFragment.CHAT_NAME) as String

        //ottengo immagine della chat
        chat_img= intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1)

        // Devo generare uno o più messaggi sulla base del tipo di notifica associata alla chat (ad
        // esempio per una notifica conversation genererò 10 messaggi)
        notificationType= intent.getStringExtra(ItemDetailFragment.NOTIFICATION) as String
        Log.d("MyR_Tipo di notifica: ", notificationType)

        when(notificationType){
            "Notifica immagine" -> generateImageMessage()
            "Notifica espandibile" -> generateSingleMessage(context, true)
            "Notifica chat bubble" -> generateSingleMessage(context, false)
            "Notifiche multiple" -> generateMultipleMessages(context)
            "Notifica custom template" -> generateSingleMessage(context,false)
            "Notifica conversation"-> generateSingleMessage(context,false)
            "Notifica media control" -> generateMediaControlMessage(context)
            "Notifica processo in background" -> generateBackgroundMessage()
            "Notifica quick actions" -> generateSingleMessage(context, false)
        }

        // Se è intervenuto prima l'altro BroadcastReceiver non mostro nemmeno la notifica
        if (resultCode != Activity.RESULT_OK){
            // Vuol dire che un activity è in foreground. Non mostrare le neotifiche
            Log.d("MyR", "Eravamo in foreground")
            return
        }
        Log.d("MyR", "Eravamo in background")

        when(notificationType){
            "Notifica espandibile" -> launchExpandableNotification(context)
            "Notifica immagine" -> launchImageNotification(context)
            "Notifica chat bubble" -> launchBubbleNotification(context)
            "Notifiche multiple" -> launchMultipleNotifications(context)
            "Notifica custom template" -> launchCustomNotifications(context)
            "Notifica conversation"-> launchConversationNotifications(context)
            "Notifica media control" -> launchMediaControlNotification(context)
            "Notifica processo in background" -> launchBackgroundProcessNotification(context)
            "Notifica quick actions" -> launchQuickActionsNotification(context)
        }
    }
    
    private fun generateImageMessage(){
        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user as String)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= "Ecco la programmazione dei film di oggi", chat = chat_id, media = R.drawable.programmazione , mittente = utenti[0].nickname)
        repository.addMessage(mex1)

        val mittenteMessaggio= MittenteMessaggio(utenti[0], mex1)
        messagesToSend.add(mittenteMessaggio)
    }

    private fun generateMultipleMessages(context: Context){
        //recupero i messaggi "standard" e ne scelgo 4 a caso
        val messages: Array<String> = context.getResources().getStringArray(R.array.dummy_messages)
        val m1= Random.nextInt(0, messages.size)
        val m2= Random.nextInt(0, messages.size)
        val m3= Random.nextInt(0, messages.size)
        val m4= Random.nextInt(0, messages.size)

        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user as String)

        //scelgo a caso 4 mittenti per i 4 messaggio
        val u1= Random.nextInt(0, messages.size)
        val u2= Random.nextInt(0, messages.size)
        val u3= Random.nextInt(0, messages.size)
        val u4= Random.nextInt(0, messages.size)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= messages[m1], chat = chat_id, media = null, mittente = utenti[u1].nickname)
        val mex2= Messaggio(testo= messages[m2], chat = chat_id, media = null, mittente = utenti[u2].nickname)
        val mex3= Messaggio(testo= messages[m3], chat = chat_id, media = null, mittente = utenti[u3].nickname)
        val mex4= Messaggio(testo= messages[m4], chat = chat_id, media = null, mittente = utenti[u4].nickname)
        repository.addMessage(mex1)
        repository.addMessage(mex2)
        repository.addMessage(mex3)
        repository.addMessage(mex4)

        var mittenteMessaggio= MittenteMessaggio(utenti[u1], mex1)
        messagesToSend.add(mittenteMessaggio)
        mittenteMessaggio= MittenteMessaggio(utenti[u2], mex2)
        messagesToSend.add(mittenteMessaggio)
        mittenteMessaggio= MittenteMessaggio(utenti[u3], mex3)
        messagesToSend.add(mittenteMessaggio)
        mittenteMessaggio= MittenteMessaggio(utenti[u4], mex4)
        messagesToSend.add(mittenteMessaggio)
    }

    private fun generateSingleMessage(context: Context, long: Boolean){
        //recupero i messaggi "standard" e ne sceglo uno a caso se il messaggio è corto, altrimenti quello lungo se il messaggio comparirà in una notifica espandibile
        val messages: Array<String> = context.getResources().getStringArray(R.array.dummy_messages)
        var iM=3 //TODO scegliere il messaggio lungo
        if(!long)
            iM = Random.nextInt(0, messages.size - 1)

        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user as String)

        //genero il messaggio e lo aggiungo al database
        val mex= Messaggio(testo= messages[iM], chat = chat_id, media = null, mittente = utenti[0].nickname)
        repository.addMessage(mex)

        val mittenteMessaggio= MittenteMessaggio(utenti[0], mex)
        messagesToSend.add(mittenteMessaggio)
    }

    private fun generateMediaControlMessage(context: Context){
        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user as String)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= "Audio", chat = chat_id, media = R.raw.doowackadoo , mittente = utenti[0].nickname)
        repository.addMessage(mex1)

        val mittenteMessaggio= MittenteMessaggio(utenti[0], mex1) //TODO Perchè?
        messagesToSend.add(mittenteMessaggio)
    }

    private fun generateBackgroundMessage(){
        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user as String)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= "Ti invio una foto della giornata di oggi", chat = chat_id, media = null , mittente = utenti[0].nickname)
        repository.addMessage(mex1)

        val mittenteMessaggio= MittenteMessaggio(utenti[0], mex1)
        messagesToSend.add(mittenteMessaggio)
    }

    private fun launchBackgroundProcessNotification(context: Context, notification_id: Int = chat_id){

        launchConversationNotifications(context)

        val notificationManager: NotificationManager? = context.getSystemService()

        val builder = NotificationCompat.Builder(context, ItemListActivity.SERVICE).apply {
            setContentTitle("Download di una foto")
            setContentText("Download in corso")
            setSmallIcon(chat_img)
        }
        val PROGRESS_MAX = 100
        var PROGRESS_CURRENT = 0

        if(notificationManager!=null){
            // imposto la notifica iniziale con progress bar a 0
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            notificationManager.notify(notification_id+1, builder.build())
            //simulo il download di un'immagine
            Thread(Runnable {
                while (PROGRESS_CURRENT < PROGRESS_MAX) {
                    try {
                        Thread.sleep(1000)
                        PROGRESS_CURRENT+=25
                        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
                        notificationManager.notify(notification_id+1, builder.build())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                builder.setContentText("Download completato")
                        .setProgress(0, 0, false)
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                notificationManager.notify(notification_id+1, builder.build());
                generateImageMessage()
                launchImageNotification(context, notification_id+1)
            }).start()
        }

    }

    private fun launchImageNotification(context: Context, notification_id: Int = chat_id){
        val target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

        target.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationManager: NotificationManager? = context.getSystemService()

        val notification = NotificationCompat.Builder(context, ItemListActivity.IMG)
                .setSmallIcon(messagesToSend[0].mittente.imgProfilo)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend.last().messaggio.media as Int))
                .setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(context.resources, messagesToSend.last().messaggio.media as Int))
                        .bigLargeIcon(null))
                .setContentIntent(pendingIntent)
                .build()

        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }
    }

    private fun launchExpandableNotification(context: Context, notification_id: Int = chat_id){
        val target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

        target.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationManager: NotificationManager? = context.getSystemService()

        var notification = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo.substring(0,10))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(messagesToSend[0].messaggio.testo))
                .setContentIntent(pendingIntent)
                .build()

        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }
    }

    private fun launchMultipleNotifications(context: Context, notification_id: Int = chat_id){
        //use constant ID for notification used as group summary
        val SUMMARY_ID = 0
        val GROUP_KEY_WORK_EMAIL = "com.android.NotificationExpo.MULTIPLE_MESSAGES"

        //TODO aggiungere intent ad ogni notifica singola

        val newMessageNotification1 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[0].mittente.nickname)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .build()

        val newMessageNotification2 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[1].mittente.nickname)
                .setContentText(messagesToSend[1].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[1].mittente.imgProfilo))
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .build()

        val newMessageNotification3 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[2].mittente.nickname)
                .setContentText(messagesToSend[2].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[2].mittente.imgProfilo))
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .build()

        val newMessageNotification4 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[3].mittente.nickname)
                .setContentText(messagesToSend[3].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[3].mittente.imgProfilo))
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .build()

        val target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

        target.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)

        val summaryNotification = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setContentTitle(chat_name)
                //set content text to support devices running API level < 24
                .setContentText("4 Nuovi Messaggi")
                .setSmallIcon(chat_img)
                //build summary info into InboxStyle template
                .setStyle(NotificationCompat.InboxStyle()
                        .addLine(""+messagesToSend[0].mittente.nickname+" "+messagesToSend[0].messaggio.testo)
                        .addLine(""+messagesToSend[1].mittente.nickname+" "+messagesToSend[0].messaggio.testo)
                        .setBigContentTitle("Altri due messaggi"))
                //specify which group this notification belongs to
                .setGroup(GROUP_KEY_WORK_EMAIL)
                //set this notification as the summary for the group
                .setContentIntent(pendingIntent)
                .setGroupSummary(true)
                .build()

        val notificationManager: NotificationManager? = context.getSystemService()

        if (notificationManager != null) {
            notificationManager.apply {
                notify(notification_id, newMessageNotification1)
                notify(notification_id+1, newMessageNotification2)
                notify(notification_id+2, newMessageNotification3)
                notify(notification_id+3, newMessageNotification4)
                notify(SUMMARY_ID, summaryNotification)
            }
        }
    }

    private fun launchBubbleNotification(context: Context, notification_id: Int = chat_id){

        val target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                .setAction("com.android.NotificationsExpo.BUBBLE")

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
        val shortcut = ShortcutInfo.Builder(context, notification_id.toString())
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

        val bubbleData = Notification.BubbleMetadata.Builder(notification_id.toString())
                .setDesiredHeight(600)
                .setAutoExpandBubble(false)
                .setSuppressNotification(false)
                .build()

        /*NOTA setSuppressBubble -->Indicates whether the bubble should be visually suppressed from the bubble stack if the user is viewing
         the same content outside of the bubble. For example, the user has a bubble with Alice and then opens up the main
         app and navigates to Alice's page. To match the activity and the bubble notification, the bubble notification
         should have a locus id set that matches a locus id set on the */

        //Secondo parametro è di tipo Long e serve per la data del messaggio nella notifica
        val message1 = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
                System.currentTimeMillis(),
                person)

        /*Per fare in modo che una notifica sia una notifica conversation serve impostare anche un MessagingSyle*/
        val notification = Notification.Builder(context, ItemListActivity.BUBBLES)
                .setBubbleMetadata(bubbleData)
                .addPerson(person)
                .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: mettere immagine migliore
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setShortcutId(notification_id.toString())
                .build()


        val notificationManager: NotificationManager? = context.getSystemService()

        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }
    }

    private fun launchCustomNotifications(context: Context, notification_id: Int = chat_id){
        //Crea una notifica custom con 2 layout 1 per la notifica stantard e l'altro per la notifica espansa
        val message = messagesToSend[0].messaggio.testo
        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_collapsed)
        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_expanded)

        notificationLayout.setImageViewResource(R.id.notification_icon,chat_img)
        notificationLayout.setTextViewText(R.id.notification_title,"Nuovo messaggio da $chat_name")
        notificationLayoutExpanded.setImageViewResource(R.id.e_notification_icon,chat_img)
        notificationLayoutExpanded.setTextViewText(R.id.e_notification_title,message)

        /*Visto che queste View sono in altro processo non si può impostare un onClickListener come il solito.
        * Bisogna utilizzare un PendingIntent in modo tale da ridirezionare la gestione dei click sugli elementi della
        * UI della notifica custom nell processo main dell'app.*/
        val cb1Intent = Intent(context,CustomNotificationReceiver::class.java)
                .setAction("com.android.NotificationsExpo.CB1_CLICKED")
                .putExtra(ItemDetailFragment.CHAT_ID,chat_id)
                .putExtra(MESSAGE,message)
                .putExtra(ItemDetailFragment.CHAT_IMG,chat_img)
                .putExtra(ItemDetailFragment.CHAT_NAME,chat_name)
        //FLAG_UPDATE_CURRENT indica che se il PendingIntent descritto esiste già, lo conservalo ma sostituisce dati salvati come extra
        val cb1PendingIntent = PendingIntent.getBroadcast(context,0,cb1Intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val cb2Intent = cb1Intent
                .setAction("com.android.NotificationsExpo.CB2_CLICKED")
        val cb2PendingIntent = PendingIntent.getBroadcast(context,0,cb2Intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val cb3Intent = cb1Intent
                .setAction("com.android.NotificationsExpo.CB3_CLICKED")
        val cb3PendingIntent = PendingIntent.getBroadcast(context,0,cb3Intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val cb4Intent = cb1Intent
                .setAction("com.android.NotificationsExpo.CB4_CLICKED")
        val cb4PendingIntent = PendingIntent.getBroadcast(context,0,cb4Intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val biIntent = cb1Intent
                .setAction("com.android.NotificationsExpo.BUTTON_INVIA_CLICKED")
        val biPendingIntent = PendingIntent.getBroadcast(context,0,biIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val baIntent = cb1Intent
                .setAction("com.android.NotificationsExpo.BUTTON_ANNULLA_CLICKED")
        val baPendingIntent = PendingIntent.getBroadcast(context,0,baIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        //Imposto il PendingIntent ai widgets della view interessati
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.cb_1,cb1PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.text_cb_1,cb1PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.cb_2,cb2PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.text_cb_2,cb2PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.cb_3,cb3PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.text_cb_3,cb3PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.cb_4,cb4PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.text_cb_4,cb4PendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.b_invia,biPendingIntent)
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.b_annulla,baPendingIntent)

        //Inten per tocco su notifica non espansa
        val intent = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
        val pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(context, ItemListActivity.CUSTOM)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  //TODO: mettere icona migliore
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle()) //aggiunge icona, nome app e tempo come quelle normali
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)        //se tocco la notifica si cancella
                .setOnlyAlertOnce(true)

        val notificationManager: NotificationManager? = context.getSystemService()

        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification.build())

        }

    }

    private fun launchConversationNotifications(context: Context, notification_id: Int = chat_id){
        val target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                .setAction("com.android.NotificationsExpo.CONVERSATION")
        val pendingIntent = PendingIntent.getActivity(context,0,target,PendingIntent.FLAG_CANCEL_CURRENT)

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
        val shortcut = ShortcutInfo.Builder(context, notification_id.toString())
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

        //Secondo parametro è di tipo Long e serve per la data del messaggio nella notifica
        val message1 = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
                System.currentTimeMillis(),
                person)

        /*Per fare in modo che una notifica sia una notifica conversation serve impostare anche un MessagingSyle*/
        val notification = Notification.Builder(context, ItemListActivity.CONVERSATION)
                .addPerson(person)
                .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: mettere immagine migliore
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setShortcutId(notification_id.toString())
                .setContentIntent(pendingIntent)
                .build()

        val notificationManager: NotificationManager? = context.getSystemService()

        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }
    }

    private fun launchMediaControlNotification(context: Context, notification_id: Int = chat_id){
        val target = Intent(context, ItemDetailActivity::class.java)
            .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
            .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
            .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
            .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

        target.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationManager: NotificationManager? = context.getSystemService()

        val person = Person.Builder()
            .setName(chat_name)
            .setImportant(true)
            .build()

        val message1 = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
            System.currentTimeMillis(),
            person)

        val notification = Notification.Builder(context, ItemListActivity.MEDIA)
            .setSmallIcon(messagesToSend[0].mittente.imgProfilo)
            .setContentTitle(chat_name)
            .setContentText(messagesToSend[0].messaggio.testo)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, chat_img as Int))
            .setStyle(Notification.MessagingStyle(person)
                .addMessage(message1)
            )
            .setContentIntent(pendingIntent)
            .build()


        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }
    }

    private fun launchQuickActionsNotification(context: Context, notification_id: Int = chat_id){
        val messaggi: ArrayList<String> = ArrayList()
        messaggi.add(messagesToSend[0].messaggio.testo)

        val replayText: String = context.getString(R.string.reply_text)
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replayText)
                .build()

        val replyIntent = Intent(context, QuickActionNotificationReceiver::class.java)
                .putExtra(ARRAY_MESSAGES_QA,messaggi) //per messaggi
                .putExtra(ItemDetailFragment.CHAT_ID, notification_id)
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
        val replyPendingIntent = PendingIntent.getBroadcast(context,0,replyIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        //intent per tocco su notifica
        val target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, notification_id)
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

        val contentIntent = PendingIntent.getActivity(context,0,target,PendingIntent.FLAG_CANCEL_CURRENT)

        val action: Notification.Action=
                Notification.Action.Builder(Icon.createWithResource(context,chat_img),context.getString(R.string.reply_button_text),replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()

        val person = Person.Builder()
                .setName(chat_name)
                .setIcon(Icon.createWithResource(context,chat_img))
                //.setImportant(true)
                .build()

        val messagingStyle: Notification.MessagingStyle = Notification.MessagingStyle(person)

        val message = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
                System.currentTimeMillis(),
                person)

        val notification = Notification.Builder(context, ItemListActivity.CUSTOM)
                .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: mettere immagine migliore
                .setStyle(messagingStyle.addMessage(message))
                .addAction(action)
                .setContentIntent(contentIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setColor(Color.GREEN)
                .setAutoCancel(true)
                .build()

        val notificationManager: NotificationManager? = context.getSystemService()
        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }

    }

    companion object{
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val ARRAY_MESSAGES_QA = "array_messages_quick_action"
        const val MESSAGE = "message"
        const val SELECTED_CB = "selected_cb"
    }
}