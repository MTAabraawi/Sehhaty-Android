package com.mtaapps.sehhaty.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.SupervisorEntity
import com.mtaapps.sehhaty.data.repository.SupervisorRepository
import com.mtaapps.sehhaty.supervisor.SupervisorAlertManager
import com.mtaapps.sehhaty.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember {
        SupervisorRepository(SehhatyApp.instance.database.supervisorDao())
    }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
    val supervisor by repository.getSupervisor(currentUid).collectAsState(initial = null)

    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("الابن") }
    var phoneNumber by remember { mutableStateOf("") }
    var alertDelay by remember { mutableStateOf("30") }
    var consentGiven by remember { mutableStateOf(true) }

    LaunchedEffect(supervisor) {
        supervisor?.let {
            name = it.name
            relation = it.relationship
            phoneNumber = it.whatsappNumber
            alertDelay = it.alertDelayMinutes.toString()
            consentGiven = it.consentGiven
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مشرف الأسرة والتنبيهات", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanatory Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = BrandTeal,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "نظام المشرف العائلي الموثوق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "يتيح هذا النظام إرسال تنبيه ودّي للمشرف العائلي في حال تأخر تأكيد تناول الدواء عن الموعد المحدد.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Supervisor Information Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "بيانات المشرف العائلي",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المشرف (مثال: محمد)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("صلة القرابة (مثال: الابن، الزوجة، الأخت)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("رقم الواتساب مع المفتاح الدولي (مثال: +96650xxxxxxx)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = BrandTeal)
                        }
                    )

                    OutlinedTextField(
                        value = alertDelay,
                        onValueChange = { alertDelay = it.filter { char -> char.isDigit() } },
                        label = { Text("مهلة الانتظار قبل التنبيه (بالدقائق)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Consent Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(
                            checked = consentGiven,
                            onCheckedChange = { consentGiven = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أوافق على تنبيه المشرف عند تأخر تأكيد الجرعة",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // WhatsApp clarification notice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "طريقة الإرسال الرسمية: عند حلول موعد التنبيه يفتح التطبيق محادثة WhatsApp مباشرة مع المشرف برسالة مهذبة ومعدة مسبقاً لتأكيد إرسالها بنقرة واحدة (وفق أمان وسياسة خصوصية Android وWhatsApp).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            val cleanPhone = phoneNumber.trim()
                            if (name.isBlank() || cleanPhone.length < 8) {
                                Toast.makeText(context, "يرجى إدخال اسم المشرف ورقم هاتف صحيح مع المفتاح الدولي", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val delay = alertDelay.toIntOrNull() ?: 30
                            val updated = SupervisorEntity(
                                id = supervisor?.id ?: 0,
                                name = name.trim(),
                                relationship = relation.trim(),
                                phoneNumber = cleanPhone,
                                whatsappNumber = cleanPhone,
                                consentGiven = consentGiven,
                                alertDelayMinutes = delay,
                                isActive = true,
                                userId = currentUid
                            )
                            coroutineScope.launch {
                                repository.saveSupervisor(updated)
                                Toast.makeText(context, "تم حفظ بيانات المشرف بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ إعدادات المشرف", fontWeight = FontWeight.Bold)
                    }

                    // Test WhatsApp Alert Button
                    if (phoneNumber.isNotBlank() && phoneNumber.length >= 8) {
                        OutlinedButton(
                            onClick = {
                                val intent = SupervisorAlertManager.createWhatsAppIntent(
                                    supervisorPhoneWithCountryCode = phoneNumber.trim(),
                                    medicationName = "الدواء التجريبي",
                                    doseStrength = "500 ملغ"
                                )
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback if WhatsApp package is not installed or different package
                                    val fallbackIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber.replace("+", "").replace(" ", "").trim())
                                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                    try {
                                        context.startActivity(fallbackIntent)
                                    } catch (e2: Exception) {
                                        Toast.makeText(context, "تعذر فتح تطبيق WhatsApp. يرجى التأكد من تثبيته.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusSuccess)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تجربة فتح تنبيه WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
