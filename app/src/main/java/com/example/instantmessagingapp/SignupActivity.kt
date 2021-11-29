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
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*


// Main class for the SignupActivity
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Listen for the create account button press
        button_create_account.setOnClickListener {
            Log.d("SignupActivity", "Button clicked")

            checkUsername()
            checkEmail()
            checkPassword()
            checkPasswordsMatch()

            val username =
                username_editText_register.text.toString() // Set username to the text entered into the username text box
            val email =
                email_editText_register.text.toString() // Set email to the text entered into the email text box
            val password =
                password_editText_register.text.toString() // Set password to the text entered into the password text box
            val confirmPassword =
                confirm_password_editText_register.text.toString() // Set confirmPassword to the text entered into the confirm_password text box

            // I have no idea why this code works, but it does
            // However, something may break cos it's shit
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6 && username.length <= 18) {
                try {
                    // Create a new user with the email and password
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            loginSuccessfulRegister(it)
                        }
                        .addOnFailureListener {
                            loginFailedRegister(it)
                        }
                }
                catch (e: Exception) {
                    loginException()
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
//            email_editText_register.text.toString()
//            password_editText_register.text.clear()
//            confirm_password_editText_register.text.clear()
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


    private fun checkUsername() {
        if (username_editText_register.text.toString().isNotEmpty()) {
            if (username_editText_register.text.toString().length <= 6 || username_editText_register.text.toString().length >= 18) {
                username_editText_register.error = "Username must be at least 6 characters and no more than 18 characters"
                username_editText_register.requestFocus()
                return
            }
        }
        else {
            username_editText_register.error = "Username cannot be empty"
            username_editText_register.requestFocus()
            return
        }
    }

    private fun checkEmail() {
        if (!email_editText_register.text.isNullOrEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email_editText_register.text.toString()).matches()) {
                email_editText_register.error = "Email is invalid"
                email_editText_register.requestFocus()
                return
            }
        }
        else {
            email_editText_register.error = "Email is required"
            email_editText_register.requestFocus()
            return
        }
    }

    private fun checkPassword() {
        if (!password_editText_register.text.isNullOrEmpty()) {
            if (password_editText_register.text.toString().length < 6) {
                password_editText_register.error = "Password must be at least 6 characters"
                password_editText_register.requestFocus()
                return
            }
        }
        else {
            password_editText_register.error = "Password cannot be empty"
            password_editText_register.requestFocus()
            return
        }
    }

    private fun checkPasswordsMatch() {
        if (!confirm_password_editText_register.text.isNullOrEmpty()) {
            if (confirm_password_editText_register.text.toString() != password_editText_register.text.toString()) {
                confirm_password_editText_register.error = "Passwords do not match"
                confirm_password_editText_register.requestFocus()
                return
            }
        }
        else {
            confirm_password_editText_register.error = "Confirm Password cannot be empty"
            confirm_password_editText_register.requestFocus()
            return
        }
    }

    private fun loginSuccessfulRegister(it: Task<AuthResult>) {
        if (!it.isSuccessful) return
        Log.d(
            "SignupActivity",
            "Successfully created user with uid: ${it.result?.user?.uid}"
        )
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun loginFailedRegister(it: java.lang.Exception) {
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
//            password_editText_register.text.clear()
//            confirm_password_editText_register.text.clear()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun loginException() {
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
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}


