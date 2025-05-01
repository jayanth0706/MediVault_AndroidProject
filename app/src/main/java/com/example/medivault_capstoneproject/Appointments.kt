package com.example.medivault_capstoneproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class Appointments : AppCompatActivity() {
    private lateinit var container: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        container = findViewById(R.id.Appointmentscontainer)
        searchEditText = findViewById(R.id.AppointmentssearchEditText)
        searchButton = findViewById(R.id.AppointmentssearchButton)
        dbHelper = DatabaseHelper(this)

        // Request SMS permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
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

    @SuppressLint("SetTextI18n")
    private fun displayPatientRecords(patientIdFilter: String? = null) {
        container.removeAllViews()
        val allPatients = dbHelper.getAllPatients()
        val patients = if (patientIdFilter != null) {
            allPatients.filter { it[DatabaseHelper.COLUMN_ID].toString() == patientIdFilter }
        } else {
            allPatients
        }

        val sharedPreferences = getSharedPreferences("com.example.medivault.appointments", Context.MODE_PRIVATE)

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

            val patientId = patient[DatabaseHelper.COLUMN_ID].toString()
            val patientName = patient[DatabaseHelper.COLUMN_NAME].toString()
            val patientMobile = patient[DatabaseHelper.COLUMN_MOBILE].toString()
            val savedDateTime = sharedPreferences.getString(patientId, "No appointment selected")

            val idView = TextView(this).apply {
                text = makeBoldLabel("Candidate Id", patientId)
                textSize = 18f
            }

            val nameView = TextView(this).apply {
                text = makeBoldLabel("Name", patientName)
                textSize = 18f
            }

            val diseaseView = TextView(this).apply {
                text = makeBoldLabel("Disease", patient[DatabaseHelper.COLUMN_DISEASE_DETAILS].toString())
                textSize = 18f
            }

            val appointmentTextView = TextView(this).apply {
                text = savedDateTime
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(10, 5, 10, 5)
            }

            val dateTimeButton = Button(this).apply {
                text = " Select Date & Time "
                setTextColor(Color.WHITE)
                textSize = 12f
                setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(10, 5, 10, 5)
                }

                setOnClickListener {
                    showDateTimePicker(patientId, patientName, patientMobile, appointmentTextView)
                }
            }

            recordLayout.addView(idView)
            recordLayout.addView(nameView)
            recordLayout.addView(diseaseView)
            recordLayout.addView(dateTimeButton)
            recordLayout.addView(appointmentTextView)

            val separator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 3
                )
                setBackgroundColor(Color.BLACK)
            }

            container.addView(recordLayout)
            container.addView(separator)
        }

        if (patients.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No records found."
                textSize = 16f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            }
            container.addView(emptyText)
        }
    }

    private fun showDateTimePicker(
        patientId: String,
        patientName: String,
        patientMobile: String,
        appointmentTextView: TextView
    ) {
        val calendar = Calendar.getInstance()
        val sharedPreferences = getSharedPreferences("com.example.medivault.appointments", Context.MODE_PRIVATE)
        val oldDateTime = sharedPreferences.getString(patientId, null)

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                val newDateTime = "Appointment Scheduled on $selectedDate $selectedTime"

                if (oldDateTime != newDateTime) {
                    with(sharedPreferences.edit()) {
                        putString(patientId, newDateTime)
                        apply()
                    }

                    appointmentTextView.text = newDateTime
                    Toast.makeText(this, "Appointment Set: $newDateTime", Toast.LENGTH_SHORT).show()

                    // Send SMS if new or changed
                    val message = "Hi $patientName, your appointment is scheduled on $selectedDate at $selectedTime."
                    sendSms(patientMobile, message)
                } else {
                    Toast.makeText(this, "No changes in appointment.", Toast.LENGTH_SHORT).show()
                }

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun sendSms(phoneNumber: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use SMS intent for Android 10+
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", message)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to open SMS app", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Direct send for Android 9 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
                try {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                    Toast.makeText(this, "SMS sent!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
