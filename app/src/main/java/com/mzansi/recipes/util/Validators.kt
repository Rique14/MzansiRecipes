package com.mzansi.recipes.util

import android.util.Patterns

data class ValidationResult(
    val valid: Boolean,
    val error: String? = null
)

fun isValidEmail(email: String): Boolean =
    email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

fun validateEmail(email: String): ValidationResult =
    if (isValidEmail(email)) ValidationResult(true)
    else ValidationResult(false, "Enter a valid email")

fun validatePassword(password: String, min: Int = 8): ValidationResult {
    val p = password.trim()
    if (p.length < min) return ValidationResult(false, "Password must be at least $min characters")
    val hasLetter = p.any { it.isLetter() }
    val hasDigit = p.any { it.isDigit() }
    if (!hasLetter || !hasDigit) return ValidationResult(false, "Use letters and numbers")
    return ValidationResult(true)
}

fun validateConfirmPassword(password: String, confirm: String): ValidationResult =
    if (password == confirm) ValidationResult(true)
    else ValidationResult(false, "Passwords do not match")

fun validateNonEmpty(value: String, label: String, min: Int = 1, max: Int = 200): ValidationResult {
    val v = value.trim()
    if (v.length < min) return ValidationResult(false, "$label is too short")
    if (v.length > max) return ValidationResult(false, "$label is too long")
    return ValidationResult(true)
}

private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,20}$")
fun validateUsername(username: String): ValidationResult =
    if (USERNAME_REGEX.matches(username.trim())) ValidationResult(true)
    else ValidationResult(false, "3–20 chars, letters, numbers, underscore")

fun validateDisplayName(name: String): ValidationResult =
    validateNonEmpty(name, "Display name", min = 2, max = 40)

fun validatePhone(phone: String): ValidationResult {
    val p = phone.trim()
    if (p.isEmpty()) return ValidationResult(true) // optional
    val digits = p.filter { it.isDigit() }
    return if (digits.length in 7..15) ValidationResult(true)
    else ValidationResult(false, "Enter a valid phone number")
}

// App-specific
fun validateRecipeTitle(title: String): ValidationResult =
    validateNonEmpty(title, "Title", min = 3, max = 80)

fun validateIngredientLine(line: String): ValidationResult =
    validateNonEmpty(line, "Ingredient", min = 2, max = 120)