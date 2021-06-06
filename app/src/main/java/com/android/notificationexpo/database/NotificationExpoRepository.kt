package com.android.notificationexpo.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.android.notificationexpo.database.dao.ChatDAO
import com.android.notificationexpo.database.entities.Messaggio
import com.android.notificationexpo.database.entities.Utente
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//classe Repository che gestisce l'accesso ai dati dell'applicazione
//in questo caso la repository gestisce solo l'accesso unificato al DB dell'app
//ma in caso di "estensione" ad un app di messaggistica vera e propria
//questo oggetto potrebbe gestire sia l'accesso al DB locale del dispositivo
//sia a DB remoti per la gestione del servizio

class NotificationExpoRepository (val context: Context) {

    //recupero ogni DAO definito

    private val database:NotificationExpoDatabase= NotificationExpoDatabase.getDatabase(context)
    private val chatDAO= database.chatDAO()
    private val messaggioDAO = database.messaggioDAO()
    private val notificaDAO = database.notificaDAO()
    private val utenteDAO = database.utenteDAO()
    private val utentiChatDAO = database.utentiChatDAO()

    //la repository è un singleton: una sola istanza per l'intera app
    companion object{
        private var INSTANCE: NotificationExpoRepository? = null

        //funzione per la creazione della repository in caso non sia già stata inizializzata
        fun initialize(context: Context){
            if(INSTANCE==null)
                INSTANCE= NotificationExpoRepository(context)
        }

        //funzione per ottenere una reference alla repository
        fun get(context: Context):NotificationExpoRepository{
            if(INSTANCE!=null)
                return INSTANCE as NotificationExpoRepository
            initialize(context)
            return INSTANCE as NotificationExpoRepository
        }
    }

    //funzione per ottenere tutte le chat di un dato utente avente il nickname fornito
    //il return è di tipo LiveData in modo da poter eseguire la query in un thread secondario al main thread e
    //poter "osservare" i dati attraverso il pattern Observer
    fun getChat(user: String): LiveData<List<ChatDAO.ChatUtente>> = chatDAO.getChatUtente(user)

    //funzione per ottenere tutti i messaggi di una data chat avente l'ID fornito
    //il return è di tipo LiveData in modo da poter eseguire la query in un thread secondario al main thread e
    //poter "osservare" i dati attraverso il pattern Observer
    fun getChatMessages(idChat: Long): LiveData<MutableList<Messaggio>> = messaggioDAO.getChatMessages(idChat)

    //funzione per ottenere tutti i gli utenti partecipanti di una data chat avente l'ID fornito
    //la query viene eseguita in una thread secondario, il quale viene "aspettato" al fine di poter continuare l'esecuzione
    fun getChatUtenti(chatID: Long, utentePredefinito: String): List<Utente> {
        val executor = Executors.newSingleThreadExecutor()
        var result: List<Utente> ?= null
        executor.execute{
            result=chatDAO.getChatUtenti(chatID, utentePredefinito)
        }
        //il thread viene eseguito ma aspetto la fine della sua esecuzione perchè ho bisogno dei suoi dati per proseguire
        try {
            executor.shutdown()
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                //aspetto la fine dell'esecuzione del thread
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return result as List<Utente>
    }

    //funzione per aggiungere un dato messaggio al DB
    //la query viene eseguita su un thread separato dal main thread
    fun addMessage(messaggio: Messaggio){
        val executor = Executors.newSingleThreadExecutor()
        executor.execute{
            messaggioDAO.insert(messaggio)
        }
    }

    //funzione per il reset del DB
    fun resetDatabase(){
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            NotificationExpoDatabase.resetDatabase()
        }
    }
}