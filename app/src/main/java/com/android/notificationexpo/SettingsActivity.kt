package com.android.notificationexpo

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.android.notificationexpo.database.NotificationExpoRepository
import com.google.android.material.slider.Slider


class SettingsActivity : AppCompatActivity() {
    private val repository:NotificationExpoRepository = NotificationExpoRepository.get(this)
    private lateinit var buttonGoSystemsSettings : Button
    private lateinit var buttonDeleteData : Button
    private lateinit var slider: Slider
    private var seconds: Int = 2
    private var modified: Boolean = false
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        // Display the layout
        setContentView(R.layout.settings_activity)

        // Get references to widgets
        buttonGoSystemsSettings = findViewById(R.id.button_go_system_settings)
        buttonDeleteData = findViewById(R.id.button_delete_data)
        slider = findViewById(R.id.slider)
        slider.value = preferences.getInt(SECONDS,2).toFloat()

        slider.addOnChangeListener { _, value, _ ->
            seconds = value.toInt()
            modified = true
        }

        // Set the action to be performed when the button is pressed
        buttonGoSystemsSettings.setOnClickListener { // Perform action on click
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        }

        // Set the action to be performed when the button is pressed
        buttonDeleteData.setOnClickListener {

            val myAlertDialog = AlertDialog.Builder(this)
            myAlertDialog.apply {
                setTitle(getString(R.string.settings_alert_dialog_title))
                setMessage(getString(R.string.settings_alert_dialog_message))
                setPositiveButton(getString(R.string.settings_alert_dialog_button),
                    DialogInterface.OnClickListener { _, _ ->
                        repository.resetDatabase(context)
                    })
                setNegativeButton(getString(R.string.settings_alert_dialog_button2),
                    DialogInterface.OnClickListener { _, _ ->
                        // User cancelled the dialog
                    })
            }


            // Create the AlertDialog
            myAlertDialog.create()
            myAlertDialog.show()

        }

    }

    override fun onPause() {
        super.onPause()
        if(modified) {
            preferences.edit().putInt(SECONDS, seconds).apply()
        }
    }
    companion object{
        const val SECONDS = "seconds"
    }
}