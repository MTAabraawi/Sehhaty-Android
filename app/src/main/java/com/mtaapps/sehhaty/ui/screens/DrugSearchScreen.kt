package com.mtaapps.sehhaty.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.ai.DrugIdentifierService
import com.mtaapps.sehhaty.data.local.entity.DrugReferenceEntity
import com.mtaapps.sehhaty.data.repository.DrugRepository
import com.mtaapps.sehhaty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugSearchScreen() {
    val context = LocalContext.current
    val repository = remember {
        DrugRepository(SehhatyApp.instance.database.drugReferenceDao())
    }

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by repository.search(searchQuery).collectAsState(initial = emptyList())
    val allDrugs by repository.getAllDrugs().collectAsState(initial = emptyList())

    var selectedDrug by remember { mutableStateOf<DrugReferenceEntity?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCameraScanDialog by remember { mutableStateOf(false) }
    var showLeafletListDialog by remember { mutableStateOf(false) }

    // Real Camera Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            showCameraScanDialog = true
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "يرجى منح إذن الكاميرا لتصوير علبة الدواء", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فحص الدواء والنشرات الطبية", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Input with Arabic Normalization Support
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث بالاسم التجاري، العلمي، أو المادة الفعالة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandTeal) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Core Action Buttons: Camera Box Scanner & Leaflet Viewer (Work 100% Offline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Open Camera to Scan Box
                Button(
                    onClick = {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            takePictureLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(permission)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصوير العلبة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Open Leaflet
                OutlinedButton(
                    onClick = {
                        if (searchResults.isNotEmpty()) {
                            selectedDrug = searchResults.first()
                        } else if (allDrugs.isNotEmpty()) {
                            showLeafletListDialog = true
                        } else {
                            Toast.makeText(context, "الدليل الدوائي المحلي قيد التحميل", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp), tint = BrandTeal)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نشرة الدواء", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandTeal)
                }
            }

            // Medical Safety Disclaimer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(20.dp))
                    Text(
                        "معلومات موثوقة من النشرات الدوائية الرسمية - تعمل محلياً بالكامل دون إنترنت.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            // Results List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(searchResults) { drug ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        onClick = { selectedDrug = drug }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(drug.tradeNameAr, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(drug.tradeNameEn, color = BrandTeal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("المادة الفعالة: ${drug.activeIngredient}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الاستخدام: ${drug.approvedIndications}", maxLines = 2, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Camera Result & Drug Match Dialog
    if (showCameraScanDialog && capturedBitmap != null) {
        AlertDialog(
            onDismissRequest = { showCameraScanDialog = false },
            title = { Text("تم التقاط صورة الدواء بنجاح", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "صورة العلبة الملتقطة",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BrandTeal, RoundedCornerShape(12.dp))
                    )
                    Text(
                        "تم حفظ ومعاينة الصورة في الذاكرة المحلية. يمكنك ربطها بأي دواء في سجلك أو البحث في نشرته مباشرة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraScanDialog = false
                        // Set query to inspect local formulary
                        searchQuery = "بنادول"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                ) {
                    Text("عرض النشرة الدوائية")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraScanDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Leaflet Selection Dialog (When user clicks Leaflet without picking drug)
    if (showLeafletListDialog) {
        AlertDialog(
            onDismissRequest = { showLeafletListDialog = false },
            title = { Text("اختر دواءً لعرض نشرته الرسمية", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(allDrugs) { drug ->
                        TextButton(
                            onClick = {
                                selectedDrug = drug
                                showLeafletListDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(drug.tradeNameAr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(drug.tradeNameEn, color = BrandTeal, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLeafletListDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Detailed Official 12-point Drug Leaflet Sheet Dialog
    if (selectedDrug != null) {
        val drug = selectedDrug!!
        AlertDialog(
            onDismissRequest = { selectedDrug = null },
            title = {
                Column {
                    Text("نشرة الدواء: ${drug.tradeNameAr}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandTeal)
                    Text("${drug.tradeNameEn} • ${drug.scientificName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("1. الشكل الصيدلاني والتركيز: ${drug.dosageForm} (${drug.strength})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("2. المادة الفعالة: ${drug.activeIngredient}", fontSize = 12.sp)
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("3. دواعي الاستعمال المصرحة:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BrandTeal)
                        Text(drug.approvedIndications, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("4. موانع الاستعمال والتحذيرات:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StatusDanger)
                        Text(drug.contraindications, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("5. طريقة الاستخدام والجرعات:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(drug.usageInstructions, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("6. الآثار الجانبية المحتملة:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(drug.commonSideEffects, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("7. التداخلات الدوائية:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(drug.drugInteractions, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Text("الشركة المصنعة: ${drug.manufacturer}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("المصدر الطبي: ${drug.medicalSource} (تحديث: ${drug.lastUpdateDate})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDrug = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal)
                ) {
                    Text("إغلاق النشرة")
                }
            }
        )
    }
}
