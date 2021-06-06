package com.android.notificationexpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

//classe che rappresenta la relazione "UTENTE" all'interno del Database

@Entity(tableName = "UTENTE")
data class Utente(
    @PrimaryKey
    @ColumnInfo(name = "Nickname")
    val nickname: String,

    @NotNull
    @ColumnInfo(name = "Nome")
    val nome:String,

    @NotNull
    @ColumnInfo(name = "Cognome")
    val cognome: String,

    @NotNull
    @ColumnInfo(name = "ImgProfilo")
    val imgProfilo: Int
)
