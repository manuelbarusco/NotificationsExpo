package com.android.notificationexpo.receivers

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.android.notificationexpo.database.NotificationExpoRepository
import com.android.notificationexpo.database.entities.Messaggio
import com.android.notificationexpo.ItemListActivity
import com.android.notificationexpo.SettingsActivity
import com.android.notificationexpo.ItemDetailFragment
import com.android.notificationexpo.ItemDetailActivity
import com.android.notificationexpo.R

//BroadcastReceiver che permette di gestire le notifiche con un template custom
class CustomNotificationReceiver : BroadcastReceiver() {
    private lateinit var repository: NotificationExpoRepository
    private var notificationManager: NotificationManager? = null
    private lateinit var user:String
    private lateinit var message: String
    private var chatId: Long = -1
    private var notificationId: Int =-1
    private var chatImg: Int = -1
    private lateinit var chatName: String
    private var selected : Int = -1
    private var twopane: Boolean = true
    private lateinit var text: String
    private var time: Int = 2

    override fun onReceive(context: Context, intent: Intent) {
        repository = NotificationExpoRepository.get(context)
        notificationManager = context.getSystemService()

        //prelevo il nome dell'utente dalle SharedPreferences, serve per salvare correttamente nel DB i messaggi scelti dall'utente
        val preferences= context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user = preferences?.getString(ItemListActivity.KEY_USER,"") as String
        //Prelevo dalle SharedPreferences i secondi di attesa per la risposta automatica (impostabili nella Settings Activity)
        time = preferences.getInt(SettingsActivity.SECONDS,2)
        message = intent.getStringExtra(AlarmManagerReceiverAlwaysOn.MESSAGE).toString()
        chatId = intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1)
        notificationId= intent.getIntExtra(ItemDetailFragment.NOTIFICATION_ID, -1)
        chatImg = intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1)
        chatName = intent.getStringExtra(ItemDetailFragment.CHAT_NAME).toString()
        selected = intent.getIntExtra(AlarmManagerReceiverAlwaysOn.SELECTED_CB,-1)
        twopane = intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,true)
        when(intent.action){
            "com.android.NotificationsExpo.CB1_CLICKED"-> updateUI(1,context,true)
            "com.android.NotificationsExpo.CB2_CLICKED"-> updateUI(2,context,true)
            "com.android.NotificationsExpo.CB3_CLICKED"-> updateUI(3,context,true)
            "com.android.NotificationsExpo.CB4_CLICKED"-> updateUI(4,context, true)
            "com.android.NotificationsExpo.BUTTON_INVIA_CLICKED"-> {

                if (selected == -1) {
                    //Se utente non ha selezionato una risposta rapita aggiorno la UI della notifica con un messaggio di errore
                    updateUI(-1,context,false)
                }
                else {
                    //Solita procedura per inviare un messaggio di risposta
                    text = getText(context)
                    //aggiungo il messaggio selezionato nel DB e poi cancello la notifica
                    val m = Messaggio(testo = text, chat = chatId, media = null, mittente = user)
                    repository.addMessage(m)
                    notificationManager?.cancel(notificationId)

                    //Risposta automatica come in ItemDetailFragment

                    // Impostiamo il timer e dopo un certo tempo verrà inviato un broadcast esplicito ad
                    // AlarmMangerReceiver
                    val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(context, AlarmManagerReceiver::class.java) // intent esplicito
                    alarmIntent.putExtra(ItemDetailFragment.NOTIFICATION,"Notifica custom template")
                    alarmIntent.putExtra(ItemDetailFragment.CHAT_ID,chatId)
                    alarmIntent.putExtra(ItemDetailFragment.NOTIFICATION_ID,notificationId)
                    alarmIntent.putExtra(ItemDetailFragment.CHAT_NAME,chatName)
                    alarmIntent.putExtra(ItemDetailFragment.CHAT_IMG,chatImg)
                    alarmIntent.putExtra(ItemDetailFragment.TWO_PANE,twopane)

                    // Genero un id da assegnare al broadcast per generare broadcast sempre diversi
                    // Se non lo faccio e genero più broadcast prima dello scadere del tempo non li vedrò
                    val boradcastId:Int = System.currentTimeMillis().toInt()
                    val pendingIntent = PendingIntent.getBroadcast(context, boradcastId, alarmIntent, 0)

                    // Nota: Sostituito con alarmManager?.set con alarmManager?.setExact per avere più precisione
                    // https://developer.android.com/reference/android/app/AlarmManager
                    alarmManager.setExact(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + time * 1000,
                            pendingIntent)

                    // Note sulla sicurezza:
                    // 1) Essendo broadcast espliciti ho la certezza che non verrranno recapitati ad altri
                    // 2) Poichè AlarmManagerReceiver è dichiarato nel manifest con exported=false, esso
                    //    riceverà solo gli intent provenienti da questa app
                    // https://developer.android.com/guide/components/broadcasts#security-and-best-practices
                }
            }

            "com.android.NotificationsExpo.BUTTON_ANNULLA_CLICKED"-> {
                //Cancello la notifica
                notificationManager?.cancel(notificationId)
            }
        }
    }
    //Funzione che ritorna il testo corrispondete per ogni possibile scelta nella notifica
    private fun getText( context: Context): String{
        return when(selected){
            1-> context.getString(R.string.text_cb_1)
            2-> context.getString(R.string.text_cb_2)
            3-> context.getString(R.string.text_cb_3)
            4-> context.getString(R.string.text_cb_4)
            else -> throw IllegalArgumentException()
        }
    }
    /*Funzione che aggiorna gli elementi grafici della notifica custom*/
    private fun updateUI(position: Int, context: Context, isSelected: Boolean){
        val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_expanded)
        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_collapsed)

        notificationLayout.setImageViewResource(R.id.notification_icon,chatImg)
        notificationLayout.setTextViewText(R.id.notification_title,"Nuovo messaggio da $chatName")
        notificationLayoutExpanded.setImageViewResource(R.id.e_notification_icon,chatImg)
        notificationLayoutExpanded.setTextViewText(R.id.e_notification_title,message)

        if(isSelected) {
            //Cambio il pallino se è stata scelta una risposta
            when (position) {
                1 -> {
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_1, R.drawable.checkbox_selected)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_2, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_3, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_4, R.drawable.checkbox)
                    selected = 1
                }
                2 -> {
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_1, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_2, R.drawable.checkbox_selected)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_3, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_4, R.drawable.checkbox)
                    selected = 2
                }
                3 -> {
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_1, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_2, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_3, R.drawable.checkbox_selected)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_4, R.drawable.checkbox)
                    selected = 3
                }
                4 -> {
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_1, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_2, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_3, R.drawable.checkbox)
                    notificationLayoutExpanded.setImageViewResource(R.id.cb_4, R.drawable.checkbox_selected)
                    selected = 4
                }
                else -> throw IllegalArgumentException("Checkbox non presente")
            }
        }
        else{
            //Se l'utente non ha selezionato nessuna risposta prima di inviare visualizzo messaggio che avvisa l'utente
            notificationLayoutExpanded.setTextViewText(R.id.e_notification_content,context.getString(R.string.e_notification_content_no_selection))
            notificationLayoutExpanded.setTextColor(R.id.e_notification_content,context.getColor(R.color.red))
            notificationLayoutExpanded.setTextViewTextSize(R.id.e_notification_content,TypedValue.COMPLEX_UNIT_DIP, 12F)
        }
        //Creo di nuovo la notifica come in NotificationLauncher
        /*Visto che queste View sono in altro processo non si può impostare un onClickListener come il solito.
        * Bisogna utilizzare un PendingIntent in modo tale da ridirezionare la gestione dei click sugli elementi della
        * UI della notifica custom nell processo main dell'app.*/
        val cb1Intent = Intent(context,CustomNotificationReceiver::class.java)
                .setAction("com.android.NotificationsExpo.CB1_CLICKED")
                .putExtra(ItemDetailFragment.CHAT_ID,chatId)
                .putExtra(ItemDetailFragment.NOTIFICATION_ID,notificationId)
                .putExtra(AlarmManagerReceiverAlwaysOn.MESSAGE,message)
                .putExtra(ItemDetailFragment.CHAT_IMG,chatImg)
                .putExtra(ItemDetailFragment.CHAT_NAME,chatName)
                .putExtra(AlarmManagerReceiverAlwaysOn.SELECTED_CB,selected) //devo passare anche la variabile selected perché ogni volta che l'utente sceglie una risposta (senza inviare) viene creata una nuova notifica
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
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            target = Intent(context, ItemDetailActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chatId)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chatName)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chatImg)
                    .putExtra(ItemDetailFragment.NOTIFICATION, "Notifica custom template")
            pendingIntent = PendingIntent.getActivity(context, 0, target, PendingIntent.FLAG_CANCEL_CURRENT)
        }
        else{
            target = Intent(context, ItemListActivity::class.java)
                    .putExtra(ItemDetailFragment.CHAT_ID, chatId)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                    .putExtra(ItemDetailFragment.CHAT_NAME, chatName)
                    .putExtra(ItemDetailFragment.CHAT_IMG, chatImg)
                    .putExtra(ItemDetailFragment.NOTIFICATION, "Notifica custom template")
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
                .setOnlyAlertOnce(true)     //avvisa l'utente con suoni/vibrazioni solo la prima volta

        notificationManager?.notify(notificationId,notification.build())
    }

}