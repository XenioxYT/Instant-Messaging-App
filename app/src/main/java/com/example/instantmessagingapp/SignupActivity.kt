package com.example.instantmessagingapp
// imports
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


// Main class for the SignupActivity
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            if (username.isEmpty()) {
                username_editText_register.error = "Username is required"
                username_editText_register.requestFocus()
            }
            // Check if the email is empty
            if (email.isEmpty()) {
                email_editText_register.error = "Email is required"
                email_editText_register.requestFocus()
            }
            // Check if the password is empty
            if (password.isEmpty()) {
                password_editText_register.error = "Password is required"
                password_editText_register.requestFocus()
            }
            // Check if the confirm password is empty
            if (confirmPassword.isEmpty()) {
                confirm_password_editText_register.error = "Confirm Password is required"
                confirm_password_editText_register.requestFocus()
            }
            // Check if the password and confirm password match
            if (password != confirmPassword) {
                confirm_password_editText_register.error = "Password does not match"
                confirm_password_editText_register.requestFocus()
            }
            // If all the fields are filled in correctly, then create the user
            // TODO: Create the user
            // TODO: Go to the conversation list activity


            // Use Logcat to output the text entered in the various text boxes
            Log.d("SignupActivity", "Username is $username")
            Log.d("SignupActivity", "Email is $email")
            Log.d("SignupActivity", "Password is $password")
            Log.d("SignupActivity", "Confirm Password is $confirmPassword")
        }

        //listen for the login button click
        button_login_signupActivity.setOnClickListener {
            Log.d("SignupActivity", "Login button clicked")
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
