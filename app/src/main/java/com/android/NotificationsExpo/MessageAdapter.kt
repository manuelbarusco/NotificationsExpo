package com.android.NotificationsExpo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.NotificationsExpo.database.entities.Messaggio
import com.android.NotificationsExpo.dummy.Message
import java.util.*

class MessageAdapter(val messageList: List<Messaggio>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    //classe MessageViewHolder per la gestione del singolo cassetto della RecyclerView che contiene un messaggio
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message)
        private val messageDate: TextView = itemView.findViewById(R.id.message_date)
        fun bind(word: String, date: Date) {
            messageText.text = word
            messageDate.text = date.toString()
        }
    }

    //metodo che verifica quale layout associare al cassetto che ospiterà il messaggio nella posizione $position
    //della lista di messaggi in base al tipo di messaggio (RECEIVED o SEND)
    override fun getItemViewType(position: Int): Int {
        if(messageList[position].mittente=="Alberto")
            return Messaggio.CONST.MESSAGE_SEND
        return Messaggio.CONST.MESSAGE_RECEIVED
    }

    //metodo che ritorna un nuovo view holder con layout coerente col tipo di messaggio dato. Il tipo di layout da inserire è specificato
    //dal parametro $viewType
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        var idLayout=R.layout.message_item
        if(viewType==Message.CONST.MESSAGE_RECEIVED)
            idLayout=R.layout.message_from_item
        val view = LayoutInflater.from(parent.context)
                .inflate(idLayout, parent, false)
        return MessageViewHolder(view)
    }

    //metodo che ritorna la lunghezza della lista di messaggi
    override fun getItemCount(): Int {
        return messageList.size
    }

    //metodo che mostra i dati di un messaggio in una certa posizione
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position].testo, messageList[position].dateTime)
    }
}