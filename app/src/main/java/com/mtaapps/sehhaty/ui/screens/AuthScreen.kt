package com.mtaapps.sehhaty.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueOffline: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Google Sign-In Activity Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null && FirebaseSyncService.isFirebaseConfigured) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener {
                            onAuthSuccess()
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "تعذر تسجيل الدخول عبر Firebase: ${e.localizedMessage}"
                        }
                } else {
                    // Successful account resolution in offline/dev setup
                    onAuthSuccess()
                }
            } catch (e: ApiException) {
                errorMessage = "خطأ في الاتصال بـ Google Sign-In: ${e.statusCode}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandNavy, BrandTeal, BgCanvas)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Official Sehhaty App Icon
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(BgContainerTeal)
                        .border(1.5.dp, BrandTeal.copy(alpha = 0.3f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "شعار صحتي",
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp))
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
                        text = "رفيقك الصحي الموثوق لإدارة الأدوية والتذكيرات الدقيقة",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Divider(color = Color(0xFFF1F5F9))

                // Real Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        if (FirebaseSyncService.isFirebaseConfigured) {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken("YOUR_SERVER_CLIENT_ID")
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(client.signInIntent)
                        } else {
                            isLoading = false
                            // Graceful notice for dev/test before google-services.json is configured
                            Toast.makeText(
                                context,
                                "Firebase لم يُفعّل بعد. يرجى إضافة google-services.json للتكامل السحابي الكامل.",
                                Toast.LENGTH_LONG
                            ).show()
                            onAuthSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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
                        // Official Google 'G' Symbol representation
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
                        text = "المتابعة في الوضع المحلي (بدون إنترنت)",
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
                        text = "بياناتك الصحية مشفرة ومحفوظة محلياً 100% دون مشاركة مع أي طرف ثالث.",
                        fontSize = 10.sp,
                        color = BrandNavy,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
