package com.example.subscriberapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringDef
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnMapReadyCallback, MessageListener {
    private lateinit var mqttSubscriber: MqttSubscriber
    private lateinit var messageTextView: TextView
    private lateinit var stopButton: Button
    private lateinit var mMap: GoogleMap
    private lateinit var databaseHelper: DatabaseHelper

    private val locationsList = mutableListOf<LatLng>() // List to store locations
    private val studentPolylines = mutableMapOf<String, Polyline>()
    private lateinit var polyline: Polyline // Polyline object

    private lateinit var studentRecyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //initialize database
        databaseHelper = DatabaseHelper(this)

        //initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize UI components
        messageTextView = findViewById(R.id.tvTitle)
        stopButton = findViewById(R.id.stopButton)

        // Initialize the MQTT Subscriber
        mqttSubscriber = MqttSubscriber(this,this)

        // Set up the disconnect button
        stopButton.setOnClickListener {
            mqttSubscriber.disconnect()
            Log.d("MainActivity", "Disconnected from MQTT broker")
            finish() // Close the activity
        }
    }

    private fun onStudentButtonClicked(studentID: String){
        Log.d("MainActivity","Button clicked for $studentID")

        val intent = Intent(this, StudentMapActivity::class.java)
        intent.putExtra("STUDENT_ID", studentID)
        startActivity(intent)
    }

    private fun updateUI(){
        val studentIds = databaseHelper.getStudentsWithMinMaxSpeed()

        studentRecyclerView = findViewById(R.id.rvStudent)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)

        studentAdapter = StudentAdapter(studentIds){studentId ->
            val intent = Intent(this, StudentMapActivity::class.java)
            intent.putExtra("STUDENT_ID", studentId) // Pass the student ID
            startActivity(intent)
        }
        studentRecyclerView.adapter = studentAdapter

    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.clearDatabase() //clears database
        mqttSubscriber.disconnect() // Ensure to disconnect when the activity is destroyed
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Initialize the polyline
        polyline = mMap.addPolyline(PolylineOptions().color(Color.BLUE).width(5f))

        val location = LatLng(10.4839156398355, -61.262252957901204) //focuses on Trinidad

        // Move the camera to that location and set the zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }

    private fun generateColour(index: Int): Int{
        val hue = (index*360/10)%360
        return Color.HSVToColor(floatArrayOf(hue.toFloat(),1f,1f))
    }

    override fun onMessageReceived(message: String) {
        try {
            val jsonObject = JSONObject(message)
            val studentId = jsonObject.getString("studentId")
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val speed = jsonObject.getInt("speed")
            val timestamp = jsonObject.getLong("timestamp")

            val lat = latitude
            val lon = longitude
            Log.d("onMessageReceived","$lat, $lon")

            runOnUiThread {
                addLocationToStudentPolyline(studentId,lat,lon)

                databaseHelper.insertMessage(message)

                updateUI()
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error processing message: $message", e)
        }
    }

    private fun addLocationToStudentPolyline(studentId: String, lat: Double, lng: Double) {
        val location = LatLng(lat, lng)

        // Check if the polyline for this student already exists
        val polyline = studentPolylines[studentId]
        if (polyline == null) {
            // Create a new polyline for the student
            val color = generateColour(studentPolylines.size) // Generate a unique color
            val polylineOptions = PolylineOptions().color(color).width(5f).add(location)
            val newPolyline = mMap.addPolyline(polylineOptions)

            // Store the new polyline in the map
            studentPolylines[studentId] = newPolyline
        } else {
            // Update the existing polyline
            val points = polyline.points.toMutableList()
            points.add(location)
            polyline.points = points // Update the polyline's points
        }

        //Add a marker for the new location
        mMap.addMarker(MarkerOptions().position(location).title("Student ID: $studentId"))
    }
}