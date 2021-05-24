package com.android.NotificationsExpo.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.NotificationsExpo.R
import com.android.NotificationsExpo.database.dao.*
import com.android.NotificationsExpo.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(
    entities = [Notifica::class, Chat::class, Utente::class, Messaggio::class, UtentiChat::class],
    version = 1
)
@TypeConverters(Converters::class)

abstract class NotificationExpoDatabase : RoomDatabase() {

    abstract fun chatDAO(): ChatDAO
    abstract fun messaggioDAO(): MessaggioDAO
    abstract fun notificaDAO(): NotificaDAO
    abstract fun utenteDAO(): UtenteDAO
    abstract fun utentiChatDAO(): UtentiChatDAO

    companion object {
        //il database è un singleton, una sola istanza per l'intera app
        //@volatile serve per far si che la proprietà è subito visibile a tutti i thread che la stanno "guardando"
        @Volatile
        private var INSTANCE: NotificationExpoDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NotificationExpoDatabase {
            if (INSTANCE != null)
                return INSTANCE as NotificationExpoDatabase
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NotificationExpoDatabase::class.java,
                        "NotificationExpo_database"
                ).addCallback(NotificationExpoDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                return instance
            }
        }
    }
    //@TODO verificare inner class
    private class NotificationExpoDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(
                        database.utenteDAO(),
                        database.chatDAO(),
                        database.notificaDAO(),
                        database.messaggioDAO(),
                        database.utentiChatDAO()
                    )
                }
            }
        }

        private fun populateDatabase(userDAO: UtenteDAO, chatDAO: ChatDAO, notificaDAO: NotificaDAO, messaggioDAO: MessaggioDAO, utentiChatDAO: UtentiChatDAO) {
            //popolamento iniziale del DB
            var u=Utente("Alberto", "Alberto", "Da Re", R.drawable.image_chat1)
            userDAO.insert(u)
            var u1=Utente("Manuel", "Manuel", "Barusco", R.drawable.image_chat2)
            userDAO.insert(u1)
            var n=Notifica("Head's up notification", "Prova")
            notificaDAO.insert(n)
            var chat=Chat(nome = null, notificaAssociata = "Head's up notification", imgChat = null)
            chatDAO.insert(chat)
            var utentiChat=UtentiChat(1,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(1, u1.nickname)
            utentiChatDAO.insert(utentiChat)
            var msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 1, mittente = u1.nickname)
            messaggioDAO.insert(msg)
        }
    }

}
