package com.example.mydays.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val USERS: String = "users"
    const val ENTRIES: String = "entries"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val MY_PROFILE_REQUEST_CODE: Int = 11
    const val CREATE_ENTRY_REQUEST_CODE: Int = 12

    const val DOCUMENT_ID: String = "documentId"

    const val ASSIGNED_TO : String = "assignedTo"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"

    const val TITLE: String = "title"
    const val ENTRY: String = "entry"
    const val RATING: String = "rating"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }
    //Getting URI's extension type for the image.
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}