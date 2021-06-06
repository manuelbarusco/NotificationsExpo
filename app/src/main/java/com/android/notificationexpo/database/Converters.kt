package com.android.notificationexpo.database
import androidx.room.TypeConverter
import java.util.*

//classe che serve a specificare a Room come convertire alcuni tipi particolari in valori rappresentabili del DB
//in questa applicazione il tipo Date non può essere rappresentato direttamente nel DB perchè Room non implementa il tipo DateTime
//https://developer.android.com/training/data-storage/room/referencing-data
//e quindi vengono definite due funzioni:
//* una per convertire da Date a Long, il cui valore viene poi salvato nel DB
//* una per convertire da Long a Date, il cui oggetto viene poi usato nell'applicazione

class Converters{

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}