package com.family.absenmengaji.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.util.*

class Repository(private val context: Context) {
    private val gson = Gson()
    private val membersFile = File(context.filesDir, "members.json")
    private val attendanceFile = File(context.filesDir, "attendance.json")

    fun loadMembers(): List<Member> {
        if (!membersFile.exists()) return emptyList()
        val json = membersFile.readText()
        val type = object : TypeToken<List<Member>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveMembers(list: List<Member>) {
        membersFile.writeText(gson.toJson(list))
    }

    fun loadAttendances(): List<Attendance> {
        if (!attendanceFile.exists()) return emptyList()
        val json = attendanceFile.readText()
        val type = object : TypeToken<List<Attendance>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveAttendances(list: List<Attendance>) {
        attendanceFile.writeText(gson.toJson(list))
    }

    fun loadAttendanceForDate(date: String): List<Attendance> {
        return loadAttendances().filter { it.date == date }
    }

    fun exportCsvForDate(date: String): String {
        val rows = loadAttendances().filter { it.date == date }
        val header = "memberId,date,present,photoPath\n"
        val body = rows.joinToString(separator = "\n") {
            "${it.memberId},${it.date},${it.present},${it.photoPath ?: ""}"
        }
        return header + body
    }

    fun savePhoto(bitmapBytes: ByteArray): String {
        val filename = "photo_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { it.write(bitmapBytes) }
        return file.absolutePath
    }

    fun loadPhoto(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }
}
