package com.android.NotificationsExpo.database
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class Converters{
    //room non implementa il tipo DateTime https://developer.android.com/training/data-storage/room/referencing-data
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}