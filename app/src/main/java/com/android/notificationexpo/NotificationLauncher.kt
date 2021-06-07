package com.android.notificationexpo

import android.app.*
import android.content.Context
import android.content.Intent
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

class NotificationLauncher(
        private val user:String,
        private val chat_id: Long,
        private val chat_name: String,
        private val chat_img: Int,
        private val messagesToSend: MutableList<AlarmManagerReceiverAlwaysOn.MittenteMessaggio>,
        private val notificationType: String,
        private val twopane: Boolean,
        private val context: Context

){
    companion object NotificationID{
        private var ID: Int=0

        fun getNextId():Int {
            ID++
            return ID-1
        }
    }


    fun launchBackgroundProcessNotification(notification_id: Int = getNextId()){

        launchConversationNotifications()

        val notificationManager: NotificationManager? = context.getSystemService()

        val builder = NotificationCompat.Builder(context, ItemListActivity.SERVICE).apply {
            setContentTitle("Download di una foto")
            setContentText("Download in corso")
            setSmallIcon(chat_img)
        }
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
                        builder.setProgress(progressMax, progressCurrent, false)
                        notificationManager.notify(notification_id, builder.build())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                builder.setContentText("Download completato")
                        .setProgress(0, 0, false)
                builder.setProgress(progressMax, progressCurrent, false)
                notificationManager.notify(notification_id, builder.build())
                val messageGenerator= MessageGenerator(user, chat_id, messagesToSend, context)
                messageGenerator.generateImageMessage()
                launchImageNotification(notification_id)
            }).start()
        }

    }

    fun launchImageNotification(notification_id: Int = getNextId()){
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
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

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
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        val notificationManager: NotificationManager? = context.getSystemService()

        val notification = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo.substring(0,10))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(messagesToSend[0].messaggio.testo))
                .setContentIntent(pendingIntent)
                .build()

        notificationManager?.notify(notification_id,notification)
    }

    fun launchMultipleNotifications(notification_id: Int = getNextId()){
        //use constant ID for notification used as group summary
        val summary_id= 0
        val group_notification = "com.android.NotificationExpo.MULTIPLE_MESSAGES"

        //TODO aggiungere intent ad ogni notifica singola quindi impostare questi intent per smartphone e tablet

        val newMessageNotification1 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[0].mittente.nickname)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[0].mittente.imgProfilo))
                .setGroup(group_notification)
                .build()

        val newMessageNotification2 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[1].mittente.nickname)
                .setContentText(messagesToSend[1].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[1].mittente.imgProfilo))
                .setGroup(group_notification)
                .build()

        val newMessageNotification3 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[2].mittente.nickname)
                .setContentText(messagesToSend[2].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[2].mittente.imgProfilo))
                .setGroup(group_notification)
                .build()

        val newMessageNotification4 = NotificationCompat.Builder(context, ItemListActivity.EXPANDABLE)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(messagesToSend[3].mittente.nickname)
                .setContentText(messagesToSend[3].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, messagesToSend[3].mittente.imgProfilo))
                .setGroup(group_notification)
                .build()

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
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

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
                .setGroup(group_notification)
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
                notify(summary_id, summaryNotification)
            }
        }
    }

    fun launchBubbleNotification(notification_id: Int = getNextId()){
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

    fun launchCustomNotifications(notification_id: Int = getNextId()){
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

        //Inten per tocco su notifica non espansa
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chat_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
                    .setAction("com.android.NotificationsExpo.CONVERSATION")
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

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

    fun launchConversationNotifications(notification_id: Int = getNextId()) {
        val notificationManager: NotificationManager? = context.getSystemService()
        val shortcutManager: ShortcutManager = context.getSystemService(AppCompatActivity.SHORTCUT_SERVICE) as ShortcutManager
        val target: Intent
        val pendingIntent: PendingIntent
        /*Creo l'intent ed il PendingIntent per la notifica a seconda se sono table o smartphone*/
        if (!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .setAction("com.android.NotificationsExpo.CONVERSATION")
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
                    .setAction("com.android.NotificationsExpo.CONVERSATION")
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
        * particolari metadati e proprietà che servono per scatenare le conversation notification e le chat bubbles.
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
                .setIntent(target)
                .setPerson(person)
                .build()

        /*pubblico lo shortcut come dinamico tramite pushDynamicShortcut che in automatico gestisce il limite
        * imposto da android degli shortcuts che si possono pubblicare e permette aggiornare uno shortcut se ha lo stesso id*/

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

        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }

    }

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
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)
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
                .setSmallIcon(messagesToSend[0].mittente.imgProfilo)
                .setContentTitle(chat_name)
                .setContentText(messagesToSend[0].messaggio.testo)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, chat_img))
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                )
                .setContentIntent(pendingIntent)
                .build()

        notificationManager?.notify(notification_id,notification)
    }

    fun launchQuickActionsNotification(notification_id: Int = getNextId()){
        val messaggi: ArrayList<String> = ArrayList()
        messaggi.add(messagesToSend[0].messaggio.testo)

        val replayText: String = context.getString(R.string.reply_text)
        val remoteInput: RemoteInput = RemoteInput.Builder(AlarmManagerReceiverAlwaysOn.KEY_TEXT_REPLY)
                .setLabel(replayText)
                .build()

        val replyIntent = Intent(context, QuickActionNotificationReceiver::class.java)
                .putExtra(AlarmManagerReceiverAlwaysOn.ARRAY_MESSAGES_QA,messaggi) //per messaggi
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.TWO_PANE,twopane)  //per diversi intent per smartphone e tablet
        val replyPendingIntent = PendingIntent.getBroadcast(context,0,replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //intent per tocco su notifica
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
                    .putExtra(ItemDetailFragment.CHAT_ID, notification_id)
                    .putExtra(ItemDetailFragment.CHAT_NAME, chat_name)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                    .putExtra(ItemDetailFragment.NOTIFICATION, notificationType)
                    .putExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,true)
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }

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
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setColor(Color.GREEN)
                .setAutoCancel(true)
                .build()

        val notificationManager: NotificationManager? = context.getSystemService()
        if (notificationManager != null) {
            notificationManager.notify(notification_id,notification)
        }

    }
}