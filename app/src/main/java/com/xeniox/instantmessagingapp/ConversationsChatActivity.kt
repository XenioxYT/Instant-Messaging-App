package com.xeniox.instantmessagingapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations_chat.*
import kotlinx.android.synthetic.main.chat_from_message.view.*
import kotlinx.android.synthetic.main.chat_to_message.view.*

class ConversationsChatActivity : AppCompatActivity() {

    companion object{
        val TAG = "ConversationsChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)

        topAppBar_chat_conversation.setNavigationOnClickListener {
            finish()
        }

//        val username = intent.getStringExtra(NewConversationActivity.USER_KEY)
        val user = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)

        topAppBar_chat_conversation.title = user?.username

        setupDummyData()

        button_send_message.setOnClickListener {
            Log.d("ConversationsChatActivity", "Attempt to send message...")
            performSendMessage()
        }
    }

    class chatMssage(val id: String, val text:String, val fromId: String, val toId: String, val timestamp: Long) {
        constructor() : this("", "", "", "", -1)
    }

    private fun performSendMessage() {
        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid

        if (fromId == null) {
            Log.d(TAG, "fromId is null")
            return
        }

        editText_chat_conversation.text.trim().let {
            reference.setValue(chatMssage(reference.key!!, it.toString(), fromId, toId, System.currentTimeMillis()/1000)).addOnSuccessListener {
                Log.d("ConversationsChatActivity", "Message sent...")
                editText_chat_conversation.text.clear()
                editText_chat_conversation.text.insert(0, "")
            }
        }

    }

    private fun setupDummyData() {
        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(chatFromItem("FROM MESSAGE"))
        adapter.add(chatToItem("TO MESSAGE"))
        adapter.add(chatFromItem("FROM MESSAGE"))
        adapter.add(chatToItem("TO MESSAGE"))
        adapter.add(chatFromItem("FROM MESSAGE"))
        adapter.add(chatToItem("To message"))
        adapter.add(chatFromItem("FROM MESSAGE"))
        recyclerView_chat_conversation.adapter = adapter
    }
}

class chatFromItem(val text: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from_message.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_message
    }
}

class chatToItem(val text: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to_message.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_message
    }
}