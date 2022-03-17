package com.xeniox.instantmessagingapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.xeniox.instantmessagingapp.ConversationsChatActivity.Companion.TAG
import kotlinx.android.synthetic.main.activity_forgotten_password.*
import kotlinx.android.synthetic.main.login_activity.*


class ForgottenPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)


        button_reset_password.setOnClickListener{
            val email = email_editText_forgot_password.text.toString()
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                email_editText_forgot_password_layout.error = "Please enter your email"
                email_editText_forgot_password.requestFocus()
                return@setOnClickListener
            } else {
//                val dialog = android.app.AlertDialog.Builder(this)
//                dialog.setTitle("Sending email")
//                dialog.setMessage("Hang on, we are sending you an email to reset your password")
//                dialog.setCancelable(false)
//                dialog.show()
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
//                        dialog.dismiss()
                            // make a dialog box telling the user that the email has been sent
                            val dialog2 = android.app.AlertDialog.Builder(this)
                            dialog2.setTitle("Email Sent")
                            dialog2.setMessage("Please check your email to reset your password")
                            dialog2.setCancelable(false)
                            dialog2.setPositiveButton("Ok") { _, _ ->
                                val intent = android.content.Intent(this, LoginActivity::class.java)
                                intent.flags =
                                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK.or(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                            dialog2.show()
                        } else {
                            Log.d(TAG, "Error: ${task.exception}")
                        }
                        Log.d(TAG, "Email sent.")
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error: ${exception.message}")
                        val dialog = android.app.AlertDialog.Builder(this)
                        dialog.setTitle("Error")
                        dialog.setMessage("There was an error sending your email, please try again")
                        dialog.setCancelable(false)
                        dialog.setPositiveButton("Ok") { _, _ ->
                        }
                        dialog.show()
                    }
            }
        }
    }
}