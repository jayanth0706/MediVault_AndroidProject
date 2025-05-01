package com.example.medivault_capstoneproject

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PatientRecords : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var container: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var deleteButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_records)

        dbHelper = DatabaseHelper(this)
        container = findViewById(R.id.container)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        deleteButton = findViewById(R.id.deleteButton)

        searchButton.setOnClickListener {
            val id = searchEditText.text.toString().trim()
            if (id.isNotEmpty()) {
                displayPatients(id)
            } else {
                displayPatients()
            }
        }

        deleteButton.setOnClickListener {
            val id = searchEditText.text.toString()
            if (id.isNotEmpty()) {
                val rowsDeleted = dbHelper.deletePatient(id)
                if (rowsDeleted > 0) {
                    Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show()
                    displayPatients()
                } else {
                    Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter an ID", Toast.LENGTH_SHORT).show()
            }
        }

        displayPatients()
    }

    private fun displayPatients(filterId: String? = null) {
        container.removeAllViews()
        val allPatients = dbHelper.getAllPatients()
        val patients = if (filterId != null) {
            allPatients.filter { it[DatabaseHelper.COLUMN_ID].toString() == filterId }
        } else {
            allPatients
        }

        if (patients.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No patient found."
                textSize = 16f
                setTextColor(android.graphics.Color.GRAY)
            }
            container.addView(emptyText)
            return
        }

        for (patient in patients) {
            val recordLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(10, 10, 10, 10)
            }

            fun makeBoldLabel(label: String, value: String): SpannableStringBuilder {
                val spannable = SpannableStringBuilder("$label: $value")
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return spannable
            }

            val idView = TextView(this).apply { text = makeBoldLabel("Candidate Id", patient[DatabaseHelper.COLUMN_ID].toString()) }
            val nameView = TextView(this).apply { text = makeBoldLabel("Name", patient[DatabaseHelper.COLUMN_NAME].toString()) }
            val genderView = TextView(this).apply { text = makeBoldLabel("Gender", patient[DatabaseHelper.COLUMN_GENDER].toString()) }
            val bloodGroupView = TextView(this).apply { text = makeBoldLabel("Blood Group", patient[DatabaseHelper.COLUMN_BLOOD_GROUP].toString()) }
            val mobileView = TextView(this).apply { text = makeBoldLabel("Mobile", patient[DatabaseHelper.COLUMN_MOBILE].toString()) }
            val diseaseView = TextView(this).apply { text = makeBoldLabel("Disease", patient[DatabaseHelper.COLUMN_DISEASE_DETAILS] + "\n") }

            recordLayout.addView(idView)
            recordLayout.addView(nameView)
            recordLayout.addView(genderView)
            recordLayout.addView(bloodGroupView)
            recordLayout.addView(mobileView)
            recordLayout.addView(diseaseView)

            val separator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 3
                )
                setBackgroundColor(android.graphics.Color.BLACK)
            }

            container.addView(recordLayout)
            container.addView(separator)
        }
    }
}
