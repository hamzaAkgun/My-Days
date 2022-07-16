package com.example.mydays.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mydays.R
import com.example.mydays.adapters.EntryItemsAdapter
import com.example.mydays.databinding.ActivityMainBinding
import com.example.mydays.firebase.Firestore
import com.example.mydays.models.Entry
import com.example.mydays.models.User
import com.example.mydays.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mUserName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noStatusBar()


        setUpActionBar(binding.appBarMain.toolbarMainActivity)
        binding.navView.setNavigationItemSelectedListener(this)
        Firestore().loadUserData(this, true)

        binding.appBarMain.fabCreateEntry.setOnClickListener {
            val intent = Intent(this, AddEntryActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, Constants.CREATE_ENTRY_REQUEST_CODE)
        }


    }

    fun populateEntriesListWithRatings(entriesList: ArrayList<Entry>, rating: String) {
        hideProgressDialog()
        if (entriesList.size > 0) {
            rv_entries.visibility = View.VISIBLE
            tv_no_entries.visibility = View.GONE
            rv_entries.layoutManager = LinearLayoutManager(this)
            rv_entries.setHasFixedSize(true)
            val adapter = EntryItemsAdapter(this, entriesList)
            rv_entries.adapter = adapter



            adapter.setOnClickListener(object : EntryItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Entry) {
                    val intent = Intent(this@MainActivity, EditActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        } else {
            rv_entries.visibility = View.GONE
            tv_no_entries.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ratings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.five_stars -> {
                try {
                    Firestore().getEntryListWithRating(this, "5.0")
                } catch (e: Exception) {
                    Log.e("Main Act", "onOptionsItemSelected: Failed due to $e")
                }
            }
            R.id.four_stars -> {
                try {
                    Firestore().getEntryListWithRating(this, "4.0")
                } catch (e: Exception) {
                    Log.e("Main Act", "onOptionsItemSelected: Failed due to $e")
                }
            }
            R.id.three_stars -> {
                try {
                    Firestore().getEntryListWithRating(this, "3.0")
                } catch (e: Exception) {
                    Log.e("Main Act", "onOptionsItemSelected: Failed due to $e")
                }
            }
            R.id.two_stars -> {
                try {
                    Firestore().getEntryListWithRating(this, "2.0")
                } catch (e: Exception) {
                    Log.e("Main Act", "onOptionsItemSelected: Failed due to $e")
                }
            }
            R.id.one_star -> {
                try {
                    Firestore().getEntryListWithRating(this, "1.0")
                } catch (e: Exception) {
                    Log.e("Main Act", "onOptionsItemSelected: Failed due to $e")
                }
            }
            R.id.all -> {
                try {
                    Firestore().getEntriesList(this)
                } catch (e: Exception) {
                    Log.e("Main Act", "onOptionsItemSelected: Failed due to $e")
                }
            }
        }
        return true
    }


    fun populateEntriesList(entriesList: ArrayList<Entry>) {
        hideProgressDialog()
        if (entriesList.size > 0) {
            rv_entries.visibility = View.VISIBLE
            tv_no_entries.visibility = View.GONE
            rv_entries.layoutManager = LinearLayoutManager(this)
            rv_entries.setHasFixedSize(true)
            val adapter = EntryItemsAdapter(this, entriesList)
            rv_entries.adapter = adapter



            adapter.setOnClickListener(object : EntryItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Entry) {
                    val intent = Intent(this@MainActivity, EditActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        } else {
            rv_entries.visibility = View.GONE
            tv_no_entries.visibility = View.VISIBLE
        }
    }

    private fun setUpActionBar(toolbar: androidx.appcompat.widget.Toolbar) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.MY_PROFILE_REQUEST_CODE) {
            Firestore().loadUserData(this)
        } else if (resultCode == Activity.RESULT_OK && requestCode == Constants.CREATE_ENTRY_REQUEST_CODE) {
            Firestore().getEntriesList(this)
        } else {
            Log.e("Main Act", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    Constants.MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, IntroActivity::class.java))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(user: User, readEntriesList: Boolean) {

        mUserName = user.name
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        tv_username.text = user.name

        if (readEntriesList) {
            showProgressDialog()
            Firestore().getEntriesList(this)
        }
    }


}