package com.android.NotificationsExpo.dummy

//classe che fornisce alla ChatActivity i messaggi della chat desiderata
class MessageDatasource(){

    fun getMessagesExample(n: Int): MutableList<Message>{
        val list= mutableListOf<Message>() //ogni elemento della lista è un messaggio, il quale ovviamente ha un testo e un tipo: 0 viene da me, 1 viene da un altro
        for(i in 0..n){
            var type=0;
            if(i%2!=0)
                type=1
            val message=Message(type,"$i")
            list.add(message)
        }
        return list
    }
}