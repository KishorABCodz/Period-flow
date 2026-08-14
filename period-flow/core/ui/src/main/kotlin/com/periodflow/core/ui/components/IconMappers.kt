package com.periodflow.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.periodflow.core.domain.model.CyclePhase
import com.periodflow.core.domain.model.FlowIntensity
import com.periodflow.core.domain.model.Mood
import com.periodflow.core.domain.model.Symptom

val Mood.icon: ImageVector
    get() = when (this) {
        Mood.HAPPY -> Icons.Outlined.SentimentSatisfied
        Mood.CALM -> Icons.Outlined.SelfImprovement
        Mood.SENSITIVE -> Icons.Outlined.FavoriteBorder
        Mood.SAD -> Icons.Outlined.SentimentDissatisfied
        Mood.ANXIOUS -> Icons.Outlined.WarningAmber
        Mood.IRRITABLE -> Icons.Outlined.Bolt
        Mood.ENERGETIC -> Icons.Outlined.FlashOn
        Mood.TIRED -> Icons.Outlined.Bedtime
    }

val Symptom.icon: ImageVector
    get() = when (this) {
        Symptom.CRAMPS -> Icons.Outlined.WaterDrop
        Symptom.HEADACHE -> Icons.Outlined.SentimentVeryDissatisfied
        Symptom.BACKACHE -> Icons.Outlined.Accessibility
        Symptom.BLOATING -> Icons.Outlined.Air
        Symptom.BREAST_TENDERNESS -> Icons.Outlined.FavoriteBorder
        Symptom.ACNE -> Icons.Outlined.Face
        Symptom.FATIGUE -> Icons.Outlined.Battery1Bar
        Symptom.NAUSEA -> Icons.Outlined.Sick
        Symptom.INSOMNIA -> Icons.Outlined.Bedtime
        Symptom.CRAVINGS -> Icons.Outlined.Restaurant
        Symptom.DIARRHEA -> Icons.Outlined.Water
        Symptom.CONSTIPATION -> Icons.Outlined.StopCircle
        Symptom.HOT_FLASHES -> Icons.Outlined.LocalFireDepartment
        Symptom.DIZZINESS -> Icons.Outlined.Sync
        Symptom.EXCESS_HAIR -> Icons.Outlined.Face
        Symptom.HAIR_THINNING -> Icons.Outlined.Face
        Symptom.PELVIC_PAIN -> Icons.Outlined.Bolt
        Symptom.SKIN_DARKENING -> Icons.Outlined.WbSunny
    }

val FlowIntensity.icon: ImageVector
    get() = when (this) {
        FlowIntensity.NONE -> Icons.Outlined.Circle
        FlowIntensity.SPOTTING -> Icons.Outlined.LensBlur
        FlowIntensity.LIGHT -> Icons.Outlined.Opacity
        FlowIntensity.MEDIUM -> Icons.Outlined.WaterDrop
        FlowIntensity.HEAVY -> Icons.Outlined.Waves
    }

val CyclePhase.icon: ImageVector
    get() = when (this) {
        CyclePhase.MENSTRUAL -> Icons.Outlined.WaterDrop
        CyclePhase.FOLLICULAR -> Icons.Outlined.Spa
        CyclePhase.OVULATION -> Icons.Outlined.WbSunny
        CyclePhase.LUTEAL -> Icons.Outlined.Park
        CyclePhase.UNKNOWN -> Icons.Outlined.HelpOutline
    }
