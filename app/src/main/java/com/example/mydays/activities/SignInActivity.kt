package com.example.mydays.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.mydays.R
import com.example.mydays.databinding.ActivitySignInBinding
import com.example.mydays.firebase.Firestore
import com.example.mydays.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noStatusBar()
        setUpActionBar(binding.toolbarSignInActivity)

        auth = FirebaseAuth.getInstance()
        binding.btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }


    }

    private fun setUpActionBar(toolbar: androidx.appcompat.widget.Toolbar) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    private fun signInRegisteredUser() {
        val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Firestore().loadUserData(this)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "createUserWithEmail:failure", task.exception?.cause)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }


        }

    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> true
        }
    }
}