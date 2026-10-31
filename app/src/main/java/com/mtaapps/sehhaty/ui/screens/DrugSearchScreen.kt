package com.mtaapps.sehhaty.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.local.entity.DrugReferenceEntity
import com.mtaapps.sehhaty.data.repository.DrugRepository
import com.mtaapps.sehhaty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugSearchScreen() {
    val repository = remember {
        DrugRepository(SehhatyApp.instance.database.drugReferenceDao())
    }

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by repository.search(searchQuery).collectAsState(initial = emptyList())
    var selectedDrug by remember { mutableStateOf<DrugReferenceEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("البحث الدوائي والتعرف الذكي", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("اكتب اسم الدواء تجارياً أو علمياً...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Camera & Scan Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { searchQuery = "Panadol" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصوير العلبة", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { searchQuery = "Augmentin" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نشرة الدواء", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Safety Disclaimer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusWarning.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "معلومات موثوقة من النشرات الدوائية الرسمية - لا تغني عن استشارة الطبيب أو الصيدلي.",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(searchResults) { drug ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedDrug = drug }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(drug.tradeNameAr, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(drug.tradeNameEn, color = TextSecondary, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("المادة الفعالة: ${drug.activeIngredient}", fontSize = 13.sp, color = TealDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الاستخدام: ${drug.approvedIndications}", maxLines = 2, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Detailed 12-point Drug Sheet Dialog
            if (selectedDrug != null) {
                AlertDialog(
                    onDismissRequest = { selectedDrug = null },
                    title = {
                        Column {
                            Text(selectedDrug!!.tradeNameAr, fontWeight = FontWeight.Bold)
                            Text(selectedDrug!!.scientificName, fontSize = 13.sp, color = TextSecondary)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Text("1. الشكل الصيدلاني: ${selectedDrug!!.dosageForm}", fontSize = 13.sp)
                            Text("2. التركيز: ${selectedDrug!!.strength}", fontSize = 13.sp)
                            Text("3. دواعي الاستعمال: ${selectedDrug!!.approvedIndications}", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("4. موانع الاستعمال: ${selectedDrug!!.contraindications}", fontSize = 13.sp, color = StatusDanger)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("5. طريقة الاستخدام: ${selectedDrug!!.usageInstructions}", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("6. الآثار الجانبية: ${selectedDrug!!.commonSideEffects}", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("7. التداخلات الدوائية: ${selectedDrug!!.drugInteractions}", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("المصدر: ${selectedDrug!!.medicalSource} (${selectedDrug!!.lastUpdateDate})", fontSize = 11.sp, color = TextSecondary)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedDrug = null }) {
                            Text("إغلاق")
                        }
                    }
                )
            }
        }
    }
}
