package com.rehab.platform.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rehab.platform.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User?,
    viewModel: ProfileViewModel? = null,
    onSave: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var hospital by remember { mutableStateOf(user?.hospital ?: "") }
    
    val uiState = viewModel?.uiState?.collectAsState()
    
    // Show success and navigate back
    LaunchedEffect(uiState?.value?.updateSuccess) {
        if (uiState?.value?.updateSuccess == true) {
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            if (viewModel != null) {
                                viewModel.updateProfile(name, email, phone, hospital)
                            } else {
                                onSave(name, email, phone, hospital)
                            }
                        },
                        enabled = uiState?.value?.isLoading != true
                    ) {
                        if (uiState?.value?.isLoading == true) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = hospital,
                onValueChange = { hospital = it },
                label = { Text("Hospital") },
                leadingIcon = { Icon(Icons.Default.LocalHospital, null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
