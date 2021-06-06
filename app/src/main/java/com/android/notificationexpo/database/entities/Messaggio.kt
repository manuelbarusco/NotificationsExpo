package com.android.notificationexpo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.*

//classe che rappresenta la relazione "MESSAGGIO" all'interno del Database

@Entity(
    tableName = "MESSAGGIO",
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
                childColumns = ["Mittente"],
                onDelete = ForeignKey.NO_ACTION,
                onUpdate = ForeignKey.CASCADE
            )
    ]
)
data class Messaggio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val ID: Long=0,

    @NotNull
    @ColumnInfo(name = "Testo")
    val testo:String,

    @ColumnInfo(name = "Media")
    val media: Int?, //può valere null in caso il messaggio non contenga dei media

    @NotNull
    @ColumnInfo(name = "Chat")
    val chat: Long, //chat di appartenenza del messaggio

    @NotNull
    @ColumnInfo(name = "Mittente")
    val mittente: String, //mittente del messaggio

    @NotNull
    @ColumnInfo(name = "DateTime")
    val dateTime: Date = Date() //data di invio del messaggio
){
    //costanti che servono per definire il tipo di messaggio all'interno dell'applicazione
    companion object{
        const val MESSAGE_SEND:Int  = 0
        const val MESSAGE_RECEIVED: Int = 1
        const val MESSAGE_RECEIVED_IMG: Int = 2
        const val MESSAGE_RECEIVED_AUDIO: Int = 3
    }
}

