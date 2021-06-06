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

        messaggi = intent.getStringArrayListExtra(AlarmManagerReceiverAlwaysOn.ARRAY_MESSAGES_QA) as ArrayList<String>
        chat_id = intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1)
        notification_id =intent.getIntExtra(ItemDetailFragment.NOTIFICATION_ID, -1)
        chat_img = intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1)
        chatName = intent.getStringExtra(ItemDetailFragment.CHAT_NAME).toString()
        twopane = intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,true)
        message = getMessageText(intent) as String
        messaggi.add(message)
        size = messaggi.size
        //prelevo il nome dell'utente dalle SharedPreferences
        val preferences= context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user = preferences?.getString(ItemListActivity.KEY_USER,"") as String
        val executor = Executors.newSingleThreadExecutor()
        if(size%2==0){
            updateNotification(context,false)
            executor.execute {
                Thread.sleep(2000L)
                updateNotification(context,true)
            }
        }

    }

    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent).getCharSequence(AlarmManagerReceiverAlwaysOn.KEY_TEXT_REPLY)
    }
    private fun updateNotification(context: Context, answer: Boolean){
        val person = Person.Builder()
                .setName(chatName)
                .setIcon(Icon.createWithResource(context,chat_img))
                //.setImportant(true)
                .build()
        val me = Person.Builder()
                .setName("Io")
                .build()

        val messagingStyle: Notification.MessagingStyle = Notification.MessagingStyle(person)
        if(answer) {
            val t = generateMessage(context)
            messaggi.add(t)
        }
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
        //aggiungo nel DB messaggio che viene scritto nella notifica
        if(!answer){
            val m = Messaggio(testo = message, chat = chat_id, media = null, mittente = user)
            repository.addMessage(m)
        }

        val replayText: String = context.getString(R.string.reply_text)
        val remoteInput: RemoteInput = RemoteInput.Builder(AlarmManagerReceiverAlwaysOn.KEY_TEXT_REPLY)
                .setLabel(replayText)
                .build()

        val replyIntent = Intent(context, QuickActionNotificationReceiver::class.java)
                .putExtra(AlarmManagerReceiverAlwaysOn.ARRAY_MESSAGES_QA,messaggi) //per messaggi
                .putExtra(ItemDetailFragment.CHAT_ID, chat_id)
                .putExtra(ItemDetailFragment.NOTIFICATION_ID, notification_id)
                .putExtra(ItemDetailFragment.CHAT_NAME, chatName)
                .putExtra(ItemDetailFragment.CHAT_IMG, chat_img)
                .putExtra(ItemDetailFragment.TWO_PANE,twopane)
        val replyPendingIntent = PendingIntent.getBroadcast(context,0,replyIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        //intent per tocco su notifica
        val target: Intent
        val pendingIntent: PendingIntent
        if(!twopane) {
            val target = Intent(context, ItemDetailActivity::class.java)
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
    private  fun generateMessage(context: Context): String{
        val messages: Array<String> = context.resources.getStringArray(R.array.dummy_messages)

        val index = Random.nextInt(0, messages.size - 1)

        //genero il messaggio e lo aggiungo al database
        val mex= Messaggio(testo= messages[index], chat = chat_id, media = null, mittente = chatName)
        repository.addMessage(mex)

        return messages[index]
    }

}