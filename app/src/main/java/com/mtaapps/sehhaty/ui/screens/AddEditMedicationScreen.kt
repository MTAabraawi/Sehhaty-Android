package com.mtaapps.sehhaty.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.MedicationEntity
import com.mtaapps.sehhaty.data.repository.MedicationRepository
import com.mtaapps.sehhaty.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationScreen(userId: String, onSaved: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val repository = remember {
        val db = SehhatyApp.instance.database
        MedicationRepository(db, db.medicationDao(), db.doseDao())
    }

    // Step state (1 to 5)
    var currentStep by remember { mutableStateOf(1) }

    // Form inputs
    var name by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var dosageForm by remember { mutableStateOf("أقراص") }
    var strength by remember { mutableStateOf("500 ملغ") }
    var instruction by remember { mutableStateOf("بعد الطعام") }
    var timesPerDay by remember { mutableStateOf("1") }
    var doseTimes by remember { mutableStateOf("08:00") }
    var durationWeeks by remember { mutableStateOf("علاج مستمر") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val forms = listOf("أقراص", "كبسولات", "شراب", "حقن", "قطرة", "بخاخ")
    val instructionsList = listOf("بعد الطعام", "قبل الطعام", "على الريق", "مع الطعام", "عند النوم")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة دواء وجدولة التذكيرات", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onSaved) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandNavy,
                    titleContentColor = Color.White
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
            // Wizard Stepper Progress Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الخطوة $currentStep من 5", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandTeal)
                        Text(
                            when (currentStep) {
                                1 -> "بيانات الدواء الأساسية"
                                2 -> "التركيز وتعليمات الأكل"
                                3 -> "مواعيد الجرعات"
                                4 -> "مدة العلاج"
                                else -> "مراجعة وحفظ"
                            },
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { currentStep / 5f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = BrandTeal,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }
            }

            // Step Content
            when (currentStep) {
                1 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. اسم وشكل الدواء", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("اسم الدواء التجاري (مثل: بنادول، كونكور)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = scientificName,
                                onValueChange = { scientificName = it },
                                label = { Text("الاسم العلمي أو المادة الفعالة (اختياري)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Text("الشكل الصيدلاني:", fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                forms.take(3).forEach { form ->
                                    FilterChip(
                                        selected = dosageForm == form,
                                        onClick = { dosageForm = form },
                                        label = { Text(form, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("2. الجرعة وتعليمات التناول", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            OutlinedTextField(
                                value = strength,
                                onValueChange = { strength = it },
                                label = { Text("التركيز والجرعة (مثل: 500 ملغ - قرص واحد)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Text("تعليمات الوجبات:", fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                instructionsList.take(3).forEach { inst ->
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
                3 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("3. عدد الجرعات والمواعيد", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            OutlinedTextField(
                                value = timesPerDay,
                                onValueChange = { timesPerDay = it },
                                label = { Text("عدد المرات يومياً") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = doseTimes,
                                onValueChange = { doseTimes = it },
                                label = { Text("المواعيد (مفصولة بفاصلة مثل: 08:00, 20:00)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Text("التنبيه سيرن بدقة عبر AlarmManager ويتكرر كل دقيقتين حتى تأكيد الجرعة.", fontSize = 11.sp, color = BrandTeal)
                        }
                    }
                }
                4 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("4. مدة العلاج", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            listOf("علاج مستمر (أمراض مزمنة)", "أسبوع واحد (مضادات حيوية)", "أسبوعين", "شهر واحد").forEach { dur ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (durationWeeks == dur) BgContainerTeal else Color.Transparent)
                                        .clickable { durationWeeks = dur }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(selected = durationWeeks == dur, onClick = { durationWeeks = dur })
                                    Text(dur, fontSize = 13.sp, color = TextDark)
                                }
                            }
                        }
                    }
                }
                5 -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("5. مراجعة بطاقة الدواء قبل الحفظ", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Divider(color = Color(0xFFF1F5F9))
                            Text("اسم الدواء: $name", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("الشكل والتركيز: $dosageForm - $strength", fontSize = 13.sp, color = TextMuted)
                            Text("التعليمات: $instruction", fontSize = 13.sp, color = TextMuted)
                            Text("المواعيد: $timesPerDay مرات يومياً ($doseTimes)", fontSize = 13.sp, color = BrandTeal)
                            Text("مدة الاستخدام: $durationWeeks", fontSize = 13.sp, color = TextMuted)
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
                        modifier = Modifier.weight(1f).height(48.dp),
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
                            val entries = doseTimes.split(",").map { it.trim() }
                            val validTime = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")

                            if (entries.isEmpty() || entries.any { it.isEmpty() || !validTime.matches(it) }) {
                                errorMessage = "وقت الجرعة غير صحيح. استخدم صيغة HH:mm مثل 08:00"
                                return@Button
                            }

                            if (entries.distinct().size != entries.size) {
                                errorMessage = "لا يمكن تكرار موعد الجرعة نفسه"
                                return@Button
                            }

                            errorMessage = null
                            coroutineScope.launch {
                                repository.addMedication(
                                    MedicationEntity(
                                        userId = userId,
                                        name = name.trim(),
                                        scientificName = scientificName.trim(),
                                        dosageForm = dosageForm,
                                        strength = strength.trim(),
                                        instructions = instruction,
                                        timesPerDay = timesPerDay.toIntOrNull() ?: 1,
                                        doseTimesCsv = doseTimes.trim()
                                    ),
                                    userId
                                )
                                onSaved()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentStep == 5) "حفظ وتفعيل التذكيرات" else "التالي", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
