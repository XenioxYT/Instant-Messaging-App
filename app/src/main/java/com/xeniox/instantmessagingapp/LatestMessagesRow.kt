package com.xeniox.instantmessagingapp

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.conversations_message_row.view.*
private lateinit var firebaseAnalytics: FirebaseAnalytics

class LatestMessageRow(private val chatMessage: ChatMessage): Item<ViewHolder>() {
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.conversations_message_row_textView.text = chatMessage.text

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        val chatPartnerId = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else {
            chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.conversations_message_username.text = chatPartnerUser?.username
                val uri = chatPartnerUser?.profileImageUrl
                Log.d("LatestMessageRow", "uri: $uri")
                if (uri != null) {
                    val targetImageView = viewHolder.itemView.user_profile_picture_conversations_message_row
                    Picasso.get().load(uri).into(targetImageView)
                } else {
                    val targetImageView = viewHolder.itemView.user_profile_picture_conversations_message_row
                    Picasso.get().load(R.drawable.ic_account_circle_black_24dp).into(targetImageView)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    override fun getLayout(): Int {
        return R.layout.conversations_message_row
    }

}