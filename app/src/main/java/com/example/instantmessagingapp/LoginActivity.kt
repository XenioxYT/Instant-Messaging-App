package com.example.instantmessagingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)



        // Listen for the login button press
        button_login_loginActivity.setOnClickListener {


            Log.d("LoginActivity", "Login button pressed")
            // Check if username is empty
            val email = email_editText_login.text.toString()
            val password = password_editText_login.text.toString()
            checkEmail()
            Log.d("LoginActivity", "Login dialog") // Log the dialog
            // Create a dialog
            val context = this
            val title = SpannableString("Logging in") // Create a title
            val dismiss = false
            title.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                title.length,
                0
            )
            val builder = AlertDialog.Builder(context) // Create a builder
            builder.setTitle(title) // Set the title
            builder.setMessage("Please wait while we log you in") // Set the message
            val dialog: AlertDialog = builder.create() // Create the dialog
            dialog.show() // Show the dialog


            // Check if password is empty
            if (password_editText_login.text.toString().isEmpty()) { // If password is empty
                password_editText_login_layout.isErrorEnabled = true // Set error
                password_editText_login_layout.error = "Password cannot be empty" // Show error
            } else {
                password_editText_login_layout.isErrorEnabled = false // Remove error
            }



            if (email_editText_login.text.toString()
                    .isNotEmpty() && password_editText_login.text.toString()
                    .isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) { // If username and password are not empty
                Log.d("LoginActivity", "Logging in user") // Log the login
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        dialog.dismiss()
                        if (!it.isSuccessful) return@addOnCompleteListener

                        Log.d(
                            "LoginActivity",
                            "Successfully logged in: ${it.result?.user?.uid}"
                        ) // Log the successful login
                        val intent = Intent(
                            this,
                            ConversationsActivity::class.java
                        ) // Create an intent to go to the ConversationsActivity
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NO_HISTORY // Clear the back stack
                        startActivity(intent) // Start the activity
                        finish() // Finish the current activity
                    }
                    .addOnFailureListener {
                        dialog.dismiss()
                        loginFailed(it)
                    }
            } else {
                dialog.dismiss()
                Log.d("LoginActivity", "Login failed") // Log the failed login
            }
        }

        // Listen for the back to sign in button press
        button_back_to_signin.setOnClickListener {
            Log.d("LoginActivity", "Back to sign in button pressed") // Log the press

            // Clear error messages
            email_editText_login_layout.error = null // Clear error
            password_editText_login_layout.error = null // Clear error

            //Call the signup activity
            val intent = Intent(
                this,
                SignupActivity::class.java
            ) // Create an intent to go to the ConversationsActivity
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) // Clear the back stack
            startActivity(intent) // Start the activity
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d("LoginActivity", "Logging in user") // Log the login
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d(
                    "LoginActivity",
                    "Successfully logged in: ${it.result?.user?.uid}"
                ) // Log the successful login
                val intent = Intent(
                    this,
                    ConversationsActivity::class.java
                ) // Create an intent to go to the ConversationsActivity
                intent.flags =
                    Intent.FLAG_ACTIVITY_NO_HISTORY // Clear the back stack
                startActivity(intent) // Start the activity
                finish()
            }
            .addOnFailureListener {
                loginFailed(it)
            }

    }

    private fun loginDialog() {
        Log.d("LoginActivity", "Login dialog") // Log the dialog
        // Create a dialog
        val context = this
        val title = SpannableString("Logging in") // Create a title
        val dismiss = false
        title.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
            0,
            title.length,
            0
        )
        val builder = AlertDialog.Builder(context) // Create a builder
        builder.setTitle(title) // Set the title
        builder.setMessage("Please wait while we log you in") // Set the message
        val dialog: AlertDialog = builder.create() // Create the dialog
        if (!dismiss) {
            dialog.show()
        }
    }

    private fun loginFailed(it: java.lang.Exception) { // Create a function called loginFailedRegister
        Log.d(
            "SignupActivity",
            "Failed to create user: ${it.message}"
        ) // Log that the user has failed to be created
        val context = this // Create a context variable
        val title =
            SpannableString("${it.message}") // Create a title variable with the error message
        if (it.message == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") { // If the error message is "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
            val title =
                SpannableString("No internet connection") // Set the title variable to "No internet connection"
            title.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment of the text to center
                0, // Start at the beginning of the string
                title.length, // End at the end of the string
                0 // No flags
            ) // End the setSpan function
            val builder = AlertDialog.Builder(context) // Create a builder variable
            builder.setTitle(title) // Set the title of the alert dialog to the title variable
            builder.setMessage("There was an error connecting to the servers. Please check your internet connection and try again.") // Set the message of the alert dialog to "There was an error connecting to the servers. Please check your internet connection and try again."
            builder.setPositiveButton("Try again") { dialog, _ -> // Create a positive button with a try again listener
                dialog.dismiss() // Dismiss the dialog
            } // End the positive button
            val dialog: AlertDialog = builder.create() // Create a dialog variable with the builder
            dialog.show() // Show the dialog
        } else { // If the error message is not "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
            title.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment of the text to center
                0, // Start at the beginning of the string
                title.length, // End at the end of the string
                0 // No flags
            )
            val builder = AlertDialog.Builder(context) // Create a builder variable
            builder.setTitle(title) // Set the title of the alert dialog to the title variable
            builder.setMessage("There seems to be an error with your registration. Please check your details and try again.") // Set the message of the alert dialog to "There seems to be an error with your registration. Please check your details and try again."
            builder.setPositiveButton("Try again") { dialog, _ -> // Create a positive button with a try again listener
                dialog.dismiss() // Dismiss the dialog
            } // End the positive button
            val dialog: AlertDialog = builder.create() // Create a dialog variable with the builder
            dialog.show() // Show the dialog
        } // End the else statement
    } // End the loginFailedRegister function

    private fun checkEmail() { // Create a function called checkEmail
        if (!email_editText_login.text.isNullOrEmpty()) { // If the email text box is not empty
            if (!Patterns.EMAIL_ADDRESS.matcher(email_editText_login.text.toString())
                    .matches()
            ) { // If the email text box does not match the email pattern
                email_editText_login_layout.isErrorEnabled = true // Enable the error
                email_editText_login_layout.error =
                    "Email is invalid" // Set the error message of the email text box to "Email is invalid"
                email_editText_login.requestFocus() // Request focus of the email text box
                return // Return
            }
        }
        if (email_editText_login.text.isNullOrEmpty()) { // If the email text box is empty
            email_editText_login_layout.isErrorEnabled =
                true // Enable the error message of the email text box
            email_editText_login_layout.error =
                "Email is required" // Set the error message of the email text box to "Email is required"
            email_editText_login.requestFocus() // Request focus of the email text box
            return // Return
        } else {
            email_editText_login_layout.isErrorEnabled =
                false // If the email text box is not empty, set the error message to false
        }
    }

}