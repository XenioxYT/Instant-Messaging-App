package com.example.instantmessagingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_conversations.*
import kotlinx.android.synthetic.main.activity_new_conversation.*

class NewConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)

        val adapter = GroupieAdapter<RecyclerView.ViewHolder>()
        recyclerView_new_conversation.adapter = adapter


        topAppBar_new_conversation.setNavigationOnClickListener { // set the navigation icon on the top app bar
            finish() // end the activity
        }


    }
}