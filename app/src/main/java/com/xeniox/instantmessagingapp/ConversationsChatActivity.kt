package com.xeniox.instantmessagingapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations_chat.*
import kotlinx.android.synthetic.main.chat_from_message.view.*
import kotlinx.android.synthetic.main.chat_to_message.view.*

class ConversationsChatActivity : AppCompatActivity() {

    companion object{
        const val TAG = "ConversationsChatActivity"
    }

    val adapter = GroupAdapter<ViewHolder>()

    val toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)

        recyclerView_chat_conversation.adapter = adapter

        topAppBar_chat_conversation.setNavigationOnClickListener {
            finish()
        }

//        val username = intent.getStringExtra(NewConversationActivity.USER_KEY)
        val toUser = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)

        topAppBar_chat_conversation.title = toUser?.username
        listenForMessages()
        button_send_message.setOnClickListener {
            Log.d("ConversationsChatActivity", "Attempt to send message...")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
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
            reference.setValue(ChatMessage(reference.key!!, it.toString(), fromId, toId, System.currentTimeMillis()/1000)).addOnSuccessListener {
                Log.d("ConversationsChatActivity", "Message sent...")
                editText_chat_conversation.text.clear()
                editText_chat_conversation.text.insert(0, "")
            }
        }
    }
}

class ChatFromItem(val text: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from_message.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_message
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to_message.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.chat_to_image

        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_message
    }
}