package com.mtaapps.sehhaty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaapps.sehhaty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات والخصوصية", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                onClick = { showBatteryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("إعدادات البطارية وتجاوز توفير الطاقة", fontWeight = FontWeight.Bold)
                        Text("لضمان استمرار رنين التنبيه كل دقيقتين حتى تأكيد الجرعة", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                onClick = { showPrivacyDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("سياسة الخصوصية وأمان البيانات", fontWeight = FontWeight.Bold)
                        Text("جميع بياناتك الصحية مشفرة ومحفوظة محلياً", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TealLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("صحتي | Sehhaty - الإصدار 1.0.0", fontWeight = FontWeight.Bold, color = TealDark)
                    Text("تطوير: MTA TECHNOLOGIES", fontSize = 13.sp, color = TextSecondary)
                    Text("حزمة التطبيق: com.mtaapps.sehhaty", fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (showBatteryDialog) {
                AlertDialog(
                    onDismissRequest = { showBatteryDialog = false },
                    title = { Text("تعليمات البطارية في Android") },
                    text = {
                        Text("أنظمة Android تقوم بإيقاف التنبيهات في الخلفية لتوفير الطاقة. لضمان دقة مواعيد الأدوية: افتح إعدادات هاتفك > التطبيقات > صحتي > البطارية > اختر «غير مقيد» (Unrestricted).")
                    },
                    confirmButton = {
                        TextButton(onClick = { showBatteryDialog = false }) { Text("حسناً") }
                    }
                )
            }

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("سياسة الخصوصية والأمان") },
                    text = {
                        Text("1. تطبيق صحتي لا يشارك بياناتك الطبية مع أي معلن أو طرف ثالث.\n2. التذكيرات تعمل محلياً بالكامل على هاتفك دون حاجة للإنترنت.\n3. قواعد Firebase تؤمن بياناتك برقم UID الخاص بك فقط.")
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) { Text("موافق") }
                    }
                )
            }
        }
    }
}
