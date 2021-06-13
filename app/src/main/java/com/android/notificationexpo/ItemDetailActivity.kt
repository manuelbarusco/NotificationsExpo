package com.android.notificationexpo

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.android.notificationexpo.receivers.AlarmManagerReceiver

/*Activity che mostra il dettaglio di una specifica chat. Questa classe viene utilizzata solo se il dispositivo utilizzato è
* uno smartphone. L'interfaccia utente è su un Fragment che viene caricato nel metodo onCreate. Nei tablet il dettaglio di
* una chat è visualizzato a lato della lista delle chat disponibili.*/

class ItemDetailActivity : AppCompatActivity() {

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

    //callback chiamata quando all'activity viene associato un nuovo intent dato che l'activity è di tipo singleTop
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //imposto il nuovo intent
        setIntent(intent)
        //Aggiorno il fragment
        // potrei farlo in onResume ma non si ha la certezza che l'aggiornamento dell'intent venga effettuato
        // prima dell'onResume
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
                .replace(R.id.item_detail_container, fragment)
                .commit()
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


    override fun onPause() {
        // Disabilito il BroadcastReceiver a runtime
        this.unregisterReceiver(onShowNotification)
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(intent.action==NotificationLauncher.ACTION_SHORTCUT && !intent.getBooleanExtra(ItemDetailFragment.TWO_PANE,true)){
            val intent = Intent(this, ItemListActivity::class.java)
            startActivity(intent)
        }
    }
}