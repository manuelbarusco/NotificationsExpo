package com.android.NotificationsExpo

//import android.support.v4.media.session.MediaSessionCompat
//import android.media.session.MediaSession.MediaSessionCompat

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


//import android.support.v4.media.MediaMetadataCompat

//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.media.app.NotificationCompat.MediaStyle;

// TODO scrivere tutto nel file delle stringhe

//ATTENZIONE AL FILE GRADLE E AGLI IMPORT CORRETTI

// QUESTA E' LA VERSIONE DELL'APP SENZA LE VERSIONI "COMPAT" (DI COMPATIBILITA')


class PlayerService : Service()
{
    private var myPlayer: MediaPlayer? = null // Reference all'oggetto MediaPlayer
    private var mediaSession: MediaSession? = null
    private var mediaStyle: Notification.MediaStyle? = null
    private var isPlaying = false
    private var myAudioResource: Int? = null
    private var audioUserName: String? = null // Il nome dell'utente che ha inviato l'audio
    private var audioUserImageResource: Int? = null // La risorsa contenente l'immagine dell'utente che ha inviato l'audio


    val callback = object: MediaSession.Callback() {
        // Per interpretare il cambiamento di posizione nella barra di socrrimento della notifica
        // TODO Altri metodi come onPlay() e onPause() vanno qui. Noi li abbiamo implementati a livello di UI ma in questo caso dovrebbero funzionare anche con il pulsante play/pausa delle cuffie
        // TODO PEr fare la resumption del playback va implementato il metodo onPlay, oltre che a onSeek
        // Vedi https://developer.android.com/guide/topics/media/media-controls
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
                                myPlayer!!.currentPosition.toLong(),// Playback position. Used to update the elapsed time and the progress bar.
                                playbackSpeed // Playback speed. Determines the rate at which the elapsed time changes.
                        )
                        .setActions(PlaybackState.ACTION_SEEK_TO)  // isSeekable. Adding the SEEK_TO action indicates that seeking is supported and makes the seekbar position marker draggable. If this is not supplied seek will be disabled but progress will still be shown.
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
                                myPlayer!!.currentPosition.toLong(),// Playback position. Used to update the elapsed time and the progress bar.
                                playbackSpeed // Playback speed. Determines the rate at which the elapsed time changes.
                        )
                        .setActions(PlaybackState.ACTION_SEEK_TO)  // isSeekable. Adding the SEEK_TO action indicates that seeking is supported and makes the seekbar position marker draggable. If this is not supplied seek will be disabled but progress will still be shown.
                        .build()
                )
            }

        }
    }


    override fun onBind(intent: Intent): IBinder?
    {
        // E' uno dei metodi che il service deve implementare

        // Con questo service non supporto il binding quindi rispondo null
        return null // Clients can not bind to this service
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        // E' uno dei metodi che il nostro service deve implementare

        // Chiamato quando un altro componente avvia il service in modalità "started" (con il metodo startService)

        // Per la risposta agli intent di chi mi vuole parlare in modalità "started" (che è la modalità
        // usata anche in questa app)

        // Se l'intent che mi è arrivato contiene la chiave PLAY_START ed è associata al valore true
        // allora avvio la riproduzione della musica (se la chiave non è presente viene creata e
        // associata al valore false dalla funzione getBooleanExtra)
        if (intent.getBooleanExtra(PLAY_START, false)){
            myAudioResource=intent.getIntExtra(AUDIO_RESOURCE,0)
            audioUserName=intent.getStringExtra(AUDIO_USER_NAME)
            audioUserImageResource=intent.getIntExtra(AUDIO_USER_IMAGE_RESOURCE,0)
            startPlayer()
        }
        // Il metodo play() è un metodo specifico del mio playerService che ho implementato sotto


        if (intent.getBooleanExtra(PLAY_PAUSE, false)==true) {
            // E' possibile finire qui se il BoradcastReceiver associato a uno dei pulsanti della notifica ha impostato il relativo Intent extra
            playPause()
        }

        if (intent.getBooleanExtra(PLAY_STOP, false)==true) {
            // E' possibile finire qui se il BoradcastReceiver associato a uno dei pulsanti della notifica ha impostato il relativo Intent extra
            stopSelf()
        }

        // Il parmaetro ritornato indica come Android deve gestire questo service
        return START_STICKY //Avvio in modalità START_STICKY (vedi lezione)
    }

    override fun onCreate()
    {
        super.onCreate()
        // E' uno dei metodi che il nostro service deve implementare

        // Chiamato quando il sistema crea il service

        // Create the NotificationChannel, but only on API level 26+ because
        // the NotificationChannel class is new and not in the support library.
        // See https://developer.android.com/training/notify-user/channels
        // Il frammento di codice all'interno dell'if è quello che prepara la notifica da mostrare
        // quando la riporoduzione del brano è in corso
        // Il come preparare la notifica dipende dalla versione di Android in cui mi trovo. Se sono
        // in Android O (quindi Androdi 8) o superiore DEVO pubblicare la mia notifica in un
        // "notifications channel" che devo descrivere. Il codice all'interno dell'if è quello che
        // mi crea il notifications channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_audio_notifications_name) // Nome del canale
            val description = getString(R.string.channel_audio_notifications_name_description) // Descrizione del canale
            val importance = NotificationManager.IMPORTANCE_LOW // L'importanza del canale
            val channel = NotificationChannel(CHANNEL_ID, name, importance) // CHANNEL_ID è specificato nel companion object in fondo a questo file
            channel.description = description
            // Register the channel with the system
            val notificationManager = getSystemService(
                NotificationManager::class.java // Ottengo un istanza di NotificationManager
            )
            notificationManager.createNotificationChannel(channel) // A NotificationManager chiedo di creare il canale con i prametri specificati

            // Nota: La comparsa della notifica sarà scatenata solo in corrispondenza dell'inizio di
            // riporoduzione della musica
        }

    }

    private fun startPlayer() // Metodo custom per questo service
    {
        if (isPlaying==true){
            myPlayer?.stop()
        }
        isPlaying = true


        // AVVIO LA RIPRODUZIONE DELLA MUSICA

        // Music downloaded from "Public Domain 4U"
        // https://publicdomain4u.com/paul-whiteman-orchestra-doo-wacka-doo-mp3-download
        myPlayer = MediaPlayer.create(this, myAudioResource as Int) // myAudioResource contiene l'id della risorsa audio
        myPlayer!!.isLooping = false // Per non far ricominciare l'audio al termine
        // myPlayer holds the PARTIAL_WAKE_LOCK lock to ensure that the CPU continues running
        // during playback. myPlayer holds the lock while playing and releases it when paused
        // or stopped
        // Per evitare che il gestore del risparmio energetico tolga CPU e interrompa la musica
        myPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        // Nota: Uso applicationContext e non il context dell'activity perchè l'activity potrebbe anche
        // essere terminata mentre è in corso la riproduzione della musica e quindi aggancio la mia
        // notifica a qualcosa che so che continua a vivere e questo è il context dell'applicazione

        myPlayer?.setOnCompletionListener(OnCompletionListener {
            // Termino il service al termine del file audio
            stopSelf()
        })

        myPlayer!!.start()
        // I used the not-null assertion operator (!!) instead of the elvis operator (?)
        // for the mutable property myPlayer so the app crashes if the MediaPlayer is not
        // available, hence the user realizes that something went wrong





        // VISUALIZZO LA NOTIFICA

        // Create a media session
        // PlayerService is this Service (responsible for media playback)
        mediaSession = MediaSession(this, "PlayerService")

        // Create a MediaStyle object and supply your media session token to it.
        mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession?.sessionToken)

        mediaStyle?.setShowActionsInCompactView(0,1) // Questo serve  per visualizzare il puslante pause/play definito dopo nella visualizzazione compatta
        //mediaStyle?.setShowActionsInCompactView(1) // Questo serve  per visualizzare il puslante stop definito dopo nella visualizzazione compatta

        mediaSession!!.setCallback(callback)

        // Specify any actions which your users can perform, such as pausing and skipping to the next track.
        // Preparo l'intent per il Broadcast receiver del pulsante di pausa
        val audioPausePlayIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java) //TODO Perchè getApplicationContext()?
        audioPausePlayIntent.action = AudioNotificationReceiver.RESUME_ACTION
        val audioPausePlaypendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioPausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val audioStopIntent = Intent(getApplicationContext(), AudioNotificationReceiver::class.java)
        audioStopIntent.action = AudioNotificationReceiver.STOP_ACTION
        val audioStopPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, AudioNotificationReceiver.REQUEST_CODE_NOTIFICATION, audioStopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Create a Notification which is styled by your MediaStyle object.
        // This connects your media session to the media controls.
        // Don't forget to include a small icon.
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
        // Per quanto riguarda il titolo questo viene preso automaticamente dal file delle stringhe con l'id app_name


        var audioMilliseconds:Long = myPlayer?.duration?.toLong() as Long
        mediaSession?.setMetadata(
            MediaMetadata.Builder()

            // Title.
            .putString(MediaMetadata.METADATA_KEY_TITLE, "Audio ricevuto")

            // Artist.
            // Could also be the channel name or TV series.
            .putString(MediaMetadata.METADATA_KEY_ARTIST, audioUserName)

            // Album art.
            // Could also be a screenshot or hero image for video content
            // The URI scheme needs to be "content", "file", or "android.resource".
            //.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, "content:")

            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, "android.resource://" + BuildConfig.APPLICATION_ID + "/drawable/" + audioUserImageResource)

            // Duration.
            // If duration isn't set, such as for live broadcasts, then the progress
            // indicator won't be shown on the seekbar.
            .putLong(MediaMetadata.METADATA_KEY_DURATION, audioMilliseconds)

            .build()
        )

        //STEP 3
        val playbackSpeed: Float = 1.0f
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setState(
                        PlaybackState.STATE_PLAYING,

                        // Playback position.
                        // Used to update the elapsed time and the progress bar.
                        myPlayer!!.currentPosition.toLong(),

                        // Playback speed.
                        // Determines the rate at which the elapsed time changes.
                        playbackSpeed
                )

                // isSeekable.
                // Adding the SEEK_TO action indicates that seeking is supported
                // and makes the seekbar position marker draggable. If this is not
                // supplied seek will be disabled but progress will still be shown.
                .setActions(PlaybackState.ACTION_SEEK_TO)
                .build()
        )

        // Runs this service in the foreground, supplying the ongoing notification to be shown to the user
        // Utilizzo il metodo startForeground per indicare all'OS che terminare il service avrebbe un
        // effetto negativo sull'utente. Devo passare una notifica che verrà mostrata fintanto che il
        // service sta eseguendo il suo lavoro.
        // E' necessaria la relativa permissione nel file manifest
        val notificationID = 5786423 // An ID for this notification unique within the app (va bene un numero a caso, tanto la mia applicazione ha un'unica notifica)
        startForeground(notificationID, notification2) //Faccio l'"autodicihiarazione" per dire che sto

        // Nota: Di default questa notifica non è rimovibile. E' vero che è possibile fare uno swipe
        // per "toglierla" ma in realtà continua a essere visualizzata se si apre del tutto il menu
        // a discesa delle impostazioni rapide




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

                            // Playback position.
                            // Used to update the elapsed time and the progress bar.
                            myPlayer!!.currentPosition.toLong(),

                            // Playback speed.
                            // Determines the rate at which the elapsed time changes.
                            playbackSpeed
                    )

                // isSeekable.
                // Adding the SEEK_TO action indicates that seeking is supported
                // and makes the seekbar position marker draggable. If this is not
                // supplied seek will be disabled but progress will still be shown.
                .setActions(PlaybackState.ACTION_SEEK_TO)
                .build()
            )

            //Visualizzo pulsante pausa (ricreo la notifica ma uso lo stesso id)
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

            val notificationID = 5786423 // An ID for this notification unique within the app (va bene un numero a caso, tanto la mia applicazione ha un'unica notifica)
            startForeground(notificationID, notification2) //Faccio l'"autodicihiarazione" per dire che sto

        }
        else
        {
            isPlaying = true

            // Metto in pausa il palyer
            myPlayer?.start() // Riprendo la riproduzione (non ricomincio dall'inizio)

            // Metto in play la media session
            val playbackSpeed: Float = 1.0f
            mediaSession?.setPlaybackState(
                    PlaybackState.Builder()
                            .setState(
                                    PlaybackState.STATE_PLAYING,

                                    // Playback position.
                                    // Used to update the elapsed time and the progress bar.
                                    myPlayer!!.currentPosition.toLong(),

                                    // Playback speed.
                                    // Determines the rate at which the elapsed time changes.
                                    playbackSpeed
                            )

                            // isSeekable.
                            // Adding the SEEK_TO action indicates that seeking is supported
                            // and makes the seekbar position marker draggable. If this is not
                            // supplied seek will be disabled but progress will still be shown.
                            .setActions(PlaybackState.ACTION_SEEK_TO)
                            .build()
            )

            //Visualizzo pulsante pausa (ricreo la notifica ma uso lo stesso id)
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

            val notificationID = 5786423 // An ID for this notification unique within the app (va bene un numero a caso, tanto la mia applicazione ha un'unica notifica)
            startForeground(notificationID, notification2) //Faccio l'"autodicihiarazione" per dire che sto

        }
    }

    private fun stop() // Chiamato nel metodo onDestroy()
    {
        if (isPlaying) {
            isPlaying = false
            myPlayer?.release() // Rilascio l'oggetto MediaPLayer
            myPlayer = null
            stopForeground(true) // Indico che il service non ha più importanza per l'utente
        }
    }

    override fun onDestroy()
    {
        // Chiamato quando l'utente preme il pulsante stop (vedi MainActivity)

        stop() // Interrompo la musica
        super.onDestroy()
    }

    companion object
    {
        private const val CHANNEL_ID = "simplebgplayer"
        const val PLAY_START = "BGPlayStart"
        const val PLAY_STOP = "BGPlayStop"
        const val PLAY_REWIND = "action_rewind"
        const val PLAY_NEXT = "action_next"
        const val PLAY_PREVIOUS = "action_previous"
        const val PLAY_PAUSE = "action_pause"

        const val AUDIO_RESOURCE = "action_resource"
        const val AUDIO_USER_NAME = "action_user_name"
        const val AUDIO_USER_IMAGE_RESOURCE = "action_user_image_resource"
    }
}