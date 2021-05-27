package com.android.NotificationsExpo.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.NotificationsExpo.database.entities.Messaggio

@Dao
interface MessaggioDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(messaggio: Messaggio)

    @Delete
    fun delete(messaggio: Messaggio)

    @Query("SELECT * FROM MESSAGGIO AS M WHERE M.Chat = :idChat")
    fun getChatMessages(idChat: Int):LiveData<MutableList<Messaggio>>
}