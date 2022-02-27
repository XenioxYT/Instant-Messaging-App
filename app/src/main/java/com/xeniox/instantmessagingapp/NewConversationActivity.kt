package com.xeniox.instantmessagingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_conversation.*

class NewConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)

        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(UserItem())// add the layout.
        adapter.add(UserItem())
        adapter.add(UserItem())


        recyclerView_new_conversation.adapter = adapter



        topAppBar_new_conversation.setNavigationOnClickListener { // set the navigation icon on the top app bar
            finish() // end the activity
        }
    }
}

class UserItem : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in our list for each user object later on...
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_conversation
    }
}