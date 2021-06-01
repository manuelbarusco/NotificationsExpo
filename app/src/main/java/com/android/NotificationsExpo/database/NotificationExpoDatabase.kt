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

            //inserisco i vari utenti di default dell'app
            val u=Utente("Alberto", "Alberto", "Da Re", R.drawable.image_chat8) //utente predefinito
            userDAO.insert(u)
            val u1=Utente("Manuel", "Manuel", "Barusco", R.drawable.image_chat3)
            userDAO.insert(u1)
            val u2=Utente("Simone", "Gregori", "Simone", R.drawable.image_chat4)
            userDAO.insert(u2)
            val u3=Utente("Alice", "Rossi", "Alice", R.drawable.image_chat2)
            userDAO.insert(u3)
            val u4=Utente("Marco", "Verdi", "Marco", R.drawable.image_chat5)
            userDAO.insert(u4)
            val u5=Utente("Giacomo", "Giallo", "Giacomo", R.drawable.image_chat9)
            userDAO.insert(u5)
            val u6=Utente("Veronica", "Gatto", "Veronica", R.drawable.image_chat1)
            userDAO.insert(u6)
            val u7=Utente("Nicola", "Ferrari", "Nicola", R.drawable.image_chat10)
            userDAO.insert(u7)
            val u8=Utente("Francesco", "Bianchi", "Francesco", R.drawable.image_chat7)
            userDAO.insert(u8)


            //inserisco i vari tipi di notifica individuati @TODO aggiornare le descrizioni delle notifiche
            var n=Notifica("Notifica espandibile", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifiche multiple", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica conversation", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica chat bubble", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica quick actions", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica media control", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica processo in background", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica custom template", "Prova")
            notificaDAO.insert(n)
            n=Notifica("Notifica immagine", "Prova")
            notificaDAO.insert(n)

            //inserisco una chat privata tra Alberto e altri utenti per tutte le notifiche ad eccezione delle notifiche multiple
            var chat=Chat(nome = null, notificaAssociata = "Notifica espandibile", imgChat = null)
            chatDAO.insert(chat)
            var utentiChat=UtentiChat(1,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(1, u1.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica conversation", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(2,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(2, u2.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica chat bubble", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(3,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(3, u3.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica quick actions", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(4,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(4, u4.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica media control", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(5,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(5, u5.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica processo in background", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(6,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(6, u6.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica custom template", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(7,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(7, u7.nickname)
            utentiChatDAO.insert(utentiChat)

            chat=Chat(nome = null, notificaAssociata = "Notifica immagine", imgChat = null)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(8,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(8, u8.nickname)
            utentiChatDAO.insert(utentiChat)

            //aggiungo chat di gruppo
            chat=Chat(nome = "Cinema stasera?", notificaAssociata = "Notifiche multiple", imgChat = R.drawable.group)
            chatDAO.insert(chat)
            utentiChat=UtentiChat(9,u.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(9, u1.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(9, u2.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(9, u3.nickname)
            utentiChatDAO.insert(utentiChat)
            utentiChat= UtentiChat(9, u8.nickname)
            utentiChatDAO.insert(utentiChat)


            //aggiungo un messaggio di benvenuto ad ogni chat
            var msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 1, mittente = u1.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 2, mittente = u2.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 3, mittente = u3.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 4, mittente = u4.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 5, mittente = u5.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 6, mittente = u6.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 7, mittente = u7.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 8, mittente = u8.nickname)
            messaggioDAO.insert(msg)
            msg=Messaggio(testo = "Ciao Alberto", media = null, chat = 9, mittente = u1.nickname)
            messaggioDAO.insert(msg)
        }
    }

}
