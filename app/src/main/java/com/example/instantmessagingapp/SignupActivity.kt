package com.example.instantmessagingapp
// imports
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*


// ...
// Initialize Firebase Auth


// Main class for the SignupActivity
class SignupActivity : AppCompatActivity() { // Start of class
    override fun onCreate(savedInstanceState: Bundle?) { // onCreate method
        super.onCreate(savedInstanceState) // call super class onCreate
        setContentView(R.layout.activity_signup) // set the layout


        // Listen for the create account button press
        button_create_account.setOnClickListener { // Create a new user with email and password
            Log.d("SignupActivity", "Button clicked") // Log the button press

            checkUsername() // Check if the username is valid
            Log.d("SignupActivity", "Username Checked")
            checkEmail() // Check if the email is valid
            Log.d("SignupActivity", "Email Checked")
            checkPassword() // Check if the password is valid
            Log.d("SignupActivity", "Password Checked")
            checkPasswordsMatch() // Check if the passwords match
            Log.d("SignupActivity", "Passwords match Checked")
            val profilepic = false
            if (checkProfilePicture(profilepic)) { // Check if the profile picture is valid
                Log.d("SignupActivity", "Profile picture Checked")
            }

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
            try { // Try to create the user
                if (username_editText_register.text.toString().isNotEmpty() && email_editText_register.text.toString().isNotEmpty() && password_editText_register.text.toString().isNotEmpty() && confirm_password_editText_register.text.toString().isNotEmpty() && password_editText_register.text.toString() == confirm_password_editText_register.text.toString()) { // If all the fields are filled in and the passwords match
                    Log.d("SignupActivity","Everything validated")

                    val context = this // Set context to this
                    val title = SpannableString("Creating Account") // Set title to the text "Creating Account"
                    title.setSpan( // Set the alignment of the text to center
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment to center
                        0, // Set the start of the alignment to 0
                        title.length, // Set the end of the alignment to the length of the text
                        0 // Set the flags to 0
                    ) // End the alignment
                    Log.d("SignupActivity", "Title aligned")
                    val builder = AlertDialog.Builder(context) // Set builder to an alert dialog builder
                    builder.setTitle(title) // Set the title of the alert dialog to the title
                    builder.setMessage("Please wait while your account is being created.") // Set the message of the alert dialog to "Please wait while your account is being created."
                    val dialog: AlertDialog = builder.create() // Set dialog to the alert dialog created by the builder
                    dialog.show()
                    Log.d("SignupActivity", "dialog shown")


                    // Create a new user with the email and password
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        Log.d(
                            "SignupActivity",
                            "user created with UID $it"
                        )// Add an onCompleteListener to the task
                        if (selectedPhotoUri == null) {
                            Log.d("SignupAcitivity", "The user has not uploaded an image")
                        }
                        if (checkProfilePicture(profilepic)) {
                            val filename = UUID.randomUUID().toString()
                            val ref =
                                FirebaseStorage.getInstance().getReference("/images/$filename")
                            val profilepic = false
                            ref.putFile(selectedPhotoUri!!).addOnSuccessListener { it ->
                                Log.d(
                                    "SignupActivity",
                                    "Successfully uploaded image: ${it.metadata?.path}"
                                )
                                ref.downloadUrl.addOnSuccessListener {
                                    Log.d("SignupActivity", "File Location: $it")
                                    val filelocation = it
                                    val uid = FirebaseAuth.getInstance().uid
                                        ?: "" // Get the user's uid
                                    val ref =
                                        FirebaseDatabase.getInstance("https://instant-messaging-app-7fed6-default-rtdb.europe-west1.firebasedatabase.app/")
                                            .getReference("/users/$uid") // Get the user's reference
                                    Log.d("SignupActivity", "${filename}, ${filelocation}")
                                    val profileImageUrl = filelocation.toString()
                                    val user = User(
                                        uid,
                                        username_editText_register.text.toString(),
                                        profileImageUrl
                                    ) // Create a user object with the user's uid and username
                                    ref.setValue(user)
                                        .addOnSuccessListener { // Add an onSuccessListener to the setValue function
                                            Log.d(
                                                "SignupActivity",
                                                "Finally we saved the user to Firebase Database"
                                            ) // Log that the user has been saved to Firebase Database
                                            Log.d("SignupActivity", "Intent = ${intent}")
                                            val intent = Intent(
                                                this,
                                                LoginAccountCreatedActivity::class.java
                                            ) // Create an intent to the LoginAccountCreatedActivity
                                            startActivity(intent) // Start the intent
                                        } // End the addOnSuccessListener
                                        .addOnFailureListener { // Add an onFailureListener to the setValue function
                                            Log.d(
                                                "SignupActivity",
                                                "Failed to set value to database: ${it.message}"
                                            ) // Log that the user has failed to be saved to Firebase Database
                                            //TODO: Handle failure and show error message to user
                                        } // End the addOnFailureListener
                                }
                            }.addOnFailureListener {
                                Log.d(
                                    "Activity",
                                    "Failed to upload image to storage: ${it.message}"
                                )
                            }
                        }
                        if (!checkProfilePicture(profilepic)) {
                            Log.d("SignupActivity","The profile picture was not selected")
                        }

                    }
                            dialog.dismiss() // Dismiss the alert dialog
                } else {
                    Log.d("SignupActivity", "Everything is not valid")
                }
            } catch (e: Exception) { // Catch any exceptions
//                loginException()
                Log.d("SignupActivity", "Signup failed (try catch)")
            } // End the try catch

            // The program is jumping straight to this section and skipping the try catch. Maybe if statement is wrong????


            // Use Logcat to output the text entered in the various text boxes
            Log.d("SignupActivity", "Username is $username") // Log the username
            Log.d("SignupActivity", "Email is $email") // Log the email
            Log.d("SignupActivity", "Password is $password") // Log the password
            Log.d("SignupActivity", "Confirm Password is $confirmPassword") // Log the confirmPassword
        }

        //listen for the login button click
        button_login_signupActivity.setOnClickListener { // Set the onClickListener of the login button to the following
            Log.d("SignupActivity", "Login button clicked") // Log the login button clicked

            // Clear the text boxes and set remove error message
            username_editText_register_layout.error =
                null // Clear the error message of the username text box
            email_editText_register_layout.error =
                null // Clear the error message of the email text box
            password_editText_register_layout.error =
                null // Clear the error message of the password text box
            confirm_password_editText_register_layout.error =
                null // Clear the error message of the confirm password text box

            //create an intent to open the login activity
            val intent = Intent(
                this,
                LoginActivity::class.java
            ) // Set intent to an intent to open the login activity
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // Set the flags of the intent to clear the task and start a new task
            startActivity(intent) // Start the login activity
        }

        button_profile_picture.setOnClickListener {
            Log.d(
                "SignupActivity",
                "Profile picture button clicked"
            ) // Log the profile picture button clicked

            //create an intent to open the gallery
            val intent = Intent(Intent.ACTION_PICK) // Set intent to an intent to open the gallery
            intent.type = "image/*" // Set the type of the intent to image
            startActivityForResult(
                intent,
                0
            ) // Start the activity for result with the intent and the request code
        }
    }

    private var selectedPhotoUri: Uri? = null // Create a variable to hold the selected photo uri

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri =
                data.data // Set the selected photo uri to the uri returned from the gallery
            val bitmap = MediaStore.Images.Media.getBitmap(
                contentResolver,
                selectedPhotoUri
            ) // Get the bitmap from the uri
            selectphoto_imageview_register.setImageBitmap(bitmap) // Set the profile picture image view to the bitmap
            button_profile_picture.alpha = 0f // Set the alpha of the profile picture button to 0
        } else {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT) // TODO: CHANGE TO SNACKBAR LATER ON
                .show() // Show a toast to the user
            // TODO: Can I check the profile picture here and stop the user from creating an account here
            // instead of creating about 500 errors because of my shit code?
            // seems likely.
        }
    }


    ///// FUNCTIONS \\\\\


    private fun checkUsername() { // Create a function called checkUsername
        if (username_editText_register.text.toString()
                .isNotEmpty()
        ) { // If the username text box is not empty
            username_editText_register_layout.isErrorEnabled = true
            if (username_editText_register.text.toString().length <= 6 || username_editText_register.text.toString().length >= 18) { // If the length of the username is less than 6 or greater than 18
                username_editText_register_layout.error = "Username must be at least 6 characters and no more than 18 characters" // Set the error message of the username text box to "Username must be at least 6 characters and no more than 18 characters"
                username_editText_register.requestFocus() // Request focus of the username text box
                return // Return
            }
        }
        if (username_editText_register.text.toString()
                .isEmpty()
        ) { // If the username text box is empty
            username_editText_register_layout.isErrorEnabled = true
            username_editText_register_layout.error = "Username cannot be empty" // Set the error message of the username text box to "Username cannot be empty"
            username_editText_register.requestFocus() // Request focus of the username text box
            Log.d("SignupActivity", "username is not valid")
            return // Return
        } else {
            username_editText_register_layout.isErrorEnabled =
                false // Clear the error message of the username text box
            Log.d("SignupActivity", "Username is valid") // Log that the username is valid
        }
    }

    private fun checkEmail() { // Create a function called checkEmail
        if (!email_editText_register.text.isNullOrEmpty()) { // If the email text box is not empty
            if (!Patterns.EMAIL_ADDRESS.matcher(email_editText_register.text.toString())
                    .matches()
            ) { // If the email text box does not match the email pattern
                email_editText_register_layout.isErrorEnabled = true
                email_editText_register_layout.error = "Email is invalid" // Set the error message of the email text box to "Email is invalid"
                email_editText_register.requestFocus() // Request focus of the email text box
                return // Return
            }
        }
        if (email_editText_register.text.toString()
                .isEmpty()
        ) { // If the email text box is empty
            email_editText_register_layout.isErrorEnabled = true
            email_editText_register_layout.error = "Email is required" // Set the error message of the email text box to "Email is required"
            email_editText_register.requestFocus() // Request focus of the email text box
            return // Return
        } else {
            email_editText_register_layout.isErrorEnabled =
                false // Clear the error message of the email text box
        }
    }

    private fun checkPassword() { // Create a function called checkPassword
        if (password_editText_register.text.toString().isNotEmpty()) { // If the password text box is not empty
            if (password_editText_register.text.toString().length < 6) { // If the length of the password is less than 6
                password_editText_register_layout.isErrorEnabled = true
                password_editText_register_layout.error = "Password must be at least 6 characters" // Set the error message of the password text box to "Password must be at least 6 characters"
                password_editText_register.requestFocus() // Request focus of the password text box
                return // Return
            }
        }
        if (password_editText_register.text.toString().isEmpty()
        ) { // If the password text box is empty
            password_editText_register_layout.isErrorEnabled = true
            password_editText_register_layout.error = "Password cannot be empty" // Set the error message of the password text box to "Password cannot be empty"
            password_editText_register.requestFocus() // Request focus of the password text box
            return // Return
        } else {
            password_editText_register_layout.isErrorEnabled =
                false // Clear the error message of the password text box
        }
    } // End the checkPassword function

    private fun checkPasswordsMatch() { // Create a function called checkPasswordsMatch
        if (confirm_password_editText_register.text.toString().isNotEmpty()) { // If the confirm password text box is not empty
            if (confirm_password_editText_register.text.toString() != password_editText_register.text.toString()) { // If the confirm password text box does not match the password text box
                confirm_password_editText_register_layout.isErrorEnabled = true
                confirm_password_editText_register_layout.error = "Passwords do not match" // Set the error message of the confirm password text box to "Passwords do not match"
                confirm_password_editText_register.requestFocus() // Request focus of the confirm password text box
                return // Return
            } // End the if statement
        }
        if (confirm_password_editText_register.text.toString().isEmpty()) { // If the confirm password text box is empty
            confirm_password_editText_register_layout.isErrorEnabled = true
            confirm_password_editText_register_layout.error = "Confirm Password cannot be empty" // Set the error message of the confirm password text box to "Confirm Password cannot be empty"
            confirm_password_editText_register.requestFocus() // Request focus of the confirm password text box
            return // Return
        } else {
            confirm_password_editText_register_layout.isErrorEnabled = false // Clear the error message of the confirm password text box
        }
    } // End the checkPasswordsMatch function



//    private fun loginSuccessfulRegister(profileImageUrl: String) { // Create a function called loginSuccessfulRegister
//        val uid = FirebaseAuth.getInstance().uid ?: "" // Get the user's uid
//        val ref =
//            FirebaseDatabase.getInstance("https://instant-messaging-app-7fed6-default-rtdb.europe-west1.firebasedatabase.app/")
//                .getReference("/users/$uid") // Get the user's reference
//
//        val user = User(uid, username_editText_register.text.toString(), profileImageUrl) // Create a user object with the user's uid and username
//        ref.setValue(user) // Set the user's reference to the user object
//            .addOnSuccessListener { // Add an onSuccessListener to the setValue function
//                Log.d(
//                    "SignupActivity",
//                    "Finally we saved the user to Firebase Database"
//                ) // Log that the user has been saved to Firebase Database
//                if (intent.toString().isEmpty()) {
//                    val intent = Intent(this, LoginAccountCreatedActivity::class.java) // Create an intent to the LoginAccountCreatedActivity
//                    startActivity(intent) // Start the intent
//                }
//            } // End the addOnSuccessListener
//            .addOnFailureListener { // Add an onFailureListener to the setValue function
//                Log.d("SignupActivity", "Failed to set value to database: ${it.message}") // Log that the user has failed to be saved to Firebase Database
//                //TODO: Handle failure and show error message to user
//            } // End the addOnFailureListener
//    } // End the loginSuccessfulRegister function

    private fun loginFailedRegister(it: java.lang.Exception) { // Create a function called loginFailedRegister
        Log.d("SignupActivity", "Failed to create user: ${it.message}") // Log that the user has failed to be created
        val context = this // Create a context variable
        val title = SpannableString("${it.message}") // Create a title variable with the error message
        if (it.message == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") { // If the error message is "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
            val title = SpannableString("No internet connection") // Set the title variable to "No internet connection"
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
        }
        else { // If the error message is not "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
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

    private fun loginException() { // Create a function called loginException
        val context = this // Create a context variable
        val title = SpannableString("Error") // Create a title variable with the error message
        title.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment of the text to center
            0, // Start at the beginning of the string
            title.length, // End at the end of the string
            0 // No flags
        ) // End the setSpan function
        val builder = AlertDialog.Builder(context) // Create a builder variable
        builder.setTitle(title) // Set the title of the alert dialog to the title variable
        builder.setMessage("There was an error creating your account. Please try again in a few minutes.") // Set the message of the alert dialog to "There was an error creating your account. Please try again in a few minutes."
        builder.setPositiveButton("OK") { dialog, _ -> // Create a positive button with an OK listener
            dialog.dismiss() // Dismiss the dialog
        } // End the positive button
        val dialog: AlertDialog = builder.create() // Create a dialog variable with the builder
        dialog.show() // Show the dialog
    } // End the loginException function

    private fun checkProfilePicture(profilepic: Boolean): Boolean {
        if (selectedPhotoUri == null) {
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
            builder.setMessage("Please select a profile picture.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
            return false
        }
        else {
            return true
        }
    }



//    private fun uploadImageToFirebase() {
//        if (selectedPhotoUri == null) return
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
//        ref.putFile(selectedPhotoUri!!)
//            .addOnSuccessListener { it ->
//                Log.d("SignupActivity", "Successfully uploaded image: ${it.metadata?.path}")
//                ref.downloadUrl.addOnSuccessListener {
//                    Log.d("SignupActivity", "File Location: $it")
//                    loginSuccessfulRegister(it)
//                }
//            }
//            .addOnFailureListener {
//                Log.d("Activity", "Failed to upload image to storage: ${it.message}")
//            }
//    }
}



// CLASSES \\

class User(
    val uid: String,
    val username: String,
    val profileImageUrl: String
) // Create a class called User


// TODO: PROBLEM DESCRIPTION BELOW AS TO WHY THE PROGRAM CRASHES WHEN CREATING AN ACCOUNT:
//the issue starts when the user tries to create an account once all the details have been filled in correctly.
//The user account is created, the profile picture uploaded, however then the loginSuccessfulRegister() function is called on line 402
//when the image has been uploaded successfully;
//passing (it) to the function, the program sees this value as null and not as a string or anything (weird, i know)
//and because of this, the application crashes when reading this variable back in the loginSuccessfulRegister() function
//Specifically the error message mentions line 7 of the loginSuccessfulRegister() function.

//Error below:


// Fatal Exception: java.lang.NullPointerException: Parameter specified as non-null is null: method kotlin.jvm.internal.Intrinsics.checkNotNullParameter, parameter it
//       at com.example.instantmessagingapp.SignupActivity.loginSuccessfulRegister$lambda-5(:7)
//       at com.example.instantmessagingapp.SignupActivity.$r8$lambda$HmNNv8KHzGOYVACLA4a8QohVUTU()
//       at com.example.instantmessagingapp.SignupActivity$$ExternalSyntheticLambda3.onSuccess(:4)
