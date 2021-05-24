package com.android.NotificationsExpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "NOTIFICA")
data class Notifica(
    @PrimaryKey
    @ColumnInfo(name = "Nome")
    val nome:String,

    @NotNull
    @ColumnInfo(name = "Descrizione")
    val descrizione: String
)

