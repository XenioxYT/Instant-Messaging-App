package com.xeniox.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_conversations.*

class ConversationsActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        verifyUserIsLoggedIn()
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_conversations) // set the layout of the activity
        fetchCurrentUser()
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
            //drawer_layout.closeDrawer(navigation_drawer) // close the drawer
            true // return true to indicate that the item was selected
        }
    }

    private fun fetchCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("ConversationsActivity","Current user: ${currentUser?.username}")
                navigation_drawer.getHeaderView(0).findViewById<TextView>(R.id.username_nav_header).text = currentUser?.username
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("ConversationsActivity", "Error: ${p0.message}")
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean { // override the onOptionsItemSelected method

        if (toggle.onOptionsItemSelected(item)) { // if the toggle was selected
            return true // return true
        }
        return super.onOptionsItemSelected(item) // return the super class onOptionsItemSelected method
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
}