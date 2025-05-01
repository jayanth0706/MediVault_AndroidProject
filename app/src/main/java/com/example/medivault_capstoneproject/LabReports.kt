package com.example.medivault_capstoneproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LabReports : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    private var currentPatientIdForUpload: String? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri: Uri? = result.data?.data
            currentPatientIdForUpload?.let { id ->
                uri?.let {
                    val prefs = getSharedPreferences("com.example.medivault.labfiles", Context.MODE_PRIVATE)
                    prefs.edit().putString(id, it.toString()).apply()
                    Toast.makeText(this, "File saved for $id", Toast.LENGTH_SHORT).show()
                    displayPatientReports()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab_reports)

        container = findViewById(R.id.LabReportscontainer)
        searchEditText = findViewById(R.id.LabReportsEditText)
        searchButton = findViewById(R.id.LabReportssearchButton)
        dbHelper = DatabaseHelper(this)

        searchButton.setOnClickListener {
            val id = searchEditText.text.toString().trim()
            if (id.isNotEmpty()) {
                displayPatientReports(id)
            } else {
                displayPatientReports()
            }
        }

        displayPatientReports()
    }

    private fun displayPatientReports() {
        val allPatients = dbHelper.getAllPatients()
        displayPatientViews(allPatients)
    }

    private fun displayPatientReports(searchId: String) {
        val filteredPatients = dbHelper.getAllPatients().filter {
            it[DatabaseHelper.COLUMN_ID].toString().equals(searchId, ignoreCase = true)
        }

        if (filteredPatients.isEmpty()) {
            container.removeAllViews()
            val notFoundText = TextView(this).apply {
                text = "No patient found with ID: $searchId"
                textSize = 16f
                setTextColor(Color.RED)
                setPadding(20, 20, 20, 20)
            }
            container.addView(notFoundText)
        } else {
            displayPatientViews(filteredPatients)
        }
    }

    private fun displayPatientViews(patients: List<Map<String, Any>>) {
        container.removeAllViews()

        val prescriptionsPrefs = getSharedPreferences("com.example.medivault.prescriptions", Context.MODE_PRIVATE)
        val labReportsPrefs = getSharedPreferences("com.example.medivault.labreports", Context.MODE_PRIVATE)
        val labFilesPrefs = getSharedPreferences("com.example.medivault.labfiles", Context.MODE_PRIVATE)

        for (patient in patients) {
            val patientId = patient[DatabaseHelper.COLUMN_ID].toString()
            val name = patient[DatabaseHelper.COLUMN_NAME].toString()
            val disease = patient[DatabaseHelper.COLUMN_DISEASE_DETAILS].toString()

            val recordLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#EEEEEE"))
            }

            fun bold(label: String, value: String): SpannableStringBuilder {
                return SpannableStringBuilder("$label: $value").apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            val idTextView = TextView(this).apply {
                text = bold("Candidate Id", patientId)
                textSize = 16f
            }

            val nameTextView = TextView(this).apply {
                text = bold("Name", name)
                textSize = 16f
            }

            val diseaseTextView = TextView(this).apply {
                text = bold("Disease", disease)
                textSize = 16f
            }

            val prescription = prescriptionsPrefs.getString(patientId, "No prescription saved")
            val prescriptionTextView = TextView(this).apply {
                text = bold("Prescription", prescription ?: "")
                setPadding(10, 8, 10, 8)
                textSize = 15f
                setTextColor(Color.DKGRAY)
            }

            val labReportInput = EditText(this).apply {
                hint = "Enter lab report notes"
                setText(labReportsPrefs.getString(patientId, ""))
                setBackgroundResource(android.R.drawable.edit_text)
                maxLines = 5
                setPadding(8, 8, 8, 8)
                setHorizontallyScrolling(false)
            }

            val savedFileUriString = labFilesPrefs.getString(patientId, null)
            val savedFileUri = savedFileUriString?.let { Uri.parse(it) }

            val fileView: View = if (savedFileUri != null && savedFileUri.toString().contains("image")) {
                ImageView(this).apply {
                    setImageURI(savedFileUri)
                    layoutParams = LinearLayout.LayoutParams(300, 300)
                    setPadding(8, 8, 8, 8)
                    setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(savedFileUri, "image/*")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        startActivity(intent)
                    }
                }
            } else {
                TextView(this).apply {
                    text = if (savedFileUri != null)
                        "Uploaded File: ${savedFileUri.lastPathSegment}"
                    else
                        "No file uploaded"
                    setTextColor(Color.DKGRAY)
                    textSize = 14f
                    setPadding(8, 4, 8, 4)
                    setOnClickListener {
                        savedFileUri?.let {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(it, "*/*")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            startActivity(intent)
                        }
                    }
                }
            }

            val uploadDocButton = Button(this).apply {
                text = "Upload PDF/Image"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    currentPatientIdForUpload = patientId
                    openFilePicker()
                }
            }

            val deleteDocButton = Button(this).apply {
                text = "Delete Uploaded File"
                setBackgroundColor(Color.parseColor("#F44336"))
                setTextColor(Color.WHITE)
                visibility = if (savedFileUri != null) View.VISIBLE else View.GONE
                setOnClickListener {
                    labFilesPrefs.edit().remove(patientId).apply()
                    Toast.makeText(this@LabReports, "Uploaded file deleted!", Toast.LENGTH_SHORT).show()
                    displayPatientReports()
                }
            }

            val saveDocButton = Button(this).apply {
                text = "Save Report Notes"
                setBackgroundColor(Color.parseColor("#2196F3"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    val reportText = labReportInput.text.toString().trim()
                    if (reportText.isNotEmpty()) {
                        // Save the report notes
                        labReportsPrefs.edit().putString(patientId, reportText).apply()
                        Toast.makeText(this@LabReports, "Report notes saved!", Toast.LENGTH_SHORT).show()

                        // Prepare the message
                        val message = "Lab Report Notes: $reportText\n"
                        val savedFileUriString = labFilesPrefs.getString(patientId, null)
                        val fileMessage = if (!savedFileUriString.isNullOrEmpty()) {
                            "Uploaded File: ${Uri.parse(savedFileUriString).lastPathSegment}"
                        } else {
                            "No file uploaded"
                        }

                        val finalMessage = "$message$fileMessage"

                        // Replace with the phone number of the user (this could be passed to this activity or fetched from the database)
                        val userPhoneNumber = "USER_PHONE_NUMBER"

                        // Send SMS
                        sendSms(userPhoneNumber, finalMessage)
                    } else {
                        Toast.makeText(this@LabReports, "Enter notes!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    4
                ).apply {
                    topMargin = 20
                    bottomMargin = 20
                }
                setBackgroundColor(Color.parseColor("#BDBDBD"))
            }

            recordLayout.apply {
                addView(idTextView)
                addView(nameTextView)
                addView(diseaseTextView)
                addView(prescriptionTextView)
                addView(labReportInput)
                addView(fileView)
                addView(uploadDocButton)
                addView(deleteDocButton)
                addView(saveDocButton)
                addView(divider)
            }

            container.addView(recordLayout)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
        }
        filePickerLauncher.launch(intent)
    }

    // Function to send SMS
    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
