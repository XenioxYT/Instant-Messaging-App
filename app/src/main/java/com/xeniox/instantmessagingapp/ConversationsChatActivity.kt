package com.xeniox.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult.STATUS_SUCCESS
import com.google.mlkit.nl.smartreply.TextMessage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_conversations_chat.*
import kotlinx.android.synthetic.main.chat_from_message.view.*
import kotlinx.android.synthetic.main.chat_to_message.view.*


class ConversationsChatActivity : AppCompatActivity() {

    companion object{
        const val TAG = "ConversationsChatActivity"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var conversations:ArrayList<TextMessage> = ArrayList()
    var suggestion1: SmartReplySuggestion? = null
    var suggestion2: SmartReplySuggestion? = null
    var suggestion3: SmartReplySuggestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)

//        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
//        setSupportActionBar(toolbar)


//         Set custom colours here:
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val refColor = FirebaseDatabase.getInstance().getReference("/users/$user")
        var getcolor = refColor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val color = p0.getValue(User::class.java)
                if (color?.color != null) {
                    Log.d("SettingsActivity", "Color is: ${color.color}")
                    val userColor = color.color
                    Log.d("SettingsActivity", "Color is: $color")

                    // Set the colour of the toolbar
                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
                    topappbar.setBackgroundColor(color.color.toInt())

                    // Set the colour of the status bar.
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color.color.toInt()

                    // Set other colours here
                    button_send_message.setBackgroundColor(color.color.toInt())
                    button_reply1.setBackgroundColor(color.color.toInt())
                    button_reply2.setBackgroundColor(color.color.toInt())
                    button_reply3.setBackgroundColor(color.color.toInt())
                    val unwrappedDrawable =
                        AppCompatResources.getDrawable(
                            this@ConversationsChatActivity,
                            R.drawable.chat_received_message_rounded
                        )
                    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
                    DrawableCompat.setTint(wrappedDrawable, color.color.toInt())
                    val userPic = findViewById<CircleImageView>(R.id.chat_from_image)


                } else {
                    Log.d("SettingsActivity", "Color is: null")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("SettingsActivity", "Failed to read color")
            }
        })



        recyclerView_chat_conversation.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            recyclerView_chat_conversation.scrollToPosition(
                adapter.itemCount - 1
            )
        }

        button_send_message.visibility = View.GONE

        editText_chat_conversation.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty()) {
                        button_send_message.visibility = View.VISIBLE
                    } else {
                        button_send_message.visibility = View.GONE
                    }
                    val user = FirebaseAuth.getInstance().currentUser?.uid
                    val refColor = FirebaseDatabase.getInstance().getReference("/users/$user/isTyping")
                    refColor.setValue(false)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    val user = FirebaseAuth.getInstance().currentUser?.uid
                    val refColor = FirebaseDatabase.getInstance().getReference("/users/$user/isTyping")
                    refColor.setValue(false)
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val user = FirebaseAuth.getInstance().currentUser?.uid
                    val refColor = FirebaseDatabase.getInstance().getReference("/users/$user/isTyping")
                    refColor.setValue(true)
                }
            }
        )


        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid
        val username = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.username
        val bio = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.bio
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        val smartReplyGenerator = SmartReply.getClient()
        val otherUserEmail = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.email
        val userProfileImageUrl = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.profileImageUrl
        topAppBar_chat_conversation.subtitle = otherUserEmail

        topAppBar_chat_conversation.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.view_profile -> {
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.bottom_sheet_user_profile, null)
                    Log.d(TAG, "onCreate: $toId")
                    Log.d(TAG, "onCreate: $otherUserEmail")

                    val textEmail = view.findViewById<TextView>(R.id.text_email_profile)
                    val textUsername = view.findViewById<TextView>(R.id.text_username_profile)
                    val userProfileImage = view.findViewById<ImageView>(R.id.user_profile_image)
                    val textBio = view.findViewById<TextView>(R.id.text_bio_profile)

                    textEmail.text = otherUserEmail.toString()
                    textUsername.text = username.toString()
                    textBio.text = bio.toString()

                    Picasso.get().load(userProfileImageUrl).into(userProfileImage)

                    dialog.setCancelable(true)
                    dialog.setContentView(view)
                    dialog.show()
                }
                R.id.search_chat -> {
//                    FirebaseAuth.getInstance().signOut()
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
                }
                R.id.mute_notifications -> {
                    Toast.makeText(this, "Mute Notifications", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        try{
            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val chatMessage = p0.getValue(ChatMessage::class.java)
//                    if (chatMessage != null) {
                    if (chatMessage != null) {
                        Log.d(TAG, chatMessage.text)
                    }
                    if (chatMessage?.fromId == FirebaseAuth.getInstance().uid) {
//                         adapter.add(ChatToItem(chatMessage.text, ConversationsActivity.currentUser!!))
                        conversations.add(
                            TextMessage.createForLocalUser(
                                chatMessage?.text.toString(),
                                System.currentTimeMillis()
                            )
                        )
//                           recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                    } else {
//                           adapter.add(ChatFromItem(chatMessage.text, intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!))
                        conversations.add(
                            TextMessage.createForRemoteUser(
                                chatMessage?.text.toString(),
                                System.currentTimeMillis(),
                                fromId!!
                            )
                        )
                           recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)

                    }
                    smartReplyGenerator.suggestReplies(conversations).addOnSuccessListener {
                        Log.d(TAG, "Suggestions: ${it.suggestions}")
                        Log.d(TAG, "Status: ${it.status}")
                        Log.d(TAG, conversations.toString())
                        Log.d(TAG, "Size: ${conversations.size}")

                        if (conversations.size > 50) {
                            Log.d(TAG, "Size: ${conversations.size}")
                            Log.d(TAG, "cleared the array")
                            conversations.clear()
                        }

                        when (it.status) {
                            STATUS_NOT_SUPPORTED_LANGUAGE -> {
                                Log.d(TAG, "Not supported language")
                                button_reply1.visibility = View.GONE
                                button_reply2.visibility = View.GONE
                                button_reply3.visibility = View.GONE
                                button_reply1.height = 0
                                button_reply2.height = 0
                                button_reply3.height = 0
                            }
                            STATUS_SUCCESS -> {
                                button_reply1.visibility = View.VISIBLE
                                button_reply2.visibility = View.VISIBLE
                                button_reply3.visibility = View.VISIBLE
                                button_reply1.height = 40
                                button_reply2.height = 40
                                button_reply3.height = 40
                                recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                Log.d(TAG, "Success")
                                var reply = ""
                                for (suggestion: SmartReplySuggestion in it.suggestions) {
                                    reply += suggestion.text + "\n"
                                }
                                Log.d(TAG, reply)
                                var suggestion1 = it.suggestions[0].text
                                if (suggestion1.isNullOrEmpty()) {
                                    button_reply1.visibility = View.GONE
                                    button_reply1.height = 0
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                } else {
                                    button_reply1.text = suggestion1
                                }
                                var suggestion2 = it.suggestions[1].text
                                if (suggestion2.isNullOrEmpty()) {
                                    button_reply2.visibility = View.GONE
                                    button_reply2.height = 0
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                } else {
                                    button_reply2.text = suggestion2
                                }
                                var suggestion3 = it.suggestions[2].text
                                if (suggestion3.isNullOrEmpty()) {
                                    button_reply3.visibility = View.GONE
                                    button_reply3.height = 0
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                } else {
                                    button_reply3.text = suggestion3
                                }
                                button_reply1.setOnClickListener {
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                    Log.d(TAG, "Reply 1")
                                    button_reply1.text = suggestion1
                                    editText_chat_conversation.append(suggestion1)
                                }
                                button_reply2.setOnClickListener {
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                    Log.d(TAG, "Reply 2")
                                    button_reply2.text = suggestion2
                                    editText_chat_conversation.append(suggestion2)
                                }
                                button_reply3.setOnClickListener {
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                    Log.d(TAG, "Reply 3")
                                    button_reply3.text = suggestion3
                                    editText_chat_conversation.append(suggestion3)
                                }
                            }
                            else -> {
                                Log.d(TAG, "Error")
                                button_reply1.visibility = View.GONE
                                button_reply2.visibility = View.GONE
                                button_reply3.visibility = View.GONE
                                button_reply1.height = 0
                                button_reply2.height = 0
                                button_reply3.height = 0
                                recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Toast.makeText(this@ConversationsChatActivity, "Failed to read value.", Toast.LENGTH_SHORT).show()
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Toast.makeText(this@ConversationsChatActivity, "Failed to read value.", Toast.LENGTH_SHORT).show()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Toast.makeText(this@ConversationsChatActivity, "Failed to read value.", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    //make a toast saying that there was a database error
                    Log.d(TAG, "Failed to read value.", error.toException())
                    Toast.makeText(
                        this@ConversationsChatActivity,
                        "Failed to read value. $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "Failed to read value.", e)
            Toast.makeText(
                this@ConversationsChatActivity,
                "Failed to read value. $e",
                Toast.LENGTH_SHORT
            ).show()
        }

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

        if (suggestion1?.text.isNullOrEmpty()) {
            button_reply1.visibility = View.GONE
            button_reply1.height = 0
            recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
        }
        if (suggestion2?.text.isNullOrEmpty()) {
            button_reply2.visibility = View.GONE
            button_reply2.height = 0
            recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
        }
        if (suggestion3?.text.isNullOrEmpty()) {
            button_reply3.visibility = View.GONE
            button_reply3.height = 0
            recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
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

        if (editText_chat_conversation.text.trim().isNullOrEmpty()) {
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

    override fun onContextItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.view_profile -> {
//                val intent = Intent(this, UserProfileActivity::class.java)
//                intent.putExtra(NewConversationActivity.USER_KEY, intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY))
//                startActivity(intent)
            }
            R.id.search_chat -> {
                // handle menu item press here
            }
            R.id.mute_notifications -> {
                // handle menu item press here
            }
        }

        return super.onContextItemSelected(item)
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