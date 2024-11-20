package com.example.subscriberapp

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.sql.Date
import kotlin.math.max

class StudentMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var studentId: String
    private lateinit var startDate: TextView
    private lateinit var endDate: TextView
    private lateinit var minSpeed: TextView
    private lateinit var maxSpeed: TextView
    private lateinit var avgSpeed: TextView
    private lateinit var title: TextView
    private lateinit var back: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_map)

        startDate = findViewById(R.id.tvStartDate)
        endDate = findViewById(R.id.tvEndDate)
        minSpeed = findViewById(R.id.tvMinSpeed)
        maxSpeed = findViewById(R.id.tvMaxSpeed)
        avgSpeed = findViewById(R.id.tvAvgSpeed)
        title = findViewById(R.id.tvTitle)
        back = findViewById(R.id.btnBack)

        // Get the student ID from the intent
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        title.text = "Summary of $studentId"

        // Initialize the database helper
        databaseHelper = DatabaseHelper(this)

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        back.setOnClickListener{
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Load the student's locations and draw the polyline
        loadStudentLocations()
    }

    private fun loadStudentLocations() {
        // Retrieve the locations for the specific student ID
        val locations = databaseHelper.getLocationsForStudent(studentId)

        // Create a list to hold the LatLng points
        val latLngList = mutableListOf<LatLng>()

        // Add markers and polyline points
        for (location in locations) {
            val latLng = LatLng(location.first, location.second)
            mMap.addMarker(MarkerOptions().position(latLng).title("Location for $studentId"))
            latLngList.add(latLng)
        }

        // Draw polyline for the student's path
        if (latLngList.isNotEmpty()) {
            val polylineOptions = PolylineOptions().color(Color.BLUE).width(5f).addAll(latLngList)
            mMap.addPolyline(polylineOptions)

            // Move the camera to the first location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList[0], 10f))
        }

        getStudentInfo(studentId)
    }

    private fun getStudentInfo(studentID: String){
        val student = databaseHelper.getSpeedInfo(studentId)

        if (student != null) {
            minSpeed.text = "Min Speed: ${student.minSpeed} km/h"
            maxSpeed.text = "Max Speed: ${student.maxSpeed} km/h"
            minSpeed.text = "Avg Speed: ${student.avgSpeed} km/h"
            startDate.text = "Start Date: ${student.startDate}"
            endDate.text = "End Date: ${student.endDate}"
        }

    }
}