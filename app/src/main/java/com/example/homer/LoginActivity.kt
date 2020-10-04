package com.example.homer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        login_button.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email:String = email_login.text.toString()
        val password:String = password_login.text.toString()

        if (email == "")
        {
            Toast.makeText(this@LoginActivity,"Please write e-mail ! ", Toast.LENGTH_LONG).show()
        }
        if (password == "")
        {
            Toast.makeText(this@LoginActivity,"Please write Password ! ", Toast.LENGTH_LONG).show()
        }
        else{
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        val intent =Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        Toast.makeText(this@LoginActivity,"Error Message " + task.exception?.message.toString(),Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }
}