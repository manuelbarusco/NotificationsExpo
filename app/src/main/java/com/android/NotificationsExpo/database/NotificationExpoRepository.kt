package com.android.NotificationsExpo.database

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

class NotificationExpoRepository private constructor(context: Context) {

    private val database:NotificationExpoDatabase= NotificationExpoDatabase.getDatabase(context, CoroutineScope(SupervisorJob()))

    private val chatDao= database.chatDAO()
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

    fun getChat(): LiveData<List<ChatDAO.ChatUtente>> = chatDao.getChatUtente("Alberto")
    fun getChatMessages(idChat: Int): LiveData<List<Messaggio>> = messaggioDAO.getChatMessages(idChat)
}