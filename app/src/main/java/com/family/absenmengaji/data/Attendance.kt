package com.family.absenmengaji.data

data class Attendance(
    val id: String = "",
    val date: String = "",
    val memberId: String = "",
    val present: Boolean = false,
    val photoPath: String? = null
)
