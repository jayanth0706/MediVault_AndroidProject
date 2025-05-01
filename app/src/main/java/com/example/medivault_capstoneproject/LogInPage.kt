package com.example.medivault_capstoneproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LogInPage : AppCompatActivity() {
    private lateinit var etUsernameOrEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in_page)

        etUsernameOrEmail = findViewById(R.id.et1)
        etPassword = findViewById(R.id.et2)
        loginButton = findViewById(R.id.btn1)
        createAccountButton = findViewById(R.id.btn2)

        val sharedPreferences = getSharedPreferences("DoctorPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", "")
        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")

        loginButton.setOnClickListener {
            val enteredUserOrEmail = etUsernameOrEmail.text.toString().trim()
            val enteredPassword = etPassword.text.toString().trim()

            if ((enteredUserOrEmail == savedUsername || enteredUserOrEmail == savedEmail)
                && enteredPassword == savedPassword) {

                val loginPreferences = getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
                loginPreferences.edit().putString("logged_in_doctor", enteredUserOrEmail).apply()

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeScreen::class.java)
                intent.putExtra("USERNAME", savedUsername) // Send username to HomeScreen
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountButton.setOnClickListener {
            startActivity(Intent(this, CreateAccount::class.java))
        }
    }
}
