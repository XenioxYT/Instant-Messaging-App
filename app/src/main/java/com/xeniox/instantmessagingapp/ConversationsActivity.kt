package com.xeniox.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.xeniox.instantmessagingapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_conversations.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.view.*

class ConversationsActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        verifyUserIsLoggedIn()
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_conversations) // set the layout of the activity

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
                }
                R.id.logout -> { // if logout was clicked
                    FirebaseAuth.getInstance().signOut() // sign out of firebase
                    val intent = Intent(this, LoginActivity::class.java) // create an intent to go to the login activity
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // set the flags
                    startActivity(intent) // start the intent
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

//    private fun changeUsernameEmailNavHeader() {
//        Log.d(
//            "ConversationsActivity",
//            "changeUsernameEmailNavHeader"
//        ) // log the changeUsernameEmailNavHeader method
//
//        val email = FirebaseAuth.getInstance().currentUser?.email // get the current user's email
//        val username = FirebaseAuth.getInstance().currentUser?.displayName // get the current user's username
//        Log.d("ConversationsActivity", "email: $email") // log the email
//        Log.d("ConversationsActivity", "username: $username") // log the username
//        try {
//            setContentView(R.layout.nav_header)
//            username_nav_header.text = email
//            Log.d("ConversationsActivity", "email set successfully") // log the email
//        }
//        catch (e: Exception) {
//            Log.d("ConversationsActivity", "username_nav_header: $e") // log the exception
//        }
}