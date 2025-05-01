package com.example.medivault_capstoneproject

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PatientDB"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "patients"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_BLOOD_GROUP = "blood_group"
        const val COLUMN_MOBILE = "mobile"
        const val COLUMN_DISEASE_DETAILS = "disease_details"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_GENDER TEXT,
                $COLUMN_BLOOD_GROUP TEXT,
                $COLUMN_MOBILE TEXT,
                $COLUMN_DISEASE_DETAILS TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun getAllPatients(): List<Map<String, String>> {
        val patients = mutableListOf<Map<String, String>>()
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val patient = mapOf(
                        COLUMN_ID to it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                        COLUMN_NAME to it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                        COLUMN_GENDER to it.getString(it.getColumnIndexOrThrow(COLUMN_GENDER)),
                        COLUMN_BLOOD_GROUP to it.getString(it.getColumnIndexOrThrow(COLUMN_BLOOD_GROUP)),
                        COLUMN_MOBILE to it.getString(it.getColumnIndexOrThrow(COLUMN_MOBILE)),
                        COLUMN_DISEASE_DETAILS to it.getString(it.getColumnIndexOrThrow(COLUMN_DISEASE_DETAILS)) // Added line
                    )
                    patients.add(patient)
                } while (it.moveToNext())
            }
        }
        db.close()
        return patients
    }

    fun deletePatient(id: String): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id))
        db.close()
        return result
    }

    fun insertPatient(
        id: String,
        name: String,
        gender: String,
        bloodGroup: String,
        mobile: String,
        diseaseDetails: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_NAME, name)
            put(COLUMN_GENDER, gender)
            put(COLUMN_BLOOD_GROUP, bloodGroup)
            put(COLUMN_MOBILE, mobile)
            put(COLUMN_DISEASE_DETAILS, diseaseDetails)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

}