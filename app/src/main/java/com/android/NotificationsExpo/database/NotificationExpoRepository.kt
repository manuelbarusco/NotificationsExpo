package com.android.NotificationsExpo.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.android.NotificationsExpo.database.dao.ChatDAO
import com.android.NotificationsExpo.database.entities.Messaggio
import com.android.NotificationsExpo.database.entities.Utente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class NotificationExpoRepository private constructor(context: Context) {

    private val database:NotificationExpoDatabase= NotificationExpoDatabase.getDatabase(context, CoroutineScope(SupervisorJob()))
    private val chatDAO= database.chatDAO()
    private val messaggioDAO = database.messaggioDAO()
    private val notificaDAO = database.notificaDAO()
    private val utenteDAO = database.utenteDAO()
    private val utentiChatDAO = database.utentiChatDAO()

    companion object{
        private var INSTANCE: NotificationExpoRepository? = null

        fun initialize(context: Context){
            if(INSTANCE==null)
                INSTANCE= NotificationExpoRepository(context)
        }

        fun get(context: Context):NotificationExpoRepository{
            if(INSTANCE!=null)
                return INSTANCE as NotificationExpoRepository
            initialize(context)
            return INSTANCE as NotificationExpoRepository
        }
    }

    fun getChat(user: String): LiveData<List<ChatDAO.ChatUtente>> = chatDAO.getChatUtente(user)

    fun getChatMessages(idChat: Int): LiveData<MutableList<Messaggio>> = messaggioDAO.getChatMessages(idChat)

    fun getChatUtenti(chatID: Int, utentePredefinito: String): List<Utente> {
        val executor = Executors.newSingleThreadExecutor()
        var result: List<Utente> ?= null
        executor.execute{
            result=chatDAO.getChatUtenti(chatID, utentePredefinito)
        }
        //il thread viene eseguito ma aspetto la fine della sua esecuzione perchè ho bisogno dei suoi dati per proseguire nel receiver
        try {
            executor.shutdown()
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) { }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return result as List<Utente>
    }

    fun addMessage(messaggio: Messaggio){
        val executor = Executors.newSingleThreadExecutor()
        executor.execute{
            messaggioDAO.insert(messaggio)
        }
    }
}