package com.example.fitnessapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.others.Constants.GO_TO_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkIntentRecievedFromService(intent)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnItemReselectedListener { /*no-operations*/ }
        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }


            }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntentRecievedFromService(intent)
    }
    private fun checkIntentRecievedFromService(intent: Intent?){
        if(intent?.action == GO_TO_TRACKING_FRAGMENT){
            navHostFragment.findNavController().navigate(R.id.global_action)
        }
    }
}