package com.xeniox.instantmessagingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_conversation.*
import kotlinx.android.synthetic.main.user_row_new_conversation.view.*

class NewConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)

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





        topAppBar_new_conversation.setNavigationOnClickListener { // set the navigation icon on the top app bar
            val intent = Intent(this, ConversationsActivity::class.java) // end the activity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

//        val intent = Intent(this, SignupActivity::class.java)
//        startActivity(intent)
        fetchUsers(dialog)
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(dialog: AlertDialog) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {


                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
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
                TODO("Not yet implemented")
            }

        })
    }
}

class UserItem(val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_username_new_conversation.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_conversation
    }
}