package com.android.notificationexpo

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.android.notificationexpo.database.NotificationExpoRepository
import com.android.notificationexpo.database.dao.ChatDAO
import java.text.DateFormat
import java.text.SimpleDateFormat
import com.android.notificationexpo.receivers.AlarmManagerReceiverAlwaysOn
import com.android.notificationexpo.receivers.AlarmManagerReceiver
import java.util.*

/*Questa Activity mostra la lista delle chat disponibili con un RecyclerView. Se un elemento viene toccato ed il dispositivo
* usato è uno smartphone l'Activity ItemDetailActivity verrà visualizzata a tutto schermo invece se il dispositivo utilizzato
* è un tablet il dettaglio verrà visualizzato a destra tramite un Fragment*/

class ItemListActivity : AppCompatActivity() {

    private var twoPane: Boolean = false //true -> sono su tablet
    private lateinit var repository: NotificationExpoRepository
    private var nChat: Int =0
    private var clickedChat: View?=null
    private var indexClickedChat : Int?=null

    companion object{
        //costanti per il salvataggio di alcune SharedPreferences
        const val SELECTED_CHAT="ChatSelected"
        const val KEY_USER= "UtenteAPP"
        const val FIRST_TIME_RUNNING= "FirstTimeRunning"

        //Constanti per i channel ID
        const val MULTIPLE = "MultipleNotification"
        const val CONVERSATION = "ConversationNotifications"
        const val EXPANDABLE = "ExpandableNotification"
        const val MEDIA = "MediaControl"
        const val BUBBLES = "ChatBubbles"
        const val SERVICE = "ProcessoBackground"
        const val CUSTOM = "CustomNotification"
        const val IMG = "NotificaImmagine"
        //mancano notifica con pulsanti, notifiche con immagini
    }


    // Broadcast receiver
    // Lo scopo di questo boradcast receiver, che viene chiamto sicuramente prima di
    // AlarmManagerReceiverAlwaysOn (in quanto si usano gli ordered broadcast) è quello di
    // intercettare il brodcast e usare il parametro resultCode per avvisare AlarmManagerReceiverAlwaysOn,
    // che verrà chiamato appena dopo, di non visualizzare le notifiche (in quanto ad app aperta si
    // è scelto di non visualizzare le notifiche)
    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Se viene eseguito questo codice vuol dire che l'activity è in foreground e quindi va
            // disabilitata la visualizzazione delle notifiche
            Log.d("MyRDinamicoDETAIL", "Devo disabilitare le notifiche")
            resultCode = Activity.RESULT_CANCELED // Cambia il result code di questo ordered broadcast
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //creo i canali di notifica che serviranno all'applicazione
        createNotificationChannels()

        //imposto l'utente dell'app, in questo caso è Alberto in maniera predefinita
        val preferences= getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        preferences.edit().putString(KEY_USER, "Alberto").apply()

        //recupero la posizione nella recycler view della chat precedentemente selezionata prima del ripristino dell'applicazione
        // (in seguito ad una rotazione ad esempio)
        if (savedInstanceState != null) {
            indexClickedChat= savedInstanceState.getInt(SELECTED_CHAT, -1)
        }

        //inizializzo la repository dell'app
        NotificationExpoRepository.initialize(this)

        //imposoto il layout grafico e la toolbar
        setContentView(R.layout.activity_item_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title
        if (findViewById<FrameLayout>(R.id.item_detail_container) != null) {
            /*Se il FrameLauout definito nel file item_list.xml (w900dp) esiste significa che Android ha scelto automaticamente
             questo layout per schermo grande quindi il dispositivo utilizato è un tablet e l'interfaccia utente sarà
             divisa in due pannelli*/
            twoPane = true
        }


        //Aggiorno il fragment se sono su tablet e nell'intent (che viene creato in ogni notifica se twopane = true) è
        // specificato che è necessario farlo
        if (twoPane && intent.getBooleanExtra(AlarmManagerReceiverAlwaysOn.UPDATE_FRAGMENT,false))
            updateDetailFragment(intent, this)


        // Determino se l'app è stata eseguita per la prima volta
        val firstTime = preferences.getBoolean(FIRST_TIME_RUNNING, true)
        if (firstTime){
            preferences.edit().putBoolean(FIRST_TIME_RUNNING, false).apply()
            val welcomeIntent = Intent(this, WelcomeActivity::class.java)
            startActivity(welcomeIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        //ottengo una reference alla repository
        repository= NotificationExpoRepository.get(this)

        val preferences= getSharedPreferences("Preferences", Context.MODE_PRIVATE)

        //ottengo la lista delle chat per l'utente impostato
        repository.getChat(preferences.getString(KEY_USER,"") as String).observe(
                this,
                Observer {chat->
                    chat?.let{
                        nChat=chat.size
                        updateUI(chat)
                    }
                }
        )

        // Abilito il BroadcastReceiver a runtime
        val filter = IntentFilter(AlarmManagerReceiver.ACTION_SHOW_NOTIFICATION)
        this.registerReceiver(onShowNotification, filter, AlarmManagerReceiver.PERM_PRIVATE, null)
    }

    //funzione che aggiorna la lista delle chat non appena la query al database è finita e i LiveData associati sono stati aggiornati
    private fun updateUI(chat: List<ChatDAO.ChatUtente>){
        val recyclerView:RecyclerView= findViewById(R.id.item_list)
        recyclerView.adapter = ChatAdapter(this, chat, twoPane)
    }

    //metodo per selezionare la chat che è stata cliccata dall'utente
    private fun clickChat(){
        clickedChat!!.setBackgroundColor(Color.parseColor("#9678cc"))
        clickedChat!!.findViewById<TextView>(R.id.notication).setTextColor(Color.parseColor("#FFFFFF"))
        clickedChat!!.findViewById<TextView>(R.id.chat_time).setTextColor(Color.parseColor("#FFFFFF"))
        clickedChat!!.findViewById<TextView>(R.id.name).setTextColor(Color.parseColor("#000000"))
        clickedChat!!.findViewById<View>(R.id.line).visibility=View.INVISIBLE
    }

    //metodo per deselezionare la chat che era stata cliccata dall'utente
    private fun unclickChat(){
        clickedChat!!.setBackgroundColor(Color.parseColor("#FFFFFF"))
        clickedChat!!.findViewById<TextView>(R.id.notication).setTextColor(Color.parseColor("#b2b2b2"))
        clickedChat!!.findViewById<TextView>(R.id.name).setTextColor(Color.parseColor("#000000"))
        clickedChat!!.findViewById<TextView>(R.id.chat_time).setTextColor(Color.parseColor("#b2b2b2"))
        clickedChat!!.findViewById<View>(R.id.line).visibility=View.VISIBLE
    }

    //metodo per modificare in modo controllato il campo indexClickedChat
    fun setIndexClickedChat(index: Int){
        if(index in 0 until nChat)
            indexClickedChat=index
    }

    override fun onPause() {
        super.onPause()
        // Disabilito il BroadcastReceiver a runtime
        this.unregisterReceiver(onShowNotification)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //salvo l'indice dell'ultima chat cliccata così da ripristinare lo stato dopo un restore del dispoitivo
        if(indexClickedChat!=null)
            outState.putInt(SELECTED_CHAT, indexClickedChat as Int)
    }

    // Codice necessario per creare il menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return true
    }

    // Codice necessario per gestire i click nel menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gestione della selezione dell'elemento
        return when (item.itemId) {
            R.id.istructions -> {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.credits -> {
                val intent = Intent(this,CreditsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Funzione che crea tutti i channel per le notifiche, uno per ogni tipo di notifica
    private fun createNotificationChannels(){
        // Creazione dei Notification Channel solo se API 26+ perché prima non è necessario lanciare una notifica in un NotificationChannel.
        // Inoltre la classe NotificationChannel è nuova e non c'è nella support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = NotificationChannel(MULTIPLE, getString(R.string.c_name_multiple), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_multiple) }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(CONVERSATION, getString(R.string.c_name_conversation), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_conversation)}
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(EXPANDABLE, getString(R.string.c_name_expandable), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_expandable) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(MEDIA, getString(R.string.c_name_media), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_media) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(BUBBLES, getString(R.string.c_name_bubbles), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_bubbles) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(SERVICE, getString(R.string.c_name_service), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_service) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(CUSTOM, getString(R.string.c_name_custom), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_custom) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(IMG, getString(R.string.c_name_img), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_img) }
            notificationManager.createNotificationChannel(channel)
        }
    }

    //classe adapter per la recycler view delle chat presente nella UI
    inner class ChatAdapter(private val parentActivity: ItemListActivity,
                      private val values: List<ChatDAO.ChatUtente>,
                      private val twoPane: Boolean) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_content, parent, false)
            return ChatViewHolder(view)
        }

        //funzione che "compila" il view holder appena creato
        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            //if che si azione durante la rigenerazione della RecyclerView dopo un restore del device,
            //serve a selezionare la chat selezionata prima del restore. Si esegue ovviamente quando siamo su tablet
            if(indexClickedChat == position  && twoPane){
                //imposto la chat data come chat cliccata in questo momento
                clickedChat = holder.itemView
                //cambio i colori della chat in modo da far vedere che è quella selezionata
                clickChat()
            }
            val item = values[position]
            holder.image.setImageResource(item.imgChat)
            holder.name.text=item.nomeChat
            holder.notification.text = item.notificaAssociata
            val dateFormat: DateFormat = SimpleDateFormat("hh:mm", Locale.ITALY)
            val strDate: String = dateFormat.format(item.lastMessageDateTime)
            holder.time.text = strDate
            with(holder.itemView) {
                //imposto il listener
                setOnClickListener{ v ->
                    if (twoPane) {
                        //controllo se la chat appena selezionata è la chat precedente selezionata, in tal caso non faccio nulla
                        //se invece la nuova chat selezionata è diversa da quella precedente allora aggiorno il fragment, effettuo le deselezioni ecc.
                        if(position!=indexClickedChat) {
                            //se è presente una chat precedentemente selezionata la deseleziono
                            if (clickedChat != null) {
                                unclickChat()
                            }
                            //aggiorno indice e riferimento alla chat attualmente selezionata
                            indexClickedChat = position
                            clickedChat = v
                            clickChat()
                            val fragment = ItemDetailFragment().apply {
                                //passati id chat, nome chat, immagine della chat, notifica associata alla chat e indicazione sul device (tablet o smartphone)
                                arguments = Bundle().apply {
                                    putBoolean(ItemDetailFragment.TWO_PANE, twoPane)
                                    putLong(ItemDetailFragment.CHAT_ID, item.idChat)
                                    putString(ItemDetailFragment.CHAT_NAME, item.nomeChat)
                                    putInt(ItemDetailFragment.CHAT_IMG, item.imgChat)
                                    putString(ItemDetailFragment.NOTIFICATION, item.notificaAssociata)
                                }
                            }
                            parentActivity.supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.item_detail_container, fragment)
                                    .commit()
                        }
                    } else {
                        //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                        val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                            putExtra(ItemDetailFragment.CHAT_ID, item.idChat)
                            putExtra(ItemDetailFragment.CHAT_NAME,item.nomeChat)
                            putExtra(ItemDetailFragment.CHAT_IMG,item.imgChat)
                            putExtra(ItemDetailFragment.NOTIFICATION,item.notificaAssociata)
                        }
                        v.context.startActivity(intent)
                    }
                }
            }
        }

        override fun getItemCount() = values.size

        //classe interna per la gestione di ogni cassetto della RecycleView delle Chat
        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.name)
            val notification: TextView = view.findViewById(R.id.notication)
            val time: TextView = view.findViewById(R.id.chat_time)
            val image: ImageView = view.findViewById(R.id.image)
        }
    }

    //Funzione che rimpiazza il fragment del dettaglio di una chat se tocco su una notifica e sono su un tablet
    private fun updateDetailFragment(intent: Intent, parentActivity: ItemListActivity){
        val fragment = ItemDetailFragment().apply {
            arguments = Bundle().apply {
                //passati id chat, nome chat, immagine della chat, notifica associata alla chat e indicazione sul device (tablet o smartphone)
                putLong(ItemDetailFragment.CHAT_ID, intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1))
                putString(ItemDetailFragment.CHAT_NAME,intent.getStringExtra(ItemDetailFragment.CHAT_NAME))
                putInt(ItemDetailFragment.CHAT_IMG,intent.getIntExtra(ItemDetailFragment.CHAT_IMG,-1))
                putString(ItemDetailFragment.NOTIFICATION,intent.getStringExtra(ItemDetailFragment.NOTIFICATION))
                putBoolean(ItemDetailFragment.TWO_PANE,twoPane)
            }
        }
        parentActivity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.item_detail_container, fragment)
                .commit()
    }
}