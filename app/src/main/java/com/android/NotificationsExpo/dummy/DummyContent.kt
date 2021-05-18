package com.android.NotificationsExpo.dummy

import com.android.NotificationsExpo.R
import java.util.ArrayList
import java.util.HashMap

//classe che incapsula il contenuto
/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent{

    /**
     * An array of sample (dummy) item.
     */
    val ITEMS: MutableList<DummyItem> = ArrayList()
    /**
     * A map of sample (dummy) item, by ID.
     */
    val ITEM_MAP: MutableMap<String, DummyItem> = HashMap()

    val  images_id: IntArray = intArrayOf(R.drawable.image_chat1,R.drawable.image_chat2,R.drawable.image_chat3,R.drawable.image_chat4,R.drawable.image_chat5,R.drawable.image_chat6,R.drawable.image_chat7,R.drawable.image_chat8,R.drawable.image_chat9,R.drawable.image_chat10)

    private val COUNT = 25

    init {
        // Add some sample item.
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }
    
    private fun addItem(item: DummyItem) {
        ITEMS.add(item)
        //ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): DummyItem {

        return DummyItem(images_id[position%10], "Luca","Heads-up Notification","14:00")
    }

    //crea testo per elemento in posizione position
    /*private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }*/

    /**
     * A dummy item representing a piece of content.
     */
    //aggiungere id chat
    data class DummyItem(val imageId: Int, val name: String, val notification: String, val time: String) {
    }
}

