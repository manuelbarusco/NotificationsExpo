package com.android.notificationexpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

//classe che rappresenta la relazione "CHAT" all'interno del Database

@Entity(
    tableName = "CHAT",
    foreignKeys = [
        ForeignKey(
            entity = Notifica::class,
            parentColumns = ["Nome"],
            childColumns = ["NotificaAssociata"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
    )]
)
data class Chat(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val ID: Int=0,

    @ColumnInfo(name = "Nome")
    val nome:String?, //può essere null in caso di chat private (e non di gruppo)

    @NotNull
    @ColumnInfo(name = "NotificaAssociata")
    val notificaAssociata: String,

    @ColumnInfo(name = "ImgChat")
    val imgChat: Int? //può valere null in caso di chat privata (non serve un immagine, si usa quella del destinatario)
)

