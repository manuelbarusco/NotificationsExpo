package com.android.notificationexpo.welcomeFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.notificationexpo.R

// Classe che permette di istanizare il fragment 3 della schemrata di benvenuto

class WelcomeFragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.welcome_fragment_3, container, false)
}