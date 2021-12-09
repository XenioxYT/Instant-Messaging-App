package com.example.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_conversations.*

class ConversationsActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_conversations) // set the layout of the activity

        verifyUserIsLoggedIn()

        topAppBar.setNavigationOnClickListener { // set the navigation icon on the top app bar
            drawer_layout.openDrawer(GravityCompat.START)
        verifyUserIsLoggedIn()// open the drawer
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
                R.id.conversations -> { // if the item was the second item
                    drawer_layout.closeDrawer(GravityCompat.START) // close the drawer
                }
                R.id.settings -> { // if the item was the third item
                    val intent = Intent(
                        this,
                        SettingsActivity::class.java
                    ) // create an intent to go to the conversations activity
                    startActivity(intent) // start the intent
                }
                R.id.logout -> { // if the item was the fourth item
                    val intent = Intent(
                        this,
                        LoginActivity::class.java
                    ) // create an intent to go to the login activity
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
        }
        else {
            Log.d("ConversationsActivity", "User is logged in") // log the user is logged in
        }
    }
}