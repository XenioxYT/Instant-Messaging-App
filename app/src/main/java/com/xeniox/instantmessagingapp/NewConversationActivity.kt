package com.xeniox.instantmessagingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xeniox.instantmessagingapp.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_conversation.*

class NewConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)

        val adapter = GroupAdapter<GroupieViewHolder>()
        recyclerView_new_conversation.adapter = adapter


        topAppBar_new_conversation.setNavigationOnClickListener { // set the navigation icon on the top app bar
            finish() // end the activity
        }
    }
}