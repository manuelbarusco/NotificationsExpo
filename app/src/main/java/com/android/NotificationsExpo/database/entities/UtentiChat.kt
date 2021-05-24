package com.android.NotificationsExpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "UTENTICHAT",
    primaryKeys = ["Chat", "Utente"],
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["ID"],
            childColumns = ["Chat"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
        ), ForeignKey(
            entity = Utente::class,
            parentColumns = ["Nickname"],
            childColumns = ["Utente"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class UtentiChat(
    @ColumnInfo(name = "Chat")
    val chat: Int,

    @ColumnInfo(name = "Utente")
    val utente: String
)