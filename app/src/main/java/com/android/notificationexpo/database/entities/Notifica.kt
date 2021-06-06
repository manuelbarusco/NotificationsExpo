package com.android.notificationexpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//classe che rappresenta la relazione "NOTIFICA" all'interno del Database

@Entity(tableName = "NOTIFICA")
data class Notifica(
    @PrimaryKey
    @ColumnInfo(name = "Nome")
    val nome:String
)

