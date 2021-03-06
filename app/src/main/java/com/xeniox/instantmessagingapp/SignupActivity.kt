package com.xeniox.instantmessagingapp
// imports
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.livinglifetechway.k4kotlin.core.hideKeyboard
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.ByteArrayOutputStream
import java.util.*


private lateinit var firebaseAnalytics: FirebaseAnalytics
// Main class for the SignupActivity
class SignupActivity : AppCompatActivity() { // Start of class
    override fun onCreate(savedInstanceState: Bundle?) { // onCreate method
        super.onCreate(savedInstanceState) // call super class onCreate
        setContentView(R.layout.activity_signup) // set the layout

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        // Listen for the create account button press
        button_create_account.setOnClickListener { // Create a new user with email and password
            Log.d("SignupActivity", "Button clicked") // Log the button press
            hideKeyboard() // Hide the keyboard

            val profilepic = false
            val username =
                username_editText_register.text.toString().trim() // Set username to the text entered into the username text box
            val email =
                email_editText_register.text.toString().trim() // Set email to the text entered into the email text box
            val password =
                password_editText_register.text.toString() // Set password to the text entered into the password text box
            val confirmPassword =
                confirm_password_editText_register.text.toString() // Set confirmPassword to the text entered into the confirm_password text box

            // I have no idea why this code works, but it does
            // However, something may break if I remove it
            try { // Try to create the user
                if (checkUsername() && checkEmail() && checkPassword() && checkPasswordsMatch() && checkProfilePicture()) { // If all the fields are filled in and the passwords match
                    Log.d("SignupActivity", "Everything validated")

                    val context = this // Set context to this
                    val title =
                        SpannableString("Creating Account") // Set title to the text "Creating Account"
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
                    dialog.setCancelable(false) // Set the alert dialog to not be cancelable
                    // Change the font of the message to the Roboto font
                    dialog.show()
                    Log.d("SignupActivity", "dialog shown")


                    // Create a new user with the email and password
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnSuccessListener { it ->
                        Log.d(
                            "SignupActivity",
                            "user created with UID $it"
                        )// Add an onCompleteListener to the task
                        if (selectedPhotoUri == null) {
                            Log.d("SignupAcitivity", "The user has not uploaded an image")
                        }
                        if (checkProfilePicture()) {
                            val filename = UUID.randomUUID().toString()
                            val ref =
                                FirebaseStorage.getInstance().getReference("/images/$filename")
                            val bitmap = MediaStore.Images.Media.getBitmap(
                                contentResolver,
                                selectedPhotoUri
                            )
                            compressBitmap(bitmap, 10)

                            // add

                            ref.putFile(selectedPhotoUri!!).addOnSuccessListener { it ->
                                Log.d(
                                    "SignupActivity",
                                    "Successfully uploaded image: ${it.metadata?.path}"
                                )


                                ref.downloadUrl.addOnSuccessListener {
                                    Log.d("SignupActivity", "File Location: $it")
                                    val fileLocation = it
                                    val uid = FirebaseAuth.getInstance().uid ?: "" // Get the user's uid
//                                    val uid = FirebaseAuth.getInstance().uid.toString()
                                    val ref =
                                        FirebaseDatabase.getInstance("https://instant-messaging-app-7fed6-default-rtdb.europe-west1.firebasedatabase.app/")
                                            .getReference("/users/$uid") // Get the user's reference
                                    Log.d("SignupActivity", "${filename}, $fileLocation")
                                    val profileImageUrl = fileLocation.toString()
                                    val user = User(
                                        uid,
                                        username_editText_register.text.toString().trim(),
                                        profileImageUrl,
                                        email,
                                        "-16728876",
                                        "Hi there, I'm using Recivo. I'm new to this app. I'm looking for a new friend to chat with.",
                                        "null",
                                        "null",
                                        "I'm using recivo"
                                    ) // Create a user object with the user's uid and username
                                    Log.d("SignupActivity", "user: $user")
                                    ref.setValue(user)
                                        .addOnSuccessListener { // Add an onSuccessListener to the setValue function
                                            val statusRef = FirebaseDatabase.getInstance("https://instant-messaging-app-7fed6-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/status/$uid")
                                            val status = Status(-1)
                                            statusRef.setValue(status) // create a user node with status.
                                            Log.d(
                                                "SignupActivity",
                                                "Finally we saved the user to Firebase Database"
                                            ) // Log that the user has been saved to Firebase Database
                                            Log.d("SignupActivity", "Intent = $intent")
                                            dialog.dismiss()
                                            val bundle = Bundle()
                                            bundle.putString(FirebaseAnalytics.Param.METHOD, "Sign Up")
                                            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
                                            val intent = Intent(
                                                this,
                                                LoginAccountCreatedActivity::class.java
                                            ) // Create an intent to the LoginAccountCreatedActivity
                                            startActivity(intent) // Start the intent
                                            finish() // Finish the current activity
                                        } // End the addOnSuccessListener
                                        .addOnFailureListener {   // Add an onFailureListener to the setValue function
                                            Log.d(
                                                "SignupActivity",
                                                "Failed to set value to database: ${it.message}"
                                            ) // Log that the user has failed to be saved to Firebase Database
                                            dialog.dismiss()
                                            //TODO: Handle failure and show error message to user
                                        } // End the addOnFailureListener
                                }
                            }.addOnFailureListener {
                                Log.d(
                                    "Activity",
                                    "Failed to upload image to storage: ${it.message}"
                                )
                                dialog.dismiss()
                            }
                        }
//                        if (!checkProfilePicture(profilepic)) {
//                            Log.d("SignupActivity","The profile picture was not selected")
//                        }

                    } .addOnFailureListener {
                        dialog.dismiss()
                        loginFailedRegister(it)
                    }
//                    dialog.dismiss() // Dismiss the alert dialog
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

            if (username_editText_register.text.toString().isNotEmpty() || email_editText_register.text.toString().isNotEmpty() || password_editText_register.text.toString().isNotEmpty() || confirm_password_editText_register.text.toString().isNotEmpty()) {
                val builder = AlertDialog.Builder(this)
                val title =
                    SpannableString("Discard information?")
                title.setSpan( // Set the alignment of the text to center
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment to center
                    0, // Set the start of the alignment to 0
                    title.length, // Set the end of the alignment to the length of the text
                    0 // Set the flags to 0
                ) // End the alignment
                builder.setTitle(title)
                val message = SpannableString("Are you sure you want to discard the information you entered?")
                message.setSpan( // Set the alignment of the text to center
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), // Set the alignment to center
                    0, // Set the start of the alignment to 0
                    message.length, // Set the end of the alignment to the length of the text
                    0 // Set the flags to 0
                ) // End the alignment
                builder.setMessage(message)
                builder.setPositiveButton("Yes") { _, _ ->
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
                    overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    ) // Override the pending transition to slide in from the right and slide out to the left
                    startActivity(intent) // Start the login activity
                    // override transition to material design
                }
                builder.setNegativeButton("No") { _, _ ->
                    builder.create().dismiss() // Dismiss the alert dialog
                }
                builder.show() // show the dialog
            } else{
                //create an intent to open the login activity
                val intent = Intent(
                    this,
                    LoginActivity::class.java
                ) // Set intent to an intent to open the login activity
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // Set the flags of the intent to clear the task and start a new task
                startActivity(intent) // Start the login activity
            }
        }

        button_profile_picture.setOnClickListener {
//            Log.d(
//                "SignupActivity",
//                "Profile picture button clicked"
//            ) // Log the profile picture button clicked
//
//            //create an intent to open the gallery
//            val intent = Intent(Intent.ACTION_PICK) // Set intent to an intent to open the gallery
//            intent.type = "image/*" // Set the type of the intent to image
//            startActivityForResult(
//                intent,
//                0
//            ) // Start the activity for result with the intent and the request code

            ImagePicker.with(this)
                .crop()
                .maxResultSize(128,128)
                .cropSquare()
                .compress(2048)
                .start()
        }
    }

    private var selectedPhotoUri: Uri? = null // Create a variable to hold the selected photo uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {

                selectedPhotoUri = data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    selectedPhotoUri
                ) // Get the bitmap from the uri

                selectphoto_imageview_register.setImageBitmap(bitmap) // Set the image view to the bitmap

                button_profile_picture.alpha = 0f

            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }


    ///// FUNCTIONS \\\\\


    private fun checkUsername(): Boolean { // Create a function called checkUsername
        if (username_editText_register.text.toString().trim().isNotEmpty()
        ) { // If the username text box is not empty
            username_editText_register_layout.isErrorEnabled = true
            if (username_editText_register.text.toString().length >= 18) { // If the length of the username is less than 6 or greater than 18
                username_editText_register_layout.error =
                    "Username must be no more than 18 characters" // Set the error message of the username text box to "Username must be at least 6 characters and no more than 18 characters"
                username_editText_register.requestFocus() // Request focus of the username text box
                loginException()
                return false// Return
            }
        }
        return if (username_editText_register.text.toString()
                .isEmpty()
        ) { // If the username text box is empty
            username_editText_register_layout.isErrorEnabled = true
            username_editText_register_layout.error =
                "Username cannot be empty" // Set the error message of the username text box to "Username cannot be empty"
            username_editText_register.requestFocus() // Request focus of the username text box
            Log.d("SignupActivity", "username is not valid")
            false// Return
        } else {
            username_editText_register_layout.isErrorEnabled =
                false // Clear the error message of the username text box
            Log.d("SignupActivity", "Username is valid") // Log that the username is valid
            true
        }
    }

    private fun checkEmail(): Boolean { // Create a function called checkEmail
        if (!email_editText_register.text.isNullOrEmpty()) { // If the email text box is not empty
            if (!Patterns.EMAIL_ADDRESS.matcher(email_editText_register.text.toString().trim()).matches()
            ) { // If the email text box does not match the email pattern
                email_editText_register_layout.isErrorEnabled = true
                email_editText_register_layout.error =
                    "Email is invalid" // Set the error message of the email text box to "Email is invalid"
                email_editText_register.requestFocus() // Request focus of the email text box
                loginException()
                return false// Return
            }
        }
        return if (email_editText_register.text.toString()
                .isEmpty()
        ) { // If the email text box is empty
            email_editText_register_layout.isErrorEnabled = true
            email_editText_register_layout.error =
                "Email is required" // Set the error message of the email text box to "Email is required"
            email_editText_register.requestFocus() // Request focus of the email text box
            loginException()
            false// Return
        } else {
            email_editText_register_layout.isErrorEnabled =
                false // Clear the error message of the email text box
            true
        }
    }

    private fun checkPassword(): Boolean { // Create a function called checkPassword
        if (password_editText_register.text.toString()
                .isNotEmpty()
        ) { // If the password text box is not empty
            if (password_editText_register.text.toString().length < 6) { // If the length of the password is less than 6
                password_editText_register_layout.isErrorEnabled = true
                password_editText_register_layout.error =
                    "Password must be at least 6 characters" // Set the error message of the password text box to "Password must be at least 6 characters"
                password_editText_register.requestFocus() // Request focus of the password text box
                loginException()
                return false // Return
            }
        }
        return if (password_editText_register.text.toString().isEmpty()
        ) { // If the password text box is empty
            password_editText_register_layout.isErrorEnabled = true
            password_editText_register_layout.error =
                "Password cannot be empty" // Set the error message of the password text box to "Password cannot be empty"
            password_editText_register.requestFocus() // Request focus of the password text box
            loginException()
            false// Return
        } else {
            password_editText_register_layout.isErrorEnabled =
                false // Clear the error message of the password text box
            true
        }
    } // End the checkPassword function

    private fun checkPasswordsMatch(): Boolean { // Create a function called checkPasswordsMatch
        if (confirm_password_editText_register.text.toString()
                .isNotEmpty()
        ) { // If the confirm password text box is not empty
            if (confirm_password_editText_register.text.toString() != password_editText_register.text.toString()) { // If the confirm password text box does not match the password text box
                confirm_password_editText_register_layout.isErrorEnabled = true
                confirm_password_editText_register_layout.error =
                    "Passwords do not match" // Set the error message of the confirm password text box to "Passwords do not match"
                confirm_password_editText_register.requestFocus() // Request focus of the confirm password text box
                loginException()
                return false// Return
            } // End the if statement
        }
        return if (confirm_password_editText_register.text.toString()
                .isEmpty()
        ) { // If the confirm password text box is empty
            confirm_password_editText_register_layout.isErrorEnabled = true
            confirm_password_editText_register_layout.error =
                "Confirm Password cannot be empty" // Set the error message of the confirm password text box to "Confirm Password cannot be empty"
            confirm_password_editText_register.requestFocus() // Request focus of the confirm password text box
            loginException()
            false // Return
        } else {
            confirm_password_editText_register_layout.isErrorEnabled = false // Clear the error message of the confirm password text box
            true
        }
    } // End the checkPasswordsMatch function

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
            dialog.setCancelable(false)
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
            dialog.setCancelable(false)
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
        builder.setMessage("There was an error creating your account. Please check your details and try again.") // Set the message of the alert dialog to "There was an error creating your account. Please try again in a few minutes."
        builder.setPositiveButton("OK") { dialog, _ -> // Create a positive button with an OK listener
            dialog.dismiss() // Dismiss the dialog
        } // End the positive button
        val dialog: AlertDialog = builder.create() // Create a dialog variable with the builder
        dialog.setCancelable(false)
        dialog.show() // Show the dialog
    } // End the loginException function

    private fun checkProfilePicture(): Boolean {
        if (selectedPhotoUri == null) {
            val context = this
            val title = SpannableString("No profile picture was selected")
            title.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                title.length,
                0
            )
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage("There was a problem creating your account. Please select a profile picture and try again.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
            return false
        } else {
            return true
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}

// CLASSES \\

// TODO PROBLEM DESCRIPTION BELOW AS TO WHY THE PROGRAM CRASHES WHEN CREATING AN ACCOUNT:
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


// 25/2/22 - Fixed error as the user could create an account with the same email and wipe the databse :( - this has now been fixed by calling the loginfailedregister() function
