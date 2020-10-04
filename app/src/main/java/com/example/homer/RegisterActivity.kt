package com.example.homer

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()

        register_button.setOnClickListener {
            registerUser()
        }

        }

    private fun registerUser() {
        val username:String = username_register.text.toString()
        val email:String = email_register.text.toString()
        val password:String = password_register.text.toString()
        if (username == "")
        {
            Toast.makeText(this@RegisterActivity,"Please write Username ! ",Toast.LENGTH_LONG).show()
        }
        if (email == "")
        {
            Toast.makeText(this@RegisterActivity,"Please write e-mail ! ",Toast.LENGTH_LONG).show()
        }
        if (password == "")
        {
            Toast.makeText(this@RegisterActivity,"Please write Password ! ",Toast.LENGTH_LONG).show()
        }
        else {
            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)
                        val userHashMap = HashMap<String,Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/homer-27e46.appspot.com/o/profile.png?alt=media&token=30516510-0876-42ce-a521-2901df78cd9a"
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/homer-27e46.appspot.com/o/cover.jpg?alt=media&token=a8aace44-127e-460c-9163-ecf6404a1ca9"
                        userHashMap["status"] = "Offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://www.google.com"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful)
                                {
                                    val intent =Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    }
                    else
                    {
                        Toast.makeText(this@RegisterActivity,"Error Message " + task.exception?.message.toString(),Toast.LENGTH_LONG).show()
                    }
                }
        }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }


}
