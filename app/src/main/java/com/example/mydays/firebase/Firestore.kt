package com.example.mydays.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.mydays.activities.*
import com.example.mydays.models.Entry
import com.example.mydays.models.User
import com.example.mydays.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class Firestore {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Registration Failed")
            }
    }

    fun createEntry(activity: AddEntryActivity, entry: Entry) {
        mFireStore.collection(Constants.ENTRIES)
            .document()
            .set(entry, SetOptions.merge())
            .addOnSuccessListener {
                activity.entryCreatedSuccessfully()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Firestore", "createEntry: FAILED: $it")
            }
    }




    fun loadUserData(activity: Activity, readEntriesList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                when (activity) {
                    is SignInActivity -> {
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity -> {
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readEntriesList)
                        }
                    }
                    is MyProfileActivity -> {
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }

            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Registration Failed", e)
            }

    }

    fun getCurrentUserId(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getEntryDetails(activity: EditActivity, documentId: String) {
        mFireStore.collection(Constants.ENTRIES)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i("Firestore", document.toString())
                val entry = document.toObject(Entry::class.java)!!
                entry.documentId = document.id
                activity.entryDetails(entry)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Firestore", "Error while getting the entry", it)

            }
    }
    fun getEntriesList(activity: MainActivity) {
        mFireStore.collection(Constants.ENTRIES)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i("Firestore", document.documents.toString())
                val entriesList: ArrayList<Entry> = ArrayList()
                for (i in document.documents) {
                    val entry = i.toObject(Entry::class.java)!!
                    entry.documentId = i.id
                    entriesList.add(entry)
                }

                activity.populateEntriesList(entriesList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Firestore", "Error while creating an entry", it)
            }
    }

    fun getEntryListWithRating(activity: MainActivity, rating: String) {
        mFireStore.collection(Constants.ENTRIES)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .whereEqualTo(Constants.RATING, rating)
            .get()
            .addOnSuccessListener { document ->
                Log.i("Firestore", document.toString())
                val entriesList: ArrayList<Entry> = ArrayList()
                for (i in document) {
                    val entry = i.toObject(Entry::class.java)
                    entry.documentId = i.id
                    entriesList.add(entry)
                }
                activity.populateEntriesListWithRatings(entriesList, rating)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Firestore", "Error while getting the entry details with rating", it)

            }
    }

    fun updateUserProfileData(
        activity: MyProfileActivity,
        userHashMap: HashMap<String, Any>
    ) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i("TAG", "Profile Data Updated Successfully")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("TAG", "updateUserProfileData Error: $it")
                Toast.makeText(activity, "Update failed!", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateEntryData(
        activity: EditActivity,
        documentId: String,
        entryHashMap: HashMap<String, Any>
    ) {


        mFireStore.collection(Constants.ENTRIES)
            .document(documentId)
            .update(entryHashMap)
            .addOnSuccessListener {
                Log.i("FireStore", "Entry updated successfully")
                activity.updateEntryListSuccess()
            }
            .addOnFailureListener {
                Log.e("FireStore", "Unable to update, caused by: $it")
                activity.hideProgressDialog()
            }
    }
}