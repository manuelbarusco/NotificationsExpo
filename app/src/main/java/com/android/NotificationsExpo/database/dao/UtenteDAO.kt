package com.android.NotificationsExpo.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.NotificationsExpo.database.entities.Utente

@Dao
interface UtenteDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(persona: Utente)

    @Delete
    fun delete(notifica: Utente)

    @Query("SELECT * FROM UTENTE AS U")
    fun getAllUser(): LiveData<List<Utente>>
}