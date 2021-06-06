package com.android.notificationexpo.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.notificationexpo.database.entities.Utente

//interfaccia DAO per la gestione di alcune query rigurdanti la relazione UTENTE e correlate

@Dao
interface UtenteDAO {

    //query di inserimento di un nuovo utente nel DB
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(persona: Utente)

    //query per cancellare un dato utente dal DB
    @Delete
    fun delete(notifica: Utente)

    //query che elenca tutti gli utenti presenti del D
    @Query("SELECT * FROM UTENTE AS U")
    fun getAllUser(): LiveData<List<Utente>>
}