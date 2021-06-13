package com.android.notificationexpo

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.notificationexpo.receivers.AlarmManagerReceiver

/*Classe identica a ItemDetailActivity, serve per gestire il fatto che se espando una Chat Bubble (quindi creo un instanza
* di ItemDetailActivity) poi tocco una qualsiasi altra notifica che ha un intent che lancia ItemDetailActivity Android esegue
* onRestart() -> onStart() -> onResume() di ItemDetailActivity senza passare per onCreate(). Quindi, visto che in onCreate()
* c'è il codice necessario per visualizzare il fragment corretto (riga 51-68), al tocco della notifica viene visualizzata
* sempre l'Activity ridimensionata collegata alla Bubble. Usando un'Activity con un nome diverso, che viene invocata solo se si
* espande una Bubble, il problema viene risolto.
* Serve anche perché altrimenti lo shortcut lancerebbe l'activity ridimensionata creata se si espansa una bubble TODO: sistemare*/

class BubbleActivity: AppCompatActivity() {
    // Broadcast receiver
    // Lo scopo di questo boradcast receiver, che viene chiamto sicuramente prima di
    // AlarmManagerReceiverAlwaysOn (in quanto si usano gli ordered broadcast) è quello di
    // intercettare il brodcast e usare il parametro resultCode per avvisare AlarmManagerReceiverAlwaysOn,
    // che verrà chiamato appena dopo, di non visualizzare le notifiche (in quanto ad app aperta si
    // è scelto di non visualizzare le notifiche)

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Se viene eseguito questo codice vuol dire che l'activity è in foreground e quindi va
            // disabilitata la visualizzazione delle notifiche
            resultCode = Activity.RESULT_CANCELED // Cambia il result code di questo ordered broadcast
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        //mostro la action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*savedInstanceState è non null quando c'è un fragment già creato da una configurazione precedente di ItemDetailActivity
      * (per esempio quando si ruota il dispositivo). In questo caso il fragment verrà automaticamente riaggiunto nel
      *  FrameLayout (con id item_detail_container) perciò non è nessario aggiungerlo manualmente*/

        if (savedInstanceState == null) {
            // Creo il fragment del dettaglio e lo aggiungo all'Activity
            // usando una fragment transaction.
            val fragment = ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    //passo al ItemDetailFragment le informazioni di cui ha bisogno
                    putLong(ItemDetailFragment.CHAT_ID, intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1))
                    putString(ItemDetailFragment.CHAT_NAME, intent.getStringExtra(ItemDetailFragment.CHAT_NAME))
                    putInt(ItemDetailFragment.CHAT_IMG, intent.getIntExtra(ItemDetailFragment.CHAT_IMG, -1))
                    putString(ItemDetailFragment.NOTIFICATION,intent.getStringExtra(ItemDetailFragment.NOTIFICATION))
                    putBoolean(ItemDetailFragment.TWO_PANE,intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,false))
                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    //Questo ID rappresenta il pulsante Home
                    navigateUpTo(Intent(this, ItemListActivity::class.java))
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onResume() {
        super.onResume()
        // Abilito il BroadcastReceiver a runtime
        val filter = IntentFilter(AlarmManagerReceiver.ACTION_SHOW_NOTIFICATION)
        this.registerReceiver(onShowNotification, filter, AlarmManagerReceiver.PERM_PRIVATE, null)
    }

    override fun onStart() {
        super.onStart()
        Log.d("BubbleActivity","${intent.getLongExtra(ItemDetailFragment.CHAT_ID,-1)}")
    }
    override fun onPause() {
        // Disabilito il BroadcastReceiver a runtime
        this.unregisterReceiver(onShowNotification)
        super.onPause()
    }

}