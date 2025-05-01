package com.example.medivault_capstoneproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Prescriptions : AppCompatActivity() {
    private lateinit var container: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prescriptions)

        container = findViewById(R.id.PrescriptionspatientsContainer)
        searchEditText = findViewById(R.id.PrescriptionssearchEditText)
        searchButton = findViewById(R.id.PrescriptionssearchButton)
        dbHelper = DatabaseHelper(this)

        // Request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }

        searchButton.setOnClickListener {
            val patientId = searchEditText.text.toString().trim()
            if (patientId.isNotEmpty()) {
                displayPatientRecords(patientId)
            } else {
                displayPatientRecords()
            }
        }

        displayPatientRecords()
    }

    private fun displayPatientRecords(filterId: String? = null) {
        container.removeAllViews()
        val patients = dbHelper.getAllPatients()
        val sharedPreferences = getSharedPreferences("com.example.medivault.prescriptions", Context.MODE_PRIVATE)

        val filteredPatients = if (filterId != null) {
            patients.filter { it[DatabaseHelper.COLUMN_ID].toString() == filterId }
        } else {
            patients
        }

        if (filteredPatients.isEmpty()) {
            val notFoundView = TextView(this).apply {
                text = "No matching patient found."
                textSize = 18f
                gravity = Gravity.CENTER
            }
            container.addView(notFoundView)
            return
        }

        for (patient in filteredPatients) {
            val recordLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(10, 10, 10, 10)
            }

            fun makeBoldLabel(label: String, value: String): SpannableStringBuilder {
                val spannable = SpannableStringBuilder("$label: $value")
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return spannable
            }

            val idView = TextView(this).apply {
                text = makeBoldLabel("Candidate Id", patient[DatabaseHelper.COLUMN_ID].toString())
                textSize = 18f
            }

            val nameView = TextView(this).apply {
                text = makeBoldLabel("Name", patient[DatabaseHelper.COLUMN_NAME].toString())
                textSize = 18f
            }

            val diseaseView = TextView(this).apply {
                text = makeBoldLabel("Disease", patient[DatabaseHelper.COLUMN_DISEASE_DETAILS].toString())
                textSize = 18f
            }

            val mobileNumber = TextView(this).apply {
                text = makeBoldLabel("Mobile", patient[DatabaseHelper.COLUMN_MOBILE].toString())
                textSize = 18f
            }

            val patientId = patient[DatabaseHelper.COLUMN_ID].toString()
            val patientName = patient[DatabaseHelper.COLUMN_NAME].toString()
            val patientMobile = patient[DatabaseHelper.COLUMN_MOBILE].toString()
            val savedPrescription = sharedPreferences.getString(patientId, "")

            val prescriptionInput = EditText(this).apply {
                hint = "Enter prescription"
                textSize = 18f
                maxLines = 5
                isVerticalScrollBarEnabled = true
                setPadding(10, 10, 10, 10)
                setBackgroundResource(android.R.drawable.edit_text)
                setText(savedPrescription)
                setHorizontallyScrolling(false)
                overScrollMode = View.OVER_SCROLL_ALWAYS
            }

            val saveButton = Button(this).apply {
                text = " Save Prescription "
                setTextColor(resources.getColor(android.R.color.white))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(20, 10, 20, 10)
                }
                setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))

                setOnClickListener {
                    val prescriptionText = prescriptionInput.text.toString().trim()
                    val previousPrescription = sharedPreferences.getString(patientId, "")

                    if (prescriptionText.isNotEmpty()) {
                        if (prescriptionText != previousPrescription) {
                            // Save updated prescription
                            with(sharedPreferences.edit()) {
                                putString(patientId, prescriptionText)
                                apply()
                            }

                            // Send SMS (use SmsManager for Android 9 and below, Intent for 10+)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:$patientMobile")
                                    putExtra("sms_body", "Hi $patientName, your prescription is: $prescriptionText")
                                }
                                try {
                                    startActivity(smsIntent)
                                    Toast.makeText(this@Prescriptions, "Prescription saved. SMS app opened.", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(this@Prescriptions, "Failed to open SMS app", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (ContextCompat.checkSelfPermission(this@Prescriptions, Manifest.permission.SEND_SMS)
                                    == PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        val smsManager = SmsManager.getDefault()
                                        smsManager.sendTextMessage(
                                            patientMobile,
                                            null,
                                            "Hi $patientName, Your Prescription is: $prescriptionText",
                                            null,
                                            null
                                        )
                                        Toast.makeText(this@Prescriptions, "Prescription saved and SMS sent!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(this@Prescriptions, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@Prescriptions, "SMS permission not granted", Toast.LENGTH_SHORT).show()
                                }
                            }

                        } else {
                            Toast.makeText(this@Prescriptions, "No changes detected. Prescription not updated.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Prescriptions, "Enter a prescription!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            recordLayout.addView(idView)
            recordLayout.addView(nameView)
            recordLayout.addView(diseaseView)
            recordLayout.addView(mobileNumber)
            recordLayout.addView(prescriptionInput)
            recordLayout.addView(saveButton)

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
