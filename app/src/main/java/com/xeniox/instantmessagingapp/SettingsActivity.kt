package com.xeniox.instantmessagingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_settings) // set the layout of the activity

        verifyUserIsLoggedIn()

        topAppBar.setNavigationOnClickListener { // set the navigation icon on the top app bar
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
                    val intent = Intent(this, LoginActivity::class.java) // create an intent to go to the login activity
                    FirebaseAuth.getInstance().signOut()
                    Log.d("Logout", "Logged out") // log the user out
                    startActivity(intent) // start the intent
                    drawer_layout.closeDrawer(GravityCompat.START)

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

}