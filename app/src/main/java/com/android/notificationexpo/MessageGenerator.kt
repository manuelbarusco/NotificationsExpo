package com.android.notificationexpo

import android.content.Context
import com.android.notificationexpo.database.NotificationExpoRepository
import com.android.notificationexpo.database.entities.Messaggio
import com.android.notificationexpo.database.entities.Utente
import kotlin.random.Random
import com.android.notificationexpo.receivers.AlarmManagerReceiverAlwaysOn

//oggetto che si occupa di creare e registrare nel DB i vari messsaggi generati dalla APP e che verranno visualizzati nelle notifiche
class MessageGenerator(
        private val user:String,
        private val chat_id: Long,
        private val messagesToSend: MutableList<AlarmManagerReceiverAlwaysOn.MittenteMessaggio>,
        private val context: Context
    ) {

    private var repository: NotificationExpoRepository = NotificationExpoRepository.get(context)

    //funzione che genera e aggiugne al DB un messaggio contenente un'immagine che dovrà essere inviato in una chat con notifica immagine
    fun generateImageMessage(){
        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= "Ecco la programmazione dei film di oggi", chat = chat_id, media = R.drawable.programmazione , mittente = utenti[0].nickname)
        repository.addMessage(mex1)

        //aggiungo la coppia Mittente-Messaggio alla lista dei messaggi di cui bisogna lanciare la notifica
        val mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[0], mex1)
        messagesToSend.add(mittenteMessaggio)
    }

    //funzione che genera e aggiugne al DB più messaggi che dovranno essere inviati in una chat di gruppo (con notifiche multiple)
    fun generateMultipleMessages(){
        //recupero i messaggi "standard" e ne scelgo 4 a caso
        val messages: Array<String> = context.resources.getStringArray(R.array.dummy_messages)
        val m1= Random.nextInt(0, messages.size)
        val m2= Random.nextInt(0, messages.size)
        val m3= Random.nextInt(0, messages.size)
        val m4= Random.nextInt(0, messages.size)

        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user)

        //scelgo a caso 4 mittenti per i 4 messaggi
        val u1= Random.nextInt(0, utenti.size)
        val u2= Random.nextInt(0, utenti.size)
        val u3= Random.nextInt(0, utenti.size)
        val u4= Random.nextInt(0, utenti.size)

        //genero i messaggi e li aggiungo al database
        val mex1= Messaggio(testo= messages[m1], chat = chat_id, media = null, mittente = utenti[u1].nickname)
        val mex2= Messaggio(testo= messages[m2], chat = chat_id, media = null, mittente = utenti[u2].nickname)
        val mex3= Messaggio(testo= messages[m3], chat = chat_id, media = null, mittente = utenti[u3].nickname)
        val mex4= Messaggio(testo= messages[m4], chat = chat_id, media = null, mittente = utenti[u4].nickname)
        repository.addMessage(mex1)
        repository.addMessage(mex2)
        repository.addMessage(mex3)
        repository.addMessage(mex4)

        //aggiungo le coppie Mittente-Messaggio alla lista dei messaggi di cui bisogna lanciare la notifica
        var mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[u1], mex1)
        messagesToSend.add(mittenteMessaggio)
        mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[u2], mex2)
        messagesToSend.add(mittenteMessaggio)
        mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[u3], mex3)
        messagesToSend.add(mittenteMessaggio)
        mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[u4], mex4)
        messagesToSend.add(mittenteMessaggio)
    }

    //funzione che genera e aggiugne al DB un singolo messaggio, il parametro long indica se il messaggio da generare deve essere lungo o corto
    fun generateSingleMessage(long: Boolean){
        //recupero i messaggi "standard" e ne sceglo uno a caso se il messaggio è corto, altrimenti quello lungo se il messaggio comparirà in una notifica espandibile
        val messages: Array<String> = context.resources.getStringArray(R.array.dummy_messages)
        val longMessages: Array<String> = context.resources.getStringArray(R.array.long_dummy_messages)

        val iM = if(long)
            (longMessages.indices).random()
        else
            (messages.indices).random()

        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user)

        //genero il messaggio e lo aggiungo al database
        val mex = if(long)
            Messaggio(testo= longMessages[iM], chat = chat_id, media = null, mittente = utenti[0].nickname)
        else
            Messaggio(testo= messages[iM], chat = chat_id, media = null, mittente = utenti[0].nickname)
        repository.addMessage(mex)

        //aggiungo la coppia Mittente-Messaggio alla lista dei messaggi di cui bisogna lanciare la notifica
        val mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[0], mex)
        messagesToSend.add(mittenteMessaggio)
    }

    //funzione che genera e aggiunge al DB un messaggio che dovrà essere inviato in una chat con notifica media control
    fun generateMediaControlMessage(){
        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= "Audio", chat = chat_id, media = R.raw.doowackadoo , mittente = utenti[0].nickname)
        repository.addMessage(mex1)

        //aggiungo la coppia Mittente-Messaggio alla lista dei messaggi di cui bisogna lanciare la notifica
        val mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[0], mex1)
        messagesToSend.add(mittenteMessaggio)
    }

    //funzione che genera e aggiunge al DB un messaggio che dovrà essere inviato in una chat con notifica di processo in background
    fun generateBackgroundMessage(){
        //recupero utenti della chat
        val utenti:List<Utente> = repository.getChatUtenti(chat_id, user)

        //genero il messaggio e lo aggiungo al database
        val mex1= Messaggio(testo= "Ti invio una foto", chat = chat_id, media = null , mittente = utenti[0].nickname)
        repository.addMessage(mex1)

        //aggiungo la coppia Mittente-Messaggio alla lista dei messaggi di cui bisogna lanciare la notifica
        val mittenteMessaggio= AlarmManagerReceiverAlwaysOn.MittenteMessaggio(utenti[0], mex1)
        messagesToSend.add(mittenteMessaggio)
    }
}