package com.example.mydays.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


data class Entry(
    val title: String = "",
    val entry: String = "",
    val date: String = "",
    val image: String = "",
    val rating: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentId: String = ""
): Serializable