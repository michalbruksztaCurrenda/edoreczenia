package com.edoreczenia.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// Kolory dopasowane do makiety (Material 3 seed: #001e40 / #003366)
private val PrimaryDark = Color(0xFF001E40)
private val PrimaryContainer = Color(0xFF003366)
private val SecondaryContainer = Color(0xFFfd8b00)
private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val OutlineVariant = Color(0xFFC3C6D1)
private val OnSurfaceVariant = Color(0xFF43474F)
private val ErrorColor = Color(0xFFBA1A1A)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.factory()),
    onNavigateToMain: () -> Unit = {},
    onNavigateToRegistration: () -> Unit = {},
    onNavigateToVerifyEmail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToMain -> onNavigateToMain()
                is LoginEffect.NavigateToRegistration -> onNavigateToRegistration()
                is LoginEffect.NavigateToVerifyEmail -> onNavigateToVerifyEmail(effect.username)
                is LoginEffect.ShowMessage -> { /* obsługiwane przez formError */ }
            }
        }
    }

    Scaffold(
        topBar = { LoginTopBar() },
        bottomBar = { LoginBottomBar() },
        containerColor = Color(0xFFF8F9FF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginCard(
                uiState = uiState,
                onUsernameChanged = viewModel::onUsernameChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onLoginClicked = viewModel::onLoginClicked,
                onRegisterClicked = viewModel::onRegisterClicked,
                focusManager = focusManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "e-Komornik",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 0.1.sp
            )
        },
        actions = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Pomoc",
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0F172A) // slate-900 jak w makiecie
        )
    )
}

@Composable
private fun LoginBottomBar() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Pomarańczowy pasek na dole — jak w makiecie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(SecondaryContainer)
        )
    }
}

@Composable
private fun LoginCard(
    uiState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onRegisterClicked: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tytuł i podtytuł
            Text(
                text = "Logowanie",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Zaloguj się do systemu...",
                fontSize = 16.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Pole: Nazwa użytkownika
            UsernameField(
                value = uiState.usernameInput,
                error = uiState.usernameError,
                enabled = !uiState.isLoading,
                onValueChange = onUsernameChanged,
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(Modifier.height(16.dp))

            // Pole: Hasło
            PasswordField(
                value = uiState.passwordInput,
                error = uiState.passwordError,
                enabled = !uiState.isLoading,
                onValueChange = onPasswordChanged,
                onDone = onLoginClicked
            )

            // Link "Nie pamiętasz hasła?" — wizualny (bez funkcji na tym etapie)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /* TODO: reset hasła — poza zakresem US1 */ }) {
                    Text(
                        text = "Nie pamiętasz hasła?",
                        fontSize = 14.sp,
                        color = PrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Komunikat błędu formularza
            if (uiState.formError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.formError,
                    color = ErrorColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Przycisk "ZALOGUJ"
            Button(
                onClick = onLoginClicked,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryContainer,
                    contentColor = Color.White,
                    disabledContainerColor = SecondaryContainer.copy(alpha = 0.6f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "ZALOGUJ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Przycisk "ZAREJESTRUJ" — outlined
            OutlinedButton(
                onClick = onRegisterClicked,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryDark),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryDark
                )
            ) {
                Text(
                    text = "ZAREJESTRUJ",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            // Separator + SSL info
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = OutlineVariant)
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Bezpieczne połączenie szyfrowane SSL",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }
        }
    }

    // Stopka z logotypami instytucji (wizualny placeholder — makieta)
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ministerstwo\nSprawiedliwości",
            fontSize = 10.sp,
            color = OnSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Krajowa Rada\nKomornicza",
            fontSize = 10.sp,
            color = OnSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UsernameField(
    value: String,
    error: String?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Nazwa użytkownika",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0B1C30),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = { Text("Wprowadź login", color = OnSurfaceVariant) },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (error != null) ErrorColor else OnSurfaceVariant
                )
            },
            isError = error != null,
            supportingText = {
                if (error != null) Text(error, color = ErrorColor)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDark,
                unfocusedBorderColor = OutlineVariant,
                errorBorderColor = ErrorColor
            ),
            singleLine = true
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    error: String?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Hasło",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0B1C30),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = { Text("••••••••", color = OnSurfaceVariant) },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (error != null) ErrorColor else OnSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ukryj hasło" else "Pokaż hasło",
                        tint = OnSurfaceVariant
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = error != null,
            supportingText = {
                if (error != null) Text(error, color = ErrorColor)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDark,
                unfocusedBorderColor = OutlineVariant,
                errorBorderColor = ErrorColor
            ),
            singleLine = true
        )
    }
}




