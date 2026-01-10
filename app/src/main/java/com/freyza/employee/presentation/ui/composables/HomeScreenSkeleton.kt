package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun HomeScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {

        Skeleton(
            Modifier
                .width(300.dp)
                .height(40.dp)
        )
        Skeleton(
            Modifier
                .width(200.dp)
                .height(20.dp)
        )
        Skeleton(
            Modifier
                .width(200.dp)
                .height(20.dp)
        )

        Spacer(Modifier.height(64.dp))

        Skeleton(
            Modifier
                .width(150.dp)
                .height(40.dp)
        )
        Skeleton(
            Modifier
                .width(200.dp)
                .height(10.dp)
        )
        Skeleton(
            Modifier
                .width(200.dp)
                .height(10.dp)
        )

        Spacer(Modifier.height(128.dp))

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false
        ) {
            items(5) {
                Skeleton(
                    Modifier
                        .width(350.dp)
                        .height(50.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }

    }

}


@Preview(showSystemUi = false, showBackground = true)
@Composable
fun HomeScreenSkeletonPreview() {
    FreyzaEmployeeTheme {
        HomeScreenSkeleton(modifier = Modifier.padding(8.dp, top = 16.dp))
    }
}
