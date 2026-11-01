package com.mtaapps.sehhaty.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mtaapps.sehhaty.R
import com.mtaapps.sehhaty.data.remote.FirebaseSyncService
import com.mtaapps.sehhaty.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Authentication Screen.
 * Provides:
 * 1. Real Google Sign-In (using default_web_client_id if configured in Firebase).
 * 2. Real Firebase Email/Password Sign-In & Registration.
 * 3. 100% Offline Local Mode without cloud dependency.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueOffline: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmailForm by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            isLoading = false
                            if (authTask.isSuccessful) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "فشل تسجيل الدخول بـ Google: " + (authTask.exception?.localizedMessage ?: "")
                            }
                        }
                } else {
                    isLoading = false
                    errorMessage = "تعذر استلام رمز مصادقة Google."
                }
            } catch (e: ApiException) {
                isLoading = false
                errorMessage = "خطأ في الاتصال بـ Google (" + e.statusCode + ")"
            }
        } else {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNeutral),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Official Sehhaty App Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgContainerTeal)
                        .border(1.5.dp, BrandTeal.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "شعار صحتي",
                        modifier = Modifier.size(68.dp).clip(RoundedCornerShape(16.dp))
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "صحتي | Sehhaty",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    Text(
                        text = "MTA TECHNOLOGIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تسجيل الدخول يتيح المزامنة السحابية الآمنة وحفظ نسخ احتياطية لأدويتكم",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Divider(color = Color(0xFFF1F5F9))

                // Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        if (FirebaseSyncService.isFirebaseConfigured) {
                            val webClientIdResId = context.resources.getIdentifier(
                                "default_web_client_id", "string", context.packageName
                            )
                            val serverClientId = if (webClientIdResId != 0) {
                                context.getString(webClientIdResId)
                            } else {
                                null
                            }

                            if (!serverClientId.isNullOrBlank()) {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestIdToken(serverClientId)
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(client.signInIntent)
                            } else {
                                isLoading = false
                                // Clear guidance if Web Client ID is not yet generated in Firebase Console
                                showEmailForm = true
                                Toast.makeText(
                                    context,
                                    "يمكنك تسجيل الدخول بالبريد الإلكتروني، أو إكمال إعداد Google Sign-In في Firebase Console",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            isLoading = false
                            onContinueOffline()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "المتابعة باستخدام Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }

                // Email Form (Expandable)
                if (showEmailForm) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandTeal) }
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandTeal) }
                    )
                    Button(
                        onClick = {
                            if (email.isBlank() || password.length < 6) {
                                errorMessage = "يرجى إدخال بريد صالح وكلمة مرور لا تقل عن 6 أحرف"
                                return@Button
                            }
                            errorMessage = null
                            isLoading = true
                            FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = task.exception?.localizedMessage
                                            ?: "تعذر تسجيل الدخول. تحقق من البريد وكلمة المرور."
                                    }
                                }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Text("تسجيل الدخول")
                    }

                    OutlinedButton(
                        onClick = {
                            if (email.isBlank() || password.length < 6) {
                                errorMessage = "يرجى إدخال بريد صالح وكلمة مرور لا تقل عن 6 أحرف"
                                return@OutlinedButton
                            }
                            errorMessage = null
                            isLoading = true
                            FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = task.exception?.localizedMessage
                                            ?: "تعذر إنشاء الحساب."
                                    }
                                }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إنشاء حساب جديد")
                    }
                } else {
                    TextButton(onClick = { showEmailForm = true }) {
                        Text("أو تسجيل الدخول عبر البريد الإلكتروني", fontSize = 12.sp, color = BrandNavy)
                    }
                }

                // Offline Guest Mode Button
                OutlinedButton(
                    onClick = onContinueOffline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTeal)
                ) {
                    Text(
                        text = "المتابعة في الوضع المحلي (100% دون إنترنت)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Security & Privacy Guarantee Notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgContainerMint)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "بياناتك الصحية محفوظة محلياً في مساحة التخزين المعزولة الخاصة بالتطبيق على جهازك دون مشاركتها مع أي طرف ثالث.",
                        fontSize = 10.sp,
                        color = BrandNavy,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
