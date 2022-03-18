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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.nav_header.*
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerDialog


private lateinit var firebaseAnalytics: FirebaseAnalytics
class SettingsActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_settings) // set the layout of the activity
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

        button_save_bio.setOnClickListener {
            val bio = edit_text_bio_settings.text.toString().trim()
            val userRef = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/bio")
            userRef.setValue(bio)
            Toast.makeText(this, "Bio saved", Toast.LENGTH_SHORT).show()
        }

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
        val ref = FirebaseDatabase.getInstance().getReference("/users/$user")
        var getcolor = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val color = p0.getValue(User::class.java)
                if (color?.color != null) {
                    Log.d("SettingsActivity", "Color is: ${color.color}")
                    val userColor = color.color
                    Log.d("SettingsActivity", "Color is: $color")
                    val topappbar = findViewById<MaterialToolbar>(R.id.topAppBar_settings)
                    topappbar.setBackgroundColor(color.color.toInt())
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color.color.toInt()

                    button_color.setBackgroundColor(color.color.toInt())
//                    val headerlayout = findViewById<Constraints>(R.id.header_layout)
                    header_layout.setBackgroundColor(color.color.toInt())
                    val userSettingsImage = findViewById<CircleImageView>(R.id.user_profile_image_settings)
                    Picasso.get().load(color.profileImageUrl).into(userSettingsImage)
                    val userSettingsName = findViewById<TextView>(R.id.text_username_settings)
                    userSettingsName.text = color.username
                    val userSettingsEmail = findViewById<TextView>(R.id.text_email_settings)
                    userSettingsEmail.text = color.email
                    val userSettingsBio = findViewById<EditText>(R.id.edit_text_bio_settings)
                    userSettingsBio.setText(color.bio)
                } else {
                    Log.d("SettingsActivity", "Color is: null")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("SettingsActivity", "Failed to read color")
            }
        })

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar_settings)
        setSupportActionBar(toolbar)

        button_color.setOnClickListener {
            ColorPickerDialog.Builder()
                .setInitialColor(0x00BCD4)
                .setColorModel(ColorModel.RGB)
                .setButtonOkText(android.R.string.ok)
                .setButtonCancelText(android.R.string.cancel)
                .onColorSelected { color ->
                    Log.d("Colour", "Colour: $color")
                    val colorInt = color.toString()
                    val colorobj = GlobalColor(colorInt)
                    Log.d("Colour", "Colour: ${colorobj.color}")
                    val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}/color")
                    ref.setValue(colorInt).addOnSuccessListener {
                        Log.d("Colour", "Colour: $colorInt")
                        button_color.setBackgroundColor(color)
                    } .addOnFailureListener {
                        Log.d("Colour", "Colour: $it")
                    }
                    topAppBar_settings.setBackgroundColor(color)
                    header_layout.setBackgroundColor(color)
                }
                .create()
                .show(supportFragmentManager, "color_picker")
        }



        verifyUserIsLoggedIn()

        fetchUserInfo()

        topAppBar_settings.setNavigationOnClickListener { // set the navigation icon on the top app bar
            drawer_layout.openDrawer(GravityCompat.START) // open the drawer
        }

        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close) // create a toggle for the drawer
        drawer_layout.addDrawerListener(toggle) // add the toggle to the drawer layout
        toggle.syncState() // sync the toggle state

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // enable the back button on the top app bar


        navigation_drawer.setNavigationItemSelectedListener { // Set the navigation click listener
            when(it.itemId){ // check which item was clicked
                R.id.conversations -> { // if the item was the second item
                    val intent = Intent(this, ConversationsActivity::class.java) // create an intent to go to the Conversations activity
                    startActivity(intent) // start the intent
                    drawer_layout.closeDrawer(GravityCompat.START)
                }
                R.id.settings -> { // if the item was the third item
                    drawer_layout.closeDrawer(GravityCompat.START)
                }
                R.id.logout -> { // if the item was the fourth item
                    // Create a dialog to confirm the user wants to log out
                    val builder = AlertDialog.Builder(this)
                    val title =
                        SpannableString("Log out?") // Set title to the text "Creating Account"
                    title.setSpan( // Set the alignment of the text to center
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment to center
                        0, // Set the start of the alignment to 0
                        title.length, // Set the end of the alignment to the length of the text
                        0 // Set the flags to 0
                    ) // End the alignment
                    builder.setTitle(title)
                    val message = SpannableString("Are you sure you want to log out?")
                    message.setSpan( // Set the alignment of the text to center
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment to center
                        0, // Set the start of the alignment to 0
                        message.length, // Set the end of the alignment to the length of the text
                        0 // Set the flags to 0
                    ) // End the alignment
                    builder.setMessage(message)
                    builder.setPositiveButton("Yes") { _, _ ->
                        val statusRef = FirebaseDatabase.getInstance().getReference("/status/${FirebaseAuth.getInstance().uid}/lastSeen")
                        statusRef.setValue(System.currentTimeMillis() / 1000)
                        FirebaseAuth.getInstance().signOut() // log out the user
                        val intent = Intent(this, LoginActivity::class.java) // create an intent to go to the login activity
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) // clear the task and start the login activity
                        startActivity(intent) // start the intent
                    }
                    builder.setNegativeButton("No") { _, _ ->
                        drawer_layout.closeDrawer(GravityCompat.START) // close the drawer
                    }
                    builder.show() // show the dialog

                }
            }
            //drawer_layout.closeDrawer(navigation_drawer) // close the drawer
            true // return true to indicate that the item was selected
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // override the onOptionsItemSelected method

        if (toggle.onOptionsItemSelected(item)) { // if the toggle was selected
            return true // return true
        }
        return super.onOptionsItemSelected(item) // return the super class onOptionsItemSelected method
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid // get the current user's uid
        if (uid == null) { // if the user is not logged in
            val intent = Intent(
                this,
                LoginActivity::class.java
            ) // create an intent to go to the login activity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) // clear the task and start a new one
            startActivity(intent) // start the intent
        }
    }

    private fun fetchUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                ConversationsActivity.currentUser = p0.getValue(User::class.java)
                Log.d("ConversationsActivity","Current user: ${ConversationsActivity.currentUser?.username}")
                navigation_drawer.findViewById<TextView>(R.id.username_nav_header).text = ConversationsActivity.currentUser?.username
                val email = ConversationsActivity.currentUser?.email
                Log.d("ConversationsActivity","Current user email: $email")
                navigation_drawer.findViewById<TextView>(R.id.email_nav_header).text = ConversationsActivity.currentUser?.email
                val navHeaderImage = findViewById<CircleImageView>(R.id.nav_header_image)
                Picasso.get().load(ConversationsActivity.currentUser?.profileImageUrl).into(navHeaderImage)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("ConversationsActivity", "Error: ${p0.message}")
            }
        })
    }

//    private fun updateUserStatus(status: Boolean, lastSeen: Long?) {
//        if (FirebaseAuth.getInstance().uid == null) return // if the user is not logged in, return
//        else {
//            val refStatus = FirebaseDatabase.getInstance()
//                .getReference("/status/${FirebaseAuth.getInstance().uid}/status")
//            refStatus.setValue(status)
//            val refLastSeen = FirebaseDatabase.getInstance()
//                .getReference("/status/${FirebaseAuth.getInstance().uid}/lastSeen")
//            refLastSeen.setValue(lastSeen)
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        val statusRef = FirebaseDatabase.getInstance().getReference("/status/${FirebaseAuth.getInstance().uid}/status")
//        statusRef.setValue(false)
//        val lastSeenRef = FirebaseDatabase.getInstance().getReference("/status/${FirebaseAuth.getInstance().uid}/lastSeen")
//        lastSeenRef.setValue(System.currentTimeMillis() / 1000)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.d("ConversationsActivity", "onResume")
//        // set a timer to update the user status every 5 seconds
//        updateUserStatus(true, -1)
//    }
}
