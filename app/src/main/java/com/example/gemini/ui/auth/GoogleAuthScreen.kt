package com.example.gemini.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.R
import com.example.gemini.ui.chat.ChatViewModel
import kotlinx.coroutines.delay

/**
 * Advanced Google Authentication & Cloud Onboarding Screen.
 * Displayed upon launching the application if the user is unauthenticated,
 * providing direct Google OAuth 2.0 PKCE authentication with local callback verification,
 * manual authorization code support, and seamless workspace entry.
 */
@Composable
fun GoogleAuthScreen(
    viewModel: ChatViewModel,
    onAuthSuccess: () -> Unit,
    onSkipOrDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val userEmail by viewModel.userEmail.collectAsState()
    val agyAuthInfo by viewModel.agyAuthInfo.collectAsState()
    val isOAuthServerListening by viewModel.isOAuthServerListening.collectAsState()
    val isOAuthServerLoading by viewModel.isOAuthServerLoading.collectAsState()
    val isAuthBusy by viewModel.isAuthBusy.collectAsState()
    val authFeedbackMessage by viewModel.authFeedbackMessage.collectAsState(initial = null)

    var statusDetailText by remember { mutableStateOf<String?>(null) }
    var manualInputCode by remember { mutableStateOf("") }
    var isManualSectionExpanded by remember { mutableStateOf(false) }
    var isSubmittingManual by remember { mutableStateOf(false) }
    var isSuccessCelebration by remember { mutableStateOf(false) }

    val isAuthenticated = !userEmail.isNullOrBlank() || agyAuthInfo?.isLoggedIn == true
    val activeEmail = userEmail ?: agyAuthInfo?.email?.takeIf { it.isNotBlank() } ?: "Google Account"

    // Automatically transition to workspace once authenticated
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            isSuccessCelebration = true
            delay(850L)
            onAuthSuccess()
        }
    }

    // Glow pulse animation for badge & banner
    val infiniteTransition = rememberInfiniteTransition(label = "auth_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    fun launchGoogleSignIn() {
        statusDetailText = "Initiating OAuth 2.0 PKCE handshake..."
        val authUrl = viewModel.getGoogleOAuthUrl()
        statusDetailText = "Opening browser for Google authorization..."
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            statusDetailText = "Waiting for callback on localhost:51121..."
        } catch (e: Exception) {
            statusDetailText = "Failed to launch browser: ${e.message}"
        }
        viewModel.loginToAgyHub(force = true)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)),
        color = Color(0xFF090D16)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient aurora background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E293B).copy(alpha = 0.5f),
                                Color(0xFF0F172A).copy(alpha = 0.85f),
                                Color(0xFF090D16)
                            ),
                            radius = 1200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header: System Chip & Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                            .border(
                                BorderStroke(1.dp, Color(0xFF334155).copy(alpha = glowAlpha)),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isAuthenticated) Color(0xFF10B981) else Color(0xFF38BDF8))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isAuthenticated) "AUTHENTICATED" else "GOOGLE CLOUD ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAuthenticated) Color(0xFF34D399) else Color(0xFF93C5FD),
                            letterSpacing = 0.8.sp
                        )
                    }

                    TextButton(
                        onClick = onSkipOrDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Skip to Workspace",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Antigravity Brand Hero
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF4285F4),
                                        Color(0xFF34A853),
                                        Color(0xFFFBBC05),
                                        Color(0xFFEA4335),
                                        Color(0xFF4285F4)
                                    )
                                )
                            )
                            .padding(2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.antigravity_logo),
                                contentDescription = "Google Antigravity Logo",
                                modifier = Modifier.size(62.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Google Antigravity",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Autonomous AI Agent & Full-Stack Development Hub",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                // Architecture Feature Pills
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AuthFeatureRow(
                        icon = Icons.Outlined.CloudQueue,
                        iconTint = Color(0xFF38BDF8),
                        title = "Google Cloud Model Hub",
                        description = "Direct access to Gemini 2.5/3.0 Pro & Flash with extended thinking."
                    )
                    AuthFeatureRow(
                        icon = Icons.Outlined.SmartToy,
                        iconTint = Color(0xFF34D399),
                        title = "Autonomous Coding Engine",
                        description = "Multi-step tool execution, semantic AST refactors, and diff tracking."
                    )
                    AuthFeatureRow(
                        icon = Icons.Outlined.Terminal,
                        iconTint = Color(0xFFFBBF24),
                        title = "Embedded Linux Runtime",
                        description = "Native Termux environment, tmux persistence, and Claude Code CLI."
                    )
                }

                Spacer(Modifier.height(26.dp))

                // Core Authentication Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSuccessCelebration || isAuthenticated) {
                            // Authenticated State View
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Signed In",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Authentication Successful",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Signed in as $activeEmail",
                                fontSize = 13.5.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onAuthSuccess,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Enter Workspace",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (isOAuthServerListening || isOAuthServerLoading || isAuthBusy) {
                            // Active Handshake State View
                            CircularProgressIndicator(
                                modifier = Modifier.size(38.dp),
                                strokeWidth = 3.dp,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Authorizing with Google...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = statusDetailText ?: "Waiting for browser authorization on http://localhost:51121/oauth-callback",
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { launchGoogleSignIn() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Icon(Icons.Outlined.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reopen", fontSize = 13.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.stopOAuthServer()
                                        statusDetailText = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF334155),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Cancel", fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Idle State: Primary Sign In With Google Button
                            Button(
                                onClick = { launchGoogleSignIn() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1F2937)
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F2937)
                                )
                            }

                            if (!statusDetailText.isNullOrBlank() || !authFeedbackMessage.isNullOrBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = statusDetailText ?: authFeedbackMessage.orEmpty(),
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Manual Code / Token Expandable Option
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isManualSectionExpanded = !isManualSectionExpanded }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isManualSectionExpanded) "Hide manual code entry" else "Enter authorization code manually",
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        AnimatedVisibility(
                            visible = isManualSectionExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                OutlinedTextField(
                                    value = manualInputCode,
                                    onValueChange = { manualInputCode = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Paste 4/0... auth code or callback URL",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (manualInputCode.isNotBlank()) {
                                            isSubmittingManual = true
                                            statusDetailText = "Verifying authorization code..."
                                            viewModel.applyManualInput(manualInputCode.trim())
                                        }
                                    },
                                    enabled = manualInputCode.isNotBlank() && !isSubmittingManual,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF334155),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Verify & Connect Code", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Bottom Offline / Guest Link & Security Notice
                OutlinedButton(
                    onClick = onSkipOrDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Text(
                        text = "Continue to Local Workspace (Offline Mode)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Encrypted",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "OAuth 2.0 PKCE • Tokens stored on-device in Android DataStore",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.6f))
            .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 15.sp
            )
        }
    }
}
