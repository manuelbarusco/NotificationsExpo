package com.android.NotificationsExpo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.NotificationsExpo.dummy.DummyContent
import com.android.NotificationsExpo.dummy.MessageDatasource

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: DummyContent.DummyItem? = null
    private lateinit var recyclerView: RecyclerView
    //private val d: DummyContent = DummyContent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = DummyContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
                //activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = item?.content
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.chat_detail, container, false)
        //TODO: risolvere problema action bar
        val messages= MessageDatasource().getMessagesExample(20)
        recyclerView=rootView.findViewById(R.id.recycler_chat)
        recyclerView.adapter=MessageAdapter(messages)
        // Show the dummy content as text in a TextView.
        item?.let {
            //rootView.findViewById<TextView>(R.id.item_detail).text = it.details
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}