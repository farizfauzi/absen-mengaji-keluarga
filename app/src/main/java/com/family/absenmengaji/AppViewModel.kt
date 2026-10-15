package com.family.absenmengaji

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.family.absenmengaji.data.Attendance
import com.family.absenmengaji.data.Member
import com.family.absenmengaji.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _todayAttendance = MutableStateFlow<Map<String, Attendance>>(emptyMap())
    val todayAttendance: StateFlow<Map<String, Attendance>> = _todayAttendance.asStateFlow()

    // simple PIN stored in files for demo (default 1234)
    private val pinFile = File(app.filesDir, "admin_pin.txt")

    init {
        val savedMembers = repo.loadMembers()
        _members.value = savedMembers

        val today = todayDate()
        val map = mutableMapOf<String, Attendance>()
        val todays = repo.loadAttendanceForDate(today)
        savedMembers.forEach { member ->
            val att = todays.find { it.memberId == member.id } ?: Attendance(id = UUID.randomUUID().toString(), date = today, memberId = member.id, present = false)
            map[member.id] = att
        }
        _todayAttendance.value = map

        if (!pinFile.exists()) pinFile.writeText("1234")
    }

    fun addMember(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val member = Member(id = UUID.randomUUID().toString(), name = trimmed)
        val updated = _members.value + member
        _members.value = updated
        repo.saveMembers(updated)

        val current = _todayAttendance.value.toMutableMap()
        current[member.id] = Attendance(id = UUID.randomUUID().toString(), date = todayDate(), memberId = member.id, present = false)
        _todayAttendance.value = current
    }

    fun setPresentWithPhoto(memberId: String, present: Boolean, photoPath: String?) {
        val current = _todayAttendance.value.toMutableMap()
        val att = current[memberId]?.copy(present = present, photoPath = photoPath) ?: Attendance(id = UUID.randomUUID().toString(), date = todayDate(), memberId = memberId, present = present, photoPath = photoPath)
        current[memberId] = att
        _todayAttendance.value = current
        saveTodayAttendance()
    }

    fun saveTodayAttendance() {
        val today = todayDate()
        val current = _todayAttendance.value
        val existing = repo.loadAttendances().filter { it.date != today }

        val records = current.values.map { it.copy(id = it.id.ifEmpty { UUID.randomUUID().toString() }) }

        repo.saveAttendances(existing + records)
    }

    fun exportCsv(context: Context) {
        saveTodayAttendance()
        val csv = repo.exportCsvForDate(todayDate())
        val file = File(context.cacheDir, "absen-${todayDate()}.csv")
        file.writeText(csv)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Bagikan rekap absen"))
    }

    fun getAttendanceForMember(memberId: String): List<Attendance> {
        return repo.loadAttendances().filter { it.memberId == memberId }.sortedByDescending { it.date }
    }

    fun saveAdminPin(pin: String) {
        pinFile.writeText(pin)
    }

    fun checkAdminPin(pin: String): Boolean {
        if (!pinFile.exists()) return false
        return pinFile.readText().trim() == pin.trim()
    }

    fun todayDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(app) as T
        }
    }
}
