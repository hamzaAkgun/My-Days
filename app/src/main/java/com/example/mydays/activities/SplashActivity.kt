package com.example.mydays.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.bumptech.glide.Glide
import com.example.mydays.R
import com.example.mydays.databinding.ActivitySplashBinding
import com.example.mydays.firebase.Firestore


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noStatusBar()


        showGif(R.drawable.writing)

        Handler().postDelayed({
            val currentUserID = Firestore().getCurrentUserId()
            if (currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2500)

    }

    private fun showGif(gif: Int) {
        Glide.with(this).load(gif).into(binding.ivWriting)
    }
}