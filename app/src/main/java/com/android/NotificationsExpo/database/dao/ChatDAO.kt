package com.android.NotificationsExpo.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.NotificationsExpo.database.entities.Chat

@Dao
interface ChatDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(chat: Chat)

    @Delete
    fun delete(chat: Chat)

    //query che elenca le chat di un utente in ordine cronologico
    @Query(
        "SELECT DISTINCT C.ID AS idChat, C.Nome AS nomeChat, C.NotificaAssociata AS notificaAssociata, C.ImgChat AS imgChatGruppo , U.Nickname AS nomeChatPrivata, U.ImgProfilo AS imgChatPrivata " +
                "FROM UTENTICHAT AS UC JOIN CHAT AS C ON UC.Chat=C.ID "+
                "JOIN MESSAGGIO AS M ON M.Chat=C.ID "+
                "JOIN UTENTE AS U ON U.Nickname=UC.Utente "+
                "WHERE UC.Utente!= :uNickname AND C.ID IN "+
                                                "(SELECT C.ID " +
                                                "FROM UTENTICHAT AS UC JOIN CHAT AS C ON UC.Chat=C.ID "+
                                                "WHERE UC.Utente=:uNickname) "+
                "ORDER BY M.DateTime"
    )
    fun getChatUtente(uNickname: String): LiveData<List<ChatUtente>>

    data class ChatUtente(
            val idChat: Int,
            val nomeChat:String?,
            val notificaAssociata: String,
            val imgChatGruppo: Int?,
            val nomeChatPrivata: String,
            val imgChatPrivata:Int
    )

}

