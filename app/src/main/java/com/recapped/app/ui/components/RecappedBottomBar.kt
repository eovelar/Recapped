package com.recapped.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recapped.app.ui.theme.BrandGradient
import com.recapped.app.ui.theme.RecappedColors

/**
 * Top-level tabs de la app. Cada uno mapea a una ruta del NavGraph.
 * Mantengo orden y labels igual que la mock para que la nav sea consistente.
 */
enum class RecappedTab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Filled.Home),
    Charts("charts", "Charts", Icons.Filled.BarChart),
    Recap("recap_gen", "Recap", Icons.Filled.Adjust),
    Profile("profile", "Perfil", Icons.Filled.Person);

    companion object {
        fun fromRoute(route: String?): RecappedTab? =
            entries.firstOrNull { it.route == route }
    }
}

/**
 * Barra inferior fija. Se renderiza desde el Scaffold del NavGraph y sólo
 * aparece cuando el back-stack está parado en una ruta de tab (no en Detail).
 */
@Composable
fun RecappedBottomBar(
    current: RecappedTab?,
    onSelect: (RecappedTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE6080808)) // bg 88% — simula el blur translúcido de la mock
    ) {
        // Hairline top — replica el `borderTop: 0.5px` de la mock
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(RecappedColors.Border)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecappedTab.entries.forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: RecappedTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Accent underline arriba — sólo en activo
            if (selected) {
                Box(
                    Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BrandGradient)
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = if (selected) RecappedColors.BrandOrange else RecappedColors.Dim,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = tab.label,
                color = if (selected) RecappedColors.BrandOrange else RecappedColors.Dim,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
