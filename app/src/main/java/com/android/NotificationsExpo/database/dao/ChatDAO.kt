package com.android.NotificationsExpo.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.NotificationsExpo.database.entities.Chat
import com.android.NotificationsExpo.database.entities.Utente
import java.util.*

@Dao
interface ChatDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(chat: Chat)

    @Delete
    fun delete(chat: Chat)

    //query che elenca le chat di un utente in ordine cronologico, la query è formata dall'unione di due query, la prima che elenca le chat
    //private dell'utente e la seconda che elenca le chat di gruppo in cui l'utente partecipa
    @Query(
            "SELECT * "+
            "FROM ( "+
                    "SELECT C.ID AS idChat, U.Nickname AS nomeChat, C.NotificaAssociata AS notificaAssociata, U.ImgProfilo AS imgChat, MAX(M.DateTime) as lastMessageDateTime " +
                    "FROM (SELECT * FROM MESSAGGIO AS M ORDER BY M.DateTime DESC) AS M JOIN CHAT AS C ON M.Chat=C.ID " +
                    "JOIN UTENTICHAT AS UC ON UC.Chat=C.ID "+
                    "JOIN UTENTE AS U ON U.Nickname=UC.Utente "+
                    "WHERE UC.Utente!= :uNickname AND C.ImgChat IS NULL AND C.ID IN "+
                                                                                    "(SELECT C.ID " +
                                                                                    "FROM UTENTICHAT AS UC JOIN CHAT AS C ON UC.Chat=C.ID "+
                                                                                    "WHERE UC.Utente=:uNickname) " +
                    "GROUP BY C.ID, U.Nickname, C.NotificaAssociata, U.ImgProfilo "+

                    "UNION " +

                    "SELECT C.ID AS idChat, C.Nome AS nomeChat, C.NotificaAssociata AS notificaAssociata, C.ImgChat AS imgChat, MAX(M.DateTime) as lastMessageDateTime " +
                    "FROM (SELECT * FROM MESSAGGIO AS M ORDER BY M.DateTime DESC) AS M JOIN CHAT AS C ON M.Chat=C.ID " +
                    "JOIN UTENTICHAT AS UC ON UC.Chat=C.ID " +
                    "JOIN UTENTE AS U ON U.Nickname=UC.Utente " +
                    "WHERE UC.Utente!= :uNickname AND C.ImgChat IS NOT NULL AND C.ID IN " +
                                                                                    "(SELECT C.ID " +
                                                                                    "FROM UTENTICHAT AS UC JOIN CHAT AS C ON UC.Chat=C.ID " +
                                                                                    "WHERE UC.Utente=:uNickname) " +
                    "GROUP BY C.ID, C.Nome, C.NotificaAssociata, C.ImgChat " +
            ")"+
                    "ORDER BY lastMessageDatetime DESC"
    )
    fun getChatUtente(uNickname: String): LiveData<List<ChatUtente>>

    data class ChatUtente(
            val idChat: Int,
            val nomeChat:String,
            val notificaAssociata: String,
            val imgChat: Int,
            val lastMessageDateTime: Date
    )

    @Query("SELECT  U.*  FROM UTENTICHAT AS UC  JOIN UTENTE AS U ON UC.Utente=U.Nickname WHERE UC.Chat=:chatID AND UC.Utente!=:utentePredefinito")
    fun getChatUtenti(chatID: Int, utentePredefinito: String): List<Utente>
}

