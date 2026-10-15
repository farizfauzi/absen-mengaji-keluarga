package com.family.absenmengaji

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(memberId: String, viewModel: AppViewModel, onClose: () -> Unit) {
    val member = viewModel.members.value.find { it.id == memberId }
    val list = viewModel.getAttendanceForMember(memberId)

    AlertDialog(onDismissRequest = onClose, title = { Text(member?.name ?: "Riwayat") }, text = {
        LazyColumn(modifier = Modifier.height(300.dp)) {
            items(list) { att ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(att.date, style = MaterialTheme.typography.subtitle1)
                        Text(if (att.present) "Hadir" else "Tidak hadir")
                    }
                    if (!att.photoPath.isNullOrEmpty()) {
                        val painter = rememberAsyncImagePainter(att.photoPath)
                        Image(painter = painter, contentDescription = "Foto", modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }, confirmButton = {
        TextButton(onClick = onClose) { Text("Tutup") }
    })
}
