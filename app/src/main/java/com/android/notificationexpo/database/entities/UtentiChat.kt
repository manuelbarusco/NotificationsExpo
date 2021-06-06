package com.android.notificationexpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

//classe che rappresenta la relazione "UTENTICHAT" all'interno del Database
//questa relazione indica per ogni chat i partecipanti a quella chat
//se la chat è "privata", quindi tra due utenti, la chat sarà presente solo in due tuple nella tabella
//se la chat è "di gruppo", quindi tra più utenti, la chat sarà presente in più di due tuple nella tabella

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
    val chat: Long,

    @ColumnInfo(name = "Utente")
    val utente: String
)