package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.HabitAnalyticsEngine
import com.example.model.AppDate
import com.example.model.Habit
import com.example.model.WeekStartDay
import com.example.ui.LifeflowUiState
import com.example.ui.components.HabitScheduleCard
import com.example.ui.theme.CoralMissed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary

@Composable
fun CalendarScreen(
    state: LifeflowUiState,
    onSelectDate: (AppDate) -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onToggleCompletion: (Habit, AppDate, Boolean) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onToggleActive: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate = state.selectedCalendarDate
    val scheduledForSelected = remember(state.habits, state.completions, selectedDate, state.today) {
        HabitAnalyticsEngine.scheduledHabitsForDate(
            state.habits,
            state.completions,
            selectedDate,
            state.today
        )
    }
    val completedIdsForSelected = remember(state.completions, selectedDate) {
        HabitAnalyticsEngine.completedHabitIdsOnDate(state.completions, selectedDate)
    }
    val selectedDayProgress = remember(state.habits, state.completions, selectedDate, state.today) {
        HabitAnalyticsEngine.dailyProgressForDate(
            state.habits,
            state.completions,
            selectedDate,
            state.today
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        CalendarHeaderAndGrid(
                            state = state,
                            onSelectDate = onSelectDate,
                            onNavigateMonth = onNavigateMonth,
                            onJumpToToday = onJumpToToday
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SelectedDateHeader(
                            selectedDate = selectedDate,
                            today = state.today,
                            completedCount = selectedDayProgress.completedCount,
                            scheduledCount = selectedDayProgress.scheduledCount,
                            percent = selectedDayProgress.completionPercent
                        )
                    }
                    if (scheduledForSelected.isEmpty()) {
                        item { EmptyDayNotice() }
                    } else {
                        items(items = scheduledForSelected, key = { it.id }) { habit ->
                            val isDone = habit.id in completedIdsForSelected
                            HabitScheduleCard(
                                habit = habit,
                                isCompleted = isDone,
                                onToggleComplete = { onToggleCompletion(habit, selectedDate, isDone) },
                                onEdit = { onEditHabit(habit) },
                                onDelete = { onDeleteHabit(habit) },
                                onToggleActive = { onToggleActive(habit) }
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        CalendarHeaderAndGrid(
                            state = state,
                            onSelectDate = onSelectDate,
                            onNavigateMonth = onNavigateMonth,
                            onJumpToToday = onJumpToToday
                        )
                    }
                    item {
                        SelectedDateHeader(
                            selectedDate = selectedDate,
                            today = state.today,
                            completedCount = selectedDayProgress.completedCount,
                            scheduledCount = selectedDayProgress.scheduledCount,
                            percent = selectedDayProgress.completionPercent
                        )
                    }
                    if (scheduledForSelected.isEmpty()) {
                        item { EmptyDayNotice() }
                    } else {
                        items(items = scheduledForSelected, key = { it.id }) { habit ->
                            val isDone = habit.id in completedIdsForSelected
                            HabitScheduleCard(
                                habit = habit,
                                isCompleted = isDone,
                                onToggleComplete = { onToggleCompletion(habit, selectedDate, isDone) },
                                onEdit = { onEditHabit(habit) },
                                onDelete = { onDeleteHabit(habit) },
                                onToggleActive = { onToggleActive(habit) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeaderAndGrid(
    state: LifeflowUiState,
    onSelectDate: (AppDate) -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onJumpToToday: () -> Unit
) {
    val displayedMonth = state.displayedMonth
    val weekStart = state.preferences.weekStartDay

    val weekdayLabels = remember(weekStart) {
        if (weekStart == WeekStartDay.MONDAY) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        } else {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        }
    }

    val calendarCells = remember(displayedMonth, weekStart) {
        val first = displayedMonth.firstDayOfMonth()
        val firstIso = first.isoDayOfWeek // 1..7
        val leadingBlanks = if (weekStart == WeekStartDay.MONDAY) {
            firstIso - 1
        } else {
            firstIso % 7
        }
        val daysInMonth = displayedMonth.daysInMonth()
        val cells = mutableListOf<AppDate?>()
        repeat(leadingBlanks) { cells.add(null) }
        for (d in 1..daysInMonth) {
            cells.add(AppDate(displayedMonth.year, displayedMonth.month, d))
        }
        while (cells.size % 7 != 0) {
            cells.add(null)
        }
        cells
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onJumpToToday) {
                Text("Today")
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigateMonth(-1) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Previous month"
                        )
                    }

                    Text(
                        text = displayedMonth.formatMonthYear(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { onNavigateMonth(1) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Next month"
                        )
                    }
                }

                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdayLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Calendar grid rows
                calendarCells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { date ->
                            if (date == null) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(0.9f))
                            } else {
                                val progress = HabitAnalyticsEngine.dailyProgressForDate(
                                    state.habits,
                                    state.completions,
                                    date,
                                    state.today
                                )
                                val isSelected = date == state.selectedCalendarDate
                                val isToday = date == state.today
                                val isPast = date < state.today

                                val indicatorColor = when {
                                    progress.scheduledCount == 0 -> Color.Transparent
                                    progress.completedCount >= progress.scheduledCount -> EmeraldSuccess
                                    progress.completedCount > 0 -> IndigoPrimary
                                    isPast -> CoralMissed
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.9f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.outline
                                                else -> Color.Transparent
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectDate(date) }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = date.day.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (progress.scheduledCount > 0 && date <= state.today) {
                                        Text(
                                            text = "${progress.completionPercent}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (indicatorColor == Color.Transparent) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                indicatorColor
                                            }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(indicatorColor)
                                    )
                                }
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendDot("Completed", EmeraldSuccess)
                    LegendDot("Partial", IndigoPrimary)
                    LegendDot("Missed", CoralMissed)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectedDateHeader(
    selectedDate: AppDate,
    today: AppDate,
    completedCount: Int,
    scheduledCount: Int,
    percent: Int
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedDate == today) "Today · ${selectedDate.formatShortMonthDay()}"
                    else selectedDate.formatReadable(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        selectedDate > today -> "Future date — previewing scheduled habits"
                        scheduledCount == 0 -> "No habits scheduled for this date"
                        else -> "$completedCount of $scheduledCount completed · Tap cards to update"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (scheduledCount > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (percent == 100) EmeraldSuccess.copy(alpha = 0.16f)
                    else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (percent == 100) EmeraldSuccess else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDayNotice() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "No habits scheduled for this day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
    }
}
