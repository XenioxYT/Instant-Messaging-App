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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_conversations.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.drawer_layout
import kotlinx.android.synthetic.main.activity_settings.navigation_drawer
import kotlinx.android.synthetic.main.activity_settings.topAppBar_settings
import kotlinx.android.synthetic.main.settings_test_button.*

class SettingsActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // call super class onCreate method
        setContentView(R.layout.activity_settings) // set the layout of the activity

        recycler_view_settings.adapter = adapter
        adapter.add(SettingsItem())

        button_test_colour.setOnClickListener {

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
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("ConversationsActivity", "Error: ${p0.message}")
            }
        })
    }

class SettingsItem() : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
    }

    override fun getLayout(): Int {
        return R.layout.settings_test_button
    }
}

}