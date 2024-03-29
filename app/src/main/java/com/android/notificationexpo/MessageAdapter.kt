package com.android.notificationexpo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.notificationexpo.database.entities.Messaggio
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


//classe adapter per la recycler view dei messaggi presente nel ItemDetailFragment
class MessageAdapter(private val messageList: List<Messaggio>, private val imgChat: Int, private val chat_type: Int, private val adapterContext: Context?) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        const val PRIVATE_CHAT = 0
        const val GROUP_CHAT = 1
    }

    //classe MessageViewHolder per la gestione del singolo cassetto della RecyclerView che contiene un messaggio
    inner class MessageViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message)
        private val messageDate: TextView = itemView.findViewById(R.id.message_date)
        private var buttonPlayStop: ImageButton? = null
        private var messageSender: TextView? = null

        private var imgMessage: ImageView? = null

        private var myAudioResource: Int? = null
        private var mittente: String? = null //L'utente che ha inviato il messaggio
        private var userImageResource: Int? = null

        init{
            //recupero il tipo di messaggio, in modo da caricare il giusto layout per il cassetto
            if(viewType == Messaggio.MESSAGE_RECEIVED && chat_type == GROUP_CHAT)
                messageSender = itemView.findViewById(R.id.message_sender)
            if(viewType == Messaggio.MESSAGE_RECEIVED_IMG)
                imgMessage = itemView.findViewById(R.id.message_img)
            if(viewType == Messaggio.MESSAGE_RECEIVED_AUDIO)
                buttonPlayStop = itemView.findViewById(R.id.button_play_stop)

            buttonPlayStop?.setOnClickListener { // Perform action on click

                // Avvio il service in modalità "started" (cioè tramite intent)
                val serviceIntent = Intent(adapterContext, PlayerService::class.java) // Intent esplicito (che identifica il mio player service)
                serviceIntent.putExtra(PlayerService.PLAY_START, true)
                serviceIntent.putExtra(PlayerService.AUDIO_RESOURCE, myAudioResource)
                serviceIntent.putExtra(PlayerService.AUDIO_USER_NAME, mittente)
                serviceIntent.putExtra(PlayerService.AUDIO_USER_IMAGE_RESOURCE, userImageResource)
                // Metto un intent extra che indica l'operazione che deve essere compiuta (play).
                // E' una coppia chiave-valore (chiave: PLAY_START, valore: true)
                // PLAY_START è una costrante definita nel service stesso (una regola di buona progettazione
                // è che è il service che fornisce la lista dei comandi che è in grado di supportare). In
                // particolare (se guardi) è definita in un companion object (il posto kotlin in cui mettere
                // i const val)
                adapterContext?.startService(serviceIntent) // Questo è un metodo della classe activity

            }
        }


        fun bind(messaggio: Messaggio) {
            messageText.text = messaggio.testo

            val dateFormat: DateFormat = SimpleDateFormat("hh:mm", Locale.ITALY)
            val strDate: String = dateFormat.format(messaggio.dateTime)
            messageDate.text = strDate
            if (messageSender != null){
                (messageSender as TextView).text= messaggio.mittente
            }
            if(imgMessage != null)
                (imgMessage as ImageView).setImageResource(messaggio.media as Int)
            if(buttonPlayStop != null)
                myAudioResource=messaggio.media as Int

            mittente = messaggio.mittente
            userImageResource = imgChat

        }
    }

    //metodo che verifica quale layout associare al cassetto che ospiterà il messaggio nella posizione $position
    //della lista di messaggi in base al tipo di messaggio (RECEIVED o SEND)
    override fun getItemViewType(position: Int): Int {
        val preferences = adapterContext?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val userApp = preferences?.getString(ItemListActivity.KEY_USER,"") as String
        if(messageList[position].mittente==userApp)
            return Messaggio.MESSAGE_SEND
        else {
            if(messageList[position].media!= null) {
                val resourceType = adapterContext?.resources?.getResourceTypeName(messageList[position].media as Int)
                if (resourceType.equals("drawable")) {
                    // E' un file immagine
                    return Messaggio.MESSAGE_RECEIVED_IMG
                }
                else {
                    // Allora resourceType è "raw", quindi un file audio
                    return Messaggio.MESSAGE_RECEIVED_AUDIO
                }

            }
        }
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
        if (viewType == Messaggio.MESSAGE_RECEIVED_IMG)
            idLayout = R.layout.message_from_item_img
        if (viewType == Messaggio.MESSAGE_RECEIVED_AUDIO)
            idLayout = R.layout.message_from_item_audio
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
        holder.bind(messageList[position])
    }
}