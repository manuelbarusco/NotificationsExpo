package com.android.NotificationsExpo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.NotificationsExpo.database.NotificationExpoRepository
import com.android.NotificationsExpo.dummy.DummyContent
import com.android.NotificationsExpo.dummy.MessageDatasource
import androidx.lifecycle.Observer
import com.android.NotificationsExpo.database.entities.Messaggio

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {
    private var chat_id:Int= -1
    private lateinit var repository: NotificationExpoRepository

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: DummyContent.DummyItem? = null
    private lateinit var recyclerView: RecyclerView
    //private val d: DummyContent = DummyContent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(CHAT_ID)) {
                chat_id=it.getInt(CHAT_ID)
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.chat_detail, container, false)
        //TODO: risolvere problema action bar
        //val messages= MessageDatasource().getMessagesExample(20)
        recyclerView=rootView.findViewById(R.id.recycler_chat)
        if(context!=null)
            repository= NotificationExpoRepository.get(context as Context)
        repository.getChatMessages(chat_id).observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer {messaggi->
                    messaggi?.let{
                        recyclerView.adapter=MessageAdapter(messaggi)
                    }
                }

        )
        val sendButton: Button= rootView.findViewById(R.id.button_chat_send)
        var alarmManager: AlarmManager
        sendButton.setOnClickListener {
            //Creo AlarmManager
            //https://developer.android.com/guide/components/broadcasts#security-and-best-practices
            alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var alarmIntent = Intent(context, AlarmManagerReceiver::class.java)
            alarmIntent.putExtra("Test","ValueReceived")

            /*
            //la classe MyReceiver2 è uguale a MyReceiver
            var alarmIntent2 = Intent(context, MyReceiver2::class.java)
            alarmIntent2.putExtra("Test","ValueReceived")*/

            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            //val pendingIntent2 = PendingIntent.getBroadcast(context, 0, alarmIntent2, 0)
            alarmManager?.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 10 * 1000,
                    pendingIntent)
            /*
            alarmManager?.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 5 * 1000,
                    pendingIntent2)*/
        }
        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val CHAT_ID = "item_id"
    }
}