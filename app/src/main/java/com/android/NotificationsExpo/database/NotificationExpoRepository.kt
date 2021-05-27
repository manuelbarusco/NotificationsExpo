package com.android.NotificationsExpo.database

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.android.NotificationsExpo.database.dao.ChatDAO
import com.android.NotificationsExpo.database.dao.UtenteDAO
import com.android.NotificationsExpo.database.entities.Chat
import com.android.NotificationsExpo.database.entities.Messaggio
import com.android.NotificationsExpo.database.entities.Utente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.Executors

class NotificationExpoRepository private constructor(context: Context) {

    private val database:NotificationExpoDatabase= NotificationExpoDatabase.getDatabase(context, CoroutineScope(SupervisorJob()))
    private val chatDAO= database.chatDAO()
    private val messaggioDAO = database.messaggioDAO()
    private val notificaDAO = database.notificaDAO()
    private val utenteDAO = database.utenteDAO()
    private val utentiChatDAO = database.utentiChatDAO()
    private val executor = Executors.newSingleThreadExecutor()

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

    fun addMessage(messaggio: Messaggio){
        executor.execute{
            messaggioDAO.insert(messaggio)
        }
    }
}