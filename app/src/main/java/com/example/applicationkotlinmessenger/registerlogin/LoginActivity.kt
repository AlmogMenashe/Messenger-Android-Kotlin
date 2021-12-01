package com.example.applicationkotlinmessenger.registerlogin

import android.content.Intent
import com.example.applicationkotlinmessenger.models.User
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationkotlinmessenger.R
import com.example.applicationkotlinmessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            Log.d("login", "Attempt login with email/pw:  $email/***")
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
              //  .addOnCompleteListener {  }
                .addOnCompleteListener(this) { it ->
                    if (!it.isSuccessful) return@addOnCompleteListener
                    //else if successful
                    Log.d("LoginActivity", "Successfully created user with uid: ${it.result?.user?.uid}")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)


                }
                .addOnFailureListener{
                    Log.d("LoginActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
                }



        }

        Back_to_registration_text_view.setOnClickListener{
            finish()
        }

    }

}