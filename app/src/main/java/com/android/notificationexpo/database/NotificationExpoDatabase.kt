package com.android.notificationexpo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.notificationexpo.R
import com.android.notificationexpo.database.dao.*
import com.android.notificationexpo.database.entities.*
import java.util.concurrent.Executors

//classe annotata per la creazione del database Room dell'applicazione

@Database(
    entities = [Notifica::class, Chat::class, Utente::class, Messaggio::class, UtentiChat::class],
    version = 1
)
@TypeConverters(Converters::class)

abstract class NotificationExpoDatabase : RoomDatabase() {

    //fornisco all'esterno tutti i DAO precedentemente definiti

    abstract fun chatDAO(): ChatDAO
    abstract fun messaggioDAO(): MessaggioDAO
    abstract fun notificaDAO(): NotificaDAO
    abstract fun utenteDAO(): UtenteDAO
    abstract fun utentiChatDAO(): UtentiChatDAO

    //il database è un singleton: una sola istanza per l'intera app

    companion object {

        //@Volatile serve per far si che la proprietà è subito visibile a tutti i thread che la stanno "guardando"
        @Volatile
        private var INSTANCE: NotificationExpoDatabase? = null

        fun getDatabase(context: Context): NotificationExpoDatabase {
            if (INSTANCE != null)
                return INSTANCE as NotificationExpoDatabase
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NotificationExpoDatabase::class.java,
                        "NotificationExpo_database"
                ).addCallback(NotificationExpoDatabaseCallback())
                .build()
                INSTANCE = instance
                return instance
            }
        }

        //funzione che permette di resettare il DB e riportarlo al suo stato iniziale
        //ovvero quando l'app è stata appena installata
        fun resetDatabase(){
            INSTANCE?.clearAllTables()
            NotificationExpoDatabaseCallback().populateDatabase(
                    INSTANCE?.utenteDAO() as UtenteDAO,
                    INSTANCE?.chatDAO() as ChatDAO,
                    INSTANCE?.notificaDAO() as NotificaDAO,
                    INSTANCE?.messaggioDAO() as MessaggioDAO,
                    INSTANCE?.utentiChatDAO() as UtentiChatDAO
            )
        }

        //oggetto che definisce una callback, il quale ha due metodi:
        // * onCreate: metodo invocato alla prima creazione del database (al momento dell'installazione del DB. Serve per il popolamento iniziale)
        // * onOpen: metodo invocato ad ogni "apertura" del database, in questo caso non serve quindi l'implementazione è vuota
        private class NotificationExpoDatabaseCallback : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    //lancio un executor in modo da eseguire il popolamento in un thread diverso dal Main Thread
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
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

            //funzione per il popolamento iniziale del database
            fun populateDatabase(userDAO: UtenteDAO, chatDAO: ChatDAO, notificaDAO: NotificaDAO, messaggioDAO: MessaggioDAO, utentiChatDAO: UtentiChatDAO) {

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
                val u9=Utente("Francesca", "Ferrari", "Francesca", R.drawable.image_chat6)
                userDAO.insert(u9)
                val u10=Utente("Luca", "Sainz", "Luca", R.drawable.image_chat8)
                userDAO.insert(u10)


                //inserisco i vari tipi di notifica individuati
                var n=Notifica("Notifica espandibile")
                notificaDAO.insert(n)
                n=Notifica("Notifiche multiple")
                notificaDAO.insert(n)
                n=Notifica("Notifica conversation")
                notificaDAO.insert(n)
                n=Notifica("Notifica chat bubble")
                notificaDAO.insert(n)
                n=Notifica("Notifica quick actions")
                notificaDAO.insert(n)
                n=Notifica("Notifica media control")
                notificaDAO.insert(n)
                n=Notifica("Notifica processo in background")
                notificaDAO.insert(n)
                n=Notifica("Notifica custom template")
                notificaDAO.insert(n)
                n=Notifica("Notifica immagine")
                notificaDAO.insert(n)

                //inserisco una chat privata tra Alberto e altri utenti per tutte le notifiche (le chat bubble hanno in totale 3 chat per verificarne il pieno funzionamento)
                var chat=Chat(nome = null, notificaAssociata = "Notifica espandibile", imgChat = null)
                val id_chat1= chatDAO.insert(chat)
                var utentiChat=UtentiChat(id_chat1,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat1, u1.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica conversation", imgChat = null)
                val id_chat2= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat2,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat2, u2.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica chat bubble", imgChat = null)
                val id_chat3= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat3,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat3, u3.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica chat bubble", imgChat = null)
                val id_chat4= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat4,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat4, u9.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica chat bubble", imgChat = null)
                val id_chat5= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat5,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat5, u10.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica quick actions", imgChat = null)
                val id_chat6= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat6,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat6, u4.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica media control", imgChat = null)
                val id_chat7= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat7,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat7, u5.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica processo in background", imgChat = null)
                val id_chat8= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat8,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat8, u6.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica custom template", imgChat = null)
                val id_chat9= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat9,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat9, u7.nickname)
                utentiChatDAO.insert(utentiChat)

                chat=Chat(nome = null, notificaAssociata = "Notifica immagine", imgChat = null)
                val id_chat10= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat10,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat10, u8.nickname)
                utentiChatDAO.insert(utentiChat)

                //aggiungo una chat di gruppo
                chat=Chat(nome = "Cinema stasera?", notificaAssociata = "Notifiche multiple", imgChat = R.drawable.group)
                val id_chat11= chatDAO.insert(chat)
                utentiChat=UtentiChat(id_chat11,u.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat11, u1.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat11, u2.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat11, u3.nickname)
                utentiChatDAO.insert(utentiChat)
                utentiChat= UtentiChat(id_chat11, u8.nickname)
                utentiChatDAO.insert(utentiChat)


                //aggiungo un messaggio di benvenuto ad ogni chat
                var msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat1, mittente = u1.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat2, mittente = u2.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat3, mittente = u3.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat4, mittente = u4.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat5, mittente = u5.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat6, mittente = u6.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat7, mittente = u7.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat8, mittente = u8.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat9, mittente = u1.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat10, mittente = u1.nickname)
                messaggioDAO.insert(msg)
                msg=Messaggio(testo = "Ciao Alberto", media = null, chat = id_chat11, mittente = u1.nickname)
                messaggioDAO.insert(msg)
            }
        }
    }
}
