package com.example.products.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.designsystem.RFGray
import com.example.products.presentation.model.CategoryUi

@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    categoryList: List<CategoryUi> = emptyList(),
) {
    var selectedCategoryId by remember { mutableStateOf(categoryList.first().id) }

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoryList) { category ->
            CategoryItem(
                name = category.name,
                isSelected = category.id == selectedCategoryId
            ) {
                selectedCategoryId = category.id
            }
        }
    }
}

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        modifier = modifier
            .background(color = if (isSelected) MaterialTheme.colorScheme.primary else RFGray, shape = RoundedCornerShape(12.dp))
            .clickable(
                onClick = {
                    onClick.invoke()
                }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        text = name,
        fontSize = 16.sp,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondary,
        style = TextStyle(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun CategoriesListPreview() {
    CategoriesList(
        modifier = Modifier.padding(6.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryItemPreview() {
    CategoryItem(
        modifier = Modifier.padding(4.dp),
        name = "Sneakers",
        isSelected = true,
        onClick = {}
    )
}