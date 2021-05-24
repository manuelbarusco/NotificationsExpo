package com.android.NotificationsExpo

import android.content.Context
import android.content.Intent
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
import com.android.NotificationsExpo.database.*
import com.android.NotificationsExpo.database.dao.ChatDAO


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
    private val KEY_USER= "UtenteAPP"
    private lateinit var repository: NotificationExpoRepository
    //private val d: DummyContent = DummyContent(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences= getPreferences(Context.MODE_PRIVATE)
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

    override fun onStart() {
        super.onStart()
        repository= NotificationExpoRepository.get(this)
        val preferences= getPreferences(Context.MODE_PRIVATE)
        repository.getChat(preferences.getString(KEY_USER,"") as String).observe(
            this,
            Observer {chat->
                chat?.let{
                    Log.i("Database",""+chat.size)
                    updateUI(chat)
                }

            }
        )
    }

    private fun updateUI(chat: List<ChatDAO.ChatUtente>){
        val recyclerView:RecyclerView= findViewById(R.id.item_list)
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, this , chat, twoPane)
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

    //classe adapter per la recycler view
    class SimpleItemRecyclerViewAdapter(private val parentActivity: ItemListActivity,
                                        private val context: Context,
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
                            putInt(ItemDetailFragment.CHAT_ID, item.idChat)      //passare id chat
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R .id.item_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.CHAT_ID, item.idChat)      //passare id chat
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
            //holder.time.text = item.

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
            val time: TextView = view.findViewById(R.id.time)
            val image: ImageView = view.findViewById(R.id.image)
        }
    }
}