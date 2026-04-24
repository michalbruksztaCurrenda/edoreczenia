package com.edoreczenia.feature.auth.presentation.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

// Kolory zgodne z makietą (Material 3, seed: #001e40)
private val PrimaryDark = Color(0xFF001E40)
private val PrimaryContainer = Color(0xFF003366)
private val SecondaryContainer = Color(0xFFfd8b00)       // pomarańczowy przycisk
private val OnSecondaryFixed = Color(0xFF2F1500)          // tekst na pomarańczowym
private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val SurfaceColor = Color(0xFFF8F9FF)
private val OutlineVariant = Color(0xFFC3C6D1)
private val OnSurfaceVariant = Color(0xFF43474F)
private val OutlineColor = Color(0xFF737780)
private val ErrorColor = Color(0xFFBA1A1A)

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = viewModel(factory = RegistrationViewModel.factory()),
    onNavigateToVerifyEmail: (username: String, email: String) -> Unit = { _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is RegistrationEffect.NavigateToVerifyEmail ->
                    onNavigateToVerifyEmail(effect.username, effect.email)
                is RegistrationEffect.NavigateToLogin -> onNavigateToLogin()
                is RegistrationEffect.ShowMessage -> { /* obsługiwane przez formError */ }
            }
        }
    }

    Scaffold(
        topBar = { RegistrationTopBar() },
        containerColor = SurfaceColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegistrationCard(
                uiState = uiState,
                onDeviceNameChanged = viewModel::onDeviceNameChanged,
                onUsernameChanged = viewModel::onUsernameChanged,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
                onRegisterClicked = viewModel::onRegisterClicked,
                onBackToLoginClicked = viewModel::onBackToLoginClicked,
                focusManager = focusManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationTopBar() {
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
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0F172A)
        )
    )
}

@Composable
private fun RegistrationCard(
    uiState: RegistrationUiState,
    onDeviceNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onBackToLoginClicked: () -> Unit,
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
            // Tytuł
            Text(
                text = "Rejestracja nowego użytkownika",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 34.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Utwórz konto, aby uzyskać dostęp do panelu kancelarii.",
                fontSize = 15.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Pole: Nazwa urządzenia
            RegistrationTextField(
                value = uiState.deviceNameInput,
                onValueChange = onDeviceNameChanged,
                label = "Nazwa urządzenia",
                placeholder = "np. Komputer Główny",
                leadingIcon = Icons.Default.Computer,
                error = uiState.deviceNameError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(Modifier.height(12.dp))

            // Pole: Nazwa użytkownika
            RegistrationTextField(
                value = uiState.usernameInput,
                onValueChange = onUsernameChanged,
                label = "Nazwa użytkownika",
                placeholder = "j.kowalski",
                leadingIcon = Icons.Default.Person,
                error = uiState.usernameError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(Modifier.height(12.dp))

            // Pole: E-mail
            RegistrationTextField(
                value = uiState.emailInput,
                onValueChange = onEmailChanged,
                label = "E-mail służbowy",
                placeholder = "kancelaria@e-komornik.pl",
                leadingIcon = Icons.Default.Email,
                error = uiState.emailError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(Modifier.height(12.dp))

            // Pole: Hasło
            PasswordRegistrationField(
                value = uiState.passwordInput,
                onValueChange = onPasswordChanged,
                label = "Hasło",
                error = uiState.passwordError,
                enabled = !uiState.isLoading,
                showHint = true,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(Modifier.height(12.dp))

            // Pole: Powtórz hasło
            PasswordRegistrationField(
                value = uiState.confirmPasswordInput,
                onValueChange = onConfirmPasswordChanged,
                label = "Powtórz hasło",
                error = uiState.confirmPasswordError,
                enabled = !uiState.isLoading,
                showHint = false,
                imeAction = ImeAction.Done,
                onImeAction = { onRegisterClicked() }
            )

            // Komunikat ogólnego błędu
            if (uiState.formError != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = uiState.formError,
                            fontSize = 14.sp,
                            color = ErrorColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Przycisk: Zarejestruj konto
            Button(
                onClick = onRegisterClicked,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryContainer,
                    contentColor = OnSecondaryFixed,
                    disabledContainerColor = SecondaryContainer.copy(alpha = 0.5f),
                    disabledContentColor = OnSecondaryFixed.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnSecondaryFixed,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Zarejestruj konto",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Link: Mam już konto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Masz już konto? ",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
                TextButton(
                    onClick = onBackToLoginClicked,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Mam już konto",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stopka z info bezpieczeństwa
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = OutlineColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SSL SECURE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OutlineColor.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Regulamin",
                        fontSize = 11.sp,
                        color = OutlineColor
                    )
                    Text(
                        text = "Polityka prywatności",
                        fontSize = 11.sp,
                        color = OutlineColor
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    error: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    color = OutlineColor.copy(alpha = 0.6f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (error != null) ErrorColor else OutlineColor
                )
            },
            isError = error != null,
            supportingText = if (error != null) {
                {
                    Text(
                        text = error,
                        color = ErrorColor,
                        fontSize = 12.sp
                    )
                }
            } else null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryContainer,
                unfocusedBorderColor = OutlineVariant,
                errorBorderColor = ErrorColor,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            )
        )
    }
}

@Composable
private fun PasswordRegistrationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    showHint: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = {
                Text(
                    text = "••••••••",
                    color = OutlineColor.copy(alpha = 0.6f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (error != null) ErrorColor else OutlineColor
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ukryj hasło" else "Pokaż hasło",
                        tint = OutlineColor
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = error != null,
            supportingText = if (error != null) {
                { Text(text = error, color = ErrorColor, fontSize = 12.sp) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryContainer,
                unfocusedBorderColor = OutlineVariant,
                errorBorderColor = ErrorColor,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            )
        )
        if (showHint && error == null) {
            Spacer(Modifier.height(6.dp))
            // Wskaźnik siły hasła (wizualny)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val strength = calculatePasswordStrength(value)
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (index < strength) SecondaryContainer else OutlineVariant,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SecondaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Hasło musi zawierać min. 8 znaków i symbol specjalny.",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun calculatePasswordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score
}



