package com.indoone.menu.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.data.UserDataRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileMenuScreen(onBack: () -> Unit) {
    val repository = remember { UserDataRepository() }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { repository.readProfile() }
            .onSuccess { profile ->
                name = profile["name"]?.toString().orEmpty()
                nickname = profile["nickname"]?.toString().orEmpty()
                profession = profile["profession"]?.toString().orEmpty()
                email = profile["email"]?.toString().orEmpty()
                mobile = profile["mobile"]?.toString().orEmpty()
            }
            .onFailure { status = it.message ?: "Could not load profile." }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("Profile", color = Color(0xFF5E2DD2), fontSize = 24.sp)
        Text("You control these personal details. AI memory does not silently replace your name or account identity.", color = Color(0xFF77707F), fontSize = 12.sp)
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nickname, { nickname = it }, label = { Text("Nickname") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(profession, { profession = it }, label = { Text("Profession") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, {}, label = { Text("Email") }, readOnly = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mobile, {}, label = { Text("Mobile") }, readOnly = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            scope.launch {
                status = "Saving…"
                runCatching { repository.updateProfileFields(name, nickname, profession) }
                    .onSuccess { status = "Profile saved." }
                    .onFailure { status = it.message ?: "Could not save profile." }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
        status?.let { Text(it, color = if (it == "Profile saved.") Color(0xFF6330DB) else Color(0xFFD93025), fontSize = 12.sp) }
    }
}
