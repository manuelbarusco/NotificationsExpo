package com.android.NotificationsExpo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.NotificationsExpo.database.NotificationExpoRepository
import com.android.NotificationsExpo.database.entities.Messaggio

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {
    private var chat_id:Int= -1
    private var nome_chat:String?=null
    private var img_chat:Int=-1
    private var notifica_chat:String?=null
    private lateinit var user:String
    private lateinit var repository: NotificationExpoRepository
    private lateinit var messaggi: MutableList<Messaggio>
    private lateinit var recyclerView: RecyclerView
    private var time: Int = 2
    private var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = activity?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user = preferences?.getString(ItemListActivity.KEY_USER,"") as String

        arguments?.let {
            if (it.containsKey(CHAT_ID))
                chat_id=it.getInt(CHAT_ID)
            if(it.containsKey(CHAT_NAME))
                nome_chat=it.getString(CHAT_NAME)
            if(it.containsKey(CHAT_IMG))
                img_chat=it.getInt(CHAT_IMG)
            if(it.containsKey(NOTIFICATION))
                notifica_chat=it.getString(NOTIFICATION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.chat_detail, container, false)
        val chat_name_view:TextView=rootView.findViewById(R.id.chat_name)
        val chat_img_view:ImageView=rootView.findViewById(R.id.chat_image)
        chat_name_view.text=nome_chat
        chat_img_view.setImageResource(img_chat)
        //TODO: risolvere problema action bar
        recyclerView=rootView.findViewById(R.id.recycler_chat)
        if(context!=null)
            repository= NotificationExpoRepository.get(context as Context)
        var chat_type= MessageAdapter.PRIVATE_CHAT
        if(repository.getChatUtenti(chat_id, user).size>1)
            chat_type=MessageAdapter.GROUP_CHAT
        repository.getChatMessages(chat_id).observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer {messaggi->
                    messaggi?.let{
                        this.messaggi=messaggi
                        recyclerView.adapter=MessageAdapter(messaggi, img_chat, chat_type, context)
                    }
                }
        )
        val userText: EditText=rootView.findViewById(R.id.user_text)
        val sendButton: Button= rootView.findViewById(R.id.button_chat_send)

        sendButton.setOnClickListener {
            val mex: Messaggio=Messaggio(testo = userText.text.toString(), mittente = user, media = null, chat=chat_id)
            repository.addMessage(mex)
            messaggi.add(mex)
            recyclerView.adapter?.notifyItemInserted(messaggi.size)
            userText.text.clear()

            // Impostiamo il timer e dopo un certo tempo verrà inviato un broadcast esplicito ad
            // AlarmMangerReceiver

            val alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var alarmIntent = Intent(context, AlarmManagerReceiver::class.java) // intent esplicito
            alarmIntent.putExtra(NOTIFICATION,notifica_chat)
            alarmIntent.putExtra(CHAT_ID,chat_id)
            alarmIntent.putExtra(CHAT_NAME,nome_chat)
            alarmIntent.putExtra(CHAT_IMG,img_chat)

            // Genero un id da assegnare al broadcast per generare broadcast sempre diversi
            // Se non lo faccio e genero più broadcast prima dello scadere del tempo non li vedrò
            val boradcastId:Int = System.currentTimeMillis().toInt()
            val pendingIntent = PendingIntent.getBroadcast(context, boradcastId, alarmIntent, 0)

            // Nota: Sostituito con alarmManager?.set con alarmManager?.setExact per avere più precisione
            // https://developer.android.com/reference/android/app/AlarmManager
            time = preferences?.getInt(SettingsActivity.SECONDS,2) as Int
            Log.d("seconds", time.toString())
            alarmManager?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + time * 1000,
                    pendingIntent)

            // Note sulla sicurezza:
            // 1) Essendo broadcast espliciti ho la certezza che non verrranno recapitati ad altri
            // 2) Poichè AlarmManagerReceiver è dichiarato nel manifest con exported=false, esso
            //    riceverà solo gli intent provenienti da questa app
            // https://developer.android.com/guide/components/broadcasts#security-and-best-practices

        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val CHAT_ID = "Chat_id"
        const val CHAT_NAME= "Nome_chat"
        const val CHAT_IMG= "Img_chat"
        const val NOTIFICATION= "Notification_chat"
    }
}