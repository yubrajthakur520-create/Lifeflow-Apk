package com.example.data.local

import com.example.model.AppDate
import com.example.model.HabitCategory
import com.example.model.HabitFrequency
import com.example.model.HabitIcon
import com.example.model.RoutinePeriod
import java.util.UUID

object StarterRoutines {

    private data class StarterTemplate(
        val title: String,
        val description: String,
        val category: HabitCategory,
        val routinePeriod: RoutinePeriod,
        val icon: HabitIcon,
        val reminderTime: String,
        val targetDurationMinutes: Int?
    )

    private val templates = listOf(
        // MORNING ROUTINE
        StarterTemplate(
            title = "Wake up early",
            description = "Start the day calmly before the rush begins",
            category = HabitCategory.LIFESTYLE,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.ALARM,
            reminderTime = "06:30",
            targetDurationMinutes = null
        ),
        StarterTemplate(
            title = "Drink water",
            description = "Hydrate with a full glass of water right after waking",
            category = HabitCategory.HEALTH,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.WATER_DROP,
            reminderTime = "06:40",
            targetDurationMinutes = null
        ),
        StarterTemplate(
            title = "Morning workout",
            description = "Energizing strength or cardio movement session",
            category = HabitCategory.FITNESS,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.FITNESS_CENTER,
            reminderTime = "07:00",
            targetDurationMinutes = 30
        ),
        StarterTemplate(
            title = "Stretching",
            description = "Release tension and improve daily mobility",
            category = HabitCategory.FITNESS,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.SELF_IMPROVEMENT,
            reminderTime = "07:35",
            targetDurationMinutes = 10
        ),
        StarterTemplate(
            title = "Shower",
            description = "Refreshing morning reset for body and mind",
            category = HabitCategory.PERSONAL_CARE,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.SHOWER,
            reminderTime = "07:50",
            targetDurationMinutes = 15
        ),
        StarterTemplate(
            title = "Breakfast",
            description = "Nourishing, balanced morning meal",
            category = HabitCategory.HEALTH,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.RESTAURANT,
            reminderTime = "08:10",
            targetDurationMinutes = 20
        ),
        StarterTemplate(
            title = "Plan the day",
            description = "Identify top 3 priorities before diving into work",
            category = HabitCategory.PRODUCTIVITY,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.EDIT_NOTE,
            reminderTime = "08:35",
            targetDurationMinutes = 10
        ),

        // AFTERNOON ROUTINE
        StarterTemplate(
            title = "Lunch",
            description = "Step away from screens for a mindful midday meal",
            category = HabitCategory.HEALTH,
            routinePeriod = RoutinePeriod.AFTERNOON,
            icon = HabitIcon.RESTAURANT,
            reminderTime = "12:30",
            targetDurationMinutes = 30
        ),
        StarterTemplate(
            title = "Study session",
            description = "Focused deep-learning block on core skills",
            category = HabitCategory.STUDY,
            routinePeriod = RoutinePeriod.AFTERNOON,
            icon = HabitIcon.PSYCHOLOGY,
            reminderTime = "14:00",
            targetDurationMinutes = 45
        ),
        StarterTemplate(
            title = "Reading",
            description = "Read non-fiction, articles, or study notes",
            category = HabitCategory.STUDY,
            routinePeriod = RoutinePeriod.AFTERNOON,
            icon = HabitIcon.MENU_BOOK,
            reminderTime = "15:30",
            targetDurationMinutes = 20
        ),
        StarterTemplate(
            title = "Complete daily tasks",
            description = "Wrap up priority work items with clear focus",
            category = HabitCategory.PRODUCTIVITY,
            routinePeriod = RoutinePeriod.AFTERNOON,
            icon = HabitIcon.CHECKLIST,
            reminderTime = "16:30",
            targetDurationMinutes = 45
        ),

        // EVENING ROUTINE
        StarterTemplate(
            title = "Evening walk",
            description = "Unwind outdoors and clear your mind after work",
            category = HabitCategory.FITNESS,
            routinePeriod = RoutinePeriod.EVENING,
            icon = HabitIcon.DIRECTIONS_WALK,
            reminderTime = "18:00",
            targetDurationMinutes = 25
        ),
        StarterTemplate(
            title = "Exercise",
            description = "Active movement or core mobility workout",
            category = HabitCategory.FITNESS,
            routinePeriod = RoutinePeriod.EVENING,
            icon = HabitIcon.DIRECTIONS_RUN,
            reminderTime = "18:45",
            targetDurationMinutes = 30
        ),
        StarterTemplate(
            title = "Review progress",
            description = "Reflect on wins, lessons, and daily habit check-ins",
            category = HabitCategory.PRODUCTIVITY,
            routinePeriod = RoutinePeriod.EVENING,
            icon = HabitIcon.CHECK_CIRCLE,
            reminderTime = "19:45",
            targetDurationMinutes = 10
        ),
        StarterTemplate(
            title = "Relaxation",
            description = "Breathing, meditation, or calm screen-free rest",
            category = HabitCategory.MINDFULNESS,
            routinePeriod = RoutinePeriod.EVENING,
            icon = HabitIcon.SELF_IMPROVEMENT,
            reminderTime = "20:30",
            targetDurationMinutes = 20
        ),

        // NIGHT ROUTINE
        StarterTemplate(
            title = "Skincare",
            description = "Evening cleanse and restorative skincare routine",
            category = HabitCategory.PERSONAL_CARE,
            routinePeriod = RoutinePeriod.NIGHT,
            icon = HabitIcon.SPA,
            reminderTime = "21:30",
            targetDurationMinutes = 10
        ),
        StarterTemplate(
            title = "Read a book",
            description = "Wind down with calm reading before bed",
            category = HabitCategory.MINDFULNESS,
            routinePeriod = RoutinePeriod.NIGHT,
            icon = HabitIcon.MENU_BOOK,
            reminderTime = "21:50",
            targetDurationMinutes = 20
        ),
        StarterTemplate(
            title = "Prepare tomorrow's schedule",
            description = "Set out essentials and map tomorrow's schedule",
            category = HabitCategory.PRODUCTIVITY,
            routinePeriod = RoutinePeriod.NIGHT,
            icon = HabitIcon.EDIT_NOTE,
            reminderTime = "22:15",
            targetDurationMinutes = 10
        ),
        StarterTemplate(
            title = "Sleep on time",
            description = "Lights out for 7–8 hours of restorative sleep",
            category = HabitCategory.HEALTH,
            routinePeriod = RoutinePeriod.NIGHT,
            icon = HabitIcon.BEDTIME,
            reminderTime = "22:30",
            targetDurationMinutes = null
        )
    )

    fun createStarterEntities(today: AppDate = AppDate.today()): List<HabitEntity> {
        val now = System.currentTimeMillis()
        val isoToday = today.toIsoString()
        return templates.mapIndexed { index, tpl ->
            HabitEntity(
                id = UUID.randomUUID().toString(),
                title = tpl.title,
                description = tpl.description,
                category = tpl.category.name,
                routinePeriod = tpl.routinePeriod.name,
                iconKey = tpl.icon.key,
                frequency = HabitFrequency.DAILY.name,
                scheduledDaysMask = "1,2,3,4,5,6,7",
                reminderTime = tpl.reminderTime,
                reminderEnabled = false,
                startDate = isoToday,
                targetDurationMinutes = tpl.targetDurationMinutes,
                isActive = true,
                isStarterRoutine = true,
                sortOrder = index,
                createdAtEpochMs = now + index,
                updatedAtEpochMs = now + index
            )
        }
    }
}
