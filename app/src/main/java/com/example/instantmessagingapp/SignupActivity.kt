package com.example.instantmessagingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_create_account.setOnClickListener {
            Log.d("SignupActivity", "Button clicked")
            val username = username_editText_register.text.toString() // Set username to the text entered into the username text box
            val email = email_editText_register.text.toString() // Set email to the text entered into the email text box
            val password = password_editText_register.text.toString() // Set password to the text entered into the password text box
            val confirmPassword = confirm_password_editText_register.text.toString() // Set confirmPassword to the text entered into the confirm_password text box


            // Use Logcat to output the text entered
            Log.d("SignupActivity", "Username is $username")
            Log.d("SignupActivity", "Email is $email")
            Log.d("SignupActivity", "Password is $password")
            Log.d("SignupActivity", "Confirm Password is $confirmPassword")
        }
        button_login_signupActivity.setOnClickListener {
            Log.d("SignupActivity", "Login button clicked")
            val intent = Intent(this, LoginActivity)

        }
    }
}
