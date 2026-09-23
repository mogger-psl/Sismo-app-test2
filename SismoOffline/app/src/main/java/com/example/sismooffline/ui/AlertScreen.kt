package com.example.sismooffline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlertScreen(
    movement: Float,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("⚠️", fontSize = 64.sp)
        Text(
            "MOVIMIENTO DETECTADO",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9A0007),
            textAlign = TextAlign.Center
        )
        Text(
            "Nivel de movimiento registrado: %.2f m/s²".format(movement),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            "⚠️ Varios celulares han registrado temblores cerca de tu zona.\n\n" +
                "Acciones recomendadas:\n" +
                "• preparar una mochila con suministros no perecederos.\n" +
                "• reunir familiares y personas cercanas a ti.\n" +
                "• evitar balcones, fachadas de edificios y cualquier estructura que pueda colapsar.\n" +
                "• si es posible, busque un lugar abierto sin estructuras que puedan colapsar y causar accidentes.\n" +
                "• durante el movimiento, agáchese, cúbrase y sujétese.\n\n" +
                "MANTENGA LA CALMA",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 26.sp
        )

        Button(onClick = onDismiss) {
            Text("Entendido")
        }

        Text(
            "DEMO ESCOLAR: detecta movimiento del teléfono; no predice terremotos.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}
