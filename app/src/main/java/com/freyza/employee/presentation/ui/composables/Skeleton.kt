package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun Skeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(15))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SkeletonPreview() {
    FreyzaEmployeeTheme {
        Skeleton(modifier = Modifier
            .width(100.dp)
            .height(25.dp))
    }
}
