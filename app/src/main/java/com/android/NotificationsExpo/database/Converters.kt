package com.android.NotificationsExpo.database
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class Converters{
    @TypeConverter
    fun dateFromString(date: String): Date {
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ITALY)
        return formatter.parse(date)
    }

    @TypeConverter
    fun dateToString(date:Date): String {
        return date.toString()
    }
}