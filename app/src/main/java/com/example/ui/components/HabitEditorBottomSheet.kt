package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.AppDate
import com.example.model.Habit
import com.example.model.HabitCategory
import com.example.model.HabitFrequency
import com.example.model.HabitIcon
import com.example.model.RoutinePeriod
import com.example.model.formatTime12Hour
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditorBottomSheet(
    editingHabit: Habit?,
    onSave: (Habit, isEdit: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editingHabit != null

    var title by remember { mutableStateOf(editingHabit?.title ?: "") }
    var description by remember { mutableStateOf(editingHabit?.description ?: "") }
    var selectedIcon by remember { mutableStateOf(editingHabit?.icon ?: HabitIcon.CHECK_CIRCLE) }
    var selectedCategory by remember { mutableStateOf(editingHabit?.category ?: HabitCategory.HEALTH) }
    var selectedRoutine by remember { mutableStateOf(editingHabit?.routinePeriod ?: RoutinePeriod.MORNING) }
    var selectedFrequency by remember { mutableStateOf(editingHabit?.frequency ?: HabitFrequency.DAILY) }
    var selectedDays by remember {
        mutableStateOf(editingHabit?.scheduledDays ?: setOf(1, 2, 3, 4, 5, 6, 7))
    }
    var reminderEnabled by remember { mutableStateOf(editingHabit?.reminderEnabled ?: false) }
    var reminderTime by remember { mutableStateOf(editingHabit?.reminderTime ?: "08:00") }
    var startDate by remember { mutableStateOf(editingHabit?.startDate ?: AppDate.today()) }
    var durationText by remember {
        mutableStateOf(editingHabit?.targetDurationMinutes?.toString() ?: "")
    }

    var titleError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEdit) "Edit Habit" else "Create New Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Habit Name
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Habit Name *") },
                placeholder = { Text("e.g., Morning Hydration, Deep Work") },
                singleLine = true,
                isError = titleError != null,
                supportingText = titleError?.let { err -> { Text(err) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_name_input")
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                placeholder = { Text("Why does this habit matter?") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_description_input")
            )

            // Icon Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitIcon.entries.forEach { icon ->
                        val selected = icon == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) selectedCategory.accentColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) selectedCategory.accentColor
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = icon }
                                .testTag("icon_option_${icon.key}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon.vector,
                                contentDescription = icon.label,
                                tint = if (selected) selectedCategory.accentColor
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Category Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HabitCategory.entries.forEach { category ->
                        val selected = category == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(category.accentColor)
                                )
                            }
                        )
                    }
                }
            }

            // Routine Period
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Daily Routine Slot",
                    style = MaterialTheme.typography.titleSmall
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RoutinePeriod.entries.forEach { period ->
                        FilterChip(
                            selected = period == selectedRoutine,
                            onClick = { selectedRoutine = period },
                            label = { Text(period.displayName.removeSuffix(" Routine")) }
                        )
                    }
                }
            }

            // Frequency & Weekdays
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Frequency & Schedule",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitFrequency.entries.forEach { freq ->
                        FilterChip(
                            selected = freq == selectedFrequency,
                            onClick = {
                                selectedFrequency = freq
                                daysError = null
                                selectedDays = when (freq) {
                                    HabitFrequency.DAILY -> setOf(1, 2, 3, 4, 5, 6, 7)
                                    HabitFrequency.WEEKDAYS -> setOf(1, 2, 3, 4, 5)
                                    HabitFrequency.CUSTOM -> selectedDays.ifEmpty { setOf(1, 3, 5) }
                                }
                            },
                            label = { Text(freq.displayName) }
                        )
                    }
                }

                val dayLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEach { (isoDay, label) ->
                        val active = isoDay in selectedDays
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable {
                                    daysError = null
                                    val next = if (active) selectedDays - isoDay else selectedDays + isoDay
                                    selectedDays = next
                                    selectedFrequency = when {
                                        next.size == 7 -> HabitFrequency.DAILY
                                        next == setOf(1, 2, 3, 4, 5) -> HabitFrequency.WEEKDAYS
                                        else -> HabitFrequency.CUSTOM
                                    }
                                },
                            shape = CircleShape,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (daysError != null) {
                    Text(
                        text = daysError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Reminder Time & Start Date Row
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Habit Reminder", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = if (reminderEnabled) "Notify at ${formatTime12Hour(reminderTime)}"
                                    else "Scheduled slot: ${formatTime12Hour(reminderTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Time: ${formatTime12Hour(reminderTime)}")
                        }
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(startDate.formatShortMonthDay())
                        }
                    }
                }
            }

            // Optional Target Duration
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Target Duration (optional)",
                    style = MaterialTheme.typography.titleSmall
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(null to "None", 10 to "10m", 15 to "15m", 20 to "20m", 30 to "30m", 45 to "45m", 60 to "60m").forEach { (mins, label) ->
                        val currentMins = durationText.toIntOrNull()
                        val selected = (mins == null && durationText.isBlank()) || (mins != null && currentMins == mins)
                        FilterChip(
                            selected = selected,
                            onClick = { durationText = mins?.toString() ?: "" },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            durationText = input
                        }
                    },
                    label = { Text("Custom minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val trimmedName = title.trim()
                        if (trimmedName.isEmpty()) {
                            titleError = "Habit name is required"
                            return@Button
                        }
                        if (selectedDays.isEmpty()) {
                            daysError = "Select at least one weekday"
                            return@Button
                        }
                        val parsedDuration = durationText.toIntOrNull()?.takeIf { it > 0 }
                        val now = System.currentTimeMillis()
                        val habit = Habit(
                            id = editingHabit?.id ?: UUID.randomUUID().toString(),
                            title = trimmedName,
                            description = description.trim(),
                            category = selectedCategory,
                            routinePeriod = selectedRoutine,
                            icon = selectedIcon,
                            frequency = selectedFrequency,
                            scheduledDays = selectedDays,
                            reminderTime = reminderTime,
                            reminderEnabled = reminderEnabled,
                            startDate = startDate,
                            targetDurationMinutes = parsedDuration,
                            isActive = editingHabit?.isActive ?: true,
                            isStarterRoutine = editingHabit?.isStarterRoutine ?: false,
                            sortOrder = editingHabit?.sortOrder ?: 0,
                            createdAtEpochMs = editingHabit?.createdAtEpochMs ?: now,
                            updatedAtEpochMs = now
                        )
                        onSave(habit, isEdit)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_habit_button")
                ) {
                    Text(if (isEdit) "Update Habit" else "Save Habit")
                }
            }
        }
    }

    if (showTimePicker) {
        HabitTimePickerDialog(
            initialTime24 = reminderTime,
            onConfirm = { newTime ->
                reminderTime = newTime
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showDatePicker) {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(startDate.year, startDate.month - 1, startDate.day, 0, 0, 0)
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = utcCal.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val pickedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        startDate = AppDate(
                            year = pickedCal.get(Calendar.YEAR),
                            month = pickedCal.get(Calendar.MONTH) + 1,
                            day = pickedCal.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTimePickerDialog(
    initialTime24: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = initialTime24.split(":")
    val initHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val initMin = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val state = rememberTimePickerState(initialHour = initHour, initialMinute = initMin, is24Hour = false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", style = MaterialTheme.typography.titleLarge) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(String.format(Locale.US, "%02d:%02d", state.hour, state.minute))
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
