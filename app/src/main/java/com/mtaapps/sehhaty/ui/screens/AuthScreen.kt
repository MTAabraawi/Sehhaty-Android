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

/**
 * Authentication Screen.
 * - Requires real sign-in (Google or Firebase Email).
 * - "Continue in offline mode" guest button is completely REMOVED.
 * - Does NOT navigate to Home until authentication is genuinely verified.
 * - Sessions are cached by Firebase Auth locally across app restarts.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmailForm by remember { mutableStateOf(false) }

    // If already authenticated, proceed directly
    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            onAuthSuccess()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            isLoading = false
                            if (authTask.isSuccessful && FirebaseAuth.getInstance().currentUser != null) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "فشل ربط الحساب بـ Firebase: " + (authTask.exception?.localizedMessage ?: "حاول مجدداً")
                            }
                        }
                } else {
                    isLoading = false
                    errorMessage = "تعذر استلام رمز مصادقة Google (ID Token). يرجى التأكد من ربط بصمة SHA-1 في Firebase Console أو استخدام البريد الإلكتروني أدناه."
                    showEmailForm = true
                }
            } catch (e: ApiException) {
                isLoading = false
                errorMessage = "خطأ في الاتصال بـ Google (" + e.statusCode + "): " + (e.localizedMessage ?: "تم إلغاء تسجيل الدخول")
            }
        } else {
            isLoading = false
            errorMessage = "تم إلغاء تسجيل الدخول أو تعذر إتمامه. يرجى المحاولة مجدداً."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "شعار صحتي",
                        modifier = Modifier.size(68.dp).clip(RoundedCornerShape(16.dp))
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "صحتي | Sehhaty",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "MTA TECHNOLOGIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سجّل دخولك لربط حسابك الصحي؛ سيعمل التطبيق لاحقاً دون الحاجة للإنترنت.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // Real Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null

                        val webClientIdResId = context.resources.getIdentifier(
                            "default_web_client_id", "string", context.packageName
                        )
                        val serverClientId = if (webClientIdResId != 0) context.getString(webClientIdResId) else null

                        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()

                        if (!serverClientId.isNullOrBlank()) {
                            gsoBuilder.requestIdToken(serverClientId)
                        }

                        val client = GoogleSignIn.getClient(context, gsoBuilder.build())
                        googleSignInLauncher.launch(client.signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Email Form (Direct Alternative)
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
                            isLoading = true
                            errorMessage = null
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful && FirebaseAuth.getInstance().currentUser != null) {
                                        isLoading = false
                                        onAuthSuccess()
                                    } else {
                                        // Attempt auto-register if account doesn't exist
                                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
                                            .addOnCompleteListener { regTask ->
                                                isLoading = false
                                                if (regTask.isSuccessful && FirebaseAuth.getInstance().currentUser != null) {
                                                    onAuthSuccess()
                                                } else {
                                                    errorMessage = regTask.exception?.localizedMessage ?: "فشل تسجيل الدخول"
                                                }
                                            }
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                    ) {
                        Text("تسجيل الدخول / إنشاء حساب جديد", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { showEmailForm = true }) {
                        Text("أو التسجيل عبر البريد الإلكتروني", fontSize = 12.sp, color = BrandTeal)
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(color = BrandTeal, modifier = Modifier.size(28.dp))
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Security & Privacy Notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
                        text = "بياناتك الصحية محمية محلياً في مساحة التخزين الخاصة بالتطبيق وتعمل دون إنترنت فور تسجيل الدخول.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
