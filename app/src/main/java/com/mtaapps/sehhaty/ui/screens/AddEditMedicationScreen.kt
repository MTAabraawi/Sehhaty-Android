package com.mtaapps.sehhaty.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mtaapps.sehhaty.data.local.entity.MedicationEntity
import com.mtaapps.sehhaty.data.repository.MedicationRepository
import com.mtaapps.sehhaty.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationScreen(onSaved: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember {
        val db = SehhatyApp.instance.database
        MedicationRepository(db.medicationDao(), db.doseDao())
    }

    var currentStep by remember { mutableStateOf(1) }

    // Form states
    var name by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") } // Optional
    var dosageForm by remember { mutableStateOf("أقراص") }
    var strength by remember { mutableStateOf("قرص واحد") }
    var instruction by remember { mutableStateOf("بعد الأكل") }
    var timesPerDay by remember { mutableStateOf(1) } // 1, 2, 3, 4

    // 12-hour Dose Times state (hour12, minute, isPM)
    // Represented in 12-hour system with Arabic AM (ص) and PM (م)
    data class DoseTime12(var hour12: Int, var minute: Int, var isPM: Boolean) {
        fun formatArabic(): String {
            val period = if (isPM) "م" else "ص"
            return String.format("%d:%02d %s", hour12, minute, period)
        }
        fun to24hString(): String {
            val h24 = when {
                isPM && hour12 < 12 -> hour12 + 12
                !isPM && hour12 == 12 -> 0
                else -> hour12
            }
            return String.format("%02d:%02d", h24, minute)
        }
        fun toTotalMinutes(): Int {
            val h24 = when {
                isPM && hour12 < 12 -> hour12 + 12
                !isPM && hour12 == 12 -> 0
                else -> hour12
            }
            return h24 * 60 + minute
        }
    }

    var doseTimesList by remember {
        mutableStateOf(listOf(DoseTime12(8, 0, false))) // Default 8:00 ص
    }

    // Automatically update default times when frequency changes
    fun updateFrequency(count: Int) {
        timesPerDay = count
        doseTimesList = when (count) {
            1 -> listOf(DoseTime12(8, 0, false))                          // 8:00 ص
            2 -> listOf(DoseTime12(8, 0, false), DoseTime12(8, 0, true))   // 8:00 ص, 8:00 م
            3 -> listOf(DoseTime12(8, 0, false), DoseTime12(2, 0, true), DoseTime12(8, 0, true)) // 8:00 ص, 2:00 م, 8:00 م
            4 -> listOf(DoseTime12(6, 0, false), DoseTime12(12, 0, true), DoseTime12(6, 0, true), DoseTime12(12, 0, false)) // 6:00 ص, 12:00 م, 6:00 م, 12:00 ص
            else -> listOf(DoseTime12(8, 0, false))
        }
    }

    var durationWeeks by remember { mutableStateOf("علاج مستمر") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingDoseIndex by remember { mutableStateOf<Int?>(null) }

    val forms = listOf("أقراص", "كبسولات", "شراب", "حقن", "قطرة", "بخاخ")
    val instructionsList = listOf("قبل الأكل", "بعد الأكل", "مع الطعام", "حسب تعليمات الطبيب")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة دواء وجدولة التذكيرات", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onSaved) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stepper indicator (1 to 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { step ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (step <= currentStep) BrandTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.toString(),
                            color = if (step <= currentStep) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    if (step < 5) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (step < currentStep) BrandTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            when (currentStep) {
                // Step 1: Medication Name & Optional Scientific Name
                1 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. معلومات الدواء الأساسية", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("اسم الدواء التجاري (مطلوب)") },
                                placeholder = { Text("مثال: بنادول، كونكور...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = scientificName,
                                onValueChange = { scientificName = it },
                                label = { Text("الاسم العلمي (اختياري)") },
                                placeholder = { Text("مثال: باراسيتامول، بيسوبرولول...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Text("الشكل الدوائي:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                forms.take(3).forEach { form ->
                                    FilterChip(
                                        selected = dosageForm == form,
                                        onClick = {
                                            dosageForm = form
                                            if (form == "أقراص") strength = "قرص واحد"
                                            if (form == "شراب") strength = "5 مل"
                                            if (form == "قطرة") strength = "قطرتان"
                                        },
                                        label = { Text(form, fontSize = 12.sp) }
                                    )
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                forms.drop(3).forEach { form ->
                                    FilterChip(
                                        selected = dosageForm == form,
                                        onClick = {
                                            dosageForm = form
                                            if (form == "حقن") strength = "أمبول واحد"
                                            if (form == "بخاخ") strength = "بخة واحدة"
                                        },
                                        label = { Text(form, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 2: Flexible Dose & Clinical Instructions (Not fixed 500mg)
                2 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("2. الجرعة وتعليمات الاستخدام", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

                            // Quick strength chips tailored to selected form
                            Text("مقدار الجرعة المناسبة لـ ($dosageForm):", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            val quickDoses = when (dosageForm) {
                                "أقراص", "كبسولات" -> listOf("قرص واحد", "قرصان", "نصف قرص", "500 ملغ", "1000 ملغ")
                                "شراب" -> listOf("5 مل", "10 مل", "15 مل", "ملعقة صغيرة", "ملعقة كبيرة")
                                "قطرة" -> listOf("قطرة واحدة", "قطرتان", "3 قطرات", "5 قطرات")
                                "بخاخ" -> listOf("بخة واحدة", "بختان", "3 بخات")
                                "حقن" -> listOf("أمبول واحد", "نصف أمبول", "وحدة إنسولين")
                                else -> listOf("جرعة واحدة", "جرعتان")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickDoses.take(3).forEach { d ->
                                    FilterChip(
                                        selected = strength == d,
                                        onClick = { strength = d },
                                        label = { Text(d, fontSize = 11.sp) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = strength,
                                onValueChange = { strength = it },
                                label = { Text("أو اكتب الجرعة والتركيز يدوياً") },
                                placeholder = { Text("مثال: 1 قرص، 250 ملغ، 5 مل...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Usage instructions: قبل الأكل، بعد الأكل، مع الطعام، حسب تعليمات الطبيب
                            Text("تعليمات الاستخدام والوجبات:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    instructionsList.take(2).forEach { inst ->
                                        FilterChip(
                                            selected = instruction == inst,
                                            onClick = { instruction = inst },
                                            label = { Text(inst, fontSize = 12.sp) }
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    instructionsList.drop(2).forEach { inst ->
                                        FilterChip(
                                            selected = instruction == inst,
                                            onClick = { instruction = inst },
                                            label = { Text(inst, fontSize = 12.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 3: Frequency & 12-Hour System Dose Times
                3 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("3. عدد المرات ونظام المواعيد (12 ساعة)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

                            Text("عدد مرات الاستخدام يومياً:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3, 4).forEach { count ->
                                    val label = when (count) {
                                        1 -> "مرة واحدة"
                                        2 -> "مرتان"
                                        3 -> "3 مرات"
                                        else -> "4 مرات"
                                    }
                                    FilterChip(
                                        selected = timesPerDay == count,
                                        onClick = { updateFrequency(count) },
                                        label = { Text(label, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                            Text("مواعيد الجرعات (نظام 12 ساعة):", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("اضغط على أي موعد لتعديل الساعة أو الفترة (صباحاً ص / مساءً م):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // List of 12h time chips
                            doseTimesList.forEachIndexed { index, doseTime ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { editingDoseIndex = index }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(20.dp))
                                            Text("الجرعة ${index + 1}:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(doseTime.formatArabic(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandTeal)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = !doseTime.isPM,
                                                onClick = {
                                                    val updated = doseTimesList.toMutableList()
                                                    updated[index] = doseTime.copy(isPM = false)
                                                    doseTimesList = updated.sortedBy { it.toTotalMinutes() }
                                                },
                                                label = { Text("ص", fontWeight = FontWeight.Bold) }
                                            )
                                            FilterChip(
                                                selected = doseTime.isPM,
                                                onClick = {
                                                    val updated = doseTimesList.toMutableList()
                                                    updated[index] = doseTime.copy(isPM = true)
                                                    doseTimesList = updated.sortedBy { it.toTotalMinutes() }
                                                },
                                                label = { Text("م", fontWeight = FontWeight.Bold) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 4: Duration (Kept as is)
                4 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("4. مدة العلاج", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            listOf("علاج مستمر (أمراض مزمنة)", "أسبوع واحد (مضادات حيوية)", "أسبوعين", "شهر واحد").forEach { dur ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (durationWeeks == dur) BrandTeal.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable { durationWeeks = dur }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(selected = durationWeeks == dur, onClick = { durationWeeks = dur })
                                    Text(dur, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Step 5: Review & Save with Alarms Activation
                5 -> {
                    val sorted24hTimes = doseTimesList.sortedBy { it.toTotalMinutes() }.joinToString(",") { it.to24hString() }
                    val displayTimesAr = doseTimesList.sortedBy { it.toTotalMinutes() }.joinToString(" • ") { it.formatArabic() }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("5. مراجعة بطاقة الدواء قبل الحفظ", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            Text("اسم الدواء: $name", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (scientificName.isNotBlank()) {
                                Text("الاسم العلمي: $scientificName", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("الشكل والجرعة: $dosageForm - $strength", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("التعليمات: $instruction", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("المواعيد ($timesPerDay مرات يومياً):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(displayTimesAr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandTeal)
                            Text("مدة الاستخدام: $durationWeeks", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            // Stepper Navigation Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("السابق")
                    }
                }
                Button(
                    onClick = {
                        if (currentStep < 5) {
                            if (currentStep == 1 && name.isBlank()) {
                                errorMessage = "يرجى كتابة اسم الدواء للمتابعة"
                                return@Button
                            }
                            errorMessage = null
                            currentStep += 1
                        } else {
                            // Step 5: Save to Database & Activate Exact Alarms
                            coroutineScope.launch {
                                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"
                                val orderedTimes = doseTimesList.sortedBy { it.toTotalMinutes() }
                                val sortedCsv = orderedTimes.joinToString(",") { it.to24hString() }
                                if (orderedTimes.map { it.to24hString() }.distinct().size != orderedTimes.size) {
                                    Toast.makeText(context, "يرجى اختيار أوقات مختلفة لكل جرعة", Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                val startDate = System.currentTimeMillis()
                                val endDate = when (durationWeeks) {
                                    "أسبوع واحد (مضادات حيوية)" -> startDate + 7L * 24 * 60 * 60 * 1000
                                    "أسبوعين" -> startDate + 14L * 24 * 60 * 60 * 1000
                                    "شهر واحد" -> java.util.Calendar.getInstance().apply {
                                        timeInMillis = startDate
                                        add(java.util.Calendar.MONTH, 1)
                                    }.timeInMillis
                                    else -> null
                                }

                                val medId = repository.addMedication(
                                    MedicationEntity(
                                        name = name.trim(),
                                        scientificName = scientificName.trim(),
                                        dosageForm = dosageForm,
                                        strength = strength.trim(),
                                        instructions = instruction,
                                        timesPerDay = timesPerDay,
                                        doseTimesCsv = sortedCsv,
                                        startDateTimestamp = startDate,
                                        endDateTimestamp = endDate,
                                        userId = currentUid
                                    )
                                )

                                // Activate exact alarms immediately in Android AlarmManager
                                repository.scheduleAlarmsForMedication(context, medId)

                                Toast.makeText(context, "تم حفظ الدواء وتفعيل تذكيرات المنبه بنجاح", Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentStep == 5) "حفظ وتفعيل التذكيرات" else "التالي", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal to manually edit hour (1..12) for selected dose
    if (editingDoseIndex != null) {
        val idx = editingDoseIndex!!
        val currentDose = doseTimesList.getOrNull(idx)
        if (currentDose != null) {
            var selectedHour by remember { mutableStateOf(currentDose.hour12) }
            var isPmState by remember { mutableStateOf(currentDose.isPM) }

            AlertDialog(
                onDismissRequest = { editingDoseIndex = null },
                title = { Text("تحديد وقت الجرعة ${idx + 1}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("اختر الساعة (1 إلى 12):", fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            (1..6).forEach { h ->
                                FilterChip(
                                    selected = selectedHour == h,
                                    onClick = { selectedHour = h },
                                    label = { Text(h.toString()) }
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            (7..12).forEach { h ->
                                FilterChip(
                                    selected = selectedHour == h,
                                    onClick = { selectedHour = h },
                                    label = { Text(h.toString()) }
                                )
                            }
                        }
                        Text("الفترة:", fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(
                                selected = !isPmState,
                                onClick = { isPmState = false },
                                label = { Text("صباحاً (ص)") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isPmState,
                                onClick = { isPmState = true },
                                label = { Text("مساءً (م)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updated = doseTimesList.toMutableList()
                            updated[idx] = currentDose.copy(hour12 = selectedHour, isPM = isPmState)
                            doseTimesList = updated.sortedBy { it.toTotalMinutes() }
                            editingDoseIndex = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                    ) {
                        Text("تأكيد الموعد")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingDoseIndex = null }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}
