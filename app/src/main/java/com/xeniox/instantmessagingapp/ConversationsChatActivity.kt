package com.xeniox.instantmessagingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations_chat.*
import kotlinx.android.synthetic.main.chat_from_message.view.*
import kotlinx.android.synthetic.main.chat_to_message.view.*

class ConversationsChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)

        topAppBar_chat_conversation.setNavigationOnClickListener {
            finish()
        }

//        val username = intent.getStringExtra(NewConversationActivity.USER_KEY)
        val user = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)

        topAppBar_chat_conversation.title = user?.username

        setupDummmyData()
    }

    private fun setupDummmyData() {
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        adapter.add(chatFromItem())
        adapter.add(chatToItem())
        recyclerView_chat_conversation.adapter = adapter
    }
}

class chatFromItem : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from_message.text = "From message......"
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_message
    }
}

class chatToItem : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to_message.text = "This is a message to the other user which is a bot longer...."
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_message
    }
}