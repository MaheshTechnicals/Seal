package com.junkfood.seal.ui.icons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PlusIcon() {

    Text(
        text = "+",
        style = MaterialTheme.typography.displayMedium.merge(
            TextStyle(
                color = Color.Magenta
            )
        ),
        fontWeight = FontWeight.Bold
    )
}