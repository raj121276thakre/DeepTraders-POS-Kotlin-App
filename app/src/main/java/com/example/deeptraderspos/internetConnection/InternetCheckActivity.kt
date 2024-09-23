package com.example.deeptraderspos.internetConnection

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deeptraderspos.R

open class InternetCheckActivity : AppCompatActivity() {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_internet_check)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.internet)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Common functionality like setting up network listener
        networkChangeReceiver = NetworkChangeReceiver { isConnected ->
            if (isConnected) {
                alertDialog?.dismiss()
                alertDialog = null
            } else {
                showNoInternetDialog()
            }
        }

    }


    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun showNoInternetDialog() {
        if (alertDialog == null) {
            alertDialog = AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setCancelable(false)
                .create()
            alertDialog?.show()
        }
    }


}













