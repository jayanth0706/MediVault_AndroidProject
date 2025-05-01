package com.example.medivault_capstoneproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateAccount : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var saveButton: Button

    private lateinit var fullName: EditText
    private lateinit var userName: EditText
    private lateinit var gmail: EditText
    private lateinit var mobileNumber: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        fullName = findViewById(R.id.et_fullname)
        userName = findViewById(R.id.et_userName)
        gmail = findViewById(R.id.et_gmail)
        mobileNumber = findViewById(R.id.et_mobileNumber)
        password = findViewById(R.id.et_password)
        confirmPassword = findViewById(R.id.et_confirmPassword)
        saveButton = findViewById(R.id.savebutton1)

        sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)

        saveButton.setOnClickListener {
            val name = fullName.text.toString()
            val uname = userName.text.toString()
            val email = gmail.text.toString()
            val phone = mobileNumber.text.toString()
            val pass = password.text.toString()
            val confirmPass = confirmPassword.text.toString()

            if (name.isEmpty() || uname.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                val editor = sharedPreferences.edit()
                editor.putString("username", uname)
                editor.putString("fullname", name)
                editor.putString("phone", phone)
                editor.putString("email", email)
                editor.putString("password", pass)
                editor.apply()

                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()


                startActivity(Intent(this, LogInPage::class.java))
                finish()
            }
        }
    }
}
