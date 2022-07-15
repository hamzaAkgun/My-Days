package com.example.mydays.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mydays.R
import com.example.mydays.databinding.ActivityMyProfileBinding
import com.example.mydays.firebase.Firestore
import com.example.mydays.models.User
import com.example.mydays.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfileBinding

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUrl: String = " "
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noStatusBar()
        setUpActionBar(binding.toolbarMyProfileActivity)

        Firestore().loadUserData(this)

        binding.ivUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding.etName.text
        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog()
                updateUserProfileData()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this, "Permissions denied for storage", Toast.LENGTH_LONG
            ).show()
        }
    }


    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivUserImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }


        }
    }

    private fun setUpActionBar(toolbar: androidx.appcompat.widget.Toolbar) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        actionBar?.title = "My Profile"
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {

        mUserDetails = user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivUserImage)
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L) {
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage() {
        showProgressDialog()
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" +
                            System.currentTimeMillis() + "." + Constants.getFileExtension(this,
                        mSelectedImageFileUri //So each image has a unique name.
                    )
                )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                Log.i("Firebase Image URL", it.metadata!!.reference!!.downloadUrl.toString())
                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageUrl = uri.toString()
                    hideProgressDialog()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }



    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()


        if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageUrl

        }
        if (binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()

        }
        if (binding.etMobile.text.toString() != mUserDetails.mobile.toString() && binding.etMobile.text.toString().isNotEmpty()) {
                userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }
        Firestore().updateUserProfileData(this, userHashMap)

    }

    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}