package com.android.notificationexpo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.notificationexpo.database.NotificationExpoRepository
import com.android.notificationexpo.database.entities.Messaggio
import com.android.notificationexpo.receivers.AlarmManagerReceiver

/* Un Fragment che rappresenta il dettaglio di una chat. Questo Fragment è contento in ItemListActivity (per tablet)
* oppure in ItemDetailActivity (per smartphone)*/

class ItemDetailFragment : Fragment() {
    private var twopane: Boolean = true                              //indicazione sulla dimensione del dispositivo
    private var chatId:Long= -1                                     //id della chat rappresentata dal fragment
    private var nomeChat:String?=null                               //nome della chat rappresentata dal fragment
    private var imgChat:Int=-1                                      //immagine della chat rappresentata dal fragment
    private var notificaChat:String?=null                           //notifica associata alla chat rappresentata dal fragment
    private lateinit var user:String                                 //utente che sta usando l'app
    private lateinit var repository: NotificationExpoRepository
    private lateinit var messaggi: MutableList<Messaggio>            //lista di messaggi della chat
    private lateinit var recyclerView: RecyclerView
    private var time: Int = 2                                        //valore di default del tempo di risposta al messaggio
    private var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //recupero dalle shared preferences l'utente che sta usando l'app
        preferences = activity?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        user = preferences?.getString(ItemListActivity.KEY_USER,"") as String

        //recupero tutte le informazioni per la visualizzazione della chat passate dalla
        //activity ItemListActivity in caso di dispositivo tablet (twopane=true) o
        //dall activity ItemDetailActivity in caso di dispositivo smartphone (twopane=false)
        arguments?.let {
            if (it.containsKey(CHAT_ID))
                chatId=it.getLong(CHAT_ID)
            if(it.containsKey(CHAT_NAME))
                nomeChat=it.getString(CHAT_NAME)
            if(it.containsKey(CHAT_IMG))
                imgChat=it.getInt(CHAT_IMG)
            if(it.containsKey(NOTIFICATION))
                notificaChat=it.getString(NOTIFICATION)
            if(it.containsKey((TWO_PANE)))
                twopane=it.getBoolean(TWO_PANE)
        }
    }

    override fun onStart() {
        super.onStart()
        //accedo alle preferences
        val preferences= context?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)

        //recupero la lista delle chat
        val chats: String? = preferences?.getString(ItemListActivity.NOT_READ, "")

        //aggiorno la lista
        if (preferences != null) {
            preferences.edit().putString(ItemListActivity.NOT_READ, //rimuovo l'id della chat alla lista
                StringParser.removeLong(chats as String,chatId)).apply()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.chat_detail, container, false)

        //disattivo la toolbar (si vede solo in modalità smartphone) in modo da non avere la sovrapposizione di due toolbar
        if(twopane) {
            val toolbar: Toolbar=rootView.findViewById(R.id.toolbar_chat)
            toolbar.visibility=Toolbar.GONE
        }

        //imposto nome e immagine della chat
        val chatNameView:TextView=rootView.findViewById(R.id.chat_name)
        val chatImgView:ImageView=rootView.findViewById(R.id.chat_image)
        chatNameView.text=nomeChat
        chatImgView.setImageResource(imgChat)
        recyclerView=rootView.findViewById(R.id.recycler_chat)

        //ottengo una reference alla repository
        if(context!=null)
            repository= NotificationExpoRepository.get(context as Context)

        //recupero il tipo di chat dalla lista dei partecipanti alla chat:
        //se il numero di partecipanti alla chat (escluso l'utente che sta usando la app) è > 1 allora la chat è di gruppo
        //altrimenti è una chat privata
        var chatType= MessageAdapter.PRIVATE_CHAT
        if(repository.getChatUtenti(chatId, user).size>1)
            chatType=MessageAdapter.GROUP_CHAT

        //recupero la lista dei messaggi della chat
        repository.getChatMessages(chatId).observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer {messaggi->
                    messaggi?.let{
                        this.messaggi=messaggi
                        recyclerView.adapter=MessageAdapter(messaggi, imgChat, chatType, context)
                    }
                }
        )

        val userText: EditText=rootView.findViewById(R.id.user_text)
        val sendButton: Button= rootView.findViewById(R.id.button_chat_send)

        sendButton.setOnClickListener {
            if(userText.text.toString()!="") {
                //creo un messaggio col testo inserito per inserirlo poi nel Database e aggiornare la RecyclerView
                val mex = Messaggio(
                    testo = userText.text.toString(),
                    mittente = user,
                    media = null,
                    chat = chatId
                )

                repository.addMessage(mex)
                messaggi.add(mex)

                //aggiorno la recycler view dei messaggi e aggiorno l'indice dell'elemento selezionato dato che ora la chat data si trova in
                //cima alla lista delle chat nella ItemListActitivy
                recyclerView.adapter?.notifyItemInserted(messaggi.size)
                if (twopane)
                    (activity as ItemListActivity).setIndexClickedChat(0)
                userText.text.clear()

                // Impostiamo il timer e dopo un certo tempo verrà inviato un broadcast esplicito ad
                // AlarmMangerReceiver
                val alarmManager: AlarmManager =
                    context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent =
                    Intent(context, AlarmManagerReceiver::class.java) // intent esplicito
                alarmIntent.putExtra(NOTIFICATION, notificaChat)
                alarmIntent.putExtra(CHAT_ID, chatId)
                alarmIntent.putExtra(CHAT_NAME, nomeChat)
                alarmIntent.putExtra(CHAT_IMG, imgChat)
                alarmIntent.putExtra(TWO_PANE, twopane)

                // Genero un id da assegnare al broadcast per generare broadcast sempre diversi
                // Se non lo faccio e genero più broadcast prima dello scadere del tempo non li vedrò
                val boradcastId: Int = System.currentTimeMillis().toInt()
                val pendingIntent = PendingIntent.getBroadcast(context, boradcastId, alarmIntent, 0)

                //Prelevo dalle SharedPreferences i secondi di attesa per la risposta automatica (impostabili nella Settings Activity)
                time = preferences?.getInt(SettingsActivity.SECONDS, 2) as Int

                // Nota: Sostituito con alarmManager?.set con alarmManager?.setExact per avere più precisione
                // https://developer.android.com/reference/android/app/AlarmManager
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + time * 1000,
                    pendingIntent
                )
                // Note sulla sicurezza:
                // 1) Essendo broadcast espliciti ho la certezza che non verrranno recapitati ad altri
                // 2) Poichè AlarmManagerReceiver è dichiarato nel manifest con exported=false, esso
                //    riceverà solo gli intent provenienti da questa app
                // https://developer.android.com/guide/components/broadcasts#security-and-best-practices
            }
        }

        return rootView
    }

    companion object {
        //costanti per identificare gli arguments passati al fragment che servono per caricare tutti i dati necesari per visualizzare una specifica chat
        const val CHAT_ID = "Chat_id"
        const val CHAT_NAME= "Nome_chat"
        const val CHAT_IMG= "Img_chat"
        const val NOTIFICATION= "Notification_chat"
        const val TWO_PANE= "TwoPane"
        const val NOTIFICATION_ID= "Notification_id"
    }
}