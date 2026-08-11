package com.arstudios.fliigo.balance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R

@Composable
fun BalanceHeader(
    storeTitle: String,
    amarilloHeader: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.profile_desc), fontSize = 20.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = storeTitle.uppercase(),
                color = amarilloHeader,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.owner_role),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.help_desc), fontSize = 20.sp)
            Text(stringResource(R.string.notifications_desc), fontSize = 20.sp)
        }
    }
}