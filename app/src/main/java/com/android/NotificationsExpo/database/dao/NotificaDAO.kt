package com.android.NotificationsExpo.database.dao

import androidx.room.*
import com.android.NotificationsExpo.database.entities.Notifica

@Dao
interface NotificaDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(notifica: Notifica)

    @Delete
    fun delete(notifica: Notifica)

    @Query("SELECT * FROM NOTIFICA AS N")
    fun getAllNotifications(): List<Notifica>
}

