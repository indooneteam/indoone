package com.indoone.menu.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { repository.readProfile() }
            .onSuccess { profile ->
                name = profile["name"]?.toString().orEmpty()
                nickname = profile["nickname"]?.toString().orEmpty()
                profession = profile["profession"]?.toString().orEmpty()
            }
            .onFailure { status = it.message ?: "Could not load profile." }
    }

    val displayName = name.trim().ifBlank { nickname.trim() }.ifBlank { "Indoone User" }
    val initials = displayName
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "I" }

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Profile",
                    color = Color(0xFF211B29),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.size(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF5F9FF),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = Color(0xFFE3EEFF),
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                initials,
                                color = Color(0xFF2168D6),
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Text(
                        displayName,
                        modifier = Modifier.padding(top = 10.dp),
                        color = Color(0xFF201B28),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Your Indoone profile",
                        modifier = Modifier.padding(top = 3.dp),
                        color = Color(0xFF77717F),
                        fontSize = 12.sp,
                    )
                }
            }

            Text(
                "Personal details",
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                color = Color(0xFF201B28),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; status = null },
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.size(10.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it; status = null },
                label = { Text("Nickname") },
                leadingIcon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.size(10.dp))

            OutlinedTextField(
                value = profession,
                onValueChange = { profession = it; status = null },
                label = { Text("Profession") },
                leadingIcon = { Icon(Icons.Outlined.WorkOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                shape = RoundedCornerShape(15.dp),
                color = Color(0xFFF9F7FC),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = Color(0xFF6B3FD1),
                    )
                    Text(
                        "These profile fields stay under your control.",
                        modifier = Modifier.padding(start = 10.dp),
                        color = Color(0xFF6E6777),
                        fontSize = 12.sp,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                shape = RoundedCornerShape(15.dp),
                color = Color(0xFFEDF5FF),
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            status = "Saving…"
                            runCatching {
                                repository.updateProfileFields(name, nickname, profession)
                            }.onSuccess {
                                status = "Profile saved."
                            }.onFailure {
                                status = it.message ?: "Could not save profile."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save profile",
                        color = Color(0xFF2168D6),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            status?.let {
                Text(
                    it,
                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                    color = if (it == "Profile saved.") Color(0xFF287A46) else Color(0xFFD93025),
                    fontSize = 12.sp,
                )
            }
        }
    }
}
