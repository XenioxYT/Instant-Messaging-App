package com.example.instantmessagingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_conversations.*

class ConversationsActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)

        topAppBar.setNavigationOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }

        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigation_drawer.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.item2 -> {
                    // Get title_app_bar from layout and change the text to "settings"
                    topAppBar.title = "SETTINGS"
                    // Close the drawer
                    drawer_layout.closeDrawer(GravityCompat.START)
                    intent = Intent(this, SettingsActivity::class.java)
                }
                R.id.item3 -> {
                    val intent = Intent(this, ConversationsActivity::class.java)
                    startActivity(intent)
                }
                R.id.item4 -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            drawer_layout.closeDrawer(navigation_drawer)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}