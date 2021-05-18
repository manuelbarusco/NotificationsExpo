package com.android.NotificationsExpo.dummy

import java.lang.IllegalArgumentException

/*
data class che serve a gestire un'istanza di messaggio nella ChatActivity
ogni messaggio viene descritto da un testo e da un intero che ne indica il tipo nel modo seguente:
MESSAGGE_SEND = 0
MESSAGE_RECEIVED = 1
come indicato nelle costanti interne della classe
*/
data class Message(val type:Int, val text: String){
    init{
        if(type!=0 && type!=1)
            throw IllegalArgumentException("Il tipo di un messaggio piò essere o 0 o 1")
    }

    object CONST{
        const val MESSAGE_SEND:Int  = 0
        const val MESSAGE_RECEIVED: Int= 1
    }
}