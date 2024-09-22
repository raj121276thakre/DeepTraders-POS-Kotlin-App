package com.example.deeptraderspos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {


    var splashTimeOut: Int = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set status bar color
        Utils.setStatusBarColor(this)


        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }

        Handler().postDelayed({
            val intent = Intent(
                this@SplashActivity,
                HomeActivity::class.java
            )
            startActivity(intent)
            finish()
        }, splashTimeOut.toLong())


    }
}