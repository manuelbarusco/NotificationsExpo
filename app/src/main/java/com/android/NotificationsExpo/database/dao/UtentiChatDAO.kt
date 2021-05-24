package com.android.NotificationsExpo.database.dao

import android.provider.ContactsContract
import androidx.room.*
import com.android.NotificationsExpo.database.entities.UtentiChat
import com.android.NotificationsExpo.database.entities.Utente
import java.util.*

@Dao
interface UtentiChatDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(uc: UtentiChat)

    @Delete
    fun delete(uc: UtentiChat)

}

