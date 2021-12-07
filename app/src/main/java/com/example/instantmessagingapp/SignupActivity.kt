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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*


// Main class for the SignupActivity
class SignupActivity : AppCompatActivity() { // Start of class
    override fun onCreate(savedInstanceState: Bundle?) { // onCreate method
        super.onCreate(savedInstanceState) // call super class onCreate
        setContentView(R.layout.activity_main) // set the layout

        // Listen for the create account button press
        button_create_account.setOnClickListener { // Start of onClickListener
            Log.d("SignupActivity", "Button clicked") // Log the button click

            checkUsername() // Check if the username is valid
            checkEmail() // Check if the email is valid
            checkPassword() // Check if the password is valid
            checkPasswordsMatch() // Check if the passwords match

            val username =
                username_editText_register.text.toString() // Set username to the text entered into the username text box
            val email =
                email_editText_register.text.toString() // Set email to the text entered into the email text box
            val password =
                password_editText_register.text.toString() // Set password to the text entered into the password text box
            val confirmPassword =
                confirm_password_editText_register.text.toString() // Set confirmPassword to the text entered into the confirm_password text box

            // I have no idea why this code works, but it does
            // However, something may break if I remove it
            // Checks all of the strings inputted to see if they are invalid
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6 && username.length <= 18) {

                // Try creating the user
                try {
                    // Build a dialog box to display to the user that their account is being created.
                    val context = this
                    val title = SpannableString("Creating Account")
                    val dismiss = false
                    // Set the title of the dialog box to the center.
                    title.setSpan( // Set the alignment of the title to center
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment of the text to center
                        0, // Start at the beginning of the string
                        title.length, // End at the end of the string
                        0 // Do not change the flags
                    )
                    // Build the dialog box with the title and dismiss button.
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(title) // Sets the title of the dialog box
                    builder.setMessage("Please wait while your account is being created.") // Sets the message of the dialog box
                    val dialog: AlertDialog = builder.create() // Creates the dialog box
                    if (!dismiss) { // If dismiss = true then show the dialog box
                        dialog.show() // Show the dialog box
                    }

                    // Create a new user with the email and password
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password) // Create a new user with the email and password
                        .addOnCompleteListener { // Add an onCompleteListener to the task
                            dialog.dismiss() // Dismiss the dialog box
                            loginSuccessfulRegister(it) // Call the loginSuccessfulRegister function
                        }
                        .addOnFailureListener { // Add an onFailureListener to the task
                            loginFailedRegister(it) // Call the loginFailedRegister function
                            dialog.dismiss() // Dismiss the dialog box
                        }
                } catch (e: Exception) {
                    loginException() // Call the loginException function
                }
            }


            // Use Logcat to output the text entered in the various text boxes
            Log.d("SignupActivity", "Username is $username") // Log the username
            Log.d("SignupActivity", "Email is $email") // Log the email
            Log.d("SignupActivity", "Password is $password") // Log the password
            Log.d("SignupActivity", "Confirm Password is $confirmPassword") // Log the confirmPassword
        }

        //listen for the login button click
        button_login_signupActivity.setOnClickListener {
            Log.d("SignupActivity", "Login button clicked") // Log the login button was clicked

            // Clear the text boxes and set remove error message
            username_editText_register.error = null // Clear the username error message
            email_editText_register.error = null // Clear the email error message
            password_editText_register.error = null // Clear the password error message
            confirm_password_editText_register.error = null // Clear the confirm password error message

            //create an intent to open the login activity
            val intent = Intent(this, LoginActivity::class.java) // Create an intent to open the login activity
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY // Set the flags to not have a history
            startActivity(intent) // Start the login activity
        }
    }


    private fun checkUsername() { // Function to check the username
        if (username_editText_register.text.toString().isNotEmpty()) { // If the username is not empty
            if (username_editText_register.text.toString().length <= 6 || username_editText_register.text.toString().length >= 18) { // If the username is less than 6 characters or greater than 18 characters
                username_editText_register.error = // Set the error message
                    "Username must be at least 6 characters and no more than 18 characters" // Set the error message
                username_editText_register.requestFocus() // Request focus on the username
                return // Return
            }
        } else { // If the username is empty
            username_editText_register.error = "Username cannot be empty" // Set the error message
            username_editText_register.requestFocus() // Request focus on the username
            return // Return
        }
    }

    private fun checkEmail() { // Function to check the email
        if (!email_editText_register.text.isNullOrEmpty()) { // If the email is not empty
            if (!Patterns.EMAIL_ADDRESS.matcher(email_editText_register.text.toString()) // If the email is not a valid email
                    .matches() // Match the email to invalid emails
            ) {
                email_editText_register.error = "Email is invalid" // Set the error message
                email_editText_register.requestFocus() // Request focus on the email
                return // Return
            }
        } else { // If the email is empty
            email_editText_register.error = "Email is required" // Set the error message
            email_editText_register.requestFocus() // Request focus on the email
            return // Return
        }
    }

    private fun checkPassword() { // Function to check the password
        if (!password_editText_register.text.isNullOrEmpty()) { // If the password not is empty
            if (password_editText_register.text.toString().length < 6) { // If the password is less than 6 characters
                password_editText_register.error = "Password must be at least 6 characters" // Set the error message
                password_editText_register.requestFocus() // Request focus on the password
                return // Return
            }
        } else { // If the password is empty
            password_editText_register.error = "Password cannot be empty" // Set the error message
            password_editText_register.requestFocus() // Request focus on the password
            return // Return
        }
    }

    private fun checkPasswordsMatch() { // Function to check the passwords match
        if (!confirm_password_editText_register.text.isNullOrEmpty()) { // If the confirm password is not empty
            if (confirm_password_editText_register.text.toString() != password_editText_register.text.toString()) { // If the confirm password does not match the password
                confirm_password_editText_register.error = "Passwords do not match" // Set the error message
                confirm_password_editText_register.requestFocus() // Request focus on the confirm password
                return // Return
            }
        } else { // If the confirm password is empty
            confirm_password_editText_register.error = "Confirm Password cannot be empty" // Set the error message
            confirm_password_editText_register.requestFocus() // Request focus on the confirm password
            return // Return
        }
    }

    private fun loginSuccessfulRegister(it: Task<AuthResult>) { // Function to handle the successful login
        if (!it.isSuccessful) return // If the login is not successful
        Log.d(
            "SignupActivity",
            "Successfully created user with uid: ${it.result?.user?.uid}"
        ) // Log the successful login

        val uid = FirebaseAuth.getInstance().uid ?: "" // Get the uid of the user
        val ref =
            FirebaseDatabase.getInstance("https://instant-messaging-app-7fed6-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users/$uid") // Get the reference to the user

        val user = User(uid, username_editText_register.text.toString()) // Create a user object
        ref.setValue(user) // Set the user object to the database
            .addOnSuccessListener { // Add an on success listener
                Log.d("SignupActivity", "Finally we saved the user to Firebase Database") // Log the successful save
                val intent = Intent(this, LoginAccountCreatedActivity::class.java) // Create an intent to the login activity
                startActivity(intent) // Start the login activity
            }
            .addOnFailureListener { // Add an on failure listener
                Log.d("SignupActivity", "Failed to set value to database: ${it.message}") // Log the failure
                //TODO: Handle failure and show error message
            }
    }

    private fun loginFailedRegister(it: java.lang.Exception) { // Function to handle the failed login
        Log.d("SignupActivity", "Failed to create user: ${it.message}") // Log the failed login
        val context = this // Get the context
        var title = SpannableString("${it.message}") // Create a title
        if (it.message == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") { // If the error is a network error
            var title = SpannableString("No internet connection") // Create a title
            title.setSpan( // Set the title
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment
                0, // Start at the beginning
                title.length, // End at the end
                0 // No flags
            )
            val builder = AlertDialog.Builder(context) // Create an alert dialog builder
            builder.setTitle(title) // Set the title
            builder.setMessage("There was an error connecting to the servers. Please check your internet connection and try again.") // Set the message
            builder.setPositiveButton("Try again") { dialog, _ -> // Set the positive button
                dialog.dismiss() // Dismiss the dialog
            }
            val dialog: AlertDialog = builder.create() // Create the dialog
            dialog.show() // Show the dialog
        }
        else { // If the error is not a network error
            title.setSpan( // Set the title
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment
                0, // Start at the beginning
                title.length, // End at the end
                0 // No flags
            )
            val builder = AlertDialog.Builder(context) // Create an alert dialog builder
            builder.setTitle(title) // Set the title
            builder.setMessage("There seems to be an error with your registration. Please check your details and try again.") // Set the message
            builder.setPositiveButton("Try again") { dialog, _ -> // Set the positive button
                dialog.dismiss() // Dismiss the dialog
            }
            val dialog: AlertDialog = builder.create() // Create the dialog
            dialog.show() // Show the dialog
        }
    }

    private fun loginException() { // Function to handle the exception
        val context = this // Get the context
        val title = SpannableString("Error") // Create a title
        title.setSpan( // Set the title
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment
            0, // Start at the beginning
            title.length, // End at the end
            0 // No flags
        )
        val builder = AlertDialog.Builder(context) // Create an alert dialog builder
        builder.setTitle(title) // Set the title
        builder.setMessage("There was an error creating your account. Please try again in a few minutes.") // Set the message
        builder.setPositiveButton("OK") { dialog, _ -> // Set the positive button
            dialog.dismiss() // Dismiss the dialog
        }
        val dialog: AlertDialog = builder.create() // Create the dialog
        dialog.show() // Show the dialog
    }
}

class User(val uid: String, val username: String) // Class to store the user


