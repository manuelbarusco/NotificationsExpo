package com.android.notificationexpo.database.dao

import androidx.room.*
import com.android.notificationexpo.database.entities.UtentiChat

//interfaccia DAO per la gestione di alcune query rigurdanti la relazione UTENTICHAT e correlate

@Dao
interface UtentiChatDAO {

    //query di inserimento di una nuova coppia utente-chat nel DB
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(uc: UtentiChat)

    //query per cancellare una data coppia utente-chat dal DB
    @Delete
    fun delete(uc: UtentiChat)

}

