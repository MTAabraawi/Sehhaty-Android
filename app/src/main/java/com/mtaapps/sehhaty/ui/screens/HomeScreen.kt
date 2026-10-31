package com.mtaapps.sehhaty.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaapps.sehhaty.R
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
    userId: String,
    onAddMedicationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSupervisorClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember {
        val db = SehhatyApp.instance.database
        MedicationRepository(db, db.medicationDao(), db.doseDao())
    }

    val todayDoses by repository.getTodayDoses(userId).collectAsState(initial = emptyList())
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale("ar")) }
    val dayFormat = remember { SimpleDateFormat("EEEE، d MMMM", Locale("ar")) }
    val todayDateFormatted = remember { dayFormat.format(Date()) }

    var showNotificationCenter by remember { mutableStateOf(false) }

    val totalDoses = todayDoses.size
    val confirmedCount = todayDoses.count { it.status == DoseStatus.CONFIRMED }
    val dueCount = todayDoses.count { it.status == DoseStatus.DUE }
    val progressPercent = if (totalDoses > 0) ((confirmedCount.toFloat() / totalDoses) * 100).toInt() else 0
    val nextDose = todayDoses.firstOrNull { it.status != DoseStatus.CONFIRMED }

    Scaffold(
        topBar = {
            // Calm, uncluttered, world-class Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BrandNavy, BrandTeal)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = "شعار صحتي",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        )
                        Column {
                            Text("صحتي | Sehhaty", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("MTA TECHNOLOGIES", fontSize = 10.sp, color = BrandMint.copy(alpha = 0.9f))
                        }
                    }

                    // Top Action Icons: Notification Center (Bell 🔔) & Search
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // In-App Notification Center Bell Icon
                        IconButton(
                            onClick = { showNotificationCenter = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            BadgedBox(
                                badge = {
                                    if (dueCount > 0) {
                                        Badge(
                                            containerColor = BrandAmber,
                                            contentColor = Color.White
                                        ) {
                                            Text(dueCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "مركز الإشعارات",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "البحث",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مرحباً بك 👋 • $todayDateFormatted", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                    Text("وضع محلي آمن", fontSize = 11.sp, color = BrandMint, fontWeight = FontWeight.Bold)
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedicationClick,
                containerColor = BrandTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة دواء جديد", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Clean Daily Adherence Metric Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الالتزام الدوائي اليومي", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Text(
                                if (totalDoses == 0) "لا توجد جرعات مجدولة اليوم" else "$confirmedCount من $totalDoses جرعات تم تناولها",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            if (nextDose != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "أقرب جرعة: ${nextDose.medicationName} (${timeFormat.format(Date(nextDose.scheduledTimeTimestamp))})",
                                    fontSize = 11.sp,
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
                                trackColor = Color(0xFFF1F5F9),
                                strokeWidth = 6.dp
                            )
                            Text("$progressPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                        }
                    }
                }
            }

            item {
                // Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("قائمة جرعات اليوم", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    TextButton(onClick = onAddMedicationClick) {
                        Text("+ دواء جديد", color = BrandTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (todayDoses.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("لا توجد جرعات مجدولة لليوم", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Text("تذكيرات دقيقة كل دقيقتين تبدأ فور إضافتك للدواء.", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onAddMedicationClick,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إضافة أول دواء الآن")
                            }
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
                                val scheduler = AlarmScheduler(context)
                                scheduler.cancelRepeatAlarm(dose.id)
                                NotificationHelper.cancelNotification(context, dose.id.toInt())
                                repository.confirmDose(
                            userId = userId,
                                    doseId = dose.id,
                                    medicationId = dose.medicationId,
                                    medName = dose.medicationName,
                                    doseStrength = dose.doseStrength,
                                    scheduledTime = dose.scheduledTimeTimestamp
                                )
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Dedicated In-App Notification Center Dialog
        if (showNotificationCenter) {
            AlertDialog(
                onDismissRequest = { showNotificationCenter = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandTeal)
                        Text("مركز الإشعارات والتنبيهات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (dueCount > 0) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = BgContainerAmber)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, tint = BrandAmber)
                                    Column {
                                        Text("تنبيه مواعيد الجرعات المستحقة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDark)
                                        Text("تنبيهات الهاتف ترن كل دقيقتين حتى تضغط «تم تناول الجرعة».", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = BgContainerTeal)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandTeal)
                                Column {
                                    Text("جاهزية التنبيهات المحلية", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDark)
                                    Text("نظام AlarmManager وقناة الإشعارات تعمل بكفاءة 100% دون إنترنت.", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationCenter = false }) {
                        Text("إغلاق", color = BrandTeal, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun QuietDoseCard(
    dose: DoseScheduleEntity,
    timeFormatted: String,
    onConfirm: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (dose.status == DoseStatus.DUE) 1.5.dp else 1.dp,
                color = if (dose.status == DoseStatus.DUE) BrandAmber else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (dose.status == DoseStatus.CONFIRMED) BgContainerMint else BgContainerTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            tint = if (dose.status == DoseStatus.CONFIRMED) StatusSuccess else BrandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(dose.medicationName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text(dose.doseStrength, fontSize = 12.sp, color = TextMuted)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (dose.status) {
                        DoseStatus.CONFIRMED -> StatusSuccess.copy(alpha = 0.12f)
                        DoseStatus.DUE -> StatusDanger.copy(alpha = 0.12f)
                        DoseStatus.UNCONFIRMED -> StatusWarning.copy(alpha = 0.12f)
                        DoseStatus.PENDING -> BrandCyan.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = when (dose.status) {
                            DoseStatus.CONFIRMED -> "تم التأكيد ✓"
                            DoseStatus.DUE -> "حان الموعد !"
                            DoseStatus.UNCONFIRMED -> "لم تؤكد بعد"
                            DoseStatus.PENDING -> "بانتظار الموعد"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = when (dose.status) {
                            DoseStatus.CONFIRMED -> StatusSuccess
                            DoseStatus.DUE -> StatusDanger
                            DoseStatus.UNCONFIRMED -> StatusWarning
                            DoseStatus.PENDING -> BrandNavy
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    Text(timeFormatted, fontSize = 12.sp, color = TextMuted)
                }

                if (dose.status != DoseStatus.CONFIRMED) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("تم تناول الجرعة ✓", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text("تم تناولها بنجاح", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
