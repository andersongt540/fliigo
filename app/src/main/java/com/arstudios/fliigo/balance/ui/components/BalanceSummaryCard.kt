package com.arstudios.fliigo.balance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import java.util.Locale

@Composable
fun BalanceSummaryCard(
    balance: Double,
    totalIncome: Double,
    totalExpenses: Double,
    textoOscuro: Color,
    verdeExito: Color,
    rojoGasto: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.label_today_utility),
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$ ${String.format(Locale.US, "%.1f", balance)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoOscuro
                )
            }

            Box(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp)
                    .background(Color.LightGray)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟢 ", fontSize = 10.sp)
                    Column {
                        Text(stringResource(R.string.total_sales), fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "$ ${String.format(Locale.US, "%.1f", totalIncome)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = verdeExito
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔴 ", fontSize = 10.sp)
                    Column {
                        Text(stringResource(R.string.total_expenses), fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "$ ${String.format(Locale.US, "%.1f", totalExpenses)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = rojoGasto
                        )
                    }
                }
            }
        }
    }
}