package com.example.instantmessagingapp

import android.content.Intent
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
            val intent = Intent(this, ConversationsActivity::class.java)
            startActivity(intent)
        }

    }
}