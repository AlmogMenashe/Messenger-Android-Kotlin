package com.example.applicationkotlinmessenger.registerlogin

import com.example.applicationkotlinmessenger.models.User

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationkotlinmessenger.messages.LatestMessagesActivity
import com.example.applicationkotlinmessenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        register_button_register.setOnClickListener {
            performRegister() //פונקציה להרשמה
                }

        already_have_an_account_text_view.setOnClickListener {
            Log.d("MainActivity", "Try to show login activity")

            // launch the login activity somehow
            val intent = Intent( this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener{
            Log.d("mainActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null ){
            //proceed and check whte the selected image was...
            Log.d("RegisterActivity", "Photo was selected")
            selectedPhotoUri = data.data
            @Suppress("DEPRECATION") val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_imageview_register.alpha = 0f
          //  val bitmapDrawable = BitmapDrawable(bitmap)
           // selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/ow", Toast.LENGTH_SHORT).show()
            return //@setOnClickListener
        }
        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password is: $password")
        //Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { it ->
                if (!it.isSuccessful) return@addOnCompleteListener
                //else if successful
                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri==null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Seccessfully uploded image: ${it.metadata?.path}" )

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity","File Location: $it")
                    seveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                //do some logging here
            }
    }

    private fun seveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid,username_edittext_register.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally we saved the user to Firebashe Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to ser value to database: ${it.message}")

            }
    }
}



