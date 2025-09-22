package com.mzansi.recipes.util

import android.app.Activity
import android.content.Context
import android.content.Intent

fun restartApp(context: Context) {
    val intent = (context as Activity).intent
    context.finish()
    context.startActivity(intent)
}