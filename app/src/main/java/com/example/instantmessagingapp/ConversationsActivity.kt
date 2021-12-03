package com.example.instantmessagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ConversationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        setNavigationViewListener()

    }

    private fun setNavigationViewListener() {
        val navigationView = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar)
        navigationView.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}