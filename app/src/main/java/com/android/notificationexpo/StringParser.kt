package com.android.notificationexpo

//oggetto che serve a definire una serie di metodi per la formattazione e il
//parsing di Stringhe, servirà a salvare dati leggermente strutturati nelle SharedPreferences
class StringParser {
    companion object{

        //funzione che riceve una stringa formattata nel tipo Long:Long:Long
        //e ritorna una lista dei long contenuti nella string
        fun parseString(s: String): List<Long>{
            if(s=="")
                return emptyList<Long>()
            val list: List<String> =s.split(":")
            val longlist: MutableList<Long> = mutableListOf()
            for(elem in list)
                longlist.add(elem.toLong())
            return longlist
        }

        //funzione che riceve una stringa formattata nel tipo Long:Long:Long
        //e ritorna la stessa stringa aggiungendo il long specificato nei parametri
        fun addLong(s:String, l:Long):String{
            if(s=="")
                return s+l.toString()
            return s+":"+l.toString()
        }

        //funzione che riceve una stringa formattata nel tipo Long:Long:Long
        //e ritorna la stessa stringa senza il long specificato nei parametri
        fun removeLong(s:String, l:Long):String{
            if(s=="")
                return ""
            val list: List<String> =s.split(":")
            var string =""
            for(elem in list)
                if(elem != l.toString())
                    string+=elem+":"
            return string.dropLast(1)
        }

        //funzione che riceve una stringa formattata nel tipo Long:Long:Long
        //e ritorna un booleno che indica se il long specificato è contenuto nella stringa
        fun isIn(s:String, l: Long):Boolean{
            if(s=="")
                return false
            val list: List<String> =s.split(":")
            val longlist: MutableList<Long> = mutableListOf()
            for(elem in list)
                longlist.add(elem.toLong())
            return l in longlist
        }

    }
}