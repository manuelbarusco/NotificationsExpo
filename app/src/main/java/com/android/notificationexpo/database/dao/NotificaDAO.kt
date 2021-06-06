package com.android.notificationexpo.database.dao

import androidx.room.*
import com.android.notificationexpo.database.entities.Notifica

//interfaccia DAO per la gestione di alcune query rigurdanti la relazione NOTIFICA e correlate

@Dao
interface NotificaDAO {

    //query di inserimento di una nuova notifica nel DB
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(notifica: Notifica)

    //query per cancellare una data notifica dal DB
    @Delete
    fun delete(notifica: Notifica)

    //query che elenca tutte le notifiche presenti nel DB
    @Query("SELECT * FROM NOTIFICA AS N")
    fun getAllNotifications(): List<Notifica>
}

