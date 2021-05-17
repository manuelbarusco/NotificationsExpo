package com.ebookfrenzy.masterdetailflow

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var buttonGoSystemsSettings : Button
    private lateinit var buttonDeleteData : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the layout
        setContentView(R.layout.settings_activity)

        // Get references to widgets
        buttonGoSystemsSettings = findViewById(R.id.button_go_system_settings)
        buttonDeleteData = findViewById(R.id.button_delete_data)

        // Set the action to be performed when the button is pressed
        buttonGoSystemsSettings.setOnClickListener { // Perform action on click
            //TODO Rimandare alle impostazioni di distema
        }

        // Set the action to be performed when the button is pressed
        buttonDeleteData.setOnClickListener {

            val myAlertDialog = AlertDialog.Builder(this)
            myAlertDialog.apply {
                setTitle("Conferma azione")
                setMessage("Sei veramente sicuro di voler eliminare tutti i dati dell'app?\n\nL'operazione è irreversibile")
                setPositiveButton("Conferma",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User clicked OK button
                    })
                setNegativeButton("Annulla",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            }


            // Create the AlertDialog
            myAlertDialog.create()
            myAlertDialog.show()

        }

    }
}