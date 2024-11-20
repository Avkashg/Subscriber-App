package com.example.subscriberapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val students: MutableList<Student>,
    private val onButtonClick: (String) -> Unit // Lambda function for button click
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentIdTextView: TextView = itemView.findViewById(R.id.studentIdTextView)
        val minSpeedTextView: TextView = itemView.findViewById(R.id.tvMinSpeed)
        val maxSpeedTextView: TextView = itemView.findViewById(R.id.tvMaxSpeed)
        val actionButton: Button = itemView.findViewById(R.id.btnViewMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        val minSpeed = student.minSpeed.toString()
        val maxSpeed = student.maxSpeed.toString()

        holder.studentIdTextView.text = student.id
        holder.minSpeedTextView.text = "Min Speed: $minSpeed km/h"
        holder.maxSpeedTextView.text = "Max Speed: $maxSpeed km/h"
        holder.actionButton.setOnClickListener {
            onButtonClick(student.id) // Call the lambda function with the student ID
        }
    }

    override fun getItemCount(): Int = students.size
}