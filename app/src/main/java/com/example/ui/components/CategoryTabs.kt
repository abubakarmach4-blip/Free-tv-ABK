package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelCategories
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        content = {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                CategoryPill(
                    title = category,
                    isSelected = isSelected,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    )
}

@Composable
private fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) CyanNeon else SurfaceDark
    val textColor = if (isSelected) SurfaceDark else TextPrimary
    val borderColor = if (isSelected) CyanNeon else BorderGlass

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("category_pill_${title.replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val emojiOrIcon = getCategoryEmoji(title)
            if (emojiOrIcon != null) {
                Text(
                    text = emojiOrIcon,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
            } else {
                val icon = getCategoryIcon(title)
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) SurfaceDark else TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                }
            }

            Text(
                text = title,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

private fun getCategoryEmoji(category: String): String? {
    return when (category) {
        ChannelCategories.PAKISTAN -> "🇵🇰"
        ChannelCategories.INDIA -> "🇮🇳"
        else -> null
    }
}

private fun getCategoryIcon(category: String): ImageVector? {
    return when (category) {
        ChannelCategories.ALL -> Icons.Filled.Tv
        ChannelCategories.SPORTS -> Icons.Filled.SportsSoccer
        ChannelCategories.NEWS -> Icons.Filled.Newspaper
        ChannelCategories.DOCUMENTARIES -> Icons.Filled.Movie
        ChannelCategories.WILDLIFE -> Icons.Filled.Pets
        ChannelCategories.SCIENCE -> Icons.Filled.School
        ChannelCategories.ENTERTAINMENT -> Icons.Filled.LiveTv
        ChannelCategories.KIDS -> Icons.Filled.ChildCare
        ChannelCategories.INTERNATIONAL -> Icons.Filled.Public
        else -> null
    }
}
