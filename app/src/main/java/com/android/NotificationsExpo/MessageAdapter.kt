package com.android.NotificationsExpo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.NotificationsExpo.database.entities.Messaggio
import java.util.*

class MessageAdapter(val messageList: List<Messaggio>, val chat_type: Int) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        const val PRIVATE_CHAT = 0
        const val GROUP_CHAT = 1
    }

    //classe MessageViewHolder per la gestione del singolo cassetto della RecyclerView che contiene un messaggio
    inner class MessageViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message)
        private val messageDate: TextView = itemView.findViewById(R.id.message_date)
        private var messageSender: TextView? = null

        init{
            if(viewType == Messaggio.MESSAGE_RECEIVED && chat_type == GROUP_CHAT)
                messageSender = itemView.findViewById(R.id.message_sender)
        }

        fun bind(word: String, date: Date, sender:String?) {
            messageText.text = word
            messageDate.text = date.toString()
            if (messageSender != null){
                (messageSender as TextView).text= sender
            }
        }
    }
    //TODO recuperare utente dalle shared
    //metodo che verifica quale layout associare al cassetto che ospiterà il messaggio nella posizione $position
    //della lista di messaggi in base al tipo di messaggio (RECEIVED o SEND)
    override fun getItemViewType(position: Int): Int {
        if(messageList[position].mittente=="Alberto")
            return Messaggio.MESSAGE_SEND
        return Messaggio.MESSAGE_RECEIVED
    }

    //metodo che ritorna un nuovo view holder con layout coerente col tipo di messaggio dato. Il tipo di layout da inserire è specificato
    //dal parametro $viewType
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        var idLayout=R.layout.message_item
        if(viewType==Messaggio.MESSAGE_RECEIVED) {
            if(chat_type== PRIVATE_CHAT)
                idLayout = R.layout.message_from_item
            else
                idLayout = R.layout.message_from_item_group
        }
        val view = LayoutInflater.from(parent.context)
                .inflate(idLayout, parent, false)
        return MessageViewHolder(view, viewType)
    }

    //metodo che ritorna la lunghezza della lista di messaggi
    override fun getItemCount(): Int {
        return messageList.size
    }

    //metodo che mostra i dati di un messaggio in una certa posizione
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position].testo, messageList[position].dateTime, messageList[position].mittente)
    }
}