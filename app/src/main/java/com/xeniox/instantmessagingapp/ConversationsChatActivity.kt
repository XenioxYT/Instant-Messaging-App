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
import kotlinx.android.synthetic.main.chat_recieved_message.view.*
import kotlinx.android.synthetic.main.chat_sent_message.view.*

class ConversationsChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)

        recyclerView_chat_conversation.adapter = adapter

        topAppBar_chat_conversation.setNavigationOnClickListener {
            finish()
        }

        if (toUser != null) {
            topAppBar_chat_conversation.title =
                toUser?.username // set the title of the toolbar to be the username that was passed from @NewConversationActivity.kt
        }

        receiveMessage()

        button_send_message.setOnClickListener {
            Log.d("Chat", "send button pressed ${it}")
            sendMessage()
        }

    }

    private fun receiveMessage() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    Log.d("Chat", message.text)
                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        if (toUser != null) {
                            adapter.add(chatToUser(message.text, toUser!!))
                        }
                    } else {
                        if (toUser != null) {
                            adapter.add(chatFromUser(message.text, toUser!!))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

        })
    }


    private fun sendMessage() {
        // Upload text inputted by the user and upload it to the database under the messages node.
        val text = editText_chat_conversation.text.toString()
//        Log.d("Chat", "${text}")

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)
        val toId = user?.uid
        if (fromId == null || toId == null) return
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val message = Message(ref.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        ref.setValue(message).addOnSuccessListener {
            Log.d("Chat", "Message uploaded to database. ${ref.key}")
        }
    }
}

class chatFromUser(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from_user.text = text

    }

    override fun getLayout(): Int {
        return R.layout.chat_recieved_message
    }
}

class chatToUser(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to_user.text = text

        // load image

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.image_message_sent)
    }

    override fun getLayout(): Int {
        return R.layout.chat_sent_message
    }
}