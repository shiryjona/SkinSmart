package com.example.skinsmart.model

import android.graphics.Color

enum class SkinType(
    val label: String,
    val bgColorHex: String,
    val textColorHex: String
) {
    DRY("Dry", "#FFCDD2", "#B71C1C"),
    OILY("Oily", "#B2EBF2", "#006064"),
    COMBINATION("Combination", "#E1BEE7", "#4A148C"),
    NORMAL("Normal", "#C8E6C9", "#1B5E20"),
    SENSITIVE("Sensitive", "#FFF9C4", "#F57F17"),
    UNKNOWN("Unknown", "#F3F4F6", "#374151");

    companion object {
        fun fromString(type: String?): SkinType {
            return entries.find { it.label.equals(type, ignoreCase = true) } ?: UNKNOWN
        }
    }

    val bgColorInt: Int get() = Color.parseColor(bgColorHex)
    val textColorInt: Int get() = Color.parseColor(textColorHex)
    val formattedLabel: String get() = if (this == UNKNOWN) "Unknown" else "$label Skin"
}
