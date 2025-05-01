package com.example.medivault_capstoneproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity() {

    private val pickImageRequestCode = 1000
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Retrieve UI elements
        val image = findViewById<ImageView>(R.id.doctorImage)
        val fullNameText = findViewById<TextView>(R.id.tv_fullname_value)
        val userNameText = findViewById<TextView>(R.id.tv_username_value)
        val emailText = findViewById<TextView>(R.id.tv_email_value)
        val phoneText = findViewById<TextView>(R.id.tv_phone_value)

        // Retrieve data from SharedPreferences
        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("fullname", "N/A")
        val username = sharedPreferences.getString("username", "N/A")
        val email = sharedPreferences.getString("email", "N/A")
        val phone = sharedPreferences.getString("phone", "N/A")


        fullNameText.text = fullName
        userNameText.text = username
        emailText.text = email
        phoneText.text = phone


        image.setImageResource(R.drawable.doctor)

        image.setOnClickListener {
            openImagePicker()
        }
    }

    // Open image picker (gallery)
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, pickImageRequestCode)
    }

    // Handle image result (for gallery pick)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == pickImageRequestCode) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                // Set the selected image to the ImageView
                val imageView = findViewById<ImageView>(R.id.doctorImage)
                imageView.setImageURI(it)

                // Optionally save the image URI to SharedPreferences (to persist it)
                val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("profile_image_uri", it.toString())
                editor.apply()
            }
        }
    }
}
