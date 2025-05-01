package com.example.medivault_capstoneproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medivault_capstoneproject.PatientRecords
import com.example.medivault_capstoneproject.R

class AddPatient : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient)

        dbHelper = DatabaseHelper(this)

        val candidateId = findViewById<EditText>(R.id.et_candidate_id)
        val patientName = findViewById<EditText>(R.id.et_patientName)
        val gender = findViewById<EditText>(R.id.et_gender)
        val bloodGroup = findViewById<EditText>(R.id.et_bloodGroup)
        val mobile = findViewById<EditText>(R.id.et_mobile)
        val diseaseDetails = findViewById<EditText>(R.id.et_diseaseDetails)
        val addButton = findViewById<Button>(R.id.addbutton1)

        addButton.setOnClickListener {
            val idText = candidateId.text.toString().trim()
            val nameText = patientName.text.toString().trim()
            val genderText = gender.text.toString().trim()
            val bloodGroupText = bloodGroup.text.toString().trim()
            val mobileText = mobile.text.toString().trim()
            val diseaseDetailsText = diseaseDetails.text.toString().trim()

            if (idText.isEmpty() || nameText.isEmpty() || genderText.isEmpty() || bloodGroupText.isEmpty() || mobileText.isEmpty() || diseaseDetailsText.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inserted = dbHelper.insertPatient(idText, nameText, genderText, bloodGroupText, mobileText, diseaseDetailsText)

            if (inserted) {
                Toast.makeText(this, "Patient added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PatientRecords::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to add patient", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
