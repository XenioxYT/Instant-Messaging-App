package com.xeniox.instantmessagingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations_chat.*

class ConversationsChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)

        topAppBar_chat_conversation.setNavigationOnClickListener {
            finish()
        }

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(chatItem())
        adapter.add(chatItem())
        adapter.add(chatItem())
        adapter.add(chatItem())
        adapter.add(chatItem())
        adapter.add(chatItem())


        recyclerView_chat_conversation.adapter = adapter


    }
}

class chatItem : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.chat_recieved_message
    }
}