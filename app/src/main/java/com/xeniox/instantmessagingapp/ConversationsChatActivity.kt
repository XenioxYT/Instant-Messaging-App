@file:Suppress("NAME_SHADOWING")

package com.xeniox.instantmessagingapp

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult.STATUS_SUCCESS
import com.google.mlkit.nl.smartreply.TextMessage
import com.livinglifetechway.k4kotlin.core.toast
import com.squareup.picasso.Picasso
import com.xeniox.instantmessagingapp.ConversationsActivity.Companion.currentUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations_chat.*
import kotlinx.android.synthetic.main.chat_from_message.view.*
import kotlinx.android.synthetic.main.chat_to_message.view.*
import java.text.SimpleDateFormat
import java.util.*


class ConversationsChatActivity : AppCompatActivity() {

    companion object{
        const val TAG = "ConversationsChatActivity"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var conversations:ArrayList<TextMessage> = ArrayList()
    private var suggestion1: SmartReplySuggestion? = null
    private var suggestion2: SmartReplySuggestion? = null
    private var suggestion3: SmartReplySuggestion? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations_chat)
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d("TOKEN", it)
            val tokenRef = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/regToken")
            tokenRef.setValue(it)
        }
        Log.d(TAG, "pushNotifications${FirebaseAuth.getInstance().uid}${intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid}")
//        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications${intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid}${FirebaseAuth.getInstance().uid}")

        // set a timer to run this code every 10 seconds
        val presenceRef = FirebaseDatabase.getInstance().getReference("/status/${FirebaseAuth.getInstance().uid}/lastSeen")
        presenceRef.onDisconnect().setValue(System.currentTimeMillis() / 1000)
        presenceRef.setValue(-1)

        val mutedRef = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/muted/${intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid}")
        mutedRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "onCancelled")
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                val muted = p0.getValue(Boolean::class.java)
                Log.d("MUTED", "$muted")
                if (muted != null && muted) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications${intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid}${FirebaseAuth.getInstance().uid}")
//                    val mutedText = findViewById<TextView>(R.id.mute_notifications)
                    toast("You have muted notifications from this user")
//                    mutedText.text = "Notifications Muted"
                } else {
//                    val mutedText = findViewById<TextView>(R.id.mute_notifications)
                    toast("You have unmuted notifications from this user")
//                    mutedText.text = "Mute Notifications"
                    FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications${intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid}${FirebaseAuth.getInstance().uid}")
                }
            }
        })

//         Set custom colours here:
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val refColor = FirebaseDatabase.getInstance().getReference("/users/$user")
        refColor.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val color = p0.getValue(User::class.java)
                if (color?.color != null) {
                    Log.d("SettingsActivity", "Color is: ${color.color}")
                    color.color
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
//                    val userPic = findViewById<CircleImageView>(R.id.chat_from_image)


                } else {
                    Log.d("SettingsActivity", "Color is: null")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("SettingsActivity", "Failed to read color")
            }
        })



        recyclerView_chat_conversation.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            recyclerView_chat_conversation.scrollToPosition(
                adapter.itemCount - 1
            )
        }

        button_send_message.visibility = View.GONE

        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid
        val username = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.username
        val bio = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.bio
        val otherUserStatus = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.shortStatus
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        val smartReplyGenerator = SmartReply.getClient()
        val otherUserEmail = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.email
        val userProfileImageUrl = intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.profileImageUrl

        val toStatusRef = FirebaseDatabase.getInstance().getReference("/status/$toId/")
        toStatusRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SimpleDateFormat")
            override fun onDataChange(p0: DataSnapshot) {
                val status = p0.getValue(Status::class.java)
                if (status?.lastSeen != -1L) {
                    Log.d("ConversationsChatActivity", "Status is: ${status?.lastSeen}")
//                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
                    topAppBar_chat_conversation.subtitle = "Offline"
                    val dateFormat = SimpleDateFormat("MMM dd HH:mm")
                    val date = Date(status?.lastSeen!! * 1000)
                    val lastSeen = dateFormat.format(date)

                    val currentDate = getDateTimeFormat(System.currentTimeMillis() / 1000)
                    Log.d("ConversationsChatActivity", "Current date is: $currentDate")
                    Log.d("ConversationsChatActivity", "Last seen is: $lastSeen")

                    if (currentDate.substring(0, 5) == lastSeen.substring(0, 5)) {
                        Log.d("ConversationsChatActivity", "Date is: ${currentDate.substring(7, lastSeen.length)}")
                        topAppBar_chat_conversation.subtitle = "Last seen today at ${lastSeen.substring(7, lastSeen.length).trim()}"
                        topAppBar_chat_conversation.isSubtitleCentered = true

                    } else {
                        topAppBar_chat_conversation.subtitle = "Last seen ${lastSeen.trim()}"
                    }
                } else {
                    Log.d("ConversationsChatActivity", "Status is: null")
                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
                    topappbar.subtitle = "Online"
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("ConversationsChatActivity", "Failed to read status")
            }
        })

        val typingRef = FirebaseDatabase.getInstance().getReference("/users/$toId/")
        typingRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SimpleDateFormat")
            override fun onDataChange(p0: DataSnapshot) {
                intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid
                val user = p0.getValue(User::class.java)
                if (user?.typing != null) {
                    Log.d("SettingsActivity", "Typing is: ${user.typing}")
                    findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
                    if (user.typing == fromId) {

//                        Toast.makeText(this@ConversationsChatActivity, "Typing...", Toast.LENGTH_SHORT).show()

                        //display the typing indicator


                        topAppBar_chat_conversation.subtitle = "Typing..."
                        // 10/03/22 - Typing displays globally and not just in the conversation
                        // TODO: Fix this
                        // This can be done by checking the UID of the user who is typing and then
                        // checking if it is the same as the UID of the user who is logged in.
                        // If it is the same, then don't show the typing indicator.
                        // If it is not the same, then show the typing indicator.
                        // This is because the typing indicator is only displayed in the conversation
                        // and not globally.
                        // If the typing indicator is displayed globally, then the user will see
                        // the typing indicator for every conversation they are in.
                        // This is not ideal.


                        // Something like this should be done:
                        // If the typingToId is the same as the UID of the user who is being messaged,
                        // then show the typing indicator.
                        // If the typingToId is not the same as the UID of the user who is being messaged,
                        // then don't show the typing indicator.

                        Log.d("SettingsActivity", "Typing is: true")
//                        textView_typing.visibility = View.VISIBLE
//                    } else {
//                        Log.d("SettingsActivity", "Typing is: false")
////                        textView_typing.visibility = View.GONE
//                        if (user.status) {
//                            topAppBar_chat_conversation.subtitle = "Online"
//                        } else {
////                            topAppBar_chat_conversation.subtitle = "Offline"
//                            val dateFormat = SimpleDateFormat("dd/MMM/yy HH:mm")
//                            val date = Date(user.lastSeen)
//                            val lastSeen = dateFormat.format(date)
//
//                            val currentDate = getDateTimeFormat(user.lastSeen, "dd/MMM/yy HH:mm")
//
//                            if (currentDate.substring(0, 9) == lastSeen.substring(0, 9)) {
//                                topAppBar_chat_conversation.subtitle = "Last seen at ${lastSeen.substring(10, lastSeen.length).trim()}"
//                                topAppBar_chat_conversation.isSubtitleCentered = true
//
//                            } else {
//                                topAppBar_chat_conversation.subtitle = "Last seen at ${lastSeen.trim()}"
//                            }
//
//
//
////                            topappbar.subtitle = "Last seen on" +
////                                    " $lastSeen"
////                            topappbar.isSubtitleCentered = true
//                        }
//                    }
                    } else {
                        Log.d("SettingsActivity", "Typing is: null")
//                        topAppBar_chat_conversation.subtitle = "Online"
                        val toStatusRef = FirebaseDatabase.getInstance().getReference("/status/$toId/")
                        toStatusRef.addValueEventListener(object : ValueEventListener {
                            @SuppressLint("SimpleDateFormat")
                            override fun onDataChange(p0: DataSnapshot) {
                                val status = p0.getValue(Status::class.java)
                                if (status?.lastSeen != -1L) {
                                    Log.d("ConversationsChatActivity", "Status is: ${status?.lastSeen}")
//                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
                                    topAppBar_chat_conversation.subtitle = "Offline"
                                    val dateFormat = SimpleDateFormat("MMM dd HH:mm")
                                    val date = Date(status?.lastSeen!! * 1000)
                                    val lastSeen = dateFormat.format(date)

                                    val currentDate = getDateTimeFormat(System.currentTimeMillis() / 1000)
                                    Log.d("ConversationsChatActivity", "Current date is: $currentDate")
                                    Log.d("ConversationsChatActivity", "Last seen is: $lastSeen")

                                    if (currentDate.substring(0, 5) == lastSeen.substring(0, 5)) {
                                        Log.d("ConversationsChatActivity", "Date is: ${currentDate.substring(7, lastSeen.length)}")
                                        topAppBar_chat_conversation.subtitle = "Last seen today at ${lastSeen.substring(7, lastSeen.length).trim()}"
                                        topAppBar_chat_conversation.isSubtitleCentered = true

                                    } else {
                                        topAppBar_chat_conversation.subtitle = "Last seen ${lastSeen.trim()}"
                                    }
                                } else {
                                    Log.d("ConversationsChatActivity", "Status is: null")
                                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_chat_conversation)
                                    topappbar.subtitle = "Online"
                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {
                                Log.d("ConversationsChatActivity", "Failed to read status")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

        editText_chat_conversation.addTextChangedListener(
            object : TextWatcher {

                private var timer: Timer = Timer()
                private val DELAY: Long = 1000 // Milliseconds


                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty()) {
                        button_send_message.visibility = View.VISIBLE
                    } else {
                        button_send_message.visibility = View.GONE
                    }
                    timer.cancel()
                    timer = Timer()
                    timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                // TODO: Do what you need here (refresh list).
                                // You will probably need to use
                                // runOnUiThread(Runnable action) for some
                                // specific actions (e.g., manipulating views).
                                val user = FirebaseAuth.getInstance().currentUser?.uid
                                val refTyping = FirebaseDatabase.getInstance().getReference("/users/$user/typing")
                                refTyping.setValue("null")
                            }
                        },
                        DELAY
                    )
//                    val user = FirebaseAuth.getInstance().currentUser?.uid
//                    val refColor = FirebaseDatabase.getInstance().getReference("/users/$user/isTyping")
//                    refColor.setValue(false)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    timer.cancel()
                    timer = Timer()
                    timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                // TODO: Do what you need here (refresh list).
                                // You will probably need to use
                                // runOnUiThread(Runnable action) for some
                                // specific actions (e.g., manipulating views).
                                val user = FirebaseAuth.getInstance().currentUser?.uid
                                val refTyping = FirebaseDatabase.getInstance().getReference("/users/$user/typing")
                                refTyping.setValue("null")
                            }
                        },
                        DELAY
                    )
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val user = FirebaseAuth.getInstance().currentUser?.uid
                    val refTyping = FirebaseDatabase.getInstance().getReference("/users/$user/typing")
                    refTyping.setValue(toId)
                }
            }
        )


        topAppBar_chat_conversation.subtitle = "Offline"

        topAppBar_chat_conversation.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_user_profile, null)
            Log.d(TAG, "onCreate: $toId")
            Log.d(TAG, "onCreate: $otherUserEmail")

            val textEmail = view.findViewById<TextView>(R.id.text_email_profile)
            val textUsername = view.findViewById<TextView>(R.id.text_username_profile)
            val userProfileImage = view.findViewById<ImageView>(R.id.user_profile_image)
            val textBio = view.findViewById<TextView>(R.id.text_bio_profile)
            val textStatus = view.findViewById<TextView>(R.id.text_status_profile)

            textEmail.text = otherUserEmail
            textUsername.text = username
            textBio.text = bio
            textStatus.text = otherUserStatus

            Picasso.get().load(userProfileImageUrl).into(userProfileImage)

            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }

        topAppBar_chat_conversation.setOnMenuItemClickListener {

            val muteRef = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().currentUser?.uid}/muted/$toId")
            val muted = muteRef.get()
            Log.d(TAG, "onCreate: $muted")

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

                    textEmail.text = otherUserEmail
                    textUsername.text = username
                    textBio.text = bio

                    Picasso.get().load(userProfileImageUrl).into(userProfileImage)

                    dialog.setCancelable(true)
                    dialog.setContentView(view)
                    dialog.show()
                }
                R.id.search_chat -> {

                }
                R.id.mute_notifications -> {
                    Toast.makeText(this, "Mute Notifications", Toast.LENGTH_SHORT).show()
//                    FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications${intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.uid}${FirebaseAuth.getInstance().uid}")
                    // set the text to "notifications muted"
                    if (it.title == "Notifications Muted") {
                        it.title = "Mute Notifications"
                        FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/muted/$toId").setValue(false)
                        Log.d(TAG, "set value to false")
                    } else {
                        it.title = "Notifications Muted"
                        FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/muted/$toId").setValue(true)
                        Log.d(TAG, "set value to true")
                    }
//                    it.title = "Notifications Muted"
//                    it.icon = ContextCompat.getDrawable(this, R.drawable.ic_account_circle_black_24dp)
//                    val mutedRef = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/muted/$toId")
//                    mutedRef.setValue(true)


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
                        button_reply1.visibility = View.GONE
                        button_reply2.visibility = View.GONE
                        button_reply3.visibility = View.GONE
                        button_reply1.height = 0
                        button_reply2.height = 0
                        button_reply3.height = 0
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
                        button_reply1.visibility = View.VISIBLE
                        button_reply2.visibility = View.VISIBLE
                        button_reply3.visibility = View.VISIBLE
                        button_reply1.height = 40
                        button_reply2.height = 40
                        button_reply3.height = 40
                        recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                    }
                    // TODO: delay this til after all the conversations have been loaded

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
                                val suggestion1 = it.suggestions[0].text
                                if (suggestion1.isEmpty()) {
                                    button_reply1.visibility = View.GONE
                                    button_reply1.height = 0
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                } else {
                                    button_reply1.text = suggestion1
                                }
                                val suggestion2 = it.suggestions[1].text
                                if (suggestion2.isEmpty()) {
                                    button_reply2.visibility = View.GONE
                                    button_reply2.height = 0
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                } else {
                                    button_reply2.text = suggestion2
                                }
                                val suggestion3 = it.suggestions[2].text
                                if (suggestion3.isEmpty()) {
                                    button_reply3.visibility = View.GONE
                                    button_reply3.height = 0
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                } else {
                                    button_reply3.text = suggestion3
                                }
                                button_reply1.setOnClickListener {
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                    Log.d(TAG, "Reply 1")
                                    performSendMessageSR(suggestion1)
                                    button_reply1.text = suggestion1
                                }
                                button_reply1.setOnLongClickListener {
                                    Log.d(TAG, "Reply 1 long")
                                    button_reply1.text = suggestion1
                                    editText_chat_conversation.append(suggestion1)
                                    true
                                }
                                button_reply2.setOnClickListener {
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                    Log.d(TAG, "Reply 2")
                                    performSendMessageSR(suggestion2)
                                    button_reply2.text = suggestion2
                                }
                                button_reply2.setOnLongClickListener {
                                    Log.d(TAG, "Reply 2 long")
                                    button_reply2.text = suggestion2
                                    editText_chat_conversation.append(suggestion2)
                                    true
                                }
                                button_reply3.setOnClickListener {
                                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                                    Log.d(TAG, "Reply 3")
                                    performSendMessageSR(suggestion3)
                                    button_reply3.text = suggestion3
                                }
                                button_reply3.setOnLongClickListener {
                                    Log.d(TAG, "Reply 3 long")
                                    button_reply3.text = suggestion3
                                    editText_chat_conversation.append(suggestion3)
                                    true
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
        SmartReply.getClient()
        ref.addChildEventListener(object: ChildEventListener {
            @SuppressLint("SimpleDateFormat", "WeekBasedYear")
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val timeAndDate = getDateTime(chatMessage.timestamp)
                        val date = Date(System.currentTimeMillis())
                        val format = SimpleDateFormat("EE dd MMM YYYY - HH:mm")
                        val currentDate = format.format(date)
                        // check if the current date is the same as the date of the message and remove the date from the message if it is
                        if (currentDate.substring(0, 18) == timeAndDate.substring(0, 18)) {
                            adapter.add(ChatToItem(chatMessage.text, ConversationsActivity.currentUser!!, timeAndDate.substring(18, timeAndDate.length)))
                        } else {
                            adapter.add(
                                ChatToItem(
                                    chatMessage.text,
                                    ConversationsActivity.currentUser!!,
                                    timeAndDate
                                )
                            )
//                        conversations.add(TextMessage.createForLocalUser(chatMessage.text, System.currentTimeMillis()))
                        }
                        recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)
                        return
                    } else {
                        val timeAndDate = getDateTime(chatMessage.timestamp)
                        val date = Date(System.currentTimeMillis())
                        val format = SimpleDateFormat("EE dd MMM YYYY - HH:mm")
                        val currentDate = format.format(date)
                        if (currentDate.substring(0, 18) == timeAndDate.substring(0, 18)) {
                            adapter.add(ChatFromItem(chatMessage.text, ConversationsActivity.currentUser!!, timeAndDate.substring(18, timeAndDate.length)))
                        } else {
                            adapter.add(
                                ChatFromItem(
                                    chatMessage.text,
                                    intent.getParcelableExtra(NewConversationActivity.USER_KEY)!!,
                                    timeAndDate
                                )
                            )
//                        conversations.add(TextMessage.createForRemoteUser(chatMessage.text, System.currentTimeMillis(), fromId!!))
                        }
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

        if (editText_chat_conversation.text.trim().isEmpty()) {
            Log.d("chat", "message is null")
            return
        } else {
            editText_chat_conversation.text.trim().let { it ->
                reference.setValue(
                    ChatMessage(
                        reference.key!!,
                        it.toString(),
                        fromId,
                        toId,
                        System.currentTimeMillis() / 1000,
                        currentUser!!.username
                    )
                ).addOnSuccessListener {
                    Log.d("ConversationsChatActivity", "Message sent... $it")
                    editText_chat_conversation.text.clear()
                    editText_chat_conversation.text.insert(0, "")
                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)

                }
                toReference.setValue(ChatMessage(toReference.key!!, it.toString(), fromId, toId, System.currentTimeMillis() / 1000, currentUser!!.username))
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
                latestMessageRef.setValue(ChatMessage(reference.key!!, editText_chat_conversation.text.toString(), fromId, toId, System.currentTimeMillis() / 1000, currentUser!!.username))
                val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
                latestMessageToRef.setValue(ChatMessage(toReference.key!!, editText_chat_conversation.text.toString(), fromId, toId, System.currentTimeMillis() / 1000, intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.username))

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
            editText_chat_conversation.text.trim().let {
                reference.setValue(
                    ChatMessage(
                        reference.key!!,
                        reply,
                        fromId,
                        toId,
                        System.currentTimeMillis() / 1000,
                        currentUser!!.username
                    )
                ).addOnSuccessListener {
                    Log.d("ConversationsChatActivity", "Message sent... $it")
                    editText_chat_conversation.text.clear()
                    editText_chat_conversation.text.insert(0, "")
                    recyclerView_chat_conversation.scrollToPosition(adapter.itemCount - 1)

                }
                toReference.setValue(ChatMessage(toReference.key!!, reply, fromId, toId, System.currentTimeMillis() / 1000, currentUser!!.username))
                val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
                latestMessageRef.setValue(ChatMessage(reference.key!!, reply, fromId, toId, System.currentTimeMillis() / 1000, currentUser!!.username))
                val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
                latestMessageToRef.setValue(ChatMessage(toReference.key!!, reply, fromId, toId, System.currentTimeMillis() / 1000, intent.getParcelableExtra<User>(NewConversationActivity.USER_KEY)!!.username))
            }
    }

    @SuppressLint("SimpleDateFormat", "WeekBasedYear")
    private fun getDateTime(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("EE dd MMM YYYY - HH:mm")
        return format.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTimeFormat(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("MMM dd HH:mm")
        return format.format(date)
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

    override fun onResume() {
        super.onResume()
        closeAllNotifications();
    }

    private fun closeAllNotifications() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}

class ChatFromItem(val text: String, val user: User, private val timedate: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_from_message.text = text
        viewHolder.itemView.text_from_message_date_time.text = timedate

        user.profileImageUrl
//        val targetImageView = viewHolder.itemView.chat_from_image

//        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_message
    }
}

class ChatToItem(val text: String, val user: User, private val timedate: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_to_message.text = text
        viewHolder.itemView.text_to_message_date_time.text = timedate

        user.profileImageUrl
//        val targetImageView = viewHolder.itemView.chat_to_image

//        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_message
    }
}