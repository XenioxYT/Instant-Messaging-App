package com.xeniox.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.google.mlkit.nl.smartreply.TextMessage
import kotlinx.android.synthetic.main.activity_conversations_chat.*
import kotlinx.android.synthetic.main.chat_from_message.view.*
import kotlinx.android.synthetic.main.chat_to_message.view.*

class ConversationsChatActivity : AppCompatActivity() {

    companion object{
        const val TAG = "ConversationsChatActivity"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var conversations:ArrayList<TextMessage> = ArrayList()
    lateinit var suggestion1: SmartReplySuggestion
    lateinit var suggestion2: SmartReplySuggestion
    lateinit var suggestion3: SmartReplySuggestion

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)


        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        val smartReplyGenerator = SmartReply.getClient()
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
//                if (chatMessage != null) {
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                }
                if (chatMessage?.fromId == FirebaseAuth.getInstance().uid) {
//                     adapter.add(ChatToItem(chatMessage.text, ConversationsActivity.currentUser!!))
                    conversations.add(
                        TextMessage.createForLocalUser(
                            chatMessage?.text.toString(),
                            System.currentTimeMillis()
                        )
                    )
//                       recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                } else {
//                       adapter.add(ChatFromItem(chatMessage.text, intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!))
                    conversations.add(
                        TextMessage.createForRemoteUser(
                            chatMessage?.text.toString(),
                            System.currentTimeMillis(),
                            fromId!!
                        )
                    )
//                       recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)

                }
                smartReplyGenerator.suggestReplies(conversations).addOnSuccessListener {
                    Log.d(TAG, "Suggestions: ${it.suggestions}")
                    Log.d(TAG, "Status: ${it.status}")

                    if (it.status == STATUS_NOT_SUPPORTED_LANGUAGE) {
                        Log.d(TAG, "Not supported language")
                    } else if (it.status == STATUS_SUCCESS) {
                        Log.d(TAG, "Success")
                        var reply = ""
                        for (suggestion: SmartReplySuggestion in it.suggestions) {
                            reply += suggestion.text + "\n"
                        }
                        Log.d(TAG, reply)
                        var suggestion1 = it.suggestions[0].text
                        var suggestion2 = it.suggestions[1].text
                        var suggestion3 = it.suggestions[2].text
                        button_reply1.text = suggestion1
                        button_reply2.text = suggestion2
                        button_reply3.text = suggestion3
                        button_reply1.setOnClickListener {
                            Log.d(TAG, "Reply 1")
                            button_reply1.text = suggestion1
                            editText_chat_conversation.setText(suggestion1)
                        }
                        button_reply2.setOnClickListener {
                            Log.d(TAG, "Reply 2")
                            button_reply2.text = suggestion2
                            editText_chat_conversation.setText(suggestion2)
                        }
                        button_reply3.setOnClickListener {
                            Log.d(TAG, "Reply 3")
                            button_reply3.text = suggestion3
                            editText_chat_conversation.setText(suggestion3)
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        recyclerView_chat_conversation.adapter = adapter

        topAppBar_chat_conversation.setNavigationOnClickListener {
            val intent = Intent(this, ConversationsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


//        srgen(conversations)
        topAppBar_chat_conversation.title =
            intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)?.username
        listenForMessages()

        button_send_message.setOnClickListener {
            Log.d("ConversationsChatActivity", "Attempt to send message...")
            performSendMessage()
        }

        try {
            button_reply1.setOnClickListener {
                Log.d(TAG, "Reply 1")
                editText_chat_conversation.setText(suggestion1.text)
            }
            button_reply2.setOnClickListener {
                Log.d(TAG, "Reply 2")
                editText_chat_conversation.setText(suggestion2.text)
            }
            button_reply3.setOnClickListener {
                Log.d(TAG, "Reply 3")
                editText_chat_conversation.setText(suggestion3.text)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
        }
    }


    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        val smartReplyGenerator = SmartReply.getClient()
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(chatMessage.text, ConversationsActivity.currentUser!!))
//                        conversations.add(TextMessage.createForLocalUser(chatMessage.text, System.currentTimeMillis()))
                        recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                        return
                    } else {
                        adapter.add(ChatFromItem(chatMessage.text,intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!))
//                        conversations.add(TextMessage.createForRemoteUser(chatMessage.text, System.currentTimeMillis(), fromId!!))
                        recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                        return
                    }
                }
                recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
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

//    private fun srgen(conversations: ArrayList<TextMessage>) {
//        val smartReplyGenerator = SmartReply.getClient()
//        smartReplyGenerator.suggestReplies(conversations).addOnSuccessListener {
//            Log.d(TAG, "Suggestions: ${it.suggestions}")
//            Log.d(TAG, "Status: ${it.status}")
//
//            if (it.status == STATUS_NOT_SUPPORTED_LANGUAGE) {
//                Log.d(TAG, "Not supported language")
//            } else if (it.status == STATUS_SUCCESS) {
//                Log.d(TAG, "Success")
//                var reply = ""
//                for (suggestion:SmartReplySuggestion in it.suggestions) {
//                    reply += suggestion.text + "\n"
//                }
//                Log.d(TAG, reply)
//                val suggestion1 = it.suggestions[0]
//                val suggestion2 = it.suggestions[1]
//                val suggestion3 = it.suggestions[2]
//
//                button_reply1.text = suggestion1.text
//
//                button_reply1.setOnClickListener {
//                    editText_chat_conversation.setText(suggestion1.text)
//                }
////                button_reply2.text = suggestion2.text
////                button_reply3.text = suggestion3.text
////
////                button_reply1.setOnClickListener {
////                    performSendMessageSR(suggestion1.text)
////                }
////                button_reply2.setOnClickListener {
////                    performSendMessageSR(suggestion2.text)
////                }
////                button_reply3.setOnClickListener {
////                    performSendMessageSR(suggestion3.text)
////                }
//
//            }
//        }
//    }



    private fun performSendMessage() {
//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid

        if (fromId == null) {
            Log.d("chat", "fromId is null")
            return
        }
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            .push() // new messages node in firebase

        val toReference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId")
                .push() // new messages node in firebase

        if (editText_chat_conversation.text.isNullOrEmpty()) {
            Log.d("chat", "message is null")
            return
        } else {
            editText_chat_conversation.text.trim().let {
                reference.setValue(
                    ChatMessage(
                        reference.key!!,
                        it.toString(),
                        fromId,
                        toId,
                        System.currentTimeMillis() / 1000
                    )
                ).addOnSuccessListener {
                    Log.d("ConversationsChatActivity", "Message sent... $it")
                    editText_chat_conversation.text.clear()
                    editText_chat_conversation.text.insert(0, "")
                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                }
                toReference.setValue(ChatMessage(toReference.key!!, it.toString(), fromId, toId, System.currentTimeMillis() / 1000))
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
                latestMessageRef.setValue(ChatMessage(reference.key!!, editText_chat_conversation.text.toString(), fromId, toId, System.currentTimeMillis() / 1000))
                val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
                latestMessageToRef.setValue(ChatMessage(toReference.key!!, editText_chat_conversation.text.toString(), fromId, toId, System.currentTimeMillis() / 1000))

            }
        }
    }

    private fun performSendMessageSR(reply: String) {
//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid

        if (fromId == null) {
            Log.d("chat", "fromId is null")
            return
        }
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            .push() // new messages node in firebase

        val toReference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId")
                .push() // new messages node in firebase

        if (editText_chat_conversation.text.isNullOrEmpty()) {
            Log.d("chat", "message is null")
            return
        } else {
            reply.trim().let {
                reference.setValue(
                    ChatMessage(
                        reference.key!!,
                        it.toString(),
                        fromId,
                        toId,
                        System.currentTimeMillis() / 1000
                    )
                ).addOnSuccessListener {
                    Log.d("ConversationsChatActivity", "Message sent")
                    editText_chat_conversation.text.clear()
                    editText_chat_conversation.text.insert(0, "")
                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                }
                toReference.setValue(ChatMessage(toReference.key!!, it.toString(), fromId, toId, System.currentTimeMillis() / 1000))
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
                latestMessageRef.setValue(ChatMessage(reference.key!!, editText_chat_conversation.text.toString(), fromId, toId, System.currentTimeMillis() / 1000))
                val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
                latestMessageToRef.setValue(ChatMessage(toReference.key!!, editText_chat_conversation.text.toString(), fromId, toId, System.currentTimeMillis() / 1000))

            }
        }
    }

}

class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from_message.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.chat_from_image

        Picasso.get().load(uri).into(targetImageView)
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