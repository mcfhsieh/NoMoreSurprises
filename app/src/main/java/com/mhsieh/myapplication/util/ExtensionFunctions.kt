package com.mhsieh.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import kotlinx.coroutines.delay
import java.util.*

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.hideKeyboard(){
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Calendar.calcShelfLife(): Int{
    val today = Calendar.getInstance()
    val timeInFridge =
        (today.timeInMillis - this.timeInMillis)
    val daysInFridge = timeInFridge.toInt()/ (1000 * 60 * 60 * 24)
    return daysInFridge
}

fun Calendar.dinerTime(): Long{

    val currentHour = this.get(Calendar.HOUR_OF_DAY)
    var hourDelay = if(currentHour >= 17){
        (23 - currentHour) + 17
    }
    else 17 - currentHour
    var delay = hourDelay * 60 * 60 * 1000
    return delay.toLong()
}

fun Context.vectorToBitmap(drawableId: Int): Bitmap? {
    val drawable = getDrawable(drawableId) ?: return null
    val bitmap = createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    ) ?: return null
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

