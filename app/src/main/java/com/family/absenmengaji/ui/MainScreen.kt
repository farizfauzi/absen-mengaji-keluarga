package com.family.absenmengaji

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun MainScreen(viewModel: AppViewModel) {
    val members by viewModel.members.collectAsState()
    val attendance by viewModel.todayAttendance.collectAsState()
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }
    var showAdminPin by remember { mutableStateOf(false) }
    var showHistoryFor: String? by remember { mutableStateOf(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        // handled per member via lambda below
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Absen Mengaji") })
    }, floatingActionButton = {
        Column {
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(onClick = { viewModel.exportCsv(context) }) { Text("CSV") }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(onClick = { showAdminPin = true }) { Text("Admin") }
        }
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Hari ini: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(members) { member ->
                    val att = attendance[member.id]
                    val present = att?.present ?: false
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(member.name, modifier = Modifier.weight(1f))
                        Button(onClick = {
                            // take photo then mark present
                            val file = File(context.filesDir, "photo_${UUID.randomUUID()}.jpg")
                            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                            // launch camera
                            val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok: Boolean ->
                                if (ok) {
                                    // save path and mark present
                                    viewModel.setPresentWithPhoto(member.id, true, file.absolutePath)
                                }
                            }
                            // can't call launcher inside onClick directly; we use a hack: startActivityForResult
                            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                            (context as? Activity)?.startActivityForResult(intent, 100 + member.id.hashCode() % 100)

                        }) {
                            Text("Foto & Hadir")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Checkbox(checked = present, onCheckedChange = { checked -> viewModel.setPresentWithPhoto(member.id, checked, att?.photoPath) })
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { showHistoryFor = member.id }) { Text("Riwayat") }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddMemberDialog(onAdd = { name -> viewModel.addMember(name); showAdd = false }, onDismiss = { showAdd = false })
    }

    if (showAdminPin) {
        AdminPinDialog(onDismiss = { showAdminPin = false }, onCorrect = { showAdminPin = false; /* open admin screen */ })
    }

    if (showHistoryFor != null) {
        HistoryScreen(memberId = showHistoryFor!!, viewModel = viewModel, onClose = { showHistoryFor = null })
    }
}

@Composable
fun AdminPinDialog(onDismiss: () -> Unit, onCorrect: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Admin PIN") }, text = {
        OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("Masukkan PIN") })
    }, confirmButton = {
        TextButton(onClick = {
            // check pin via ViewModel? for simplicity, call static check by creating ViewModel is not ideal.
            // In real app, pass viewModel as param. We'll keep this as placeholder: accept 1234 only.
            if (pin == "1234") onCorrect()
        }) { Text("Masuk") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Batal") }
    })
}
