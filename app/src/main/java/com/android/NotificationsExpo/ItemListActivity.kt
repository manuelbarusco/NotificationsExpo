package com.android.NotificationsExpo

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.android.NotificationsExpo.database.NotificationExpoRepository
import com.android.NotificationsExpo.database.dao.ChatDAO
import java.text.SimpleDateFormat


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of ITEMS, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of ITEMS and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private lateinit var repository: NotificationExpoRepository

    companion object{
        const val KEY_USER= "UtenteAPP"
        //Constanti per i channel ID
        const val MULTIPLE = "MultipleNotification"
        const val CONVERSATION = "ConversationNotifications"
        const val EXPANDABLE = "ExpandableNotification"
        const val MEDIA = "MediaControl"
        const val BUBBLES = "ChatBubbles"
        const val SERVICE = "ProcessoBackground"
        const val CUSTOM = "CustomNotification"
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
        //TODO: Controllare se quando canali esistono già vengono ricreati
        createNotificationChannels()
        val preferences= getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        preferences.edit().putString(KEY_USER, "Alberto").apply()
        NotificationExpoRepository.initialize(this)
        /*val images_id: Int = resources.getIdentifier("image_chat1","drawable", "com.ebookfrenzy.masterdetailflow")
        Log.d("Debug","id immagine: $images_id invece di ${R.drawable.image_chat1}")*/
        setContentView(R.layout.activity_item_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        if (findViewById<FrameLayout>(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }
    }

    override fun onResume() {
        super.onResume()
        repository= NotificationExpoRepository.get(this)
        val preferences= getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        repository.getChat(preferences.getString(KEY_USER,"") as String).observe(
                this,
                Observer {chat->
                    chat?.let{
                        Log.i("Database",""+chat.size)
                        updateUI(chat)
                    }
                }
        )
        // Abilito il BroadcastReceiver a runtime
        val filter = IntentFilter(AlarmManagerReceiver.ACTION_SHOW_NOTIFICATION)
        this.registerReceiver(onShowNotification, filter, AlarmManagerReceiver.PERM_PRIVATE, null)
    }

    private fun updateUI(chat: List<ChatDAO.ChatUtente>){
        val recyclerView:RecyclerView= findViewById(R.id.item_list)
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, chat, twoPane)
    }


    //classe adapter per la recycler view
    class SimpleItemRecyclerViewAdapter(private val parentActivity: ItemListActivity,
                                        private val values: List<ChatDAO.ChatUtente>,
                                        private val twoPane: Boolean) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener
        //click listener per l'elemento della lista
        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as ChatDAO.ChatUtente
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt(ItemDetailFragment.CHAT_ID, item.idChat)     //passati id chat, nome chat, immagine della chat e notifica associata alla chat
                            putString(ItemDetailFragment.CHAT_NAME,item.nomeChat)
                            if(item.nomeChat==null)
                                putString(ItemDetailFragment.CHAT_NAME,item.nomeChatPrivata)
                            putInt(ItemDetailFragment.CHAT_IMG,item.imgChatGruppo as Int)
                            if(item.imgChatGruppo==null)
                                putInt(ItemDetailFragment.CHAT_IMG,item.imgChatPrivata)
                            putString(ItemDetailFragment.NOTIFICATION,item.notificaAssociata)
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.CHAT_ID, item.idChat)       //passati id chat, nome chat e immagine della chat e notifica associata alla chat
                        putExtra(ItemDetailFragment.CHAT_NAME,item.nomeChat)
                        if(item.nomeChat==null)
                            putExtra(ItemDetailFragment.CHAT_NAME,item.nomeChatPrivata)
                        putExtra(ItemDetailFragment.CHAT_IMG,item.imgChatGruppo)
                        if(item.imgChatGruppo==null)
                            putExtra(ItemDetailFragment.CHAT_IMG,item.imgChatPrivata)
                        putExtra(ItemDetailFragment.NOTIFICATION,item.notificaAssociata)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            if(item.imgChatGruppo!=null)
                holder.image.setImageResource(item.imgChatGruppo)
            else
                holder.image.setImageResource(item.imgChatPrivata)
            if(item.nomeChat!=null)
                holder.name.text=item.nomeChat
            else
                holder.name.text=item.nomeChatPrivata
            holder.notification.text = item.notificaAssociata
            //holder.time.text = item.lastMessageDateTime.time.toString()

            with(holder.itemView) {
                tag = item
                //imposto il listener scritto sopra
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.name)
            val notification: TextView = view.findViewById(R.id.notication)
            val time: TextView = view.findViewById(R.id.chat_time)
            val image: ImageView = view.findViewById(R.id.image)
        }
    }

    override fun onPause() {
        // Disabilito il BroadcastReceiver a runtime
        this.unregisterReceiver(onShowNotification)

        super.onPause()
    }

    // Codice necessario per creare il menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return true
    }

    // Codice necessario per gestire i click nel menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
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
                //TODO Aggiungere codice per activity credits
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Funzione che crea tutti i channel per le notifiche, uno per ogni tipo di notifica
    private fun createNotificationChannels(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = NotificationChannel(MULTIPLE, getString(R.string.c_name_multiple), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_multiple) }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(CONVERSATION, getString(R.string.c_name_conversation), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_conversation)}
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(EXPANDABLE, getString(R.string.c_name_expandable), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_expandable) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(MEDIA, getString(R.string.c_name_media), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_media) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(BUBBLES, getString(R.string.c_name_bubbles), NotificationManager.IMPORTANCE_HIGH).apply { description = getString(R.string.c_descr_bubbles) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(SERVICE, getString(R.string.c_name_service), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_service) }
            notificationManager.createNotificationChannel(channel)

            channel = NotificationChannel(CUSTOM, getString(R.string.c_name_custom), NotificationManager.IMPORTANCE_DEFAULT).apply { description = getString(R.string.c_descr_custom) }
            notificationManager.createNotificationChannel(channel)
        }
    }

}