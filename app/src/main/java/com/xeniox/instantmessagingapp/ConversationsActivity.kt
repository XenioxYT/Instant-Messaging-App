package com.xeniox.instantmessagingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xeniox.instantmessagingapp.NewConversationActivity.Companion.USER_KEY
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations.*
import kotlinx.android.synthetic.main.nav_header.*

class ConversationsActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        verifyUserIsLoggedIn()
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_conversations) // set the layout of the activity

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
        builder.setMessage("Please wait while we load the users") // Set the message of the alert dialog to "There was an error connecting to the servers. Please check your internet connection and try again."
        val dialog: AlertDialog = builder.create() // Create a dialog variable with the builder
        dialog.setCancelable(false)
        dialog.show() // Show the dialog


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
                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar)
                    topappbar.setBackgroundColor(color.color.toInt())

                    // Set the colour of the nav header
                    header_layout.setBackgroundColor(color.color.toInt())

                    // Set the colour of the status bar.
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color.color.toInt()

                    // Set other colours here


                } else {
                    Log.d("SettingsActivity", "Color is: null")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("SettingsActivity", "Failed to read color")
            }
        })

        recycler_view_all_conversations.adapter = adapter // set the adapter to the recycler view
        recycler_view_all_conversations.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ConversationsChatActivity::class.java)

            // we are missing the chat partner user

            val row = item as LatestMessageRow
            row.chatPartnerUser?.let {
                intent.putExtra(USER_KEY, it)
                startActivity(intent)
            }
        }
        fetchCurrentUser(dialog)


//        setupDummyRows()

        listenForConversations()

        topAppBar.setNavigationOnClickListener { // set the navigation icon on the top app bar
            drawer_layout.openDrawer(GravityCompat.START) // open the drawer
        }
        floating_action_button_start_chat.setOnClickListener {
            val intent = Intent(this, NewConversationActivity::class.java)
            startActivity(intent)
        }
        toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            R.string.open,
            R.string.close
        ) // create a toggle for the drawer
        drawer_layout.addDrawerListener(toggle) // add the toggle to the drawer layout
        toggle.syncState() // sync the toggle state

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // enable the back button on the top app bar

        navigation_drawer.setNavigationItemSelectedListener { // Set the navigation click listener
            when (it.itemId) { // check which item was clicked
                R.id.conversations -> { // if the conversations item was clicked
                    drawer_layout.closeDrawer(GravityCompat.START) // close the drawer
                }
                R.id.settings -> { // if the settings item was clicked
                    val intent = Intent(
                        this,
                        SettingsActivity::class.java
                    ) // create an intent to go to the conversations activity
                    startActivity(intent) // start the intent
                    drawer_layout.closeDrawer(GravityCompat.START)
                }
                R.id.logout -> { // if logout was clicked
                    FirebaseAuth.getInstance().signOut() // sign out of firebase
                    val intent = Intent(this, LoginActivity::class.java) // create an intent to go to the login activity
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // set the flags
                    startActivity(intent) // start the intent
                    drawer_layout.closeDrawer(GravityCompat.START)
                }
            }
            //drawer_layout.closeDrawer(navigation_drawer_conversations) // close the drawer
            true // return true to indicate that the item was selected
        }
    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun listenForConversations() {
        val fromId = FirebaseAuth.getInstance().uid // get the current user's id
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId") // get the reference to the user-conversations node
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return // get the chat message
                latestMessagesMap[snapshot.key!!] = chatMessage // add the chat message to the map
                refreshRecyclerViewMessages() // refresh the recycler view
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return // get the chat message
                latestMessagesMap[snapshot.key!!] = chatMessage // add the chat message to the map
                refreshRecyclerViewMessages() // refresh the recycler view
            }

            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
        })
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear() // clear the adapter
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }



    val adapter = GroupAdapter<ViewHolder>() // create a new group adapter

    private fun fetchCurrentUser(dialog: AlertDialog) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("ConversationsActivity","Current user: ${currentUser?.username}")
                if (currentUser?.username != null && currentUser?.email != null) {
                    navigation_drawer.findViewById<TextView>(R.id.username_nav_header).text =
                        currentUser?.username
                    val email = currentUser?.email
                    Log.d("ConversationsActivity", "Current user email: $email")
                    navigation_drawer.findViewById<TextView>(R.id.email_nav_header).text =
                        currentUser?.email
                    dialog.dismiss()
                    Picasso.get().load(currentUser?.profileImageUrl)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("ConversationsActivity", "Error: ${p0.message}")
            }
        })
    }



    private fun verifyUserIsLoggedIn() { // verify the user is logged in
        val uid = FirebaseAuth.getInstance().uid // get the current user's uid
        Log.d("ConversationsActivity", "uid: $uid") // log the uid
        if (uid == null) { // if the user is not logged in
            Log.d("ConversationsActivity", "User is not logged in") // log the user is not logged in
            val intent = Intent(
                this,
                LoginActivity::class.java
            ) // create an intent to go to the login activity
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) // clear the task and start a new one
            startActivity(intent) // start the intent
            finish()
        } else {
            Log.d("ConversationsActivity", "User is logged in") // log the user is logged in
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean { // override the onOptionsItemSelected method

        if (toggle.onOptionsItemSelected(item)) { // if the toggle was selected
            return true // return true
        }
        return super.onOptionsItemSelected(item) // return the super class onOptionsItemSelected method
    }
}
