package com.parisara.cycle.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.parisara.cycle.data.model.UserProfile
import com.parisara.cycle.di.AppContainer
import com.parisara.cycle.ui.components.LoadingOverlay
import com.parisara.cycle.ui.components.ParisaraTextField
import com.parisara.cycle.ui.theme.*
import com.parisara.cycle.ui.viewmodel.AuthViewModel
import com.parisara.cycle.ui.viewmodel.ParisaraViewModelFactory

@Composable
fun LoginScreen(
    container: AppContainer,
    onNavigateRegister: () -> Unit,
    onNavigateForgot: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val vm: AuthViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    AuthScaffold(title = "Welcome Back", subtitle = "Sign in to continue your green journey") {
        ParisaraTextField(email, { email = it }, "Email", leadingIcon = Icons.Default.Email)
        Spacer(Modifier.height(12.dp))
        ParisaraTextField(password, { password = it }, "Password", leadingIcon = Icons.Default.Lock, isPassword = true)
        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onNavigateForgot, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.login(email, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = email.isNotBlank() && password.length >= 6
        ) { Text("Sign In") }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("New user? ", color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = onNavigateRegister) { 
                Text("Create Account", color = MaterialTheme.colorScheme.primary) 
            }
        }
    }
    if (state.isLoading) LoadingOverlay("Signing in...")
}

@Composable
fun RegisterScreen(container: AppContainer, onBack: () -> Unit, onSuccess: () -> Unit) {
    val vm: AuthViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val state by vm.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    LaunchedEffect(state.isRegistrationSuccess) { 
        if (state.isRegistrationSuccess) onSuccess() 
    }

    AuthScaffold(title = "Join Parisara", subtitle = "Create your eco-commuter profile") {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(
                Icons.Default.ArrowBack, 
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri, 
                        contentDescription = "Profile", 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            TextButton(onClick = { imagePicker.launch("image/*") }) {
                Text(
                    if (imageUri != null) "Change Photo" else "Add Profile Photo (Optional)", 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        ParisaraTextField(name, { name = it }, "Full Name", leadingIcon = Icons.Default.Person)
        Spacer(Modifier.height(12.dp))
        ParisaraTextField(email, { email = it }, "Email", leadingIcon = Icons.Default.Email)
        Spacer(Modifier.height(12.dp))
        ParisaraTextField(phone, { phone = it }, "Phone Number", leadingIcon = Icons.Default.Phone)
        Spacer(Modifier.height(12.dp))
        ParisaraTextField(city, { city = it }, "City", leadingIcon = Icons.Default.LocationCity)
        Spacer(Modifier.height(12.dp))
        ParisaraTextField(password, { password = it }, "Password", leadingIcon = Icons.Default.Lock, isPassword = true)
        Spacer(Modifier.height(12.dp))
        ParisaraTextField(confirm, { confirm = it }, "Confirm Password", leadingIcon = Icons.Default.Lock, isPassword = true)
        
        state.error?.let { 
            Text(
                it, 
                color = MaterialTheme.colorScheme.error, 
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            ) 
        }
        
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (password == confirm) {
                    vm.register(
                        email = email, 
                        password = password, 
                        profile = UserProfile(name = name, phone = phone, city = city), 
                        imageUri = imageUri,
                        onSuccess = onSuccess
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && password == confirm
        ) { Text("Register") }
    }
    if (state.isLoading) LoadingOverlay("Creating account...")
}

@Composable
fun ForgotPasswordScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: AuthViewModel = viewModel(factory = ParisaraViewModelFactory(container))
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    AuthScaffold(title = "Reset Password", subtitle = "We'll send a reset link to your email") {
        IconButton(onClick = onBack) { 
            Icon(
                Icons.Default.ArrowBack, 
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            ) 
        }
        ParisaraTextField(email, { email = it }, "Email", leadingIcon = Icons.Default.Email)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.resetPassword(email) { sent = true } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Text("Send Reset Link") }
        if (sent) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Reset link sent! Check your inbox.", 
                color = MaterialTheme.colorScheme.primary, 
                textAlign = TextAlign.Center
            )
        }
        state.error?.let { 
            Text(
                it, 
                color = MaterialTheme.colorScheme.error, 
                modifier = Modifier.padding(top = 8.dp)
            ) 
        }
    }
    if (state.isLoading) LoadingOverlay()
}

@Composable
private fun AuthScaffold(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            title, 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            subtitle, 
            style = MaterialTheme.typography.bodyMedium, 
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        content()
    }
}
