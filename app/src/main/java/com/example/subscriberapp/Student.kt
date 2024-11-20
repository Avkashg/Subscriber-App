package com.example.subscriberapp

data class Student(
    val id: String,
    val minSpeed: Int? = null,
    val maxSpeed: Int? = null,
    val avgSpeed: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null
)