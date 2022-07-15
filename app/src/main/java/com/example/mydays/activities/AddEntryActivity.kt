package com.example.mydays.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.mydays.R
import com.example.mydays.databinding.ActivityAddEntryBinding
import com.example.mydays.firebase.Firestore
import com.example.mydays.models.Entry
import com.example.mydays.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddEntryActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mEntryImageURL: String = ""

    private lateinit var binding: ActivityAddEntryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noStatusBar()

        if (intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        setUpActionBar(binding.toolbarAddEntry)

        binding.fabAddImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
                binding.ivAddEntry.visibility = View.VISIBLE
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding.btnSaveEntries.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadEntryImage()
            }else{
                showProgressDialog()
                binding.ivAddEntry.visibility = View.GONE
                createEntry()
            }

        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        return simpleDateFormat.format(Date())

    }
    private fun createEntry(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())
        val entry = Entry(
            binding.etTitle.text.toString(),
            binding.etSubtitle.text.toString(),
            getDate(), //Caution
            mEntryImageURL,
            binding.ratingBar.rating.toString(),
            assignedUsersArrayList
        )

        Firestore().createEntry(this, entry)
    }

    private fun uploadEntryImage(){
        showProgressDialog()
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "ENTRY_IMAGE" +
                            System.currentTimeMillis() + "." + Constants.getFileExtension(this,
                        mSelectedImageFileUri //So each image has a unique name.
                    )
                )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                Log.i("Entry Image URL", it.metadata!!.reference!!.downloadUrl.toString())

                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mEntryImageURL = uri.toString()
                    hideProgressDialog()
                    binding.ivAddEntry.visibility = View.VISIBLE
                    createEntry()
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun entryCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
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
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .into(binding.ivAddEntry)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}