package com.xeniox.instantmessagingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_conversation.*
import kotlinx.android.synthetic.main.user_row_new_conversation.view.*


private lateinit var firebaseAnalytics: FirebaseAnalytics
class NewConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)
        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        val presenceRef = FirebaseDatabase.getInstance().getReference("/status/${FirebaseAuth.getInstance().uid}/lastSeen")
        presenceRef.onDisconnect().setValue(System.currentTimeMillis() / 1000)
        presenceRef.setValue(-1)

//        val statusRef = FirebaseDatabase.getInstance().getReference("status/${FirebaseAuth.getInstance().uid}/")
//        statusRef.addValueEventListener(object : ValueEventListener {
//            override fun onCancelled(p0: DatabaseError) {
////                TODO("Not yet implemented")
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                val userStatus = p0.getValue(Status::class.java)
//                if (userStatus != null) {
//                    if (!userStatus.status) {
//                        val statusRef = FirebaseDatabase.getInstance().getReference("status/${FirebaseAuth.getInstance().uid}/status")
//                        statusRef.setValue(true)
//                        val lastSeenRef = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/lastSeen")
//                        lastSeenRef.setValue(-1)
//                    }
//                }
//            }
//        })

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
                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_new_conversation)
                    topappbar.setBackgroundColor(color.color.toInt())

                    // Set the colour of the status bar.
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color.color.toInt()

                } else {
                    Log.d("SettingsActivity", "Color is: null")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("SettingsActivity", "Failed to read color")
            }
        })

        recyclerView_new_conversation.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

//        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar_new_conversation)
//        setSupportActionBar(toolbar)

        val title =
            SpannableString("Loading") // Set the title variable to "No internet connection"
        title.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment of the text to center
            0, // Start at the beginning of the string
            title.length, // End at the end of the string
            0 // No flags
        ) // End the setSpan function
        val builder = AlertDialog.Builder(this) // Create a builder variable
        builder.setTitle(title) // Set the title of the alert dialog to the title variable
        builder.setMessage("Please wait while we load your contacts")
        val dialog: AlertDialog = builder.create() // Create a dialog variable with the builder
        dialog.setCancelable(false)
        dialog.show() // Show the dialog


        topAppBar_new_conversation.setNavigationOnClickListener { // set the navigation icon on the top app bar
            val intent = Intent(this, ConversationsActivity::class.java) // end the activity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        fetchUsers(dialog)
    }

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(dialog: AlertDialog) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {


                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                        adapter.add(UserItem(user))
                    }
                    dialog.dismiss()
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent = Intent(view.context, ConversationsChatActivity::class.java)
//                    intent.putExtra(USER_KEY, userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }
                recyclerView_new_conversation.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@NewConversationActivity, "Failed to read value.", Toast.LENGTH_SHORT).show()
            }

        })
    }
}

class UserItem(val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_username_new_conversation.text = user.username
        viewHolder.itemView.text_email_new_conversation.text = user.email
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_conversation
    }
}