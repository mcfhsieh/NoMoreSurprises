package com.mhsieh.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mhsieh.myapplication.util.ARG_FOOD_DATE
import java.util.*


class CalendarDialog : DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date: Calendar)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val cal: Calendar = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onDateSelected(cal)
                }

            }

        val date = arguments?.getLong(ARG_FOOD_DATE)
        if (date != null) {
            cal.timeInMillis = date
        }

        return DatePickerDialog(
            requireContext(),
            R.style.CalendarDialogStyle,
            dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    companion object {
        fun newInstance(date: Calendar): CalendarDialog {
            val args = Bundle().apply {
                putLong(ARG_FOOD_DATE, date.timeInMillis)
            }

            return CalendarDialog().apply {
                arguments = args
            }
        }
    }
}

