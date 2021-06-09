package com.android.notificationexpo

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.notificationexpo.welcomeFragments.*

class WelcomeActivity : FragmentActivity() { // TODO Mettere FragmentActivity diasattiva l'App bar, da capire perchè

    object CONST{
        //Il numero di fragments per le pagine di benvenuto.
        const val NUM_WELCOME_FRAGMENTS:Int = 5
    }

    // Class variables (per avere le reference ai widget che possono cambiare)
    // leteinit perchè venbgono inizializzate in onCreate()
    private lateinit var indicator0 : ImageView
    private lateinit var indicator1 : ImageView
    private lateinit var indicator2 : ImageView
    private lateinit var indicator3 : ImageView
    private lateinit var indicator4 : ImageView
    private lateinit var welcomeButtonPrevious : Button
    private lateinit var welcomeButtonNext : Button
    private lateinit var welcomeViewPager: ViewPager2   // Il widget ViewPager2 che gestisce le animazioni e premette lo scroll orizzonatle fra i fragment
    private lateinit var myWelcomeViewPagerAdapter: WelcomeViewPagerAdapter

    // TODO Nota: usando ViewPager2 viene gestita dalla classe il salvataggio dell'istanza per la transazione da portrait a landscape ma la cosa non viene scritta bene nella documentazione

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Visualizza il layout
        setContentView(R.layout.welcome_activity)

        // Ottengo la reference al widget ViewPager2
        welcomeViewPager = findViewById(R.id.welcome_viewpager)

        // Istanzio un PagerAdapter, che fornisce le pagine al widget ViewPager2  e lo associo al mio ViewPager2
        myWelcomeViewPagerAdapter = WelcomeViewPagerAdapter(this)
        welcomeViewPager.adapter = myWelcomeViewPagerAdapter

        // Ottengo la reference agli altri widget
        indicator0 = findViewById(R.id.indicator_0)
        indicator1 = findViewById(R.id.indicator_1)
        indicator2 = findViewById(R.id.indicator_2)
        indicator3 = findViewById(R.id.indicator_3)
        indicator4 = findViewById(R.id.indicator_4)
        welcomeButtonPrevious = findViewById(R.id.welcome_button_previous)
        welcomeButtonNext = findViewById(R.id.welcome_button_next)

        // Gestisco il caso in cui una pagina in ViewPager2 viene cambiata
        /* Spiegazione:
           Per poter capire quando una pagina viene cambiata dobbiamo passare un oggetto di tipo
           OnPageChangeCallback alla nostra welcomeViewPager tramite il metodo
           registerOnPageChangeCallback.
           La classe OnPageChangeCallback è una classe astratta quindi dobbiamo necessariamente creare una
           sottoclasse. Al posto di creare specificatamente una sottoclasse, istanziare un oggetto
           da tale classe e passarlo al metodo registerOnPageChangeCallback è possibile usare una
           inner class anonima (che si specifica con l'espressione object) in modo da rendere il
           codice più compatto e comprensibile. Con il codice nelle righe seguenti creiamo e
           istanziamo una sottoclasse di OnPageChangeCallback su cui è stato fatto l'override del
           metodo onPageSelected per gestire la logica della nostra applicazione
           Ulteriore docimentazione a riguardo delle inner class anonime:
           https://kotlinlang.org/docs/nested-classes.html#anonymous-inner-classes
           https://kotlinlang.org/docs/object-declarations.html#inheriting-anonymous-objects-from-supertypes
        */
        welcomeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){

            override fun onPageSelected(position: Int){
                if (position == 0){
                    // E' visualizzata la prima pagina
                    indicator0.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator_selected, theme)
                    indicator1.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator2.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator3.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator4.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    welcomeButtonPrevious.isVisible= false;
                }
                else if (position == 1){
                    // E' visualizzata la seconda pagina
                    indicator0.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator1.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator_selected, theme)
                    indicator2.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator3.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator4.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    welcomeButtonPrevious.isVisible= true;
                }
                else if (position == 2){
                    // E' visualizzata la terza pagina
                    indicator0.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator1.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator2.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator_selected, theme)
                    indicator3.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator4.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                }
                else if (position == 3){
                    // E' visualizzata la quarta pagina
                    indicator0.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator1.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator2.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator3.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator_selected, theme)
                    indicator4.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    welcomeButtonNext.text = "Avanti"
                }
                else {
                    // E' visualizzata la quinta pagina
                    indicator0.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator1.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator2.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator3.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator, theme)
                    indicator4.background = ResourcesCompat.getDrawable(resources,R.drawable.indicator_selected, theme)
                    welcomeButtonNext.text = "Inizia"
                }
            }
        })

        // Gestico il pulsante per tornare indietro
        welcomeButtonPrevious.setOnClickListener{
            if (welcomeViewPager.currentItem > 0) {
                // Vado alla pgaina precedente
                welcomeViewPager.currentItem = welcomeViewPager.currentItem - 1
            }
        }

        // Gestisco il pulsante per andare avanti
        welcomeButtonNext.setOnClickListener{
            if (welcomeViewPager.currentItem < 4) {
                // Vado alla pagina successiva
                welcomeViewPager.currentItem = welcomeViewPager.currentItem + 1
            }
            else if (welcomeViewPager.currentItem == 4){
                // Se siamo qui l'utente vuole uscire dal welcome screen e andare alla schemrata successiva
                this.finish() //Chiudo questa activity
            }
        }

    } // Fine del metodo onCreate()



    // Per gestire il puslante back di Android e tornare indietro di una pagina
    override fun onBackPressed() {
        if (welcomeViewPager.currentItem == 0) {
            // L'utente è nella prima pagina ed è stato premuto indietro.
            // Lasciamo che sia il sistema a gestire il pulsante indietro.
            // Questo comporterà la chiamata a finish() dell'activity e verrà fatto un pop dal back stack
            super.onBackPressed()
        } else {
            // Altrimenti, torniamo indietro di una pagina
            welcomeViewPager.currentItem = welcomeViewPager.currentItem - 1
        }
    }


    // L'adapter per ViewPager2 che permette di ottenere il Fragment corretto per ciascuna posizione
    // Nota: E' una inner class, ma funzioan anche senza dichiararla inner
    // TODO Provare a istanziare sempre la stessa classe WelcomeFragment con diverse UI
    private inner class WelcomeViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = CONST.NUM_WELCOME_FRAGMENTS

        override fun createFragment(position: Int): Fragment{
            if (position == 0){
                return WelcomeFragment0();
            }
            else if (position == 1){
                return WelcomeFragment1();
            }
            else if (position == 2){
                return WelcomeFragment2();
            }
            else if (position == 3){
                return WelcomeFragment3();
            }
            else {
                return WelcomeFragment4();
            }
        }
    }
} // Fine classe MainActivity