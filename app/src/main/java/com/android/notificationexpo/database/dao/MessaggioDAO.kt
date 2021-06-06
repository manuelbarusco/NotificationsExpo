package com.android.notificationexpo.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.notificationexpo.database.entities.Messaggio

//interfaccia DAO per la gestione di alcune query rigurdanti la relazione MESSAGGIO e correlate

@Dao
interface MessaggioDAO {

    //query di inserimento di un nuovo messaggio nel DB
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(messaggio: Messaggio)

    //query per cancellare un dato messaggio dal DB
    @Delete
    fun delete(messaggio: Messaggio)

    //query che elenca tutti i messaggi di una chat data
    @Query("SELECT * FROM MESSAGGIO AS M WHERE M.Chat = :idChat")
    fun getChatMessages(idChat: Long):LiveData<MutableList<Messaggio>>
}