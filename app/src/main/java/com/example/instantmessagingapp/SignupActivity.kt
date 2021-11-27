package com.example.instantmessagingapp
// imports
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_main.*


// Main class for the SignupActivity
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Listen for the create account button press
        button_create_account.setOnClickListener {
            Log.d("SignupActivity", "Button clicked")
            val username =
                username_editText_register.text.toString() // Set username to the text entered into the username text box
            val email =
                email_editText_register.text.toString() // Set email to the text entered into the email text box
            val password =
                password_editText_register.text.toString() // Set password to the text entered into the password text box
            val confirmPassword =
                confirm_password_editText_register.text.toString() // Set confirmPassword to the text entered into the confirm_password text box


            // Check if the username is empty
            if (username_editText_register.text.isNullOrEmpty()) {
                username_editText_register.error = "Username Required"
                username_editText_register.requestFocus()
            }
            // Check if the email is empty
            if (email_editText_register.text.isNullOrEmpty()) {
                email_editText_register.error = "Email is required"
                email_editText_register.requestFocus()
            }
            // Check if the password is empty
            if (password_editText_register.text.isNullOrEmpty()) {
                password_editText_register.error = "Password is required"
                password_editText_register.requestFocus()
            }
            // Check if the confirm password is empty
            if (confirm_password_editText_register.text.isNullOrEmpty()) {
                confirm_password_editText_register.error = "Confirm Password is required"
                confirm_password_editText_register.requestFocus()
            }
            // if all the text boxes are filled in, then check if the password and confirm password match
            if (password != confirmPassword) {
                confirm_password_editText_register.error = "Passwords do not match"
                confirm_password_editText_register.requestFocus()
            }
            // If all the text boxes are filled in, then check if the email is valid
            if (!email_editText_register.text.isNullOrEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    email_editText_register.error = "Email is invalid"
                    email_editText_register.requestFocus()
                }
            }
            // If all the text boxes are filled in, then check if the password is valid
            if (!password_editText_register.text.isNullOrEmpty()) {
                if (password.length < 6) {
                    password_editText_register.error = "Password must be at least 6 characters"
                    password_editText_register.requestFocus()
                }
            }
            // If all the text boxes are filled in, then check if the username is valid
            if (!username_editText_register.text.isNullOrEmpty()) {
                if (username.length <= 6 || username.length >= 18) {
                    username_editText_register.error = "Username must be at least 6 characters and no more than 18 characters"
                    username_editText_register.requestFocus()
                }
            }

            // I have no idea why this code works, but it does
            // However, something may break cos it's shit
            if (username_editText_register.text.isNotEmpty() && email_editText_register.text.isNotEmpty() && password_editText_register.text.isNotEmpty() && confirm_password_editText_register.text.isNotEmpty() && password == confirmPassword && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6 && username.length <= 18) {
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) return@addOnCompleteListener
                            Log.d(
                                "SignupActivity",
                                "Successfully created user with uid: ${it.result?.user?.uid}"
                            )
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Log.d("SignupActivity", "Failed to create user: ${it.message}")
                            val context = this
                            val title = SpannableString("${it.message}")
                            title.setSpan(
                                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                                0,
                                title.length,
                                0
                            )
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle(title)
                            builder.setMessage("There seems to be an error with your registration. Please check your details and try again by clicking the button below.")
                            builder.setPositiveButton("Try again") { dialog, _ ->
                                dialog.dismiss()
                                password_editText_register.text.clear()
                                confirm_password_editText_register.text.clear()
                            }
                            val dialog: AlertDialog = builder.create()
                            dialog.show()
                        }
                }
                catch (e: Exception) {
                    val context = this
                    val title = SpannableString("Error")
                    title.setSpan(
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0,
                        title.length,
                        0
                    )
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(title)
                    builder.setMessage("There was an error creating your account. Please try again in a few minutes.")
                    builder.setPositiveButton("OK") { dialog, which ->
                        dialog.dismiss()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }




            // Use Logcat to output the text entered in the various text boxes
            Log.d("SignupActivity", "Username is $username")
            Log.d("SignupActivity", "Email is $email")
            Log.d("SignupActivity", "Password is $password")
            Log.d("SignupActivity", "Confirm Password is $confirmPassword")
        }

        //listen for the login button click
        button_login_signupActivity.setOnClickListener {
            Log.d("SignupActivity", "Login button clicked")

            // Clear the text boxes and set remove error message
            username_editText_register.text.clear()
            email_editText_register.text.clear()
            password_editText_register.text.clear()
            confirm_password_editText_register.text.clear()
            username_editText_register.error = null
            email_editText_register.error = null
            password_editText_register.error = null
            confirm_password_editText_register.error = null

            //create an intent to open the login activity
            Intent(
                this,
                LoginActivity::class.java
            ).also {
                startActivity(it)
            }
        }
    }
}


