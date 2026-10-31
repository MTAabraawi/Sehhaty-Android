package com.mtaapps.sehhaty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtaapps.sehhaty.SehhatyApp
import com.mtaapps.sehhaty.data.repository.MedicationRepository
import com.mtaapps.sehhaty.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoseHistoryScreen(userId: String) {
    val repository = remember {
        val db = SehhatyApp.instance.database
        MedicationRepository(db, db.medicationDao(), db.doseDao())
    }

    val historyLogs by repository.getDoseHistory(userId).collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل الجرعات السابقة", fontWeight = FontWeight.Bold) },
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
            if (historyLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا يوجد سجل جرعات مسجلة حتى الآن", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(historyLogs) { log ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.medicationName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("الجرعة: ${log.doseStrength}", fontSize = 13.sp, color = TextSecondary)
                                    Text("وقت التأكيد: ${dateFormat.format(Date(log.confirmedTimeTimestamp))}", fontSize = 11.sp, color = TealDark)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess)
                            }
                        }
                    }
                }
            }
        }
    }
}
