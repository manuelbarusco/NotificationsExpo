package com.android.notificationexpo

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.android.notificationexpo.receivers.AlarmManagerReceiverAlwaysOn
import com.android.notificationexpo.receivers.CustomNotificationReceiver
import com.android.notificationexpo.receivers.QuickActionNotificationReceiver

//oggetto che si occupa di lanciare le notifiche contenenti i messaggi precedentemente generati dal MessageGenerator
class NotificationLauncher(
        private val user:String,
        private val chat_id: Long,
        private val chat_name: String,
        private val chat_img: Int,
        private val messagesToSend: MutableList<AlarmManagerReceiverAlwaysOn.MittenteMessaggio>,
        private val notificationType: String,
        private val twopane: Boolean,
        private val context: Context,
        private val preferences: SharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)

){
    //oggetto che permette di ottenere un ID sempre diverso per le notifiche
    companion object NotificationID{
        private var ID: Int=0

        fun getNextId():Int {
            ID++
            return ID-1
        }
        const val ACTION_BUBBLE = "com.android.NotificationsExpo.BUBBLE"
        const val ACTION_CONVERSATION = "com.android.NotificationsExpo.CONVERSATION"
        const val ACTION_SHORTCUT = "com.android.NotificationsExpo.SHORTCUT"
    }

    //metodo che aggiorna le chat che contengono messaggi non letti dopo che è stata inviata la notifica
    private fun updateNotReadChat() {

        //recupero la lista delle chat
        val chats: String? = preferences.getString(ItemListActivity.NOT_READ, "")
        val notReadChat = StringParser.parseString(chats as String)

        //aggiungo l'id della chat alla lista se non è già presente
        if (chat_id !in notReadChat)
            preferences.edit()
                .putString(ItemListActivity.NOT_READ, StringParser.addLong(chats, chat_id)).apply()
    }

    //tutti i metodi di seguito hanno come parametro il notification_id il quale specifica l'id da usare nel lancio della notifica

    //funzione che lancia una notifica per un processo in background
    fun launchBackgroundProcessNotification(notification_id: Int = getNextId()){

        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager: NotificationManager? = context.getSystemService()

        //lancio prima di tutto un messaggio per notificare l'arrivo di un messaggi
        val newMessageNotification = NotificationCompat.Builder(context, ItemListActivity.CONVERSATION)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(messagesToSend[0].mittente.nickname)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setContentIntent(pendingIntent)
                .build()

        if (notificationManager != null) {
            notificationManager.notify(getNextId(),newMessageNotification)
        }

        //costruisco la notifica
        val builder = NotificationCompat.Builder(context, ItemListActivity.SERVICE).apply {
            setContentTitle("Download di una foto")
            setContentText("Download in corso")
            setCategory(Notification.CATEGORY_MESSAGE)
            setAutoCancel(true)
            setSmallIcon(R.drawable.ic_stat_name)
        }

        //imposto i valori massimi e minimi della barra di avanzamento del download
        val progressMax = 100
        var progressCurrent = 0

        if(notificationManager!=null){
            // imposto la notifica iniziale con progress bar a 0
            builder.setProgress(progressMax, progressCurrent, false)
            notificationManager.notify(notification_id, builder.build())
            //simulo il download di un'immagine
            Thread(Runnable {
                while (progressCurrent < progressMax) {
                    try {
                        Thread.sleep(1000)
                        progressCurrent+=10
                        //aggiorno di volta in volta il progresso nel download nella notifica
                        builder.setProgress(progressMax, progressCurrent, false)
                        notificationManager.notify(notification_id, builder.build())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                //imposto il download completato nella notifica
                builder.setContentText("Download completato")
                        .setProgress(0, 0, false)
                builder.setProgress(progressMax, progressCurrent, false)
                notificationManager.notify(notification_id, builder.build())

                //genero un messaggio immagine per simulare il download correttamente avvenuto
                val messageGenerator= MessageGenerator(user, chat_id, messagesToSend, context)
                messageGenerator.generateImageMessage()

                //lancio una ImageNotification con il notification_id della notifica con progress bar in modo da sostituirla a downlaod avvenuto
                launchImageNotification(notification_id)
            }).start()
        }

    }

    //funzione che lancia una notifica espandibile contenente un'immagine
    fun launchImageNotification(notification_id: Int = getNextId()){
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager: NotificationManager? = context.getSystemService()

        val notification = NotificationCompat.Builder(context, ItemListActivity.IMG)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend.last().messaggio.media as Int))
                .setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(context.resources, messagesToSend.last().messaggio.media as Int))
                        .bigLargeIcon(null))
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

        updateNotReadChat()

        notificationManager?.notify(notification_id,notification)
    }

    //funzione che lancia una notifica espandibile contenente solo testo
    fun launchExpandableNotification(notification_id: Int = getNextId()){
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val notificationManager: NotificationManager? = context.getSystemService()

        val notification = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo.substring(0,20)+"...")
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setStyle(NotificationCompat.BigTextStyle().bigText(messagesToSend[0].messaggio.testo))
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

        updateNotReadChat()

        notificationManager?.notify(notification_id,notification)
    }

    //funzione che lancia più notifiche le quali vengono poi raggruppate
    fun launchMultipleNotifications(notification_id: Int = getNextId()){
        //uso un valore intero e una stringa per raggruppare le varie notifiche
        val summary_id= 0
        val group_notification = "com.android.notificationexpo.MULTIPLE_MESSAGES"

        val target: Intent
        val pendingIntent: PendingIntent

        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        //definisco le varie notifiche singole del gruppo di notifiche, definendo per ognuna il gruppo di appartenenza
        val newMessageNotification1 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(messagesToSend[0].mittente.nickname)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setGroup(group_notification)
                .setContentIntent(pendingIntent)
                .build()

        val newMessageNotification2 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(messagesToSend[1].mittente.nickname)
                .setContentText(messagesToSend[1].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[1].mittente.imgProfilo))
                .setContentIntent(pendingIntent)
                .setGroup(group_notification)
                .build()

        val newMessageNotification3 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(messagesToSend[2].mittente.nickname)
                .setContentText(messagesToSend[2].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[2].mittente.imgProfilo))
                .setContentIntent(pendingIntent)
                .setGroup(group_notification)
                .build()

        val newMessageNotification4 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(messagesToSend[3].mittente.nickname)
                .setContentText(messagesToSend[3].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[3].mittente.imgProfilo))
                .setContentIntent(pendingIntent)
                .setGroup(group_notification)
                .build()

        //definisco una notifica di riassunto che contiene le altre notifiche
        val summaryNotification = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setContentTitle(chat_name)
                //set content per mantenere la compatibilità con Android API level < 24
                .setContentText("4 Nuovi Messaggi")
                .setSmallIcon(R.drawable.ic_stat_name)
                //costruisco il riassunto
                .setStyle(NotificationCompat.InboxStyle()
                        .addLine(""+messagesToSend[0].mittente.nickname+" "+messagesToSend[0].messaggio.testo)
                        .addLine(""+messagesToSend[1].mittente.nickname+" "+messagesToSend[0].messaggio.testo)
                        .setBigContentTitle("Altri due messaggi"))
                //specifico il gruppo di appartenenza di questa notifica
                .setGroup(group_notification)
                //definisco quessta notifica come "padre" delle altre notifiche
                .setContentIntent(pendingIntent)
                .setGroupSummary(true)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

        updateNotReadChat()

        val notificationManager: NotificationManager? = context.getSystemService()

        notificationManager?.apply {
            //specifico un id diverso per ogni notifica
            notify(notification_id, newMessageNotification1)
            notify(getNextId(), newMessageNotification2)
            notify(getNextId(), newMessageNotification3)
            notify(getNextId(), newMessageNotification4)
            notify(summary_id, summaryNotification)
        }
    }

    //funzione che lancia una bubble notification
    fun launchBubbleNotification(notification_id: Int = chat_id.toInt()){
        val target: Intent
        //Intent per shortcut
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                .setAction(ACTION_SHORTCUT)
            target.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.TWO_PANE,twopane)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .setAction(ACTION_SHORTCUT)
            target.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        //Intet per espansione bubble
        val target2 = Intent(context, BubbleActivity::class.java)
            .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
            .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
            .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
            .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
            .setAction(ACTION_BUBBLE)
        /*target2.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }*/

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
                .setLocusId(LocusId(notification_id.toString()))
                .build()

        /*pubblico lo shortcut come dinamico tramite pushDynamicShortcut che in automatico gestisce il limite
        * imposto da android degli shortcuts che si possono pubblicare e permette aggiornare uno shortcut se ha lo stesso id*/
        val shortcutManager: ShortcutManager = context.getSystemService(AppCompatActivity.SHORTCUT_SERVICE) as ShortcutManager
        shortcutManager.pushDynamicShortcut(shortcut)

        /*setDesiredHeight(600) --> imposta la lunghezza della bubble espansa (in dp)
        * setAutoExpandBubble(true) --> bubble espansa in automatico (solo se l'app è in foreground) default = false
        * setSuppressNotification()--> imposta se la bubble verrà pubblicato senza la notifica associata nell'area apposita default = false.*/

        val bubbleData = Notification.BubbleMetadata.Builder(PendingIntent.getActivity(context,0,target2,PendingIntent.FLAG_UPDATE_CURRENT),Icon.createWithResource(context,chat_img))
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
                .setSmallIcon(R.drawable.ic_stat_name)
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setShortcutId(notification_id.toString())
                .setCategory(Notification.CATEGORY_MESSAGE)
                .build()


        val notificationManager: NotificationManager? = context.getSystemService()

        updateNotReadChat()

        notificationManager?.notify(notification_id,notification)
    }

    //funzione che lancia una custom notification
    fun launchCustomNotifications(notification_id: Int = getNextId()){
        val message = messagesToSend[0].messaggio.testo
        /*Crea una notifica custom con 2 layout 1 per la notifica stantard e l'altro per la notifica espansa
        Di ogni layout viene fatto l'inflate su oggetto RemoteViews che permette di creare la UI direttamente sulla notifica
        (che viene gestita dal sistema operativo in un altro processo)
        */
        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_collapsed)
        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_expanded)

        notificationLayout.setImageViewResource(R.id.notification_icon,chat_img)
        notificationLayout.setTextViewText(R.id.notification_title,"Nuovo messaggio da $chat_name")
        notificationLayoutExpanded.setImageViewResource(R.id.e_notification_icon,chat_img)
        notificationLayoutExpanded.setTextViewText(R.id.e_notification_title,message)

        /*Visto che queste View sono in altro processo non si può impostare un onClickListener come il solito.
        * Bisogna utilizzare un PendingIntent in modo tale da ridirezionare la gestione dei click sugli elementi della
        * UI della notifica custom nel processo main dell'app; esattamente vengono ridirezionati nel Broadcast receiver
        * CustomNotificationReceiver passando tutti i dati necessari per la gestione della notifica come Extra.*/
        //Per distinguere i vari eventi utilizzo l'action dell'intent
        val cb1Intent = Intent(context,CustomNotificationReceiver::class.java)
                .setAction("com.android.NotificationsExpo.CB1_CLICKED")
                .putExtra(ItemDetailFragment.CHAT_ID,chat_id)
                .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                .putExtra(AlarmManagerReceiverAlwaysOn.MESSAGE,message)
                .putExtra(ItemDetailFragment.CHAT_IMG,chat_img)
                .putExtra(ItemDetailFragment.CHAT_NAME,chat_name)
        //FLAG_UPDATE_CURRENT indica che se il PendingIntent descritto esiste già, lo conservalo ma sostituisce dati salvati come extra
        val cb1PendingIntent = PendingIntent.getBroadcast(context,0,cb1Intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cb2Intent = cb1Intent
                .setAction("com.android.NotificationsExpo.CB2_CLICKED")
        val cb2PendingIntent = PendingIntent.getBroadcast(context,0,cb2Intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cb3Intent = cb1Intent
                .setAction("com.android.NotificationsExpo.CB3_CLICKED")
        val cb3PendingIntent = PendingIntent.getBroadcast(context,0,cb3Intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cb4Intent = cb1Intent
                .setAction("com.android.NotificationsExpo.CB4_CLICKED")
        val cb4PendingIntent = PendingIntent.getBroadcast(context,0,cb4Intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val biIntent = cb1Intent
                .setAction("com.android.NotificationsExpo.BUTTON_INVIA_CLICKED")
        val biPendingIntent = PendingIntent.getBroadcast(context,0,biIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val baIntent = cb1Intent
                .setAction("com.android.NotificationsExpo.BUTTON_ANNULLA_CLICKED")
        val baPendingIntent = PendingIntent.getBroadcast(context,0,baIntent, PendingIntent.FLAG_UPDATE_CURRENT)

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

        //Intent per tocco su notifica differenziato se si è su smartphone o tablet
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
            /*target.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }*/
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val notification = NotificationCompat.Builder(context, ItemListActivity.CUSTOM)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle()) //aggiunge icona, nome app e tempo come quelle normali
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setAutoCancel(true)        //se tocco la notifica si cancella
                .setOnlyAlertOnce(true)

        val notificationManager: NotificationManager? = context.getSystemService()

        updateNotReadChat()

        notificationManager?.notify(notification_id,notification.build())

    }

    //funzione che lancia una conversation notification
    fun launchConversationNotifications(notification_id: Int = chat_id.toInt()) {
        val notificationManager: NotificationManager? = context.getSystemService()
        val shortcutManager: ShortcutManager = context.getSystemService(AppCompatActivity.SHORTCUT_SERVICE) as ShortcutManager
        val target: Intent
        val pendingIntent: PendingIntent
        /*Creo l'intent ed il PendingIntent per la notifica a seconda se sono su table o su smartphone*/
        if (!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .setAction(ACTION_CONVERSATION)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
                    .setAction(ACTION_CONVERSATION)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        /*Oggetto Person può essere riutilizzato su altre API per una migliore integrazione
        * setImortant per indicare gli utenti che interagiscono frequentemente con l'utente*/
        val person = Person.Builder()
                .setName(chat_name)
                .setImportant(true)
                .build()

        /*Creo un conversation shortcut.
        * Per lanciare una conversation notification e/o una chat bubble è necessario assiociare alla notifica un
        * conversation shortcut che è uno shortcut (https://developer.android.com/guide/topics/ui/shortcuts) con
        * particolari metadati e proprietà che servono per scatenare le conversation notification e/o le chat bubbles.
        * Ad uno shortcut "normale" bisogna aggiungere un Icon, un intent (che lancia una activity quando si usa lo
        * shortcut) e deve essere LongLived (così il S.O. lo memorizza nella cache per poterlo usare anche per altre
        * features delle conversazioni). Inoltre è consigliato (non obbligatorio) impostare anche un oggetto Person
        * che serve al S.O. per "conoscere" di più del contesto della conversazione ed utile ad es. per classificare
        * gli shortcut e per fornire suggerimenti per la condivisione.*/
        val shortcut = ShortcutInfo.Builder(context, notification_id.toString())
                .setLongLived(true)
                .setIcon(Icon.createWithResource(context, chat_img))
                .setShortLabel(chat_name as CharSequence)
                .setLongLabel(chat_name as CharSequence)
                .setIntent(target.apply {
                    action = ACTION_SHORTCUT
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                .setPerson(person)
                .setLocusId(LocusId(notification_id.toString()))
                .build()

        /*pubblico lo shortcut come dinamico tramite pushDynamicShortcut che in automatico gestisce il limite
        * imposto da android degli shortcuts che si possono pubblicare e permette aggiornare uno shortcut se ha lo stesso id*/

        shortcutManager.pushDynamicShortcut(shortcut)

        //Secondo parametro è di tipo Long e serve per la data del messaggio nella notifica
        val message1 = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
                System.currentTimeMillis(),
                person)

        /*Per fare in modo che una notifica sia una notifica conversation serve impostare anche un MessagingSyle e
        * aggiungere il messaggio al suo interno*/
        val notification = Notification.Builder(context, ItemListActivity.CONVERSATION)
                .addPerson(person)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setShortcutId(notification_id.toString())
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()

        updateNotReadChat()

        notificationManager?.notify(notification_id,notification)

    }

    //funzione che lancia una media control notification
    fun launchMediaControlNotification(notification_id: Int = getNextId()){
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val notificationManager: NotificationManager? = context.getSystemService()

        val person = Person.Builder()
                .setName(chat_name)
                .setImportant(true)
                .build()

        val message1 = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
                System.currentTimeMillis(),
                person)

        val notification = Notification.Builder(context, ItemListActivity.MEDIA)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, chat_img))
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .build()

        updateNotReadChat()

        notificationManager?.notify(notification_id,notification)
    }

    //funzione che lancia una notifica con azioni rapide su di essa
    fun launchQuickActionsNotification(notification_id: Int = getNextId()){
        /*Creo un ArrayList per salvare i messaggi che potrebbero essere scritti direttamente sulla notifica, questo serve per
        * poter aggiornare la notifica con i messaggi inviati e ricevuti mentre la notifica è ancora nel panello delle notifiche */
        val messaggi: ArrayList<String> = ArrayList()
        messaggi.add(messagesToSend[0].messaggio.testo)

        val replayText: String = context.getString(R.string.reply_text)

        /*Per poter scrivere nella notifica è necessario usare un oggetto di tipo RemoteInput e aggiungerlo nell'action, questo
        * aggiunge in automatico una editText e un pulsante di invio nella notifica se si tocca sul pulsante definito nell'action*/
        val remoteInput: RemoteInput = RemoteInput.Builder(AlarmManagerReceiverAlwaysOn.KEY_TEXT_REPLY)
                .setLabel(replayText)
                .build()

        //intent per tocco su notifica differenziato se sono su tablet o smartphone
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)

            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        /*Creo un pendingIntent per lanciare un broadcast esplicito a QuickActionNotificationReceiver per gestire
        * l'aggiornamento della notifica se dovesse esserci una risposta tramite l'action*/
        val replyIntent = Intent(context, QuickActionNotificationReceiver::class.java)
                .putExtra(AlarmManagerReceiverAlwaysOn.ARRAY_MESSAGES_QA,messaggi) //per messaggi
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.NOTIFICATION_ID,notification_id)
                .putExtra(ItemDetailFragment.TWO_PANE,twopane)  //per diversi intent per smartphone e tablet
        val replyPendingIntent = PendingIntent.getBroadcast(context,0,replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        /*un' Action permette di aggiungere un pulsante nella notifica. Va specificato un pendingIntent per specificare quale
        * componete dovrà gestirla*/
        val action: Notification.Action=
                Notification.Action.Builder(Icon.createWithResource(context,chat_img),context.getString(R.string.reply_button_text),replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()

        val person = Person.Builder()
                .setName(chat_name)
                .setIcon(Icon.createWithResource(context,chat_img))
                .build()
        //Creao messagingStyle e aggiungo il primo messaggio di risposta
        val messagingStyle: Notification.MessagingStyle = Notification.MessagingStyle(person)

        val message = Notification.MessagingStyle.Message(messagesToSend[0].messaggio.testo,
                System.currentTimeMillis(),
                person)

        val notification = Notification.Builder(context, ItemListActivity.CUSTOM)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setStyle(messagingStyle.addMessage(message))
                .addAction(action)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setColor(Color.GREEN)
                .setAutoCancel(true)
                .build()

        updateNotReadChat()

        val notificationManager: NotificationManager? = context.getSystemService()
        notificationManager?.notify(notification_id,notification)

    }
}