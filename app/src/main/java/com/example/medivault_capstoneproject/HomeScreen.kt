package com.example.medivault_capstoneproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)
        val username = intent.getStringExtra("USERNAME") ?: "Doctor"
        val welcomeTextView = findViewById<TextView>(R.id.welcomeText)
        welcomeTextView.text = "Welcome, Dr. $username"

        val patientRecordsButton = findViewById<Button>(R.id.patientRecordsButton)
        patientRecordsButton.setOnClickListener {
            val intent = Intent(this, PatientRecords::class.java)
            startActivity(intent)
        }

        val appointmentsButton = findViewById<Button>(R.id.appointmentsButton)
        appointmentsButton.setOnClickListener {
            val intent = Intent(this, Appointments::class.java)
            startActivity(intent)
        }

        val prescriptionsButton = findViewById<Button>(R.id.prescriptionsButton)
        prescriptionsButton.setOnClickListener {
            val intent = Intent(this, Prescriptions::class.java)
            startActivity(intent)
        }

        val labReportsButton = findViewById<Button>(R.id.labReportsButton)
        labReportsButton.setOnClickListener {
            val intent = Intent(this, LabReports::class.java)
            startActivity(intent)
        }

        val addPatientButton = findViewById<Button>(R.id.addPatientButton)
        addPatientButton.setOnClickListener {
            val intent = Intent(this, AddPatient::class.java)
            startActivity(intent)
        }

        val profile = findViewById<Button>(R.id.homeTab)
        profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        val settings = findViewById<Button>(R.id.settingsTab)
        settings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }


    }
}
