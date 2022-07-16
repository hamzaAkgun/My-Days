package com.example.mydays.activities

import android.Manifest
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
import com.example.mydays.databinding.ActivityEditBinding
import com.example.mydays.firebase.Firestore
import com.example.mydays.models.Entry
import com.example.mydays.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class EditActivity : BaseActivity() {

    private lateinit var binding: ActivityEditBinding
    private var mSelectedImageFileUri: Uri? = null
    private var mEntryImageURL: String = ""
    private lateinit var mEntryDetails: Entry
    var entryDocumentId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noStatusBar()
        setUpActionBar(binding.toolbarEditEntry)


        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            entryDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog()
        Firestore().getEntryDetails(this, entryDocumentId) // We set the UI here.


        binding.fabUpdateImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
                binding.ivEditEntry.visibility = View.VISIBLE
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        binding.btnUpdateEntries.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                updateEntryImage()
            } else {
                showProgressDialog()
                binding.ivEditEntry.visibility = View.GONE
                updateEntryData()
                startActivity(Intent(this, MainActivity::class.java))

            }
        }
    }

    fun entryDetails(entry: Entry) {
        mEntryDetails = entry
        hideProgressDialog()
        binding.etTitle.setText(entry.title)
        binding.etSubtitle.setText(entry.entry)
        binding.ratingBar.rating = entry.rating.toFloat()
        Glide
            .with(this@EditActivity)
            .load(entry.image)
            .centerCrop()
            .into(binding.ivEditEntry)

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

    private fun updateEntryImage() {
        showProgressDialog()
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "ENTRY_IMAGE" +
                            System.currentTimeMillis() + "." + Constants.getFileExtension(
                        this,
                        mSelectedImageFileUri //So each image has a unique name.
                    )
                )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                Log.i("Entry Image URL", it.metadata!!.reference!!.downloadUrl.toString())

                it.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mEntryImageURL = uri.toString()
                    hideProgressDialog()
                    if(binding.ivEditEntry.visibility != View.VISIBLE){
                        binding.ivEditEntry.visibility = View.VISIBLE
                    }
                    updateEntryData()
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun updateEntryListSuccess() {
        hideProgressDialog()
        Firestore().getEntryDetails(this, mEntryDetails.documentId)
    }

    private fun updateEntryData() {
        val entryHashMap = HashMap<String, Any>()


        if (mEntryImageURL.isNotEmpty() && mEntryImageURL != mEntryDetails.image) {
            entryHashMap[Constants.IMAGE] = mEntryImageURL

        }
        if (binding.etTitle.text.toString() != mEntryDetails.title) {
            entryHashMap[Constants.TITLE] = binding.etTitle.text.toString()

        }
        if (binding.etSubtitle.text.toString() != mEntryDetails.entry && binding.etSubtitle.text.toString()
                .isNotEmpty()
        ) {
            entryHashMap[Constants.ENTRY] = binding.etSubtitle.text.toString()
        }
        if (binding.ratingBar.rating.toString() != mEntryDetails.rating && binding.ratingBar.rating.toString()
                .isNotEmpty()
        ) {
            entryHashMap[Constants.RATING] = binding.ratingBar.rating.toString()
        }
        Firestore().updateEntryData(this, entryDocumentId, entryHashMap)


    }

}