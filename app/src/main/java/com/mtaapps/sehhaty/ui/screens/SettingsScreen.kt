package com.mtaapps.sehhaty.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.mtaapps.sehhaty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "مستخدم محلي مسجل"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات والخصوصية", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Account Info Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الحساب المسجل", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (currentUser != null) {
                        TextButton(onClick = { showSignOutDialog = true }) {
                            Text("خروج", color = StatusDanger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Battery Optimization Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                onClick = { showBatteryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(28.dp))
                    Column {
                        Text("إعدادات البطارية وتجاوز توفير الطاقة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("لضمان استمرار رنين التنبيه كل دقيقتين حتى تأكيد الجرعة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Privacy Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                onClick = { showPrivacyDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(28.dp))
                    Column {
                        Text("سياسة الخصوصية وأمان البيانات", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("بياناتك محفوظة محلياً في مساحة التخزين الخاصة بهاتفك", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Identity & Build info
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("صحتي | Sehhaty - الإصدار 1.0.0", fontWeight = FontWeight.Bold, color = BrandTeal, fontSize = 14.sp)
                    Text("تطوير: MTA TECHNOLOGIES", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("حزمة التطبيق: com.mtaapps.sehhaty", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Battery Dialog
            if (showBatteryDialog) {
                AlertDialog(
                    onDismissRequest = { showBatteryDialog = false },
                    title = { Text("تعليمات دقة التنبيهات في Android") },
                    text = {
                        Text("أنظمة Android تقوم بإيقاف التنبيهات في الخلفية لتوفير شحن البطارية. لضمان دقة مواعيد أخذ الأدوية: افتح إعدادات هاتفك > التطبيقات > صحتي > البطارية > اختر «غير مقيد» (Unrestricted).")
                    },
                    confirmButton = {
                        TextButton(onClick = { showBatteryDialog = false }) { Text("حسناً") }
                    }
                )
            }

            // Privacy Dialog
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("سياسة الخصوصية والأمان") },
                    text = {
                        Text("1. تطبيق صحتي لا يشارك بياناتك الطبية أو أسماء أدويتك مع أي جهة إعلانية أو طرف ثالث.\n2. التذكيرات تعمل محلياً بالكامل على هاتفك دون حاجة للإنترنت.\n3. قواعد Firebase تؤمن بياناتك برقم UID الخاص بك حصراً.")
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) { Text("موافق") }
                    }
                )
            }

            // Sign out Dialog
            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    title = { Text("تسجيل الخروج") },
                    text = { Text("هل تود تسجيل الخروج من حسابك في تطبيق صحتي؟ ستبقى أدويتك المحفوظة محلياً على جهازك.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                showSignOutDialog = false
                                Toast.makeText(context, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                        ) {
                            Text("نعم، تسجيل الخروج")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSignOutDialog = false }) { Text("إلغاء") }
                    }
                )
            }
        }
    }
}
