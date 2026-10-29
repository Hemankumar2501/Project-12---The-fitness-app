package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.data.model.MealEntry
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: AppViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.foodSearchQuery.collectAsState()

    var showAddFoodSheet by remember { mutableStateOf(false) }
    var selectedMealCategory by remember { mutableStateOf("Breakfast") }
    var showCustomFoodDialog by remember { mutableStateOf(false) }

    // Daily totals calculated strictly from saved items
    val totalCalories = todayMeals.sumOf { it.calories }.toInt()
    val totalProtein = todayMeals.sumOf { it.protein }.toInt()
    val totalCarbs = todayMeals.sumOf { it.carbs }.toInt()
    val totalFat = todayMeals.sumOf { it.fat }.toInt()
    val totalFibre = todayMeals.sumOf { it.fibre }.toInt()

    val targetCalories = profile?.dailyCaloriesTarget ?: 2100
    val targetProtein = profile?.dailyProteinTarget ?: 150

    val mealCategories = listOf("Breakfast", "Mid-Morning Snack", "Lunch", "Evening Snack", "Dinner", "Other")

    Scaffold(
        containerColor = ProjectBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nutrition & Macros",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { showCustomFoodDialog = true }) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Add Custom Food", tint = NeonLime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProjectBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddFoodSheet = true },
                containerColor = NeonLime,
                contentColor = Color.Black,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Food", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("log_food_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
        ) {
            // Summary Dashboard Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ProjectSurfaceElevated),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "DAILY CALORIES", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, letterSpacing = 1.sp))
                                Text(
                                    text = "$totalCalories / $targetCalories kcal",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                )
                            }
                            val remainingCal = (targetCalories - totalCalories).coerceAtLeast(0)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonLimeContainer)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$remainingCal kcal left",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeonLime, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (totalCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = NeonLime,
                            trackColor = ProjectBorder
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macros Breakdown Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroMetric(name = "Protein", value = "${totalProtein}g", target = "${targetProtein}g", color = NeonLime)
                            MacroMetric(name = "Carbs", value = "${totalCarbs}g", target = "220g", color = AccentBlue)
                            MacroMetric(name = "Fat", value = "${totalFat}g", target = "60g", color = AccentOrange)
                            MacroMetric(name = "Fibre", value = "${totalFibre}g", target = "30g", color = AccentPurple)
                        }
                    }
                }
            }

            // Meal Categories with logged items
            mealCategories.forEach { category ->
                val categoryMeals = todayMeals.filter { it.mealType.equals(category, ignoreCase = true) }
                item {
                    MealCategorySection(
                        categoryName = category,
                        meals = categoryMeals,
                        onAddFoodClick = {
                            selectedMealCategory = category
                            showAddFoodSheet = true
                        },
                        onDeleteMeal = { meal -> viewModel.deleteMeal(meal) }
                    )
                }
            }
        }
    }

    // Add Food Bottom Sheet with Indian Food Library
    if (showAddFoodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddFoodSheet = false },
            containerColor = ProjectSurfaceElevated
        ) {
            AddFoodSheetContent(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.foodSearchQuery.value = it },
                foods = searchResults,
                selectedCategory = selectedMealCategory,
                onCategoryChange = { selectedMealCategory = it },
                onAddFood = { food, portion ->
                    viewModel.addMeal(food, portion, selectedMealCategory)
                    showAddFoodSheet = false
                },
                onCreateCustomFoodClick = {
                    showAddFoodSheet = false
                    showCustomFoodDialog = true
                }
            )
        }
    }

    // Custom Food Creator Dialog
    if (showCustomFoodDialog) {
        CustomFoodDialog(
            onDismiss = { showCustomFoodDialog = false },
            onSave = { name, cat, cals, protein, carbs, fat, fibre, isVeg ->
                viewModel.addCustomFoodItem(name, cat, cals, protein, carbs, fat, fibre, isVeg)
                showCustomFoodDialog = false
            }
        )
    }
}

@Composable
private fun MacroMetric(name: String, value: String, target: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold))
        Text(text = "goal: $target", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp))
    }
}

@Composable
private fun MealCategorySection(
    categoryName: String,
    meals: List<MealEntry>,
    onAddFoodClick: () -> Unit,
    onDeleteMeal: (MealEntry) -> Unit
) {
    val cals = meals.sumOf { it.calories }.toInt()
    val protein = meals.sumOf { it.protein }.toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ProjectSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProjectBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $cals kcal ($protein g protein)",
                        style = MaterialTheme.typography.bodySmall.copy(color = NeonLime)
                    )
                }

                IconButton(
                    onClick = onAddFoodClick,
                    modifier = Modifier.size(32.dp).testTag("add_to_$categoryName")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Food", tint = NeonLime)
                }
            }

            if (meals.isEmpty()) {
                Text(
                    text = "No items logged yet. Tap + to add from the Indian food library.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                meals.forEach { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${meal.foodName} (${meal.portion} ${meal.unit})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${meal.calories.toInt()} kcal • ${meal.protein.toInt()}g protein • ${meal.carbs.toInt()}g carbs • ${meal.fat.toInt()}g fat",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteMeal(meal) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AccentRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFoodSheetContent(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    foods: List<FoodItem>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onAddFood: (FoodItem, Double) -> Unit,
    onCreateCustomFoodClick: () -> Unit
) {
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var portionMultiplier by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Add Food to $selectedCategory",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            label = { Text("Search Indian food library...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonLime) },
            modifier = Modifier.fillMaxWidth().testTag("food_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonLime,
                unfocusedBorderColor = ProjectBorder
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedFood != null) {
            val food = selectedFood!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NeonLimeContainer),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Selected: ${food.name}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Serving: ${(portionMultiplier * food.servingSize).toInt()} ${food.servingUnit} • ${(food.calories * portionMultiplier).toInt()} kcal • ${(food.protein * portionMultiplier).toInt()}g protein",
                        style = MaterialTheme.typography.bodySmall.copy(color = NeonLime)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Portion: ${"%.1f".format(portionMultiplier)}x", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        Slider(
                            value = portionMultiplier,
                            onValueChange = { portionMultiplier = it },
                            valueRange = 0.5f..5.0f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = NeonLime, activeTrackColor = NeonLime)
                        )
                    }
                    Button(
                        onClick = { onAddFood(food, portionMultiplier.toDouble()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black)
                    ) {
                        Text("Confirm & Log Meal", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Indian & South Indian Library", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            TextButton(onClick = onCreateCustomFoodClick) {
                Text("+ Custom Food", color = NeonLime, style = MaterialTheme.typography.labelSmall)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foods) { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ProjectSurface)
                        .clickable { selectedFood = food }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = food.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                            if (food.isVegetarian) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                            }
                        }
                        Text(
                            text = "${food.servingUnit} • ${food.calories.toInt()} kcal • ${food.protein.toInt()}g protein • ${food.carbs.toInt()}g carbs",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Select", tint = NeonLime)
                }
            }
        }
    }
}

@Composable
private fun CustomFoodDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double, Double, Double, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("150") }
    var proteinText by remember { mutableStateOf("10") }
    var carbsText by remember { mutableStateOf("15") }
    var fatText by remember { mutableStateOf("5") }
    var fibreText by remember { mutableStateOf("2") }
    var isVeg by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProjectSurfaceElevated,
        title = { Text("Create Custom Food", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it },
                        label = { Text("Calories") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            "Custom",
                            caloriesText.toDoubleOrNull() ?: 0.0,
                            proteinText.toDoubleOrNull() ?: 0.0,
                            carbsText.toDoubleOrNull() ?: 0.0,
                            fatText.toDoubleOrNull() ?: 0.0,
                            fibreText.toDoubleOrNull() ?: 0.0,
                            isVeg
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black)
            ) {
                Text("Save to Library", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
