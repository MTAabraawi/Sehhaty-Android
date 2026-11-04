package com.mtaapps.sehhaty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.mtaapps.sehhaty.alarm.AlarmScheduler
import com.mtaapps.sehhaty.alarm.NotificationHelper
import com.mtaapps.sehhaty.data.local.entity.DoseScheduleEntity
import com.mtaapps.sehhaty.data.local.entity.DoseStatus
import com.mtaapps.sehhaty.data.repository.MedicationRepository
import com.mtaapps.sehhaty.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddMedicationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSupervisorClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember {
        val db = SehhatyApp.instance.database
        MedicationRepository(db.medicationDao(), db.doseDao())
    }

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
    val todayDoses by repository.getTodayDoses(currentUid).collectAsState(initial = emptyList())

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale("ar")) }
    val dayFormat = remember { SimpleDateFormat("EEEE، d MMMM", Locale("ar")) }
    val todayDateFormatted = remember { dayFormat.format(Date()) }

    val totalDoses = todayDoses.size
    val confirmedCount = todayDoses.count { it.status == DoseStatus.CONFIRMED }
    val dueCount = todayDoses.count { it.status == DoseStatus.DUE }
    val progressPercent = if (totalDoses > 0) ((confirmedCount.toFloat() / totalDoses) * 100).toInt() else 0
    val nextDose = todayDoses.firstOrNull { it.status != DoseStatus.CONFIRMED }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
        // Floating action button is REMOVED as requested (only central button remains)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Calm, Minimalist Date Banner (Without intrusive branding bar or local mode badge)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = todayDateFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "جدول الأدوية والجرعات",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Notification / Due Alert Integrated Inside Content
            if (dueCount > 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandAmber.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BrandAmber)
                            Column {
                                Text(
                                    text = "تنبيه: لديك $dueCount جرعة حان موعد تناولها الآن",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "يرجى تأكيد تناول الجرعة لإيقاف التنبيه المكرر.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Adherence Metric Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "الالتزام الدوائي لليوم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (totalDoses == 0) "لا توجد جرعات مجدولة بعد" else "$confirmedCount من $totalDoses جرعات تم تناولها",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (nextDose != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "أقرب جرعة: ${nextDose.medicationName} (${timeFormat.format(Date(nextDose.scheduledTimeTimestamp))})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandTeal
                                )
                            }
                        }

                        // Circular Progress Indicator
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                            CircularProgressIndicator(
                                progress = { progressPercent / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = BrandTeal,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                strokeWidth = 6.dp
                            )
                            Text(
                                "$progressPercent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Central "إضافة دواء جديد" Action Button (The ONLY Add button on Home Screen)
            item {
                Button(
                    onClick = onAddMedicationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "إضافة دواء جديد وجدولة التذكيرات",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Doses Section Title
            item {
                Text(
                    text = "قائمة جرعات اليوم",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (todayDoses.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Medication,
                                contentDescription = null,
                                tint = BrandTeal,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "لا توجد جرعات مجدولة لليوم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "اضغط على زر إضافة دواء أعلاه لتسجيل مواعيد أدويتك بدقة.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(todayDoses, key = { it.id }) { dose ->
                    QuietDoseCard(
                        dose = dose,
                        timeFormatted = timeFormat.format(Date(dose.scheduledTimeTimestamp)),
                        onConfirm = {
                            coroutineScope.launch {
                                repository.confirmDose(
                                    doseId = dose.id,
                                    medicationId = dose.medicationId,
                                    medName = dose.medicationName,
                                    doseStrength = dose.doseStrength,
                                    scheduledTime = dose.scheduledTimeTimestamp
                                )
                                AlarmScheduler(context).cancelAllDoseAlarms(dose.id)
                                NotificationHelper.cancelNotification(context, dose.id.toInt())
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuietDoseCard(
    dose: DoseScheduleEntity,
    timeFormatted: String,
    onConfirm: () -> Unit
) {
    val isConfirmed = dose.status == DoseStatus.CONFIRMED
    val isDue = dose.status == DoseStatus.DUE

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDue) {
                BrandAmber.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(if (isDue) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when {
                    isDue -> BrandAmber
                    isConfirmed -> StatusSuccess.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isConfirmed -> StatusSuccess.copy(alpha = 0.15f)
                                isDue -> BrandAmber.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.Medication,
                        contentDescription = null,
                        tint = when {
                            isConfirmed -> StatusSuccess
                            isDue -> BrandAmber
                            else -> BrandTeal
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = dose.medicationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${dose.doseStrength} • $timeFormatted",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isConfirmed) {
                Surface(
                    color = StatusSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "تم التأكيد",
                        color = StatusSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDue) BrandAmber else BrandTeal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isDue) "تناول الآن" else "تأكيد الجرعة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
