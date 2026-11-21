package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.AppDate
import com.example.model.Habit
import com.example.model.HabitCategory
import com.example.model.HabitCompletion
import com.example.model.HabitFrequency
import com.example.model.HabitIcon
import com.example.model.RoutinePeriod

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val routinePeriod: String,
    val iconKey: String,
    val frequency: String,
    val scheduledDaysMask: String,
    val reminderTime: String?,
    val reminderEnabled: Boolean,
    val startDate: String,
    val targetDurationMinutes: Int?,
    val isActive: Boolean,
    val isStarterRoutine: Boolean,
    val sortOrder: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
) {
    fun toDomain(): Habit {
        val parsedDays = scheduledDaysMask
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .toSet()
            .ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }

        return Habit(
            id = id,
            title = title,
            description = description,
            category = HabitCategory.fromKey(category),
            routinePeriod = RoutinePeriod.fromKey(routinePeriod),
            icon = HabitIcon.fromKey(iconKey),
            frequency = HabitFrequency.fromKey(frequency),
            scheduledDays = parsedDays,
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled,
            startDate = AppDate.parseIso(startDate) ?: AppDate.today(),
            targetDurationMinutes = targetDurationMinutes,
            isActive = isActive,
            isStarterRoutine = isStarterRoutine,
            sortOrder = sortOrder,
            createdAtEpochMs = createdAtEpochMs,
            updatedAtEpochMs = updatedAtEpochMs
        )
    }

    companion object {
        fun fromDomain(habit: Habit): HabitEntity = HabitEntity(
            id = habit.id,
            title = habit.title,
            description = habit.description,
            category = habit.category.name,
            routinePeriod = habit.routinePeriod.name,
            iconKey = habit.icon.key,
            frequency = habit.frequency.name,
            scheduledDaysMask = habit.scheduledDays.sorted().joinToString(","),
            reminderTime = habit.reminderTime,
            reminderEnabled = habit.reminderEnabled,
            startDate = habit.startDate.toIsoString(),
            targetDurationMinutes = habit.targetDurationMinutes,
            isActive = habit.isActive,
            isStarterRoutine = habit.isStarterRoutine,
            sortOrder = habit.sortOrder,
            createdAtEpochMs = habit.createdAtEpochMs,
            updatedAtEpochMs = habit.updatedAtEpochMs
        )
    }
}

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["date"]),
        Index(value = ["habitId", "date"], unique = true)
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: String,
    val completed: Boolean,
    val completedAtEpochMs: Long
) {
    fun toDomain(): HabitCompletion = HabitCompletion(
        id = id,
        habitId = habitId,
        date = AppDate.parseIso(date) ?: AppDate.today(),
        completed = completed,
        completedAtEpochMs = completedAtEpochMs
    )
}
