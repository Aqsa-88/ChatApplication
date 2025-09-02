package com.example.mychatapplication.Message

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.mychatapplication.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        // 2 second delay for splash
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Already logged in → Go to RegisterActivity
                startActivity(Intent(this, Register::class.java))
            } else {
                // Not logged in → Go to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // close splash activity
        }, 4000) // 4sec
    }
}