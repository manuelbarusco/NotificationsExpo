package com.android.notificationexpo

import android.app.*
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.android.notificationexpo.receivers.AudioNotificationReceiver


class PlayerService : Service()
{
    private var myPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null
    private var mediaStyle: Notification.MediaStyle? = null
    private var isPlaying = false
    private var myAudioResource: Int? = null
    private var audioUserName: String? = null // Il nome dell'utente che ha inviato l'audio
    private var audioUserImageResource: Int? = null // La risorsa contenente l'immagine dell'utente che ha inviato l'audio


    val callback = object: MediaSession.Callback() {
        // Per interpretare il cambiamento di posizione nella barra di socrrimento della notifica

        override fun onSeekTo(pos: Long) {
            // Aggiorno il player
            myPlayer?.seekTo(pos.toInt())

            if (isPlaying==true)
            {
                // Aggiorno la barra di scorrimento
                val playbackSpeed: Float = 1.0f
                mediaSession?.setPlaybackState(
                    PlaybackState.Builder()
                        .setState(
                                PlaybackState.STATE_PLAYING,
                                myPlayer!!.currentPosition.toLong(), // Playback position
                                playbackSpeed
                        )
                        .setActions(PlaybackState.ACTION_SEEK_TO)  // Specificare il parametro SEEK_TO abilita lo scorrimento del curosore della barra di avanzamento. Se questo parametro non viene fornito il cursore sarà disabilitato ma potrà comunque essere visualizzata la barra di scorrimento
                        .build()
                )
            }
            else{
                // Aggiorno la barra di scorrimento
                val playbackSpeed: Float = 1.0f
                mediaSession?.setPlaybackState(
                    PlaybackState.Builder()
                        .setState(
                                PlaybackState.STATE_PAUSED,
                                myPlayer!!.currentPosition.toLong(),// Playback position
                                playbackSpeed
                        )
                        .setActions(PlaybackState.ACTION_SEEK_TO)  // Specificare il parametro SEEK_TO abilita lo scorrimento del curosore della barra di avanzamento. Se questo parametro non viene fornito il cursore sarà disabilitato ma potrà comunque essere visualizzata la barra di scorrimento
                        .build()
                )
            }

        }
    }


    override fun onBind(intent: Intent): IBinder?
    {
        // E' uno dei metodi che il service deve implementare

        // Con questo service non supporto il binding quindi rispondo null
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        // E' uno dei metodi che il nostro service deve implementare

        // Chiamato quando un altro componente avvia il service in modalità "started" (con il metodo startService)

        // Se l'intent che mi è arrivato contiene la chiave PLAY_START ed è associata al valore true
        // allora avvio la riproduzione della musica
        if (intent.getBooleanExtra(PLAY_START, false)){
            myAudioResource=intent.getIntExtra(AUDIO_RESOURCE,0)
            audioUserName=intent.getStringExtra(AUDIO_USER_NAME)
            audioUserImageResource=intent.getIntExtra(AUDIO_USER_IMAGE_RESOURCE,0)
            startPlayer()
        }

        if (intent.getBooleanExtra(PLAY_PAUSE, false)==true) {
            // E' possibile finire qui se il BoradcastReceiver associato a uno dei pulsanti della notifica ha impostato il relativo Intent extra
            playPause()
        }

        if (intent.getBooleanExtra(PLAY_STOP, false)==true) {
            // E' possibile finire qui se il BoradcastReceiver associato a uno dei pulsanti della notifica ha impostato il relativo Intent extra
            stopSelf()
        }

        // Il parmaetro ritornato indica come Android deve gestire questo service
        return START_STICKY //Avvio in modalità START_STICKY
    }

    override fun onCreate()
    {
        super.onCreate()
        // E' uno dei metodi che il nostro service deve implementare

        // Chiamato quando il sistema crea il service

        // Crea il NotificationChannel se API level è >= 26
        // Il frammento di codice all'interno dell'if è quello che prepara la notifica da mostrare
        // quando la riporoduzione del brano è in corso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_audio_notifications_name) // Nome del canale
            val description = getString(R.string.channel_audio_notifications_name_description) // Descrizione del canale
            val importance = NotificationManager.IMPORTANCE_LOW // L'importanza del canale
            val channel = NotificationChannel(CHANNEL_ID, name, importance) // CHANNEL_ID è specificato nel companion object in fondo a questo file
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java // Ottengo un istanza di NotificationManager
            )
            notificationManager.createNotificationChannel(channel) // Creo il canale con i prametri specificati

        }

    }

    private fun startPlayer()
    {
        // Metodo specifico per questo service

        if (isPlaying==true){
            myPlayer?.stop() // Fermo il Player (per avviarlo poi con la nuova musica)
        }
        isPlaying = true



        // AVVIO LA RIPRODUZIONE DELLA MUSICA

        // Musica scaricata da "Public Domain 4U"
        // https://publicdomain4u.com/paul-whiteman-orchestra-doo-wacka-doo-mp3-download
        myPlayer = MediaPlayer.create(this, myAudioResource as Int) // myAudioResource contiene l'id della risorsa audio
        myPlayer!!.isLooping = false // Per non far ricominciare l'audio al termine
        // myPlayer utilizza il parametro PARTIAL_WAKE_LOCK per assicurarsi che la CPU non vada in standby
        // durante la riproduzione della musica
        myPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        // Nota: Uso applicationContext e non il context dell'activity perchè l'activity potrebbe anche
        // essere terminata mentre è in corso la riproduzione della musica
        myPlayer?.setOnCompletionListener(OnCompletionListener {
            // Termino il service al termine del file audio
            stopSelf()
        })

        myPlayer!!.start()



        // VISUALIZZO LA NOTIFICA

        // Creo la media session
        mediaSession = MediaSession(this, "PlayerService")

        // Imposto i dettagli del brano
        var audioMilliseconds:Long = myPlayer?.duration?.toLong() as Long
        mediaSession?.setMetadata(
            MediaMetadata.Builder()

            // Titolo
            .putString(MediaMetadata.METADATA_KEY_TITLE, getString(R.string.message_audio_received))

            // Artista
            .putString(MediaMetadata.METADATA_KEY_ARTIST, audioUserName)

            // Immagine dell'album
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, "android.resource://" + BuildConfig.APPLICATION_ID + "/drawable/" + audioUserImageResource)

            // Durata
            // Se la durata non è impostata la barra di avanzamento sarà sostituita da una linea continua
            .putLong(MediaMetadata.METADATA_KEY_DURATION, audioMilliseconds)

            .build()
        )

        // Imposto la stato della riproduzione
        val playbackSpeed: Float = 1.0f // Velcoità normale
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setState(
                        PlaybackState.STATE_PLAYING,

                        // Posizione corrente del player multimediale (usata per sincronizzare la barra di avanzamento)
                        myPlayer!!.currentPosition.toLong(),

                        // Velocità di riproduzione
                        playbackSpeed
                )

                    // Il parametro SEEK_TO serve a specificare che è possibile usare il cursore della
                    // barra di avanzamento per spostarsi all'internod del brano
                    // Se il parametro non viene impostato la barra di avanzamento potrà essere mostrata
                    // ma senza il cursore
                    .setActions(PlaybackState.ACTION_SEEK_TO)
                    .build()
        )

        mediaSession!!.setCallback(callback)

        // Creo un oggetto MediaStyle e passo il token dell'oggetto di tipo MediaSession
        mediaStyle = Notification.MediaStyle()
        mediaStyle?.setMediaSession(mediaSession?.sessionToken)

        mediaStyle?.setShowActionsInCompactView(0,1) // Questo serve  per visualizzare il puslante pause/play definito dopo nella visualizzazione compatta


        // Preparo l'intent per il Broadcast receiver del pulsante di pausa/play
        val audioPausePlayIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
        audioPausePlayIntent.action = AudioNotificationReceiver.RESUME_ACTION
        val audioPausePlaypendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioPausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Preparo l'intent per il Broadcast receiver del pulsante di stop
        val audioStopIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
        audioStopIntent.action = AudioNotificationReceiver.STOP_ACTION
        val audioStopPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioStopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Creo la notifica
        val notification2 = Notification.Builder(this@PlayerService, CHANNEL_ID)
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_pause_filled),
                    "Pause",
                    audioPausePlaypendingIntent)
                    .build()
                )
                .addAction(Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_stop_filled),
                    "Stop",
                    audioStopPendingIntent)
                    .build()
                )
                .build()


        // Utilizzo il metodo startForeground per indicare all'OS che terminare il service avrebbe un
        // effetto negativo sull'utente. Devo passare una notifica che verrà mostrata fintanto che il
        // service sta eseguendo il suo lavoro.
        // E' necessaria la relativa permission nel file manifest
        val notificationID = MEDIA_PLAYER_NOTIFICATION_ID // Id per la notifica
        startForeground(notificationID, notification2)

        // Nota: Questa notifica non è rimovibile. E' vero che è possibile fare uno swipe
        // per "toglierla" ma in realtà continua a essere visualizzata se si apre del tutto il menu
        // a tendina delle impostazioni rapide

    }

    private fun playPause() {
        if (isPlaying) {

            isPlaying = false

            // Metto in pausa il player
            myPlayer?.pause()

            // Metto in pausa la media session
            val playbackSpeed: Float = 1.0f
            mediaSession?.setPlaybackState(
                PlaybackState.Builder()
                    .setState(
                            PlaybackState.STATE_PAUSED,

                            myPlayer!!.currentPosition.toLong(),

                            playbackSpeed
                    )

                .setActions(PlaybackState.ACTION_SEEK_TO)
                .build()
            )

            // Visualizzo pulsanti (ricreo la notifica ma uso lo stesso id)
            val audioPausePlayIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
            audioPausePlayIntent.action = AudioNotificationReceiver.RESUME_ACTION
            val audioPausePlayPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioPausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val audioStopIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
            audioStopIntent.action = AudioNotificationReceiver.STOP_ACTION
            val audioStopPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioStopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification2 = Notification.Builder(this@PlayerService, CHANNEL_ID)
                    .setStyle(mediaStyle)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .addAction(Notification.Action.Builder(
                            Icon.createWithResource(this, R.drawable.ic_play_filled),
                            "Pause",
                            audioPausePlayPendingIntent)
                            .build()
                    )
                    .addAction(Notification.Action.Builder(
                        Icon.createWithResource(this, R.drawable.ic_stop_filled),
                        "Stop",
                        audioStopPendingIntent)
                        .build()
                    )
                    .build()

            val notificationID = MEDIA_PLAYER_NOTIFICATION_ID
            startForeground(notificationID, notification2)

        }
        else
        {
            isPlaying = true

            myPlayer?.start() // Riprendo la riproduzione (non ricomincio dall'inizio)

            // Metto in play la media session
            val playbackSpeed: Float = 1.0f
            mediaSession?.setPlaybackState(
                    PlaybackState.Builder()
                            .setState(
                                    PlaybackState.STATE_PLAYING,

                                    myPlayer!!.currentPosition.toLong(),

                                    playbackSpeed
                            )

                            .setActions(PlaybackState.ACTION_SEEK_TO)
                            .build()
            )

            // Ricreo pulsanti (ricreo la notifica ma uso lo stesso id)
            val audioPausePlayIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
            audioPausePlayIntent.action = AudioNotificationReceiver.RESUME_ACTION
            val audioPausePlayPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioPausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val audioStopIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
            audioStopIntent.action = AudioNotificationReceiver.STOP_ACTION
            val audioStopPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioStopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification2 = Notification.Builder(this@PlayerService, CHANNEL_ID)
                    .setStyle(mediaStyle)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .addAction(Notification.Action.Builder(
                            Icon.createWithResource(this, R.drawable.ic_pause_filled),
                            "Pause",
                            audioPausePlayPendingIntent)
                            .build()
                    )
                    .addAction(Notification.Action.Builder(
                        Icon.createWithResource(this, R.drawable.ic_stop_filled),
                        "Stop",
                        audioStopPendingIntent)
                        .build()
                    )
                    .build()

            val notificationID = MEDIA_PLAYER_NOTIFICATION_ID
            startForeground(notificationID, notification2)

        }
    }

    private fun stop() // Chiamato nel metodo onDestroy()
    {
        if (isPlaying) {
            isPlaying = false
            myPlayer?.release() // Rilascio l'oggetto MediaPLayer
            myPlayer = null
            mediaSession?.release()
            stopForeground(true) // Indico che il service non ha più importanza per l'utente
        }
    }

    override fun onDestroy()
    {
        // Chiamato quando l'utente preme il pulsante stop della notifica

        stop() // Interrompo la musica
        super.onDestroy()
    }

    companion object
    {
        private const val CHANNEL_ID = "simplebgplayer"
        const val PLAY_START = "BGPlayStart"
        const val PLAY_STOP = "BGPlayStop"
        const val PLAY_PAUSE = "action_pause"

        const val AUDIO_RESOURCE = "action_resource"
        const val AUDIO_USER_NAME = "action_user_name"
        const val AUDIO_USER_IMAGE_RESOURCE = "action_user_image_resource"

        const val MEDIA_PLAYER_NOTIFICATION_ID = 1
    }
}