package com.example.skinsmart.model

import android.content.res.ColorStateList
import android.graphics.Color

enum class SkinType(
    val displayName: String,
    val bgColorHex: String,
    val textColorHex: String
) {
    OILY("Oily Skin", "#E0F2F1", "#00695C"),        // Teal
    DRY("Dry Skin", "#FFF3E0", "#E65100"),         // Orange
    COMBINATION("Combination", "#F3E5F5", "#4A148C"), // Purple
    NORMAL("Normal", "#E8F5E9", "#1B5E20"),        // Green
    SENSITIVE("Sensitive", "#FFEBEE", "#B71C1C"); // Red

    fun getBackgroundColor(): Int = Color.parseColor(bgColorHex)
    fun getTextColor(): Int = Color.parseColor(textColorHex)
    
    fun getBackgroundColorStateList(): ColorStateList = ColorStateList.valueOf(getBackgroundColor())

    companion object {
        fun fromString(value: String): SkinType {
            return values().find { it.displayName.equals(value, ignoreCase = true) } ?: NORMAL
        }
    }
}