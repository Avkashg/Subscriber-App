package com.example.subscriberapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "location_data.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "locations"
        const val COLUMN_ID = "id"
        const val COLUMN_STUDENT_ID = "studentID"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_SPEED = "speed"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STUDENT_ID + " INTEGER, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_SPEED + " INTEGER, " +
                COLUMN_TIMESTAMP + " INTEGER)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertMessage(message: String) {
        try {
            val jsonObject = JSONObject(message)
            val studentId = jsonObject.getInt("studentId")
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val speed = jsonObject.getInt("speed")
            val timestamp = jsonObject.getLong("timestamp")

            val db = writableDatabase
            val values = ContentValues()
            values.put(COLUMN_STUDENT_ID, studentId)
            values.put(COLUMN_LATITUDE, latitude)
            values.put(COLUMN_LONGITUDE, longitude)
            values.put(COLUMN_SPEED, speed)
            values.put(COLUMN_TIMESTAMP, timestamp)

            db.insert(TABLE_NAME, null, values)
            db.close()

            Log.d("Database","Location added to database: $message")
        }catch (e: Exception){
            Log.e("Database","Error adding location to database")
        }

    }

    fun clearDatabase() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
        Log.d("Database", "All locations cleared from database")
    }

    @SuppressLint("Range")
    fun getStudentsWithMinMaxSpeed(): MutableList<Student> {
        val students = mutableListOf<Student>()
        val db = readableDatabase

        // Query to get distinct student IDs with their minimum and maximum speeds
        val cursor = db.rawQuery(
            "SELECT $COLUMN_STUDENT_ID, MIN($COLUMN_SPEED) AS min_speed, MAX($COLUMN_SPEED) AS max_speed " +
                    "FROM $TABLE_NAME GROUP BY $COLUMN_STUDENT_ID", null
        )

        if (cursor.moveToFirst()) {
            do {
                val studentId = cursor.getString(cursor.getColumnIndex(COLUMN_STUDENT_ID))
                val minSpeed = cursor.getInt(cursor.getColumnIndex("min_speed"))
                val maxSpeed = cursor.getInt(cursor.getColumnIndex("max_speed"))
                students.add(Student(studentId, minSpeed, maxSpeed))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return students
    }

    @SuppressLint("Range")
    fun getLocationsForStudent(studentId: String): List<Pair<Double, Double>> {
        val locations = mutableListOf<Pair<Double, Double>>()
        // Query your database to get the locations for the given student ID
        val cursor = readableDatabase.rawQuery("SELECT latitude, longitude FROM locations WHERE studentId = ?", arrayOf(studentId))

        if (cursor.moveToFirst()) {
            do {
                val latitude = cursor.getDouble(cursor.getColumnIndex("latitude"))
                val longitude = cursor.getDouble(cursor.getColumnIndex("longitude"))
                locations.add(Pair(latitude, longitude))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return locations
    }

    @SuppressLint("Range")
    fun getSpeedInfo(studentId: String): Student? {
        val db = this.readableDatabase
        var studentInfo: Student? = null

        // Updated query to get min, max, avg speed and min, max timestamp
        val query = """
        SELECT 
            MIN($COLUMN_SPEED) AS minSpeed, 
            MAX($COLUMN_SPEED) AS maxSpeed, 
            AVG($COLUMN_SPEED) AS avgSpeed,
            MIN($COLUMN_TIMESTAMP) AS startDate,
            MAX($COLUMN_TIMESTAMP) AS endDate
        FROM $TABLE_NAME 
        WHERE $COLUMN_STUDENT_ID = ?
    """
        val cursor = db.rawQuery(query, arrayOf(studentId))

        if (cursor.moveToFirst()) {
            val minSpeed = cursor.getInt(cursor.getColumnIndex("minSpeed"))
            val maxSpeed = cursor.getInt(cursor.getColumnIndex("maxSpeed"))
            val avgSpeed = cursor.getInt(cursor.getColumnIndex("avgSpeed"))
            val startDateMillis = cursor.getLong(cursor.getColumnIndex("startDate"))
            val endDateMillis = cursor.getLong(cursor.getColumnIndex("endDate"))

            // Format the start and end dates
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = dateFormat.format(startDateMillis)
            val endDate = dateFormat.format(endDateMillis)

            studentInfo = Student(studentId,minSpeed,maxSpeed,avgSpeed,startDate,endDate)
        }

        cursor.close()
        return studentInfo
    }
}