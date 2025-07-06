package com.example.cube_solver.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class RubiksCubeColors(val colorName: String, val colorCode: Color) {
    RED("Red", Color(0xFFFF0000)),
    GREEN("Green", Color(0xFF00FF00)),
    BLUE("Blue", Color(0xFF0000FF)),
    YELLOW("Yellow", Color(0xFFF1F117)),
    WHITE("White", Color(0xFFFFFFFF)),
    ORANGE("Orange", Color(0xFFFFA500))
}

@Composable
fun ColorPicker(modifier: Modifier = Modifier, onPick: (RubiksCubeColors) -> Unit = {}) {
    var currentSelectedPosition by rememberSaveable { mutableIntStateOf(0) }
    LazyRow(modifier) {
        items(RubiksCubeColors.entries.size) { index ->
            RawColorPick(
                color = RubiksCubeColors.entries[index],
                currentSelectedPosition == index
            ) {
                currentSelectedPosition = index
                onPick(RubiksCubeColors.entries[currentSelectedPosition])
            }
        }
    }
}

@Composable
fun RawColorPick(
    color: RubiksCubeColors = RubiksCubeColors.RED,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(end = 10.dp)
            .size(50.dp),
        colors = CardDefaults.cardColors().copy(containerColor = Color(0xFF483E3E)),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(25.dp),
                shape = CircleShape,
                color = color.colorCode.takeIf { isSelected.not() }
                    ?: color.colorCode.copy(alpha = 0.5f),
            ) {}

            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                if (isSelected) {
                    Surface(
                        modifier = Modifier.size(35.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Filled.Done,
                                tint = Color.LightGray,
                                contentDescription = "Arrow Right"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun RawColorPickPreview() {
    ColorPicker {

    }
}
