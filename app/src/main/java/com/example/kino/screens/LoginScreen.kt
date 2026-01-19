package com.example.kino.screens

import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kino.dialogs.ForgotPasswordDialog
import com.example.kino.viewmodel.CinemaViewModel
import kotlinx.coroutines.launch
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginScreen(
    viewModel: CinemaViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterNavigate: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isError by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showResendDialog by remember { mutableStateOf(false) }


    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun performLogin() {
        focusManager.clearFocus()

        var isValid = true
        var errorText = ""
        if (email.isBlank()) {
            isValid = false
            errorText = "Введите Email"
        } else if (!isEmailValid(email)) {
            isValid = false
            errorText = "Неверный формат Email"
        } else if (password.length < 6) {
            isValid = false
            errorText = "Пароль слишком короткий"
        }

        if (!isValid) {
            isError = true
            errorMessage = errorText
            scope.launch {
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 400
                        0f at 0
                        10f at 50
                        -10f at 100
                        10f at 150
                        0f at 400
                    }
                )
            }
        } else {
            viewModel.loginUser(email, password) { resultError ->
                if (resultError == null) {
                    onLoginSuccess()
                } else {
                    isError = true
                    errorMessage = resultError
                    Toast.makeText(context, resultError, Toast.LENGTH_LONG).show()
                    scope.launch {
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                0f at 0
                                10f at 50
                                -10f at 100
                                10f at 150
                                0f at 400
                            }
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .offset(x = shakeOffset.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CINEMA",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Text(
                text = "Вход в систему",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isError = false
                    errorMessage = null
                },
                label = { Text("Email") },
                isError = isError,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isError = false
                    errorMessage = null
                },
                label = { Text("Пароль") },
                isError = isError,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { showResendDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Нет письма?",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                TextButton(
                    onClick = { showResetDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Забыли пароль?", color = MaterialTheme.colorScheme.primary)
                }
            }
            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { performLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(6.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "ВОЙТИ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Нет аккаунта? ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(
                    text = "Регистрация",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRegisterNavigate() }
                )
            }
        }
        if (showResetDialog) {
            ForgotPasswordDialog(
                isLoading = isLoading,
                onDismiss = { showResetDialog = false },
                onConfirm = { emailForReset ->
                    viewModel.resetPassword(
                        email = emailForReset,
                        onSuccess = {
                            showResetDialog = false
                            Toast.makeText(context, "Ссылка отправлена", Toast.LENGTH_LONG).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
        if (showResendDialog) {
            ForgotPasswordDialog(
                isLoading=isLoading,
                onDismiss = { showResendDialog = false },
                onConfirm = { emailResend ->
                    if (password.isNotEmpty() && emailResend == email) {
                        viewModel.resendVerificationEmail(email, password) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            showResendDialog = false
                        }
                    } else {
                        Toast.makeText(context, "Введите Email и Пароль в поля ввода, затем нажмите эту кнопку снова", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}