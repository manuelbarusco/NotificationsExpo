package com.android.NotificationsExpo

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService

// Questo BroadcastReceiver entrerà in funzione anche quando l'app non è in eseguzione in quanto viene
// registrato direttamente nel manifest

// Il compito di questo BroadcastReceiver è:
// - Inserire il messaggio del Database
// - Visualizzare la relativa notifica (a meno che prima non sia intervenuto il BoradcastReceiver
//   dell'app in foreground per chiedere che questa operazione non venga eseguita; questo comportamento
//   è possibile in quanto questo è un Ordered Broadcast e quindi, se è presente il BroadcastReceiver
//   dell'app in foreground, viene garantita l'eseguzione di quel BoradcastReceiver prima di questo)

class AlarmManagerReceiverAlwaysOn: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("MyReceiver_ALWAYSON","${System.currentTimeMillis().toString()} ${intent?.action}") //Mostriamo una riga con un testo sempre nuovo (altrimenti Logcat scrive che ci sono altre n righe simili)



        // TODO Generare un messaggio e scriverlo nel Database (l'UI verrà aggiornata tramite LiveData)
        // Devo generare uno o più messaggi sulla base del tipo di notifica associata alla chat (ad
        // esempio per una notifica conversation genererò 10 messaggi)
        val notificationType:String = intent.getStringExtra("notificationType") as String
        Log.d("MyR_Tipo di notifica: ", notificationType)




        // Se è intervenuto prima l'altro BroadcastReceiver non mostro nemmeno la notifica
        if (resultCode != Activity.RESULT_OK){
            // Vuol dire che un activity è in foreground. Non mostrare le neotifiche
            Log.d("MyR", "Eravamo in foreground")
            return
        }
        Log.d("MyR", "Eravamo in background")
         // ottengo eventuali valori dall'intent extra

        // TODO Mostro la notifica

        //TODO: selezionare tipo di notifica

        //******************* CHAT BUBBLE E CONVERSATION *************
        val target = Intent(context, ItemDetailActivity::class.java)
                .setAction("bubble")
        val bubbleIntent = PendingIntent.getActivity(context, 0, target, 0)

        /*Oggetto Person può essere riutilizzato su altre API per una migliore integrazione
        * setImortant per indicare gli utenti che interagiscono frequentemente con l'utente*/
        //TODO: Riempire con i dati del mittente reale (testo e immagini)
        val person = Person.Builder()
                .setName("Luca")
                .setImportant(true)
                .build()

        /*Creo un conversation shortcut.
        * Per lanciare una conversation notification e/o una chat bubble è necessario assiociare alla notifica un
        * conversation shortcut che è uno shortcut (https://developer.android.com/guide/topics/ui/shortcuts) con
        * particolari metadati e proprietà che servono per scatenare le conversation notification e le chat bubbles.
        * Ad uno shortcut "normale" bisogna aggiungere un Icon, un intent (che lancia una activity quando si usa lo
        * shortcut) e deve essere LongLived (così il S.O. lo memorizza nella cache per poterlo usare anche per altre
        * features delle conversazioni). Inoltre è consigliato (non obbligatorio) impostare anche un oggetto Person
        * che serve al S.O. per "conoscere" di più del contesto della conversazione ed utile ad es. per classificare
        * gli shortcut e per fornire suggerimenti per la condivisione.*/
        val shortcut = ShortcutInfo.Builder(context, conversationShortcutID)
                .setLongLived(true)
                .setIcon(Icon.createWithResource(context,R.drawable.image_chat1))
                .setShortLabel("Short label")
                .setLongLabel("Long label")
                .setIntent(target)
                .setPerson(person)
                .build()

        /*pubblico lo shortcut come dinamico tramite pushDynamicShortcut che in automatico gestisce il limite
        * imposto da android degli shortcuts che si possono pubblicare e permette aggiornare uno shortcut se ha lo stesso id*/
        val shortcutManager: ShortcutManager = context.getSystemService(AppCompatActivity.SHORTCUT_SERVICE) as ShortcutManager
        shortcutManager.pushDynamicShortcut(shortcut)

        /*val bubbleData = Notification.BubbleMetadata.Builder(bubbleIntent,
                Icon.createWithResource(context, R.drawable.image_chat1))
                .setDesiredHeight(600)
                .setAutoExpandBubble(false)
                .setSuppressNotification(false)
                .build()*/

        //Invece di fare come sopra (commento) posso riutilizzare i dati dello shortcut passando il suo id
        /*setDesiredHeight(600) --> imposta la lunghezza della bubble espansa (in dp)
        * setAutoExpandBubble(true) --> bubble espansa in automatico (solo se l'app è in foreground) default = false
        * .setSuppressNotification()--> imposta se la bubble verrà pubblicato senza la notifica associata nell'area apposita default = false.*/

        val bubbleData = Notification.BubbleMetadata.Builder(conversationShortcutID)
                .setDesiredHeight(600)
                .setAutoExpandBubble(false)
                .setSuppressNotification(false)
                .build()

        /*NOTA setSuppressBubble -->Indicates whether the bubble should be visually suppressed from the bubble stack if the user is viewing
         the same content outside of the bubble. For example, the user has a bubble with Alice and then opens up the main
         app and navigates to Alice's page. To match the activity and the bubble notification, the bubble notification
         should have a locus id set that matches a locus id set on the */

        //Secondo parametro è di tipo Long e serve per la data del messaggio nella notifica
        var message1 = Notification.MessagingStyle.Message("messaggio 1",
                System.currentTimeMillis(),
                person)

        var message2 = Notification.MessagingStyle.Message("messaggio 2 vecchio",
                System.currentTimeMillis()-5*1000,
                person)

        /*Per fare in modo che una notifica sia una notifica conversation serve impostare anche un MessagingSyle*/
        val notification = Notification.Builder(context, ItemListActivity.BUBBLES)
                .setContentTitle("Nuovo messaggio da ${person.name.toString()}")   //probabilmete non serve a niente
                .setBubbleMetadata(bubbleData)
                .addPerson(person)
                .setSmallIcon(R.drawable.ic_launcher_foreground) //TODO: mettere immagine migliore
                .setStyle(Notification.MessagingStyle(person)
                        .addMessage(message1)
                        .addHistoricMessage(message2)    //non succede niente (?)
                )
                .setShortcutId(conversationShortcutID)
                .build()


        val notificationManager: NotificationManager? = context.getSystemService()

        if (notificationManager != null) {
            notificationManager.notify(100,notification)
        }
    }

    companion object{
        const val conversationShortcutID = "shortcut1"
    }
}