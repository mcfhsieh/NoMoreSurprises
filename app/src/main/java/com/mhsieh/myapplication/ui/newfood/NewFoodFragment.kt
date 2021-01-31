package com.mhsieh.myapplication.ui.newfood

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mhsieh.myapplication.CalendarDialog
import com.mhsieh.myapplication.R
import com.mhsieh.myapplication.databinding.NewFoodFragmentLayoutBinding
import com.mhsieh.myapplication.util.DIALOG_DATE
import com.mhsieh.myapplication.util.showKeyboard
import kotlinx.android.synthetic.main.new_food_fragment_layout.*
import java.util.*

class NewFoodFragment : BottomSheetDialogFragment(), CalendarDialog.Callbacks {
    private var foodName: String = ""
    private lateinit var binding: NewFoodFragmentLayoutBinding
    private lateinit var datePickerButton: Button
    private lateinit var addFoodButton: Button
    private lateinit var behavior: BottomSheetBehavior<View>
    lateinit var dialog: BottomSheetDialog
    private lateinit var viewModel: NewFoodViewModel
    private lateinit var cal: Calendar

    companion object {
        fun newInstance() = NewFoodFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        cal = Calendar.getInstance()
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val sheet = d.bottom_sheet
            behavior = BottomSheetBehavior.from(sheet)
            behavior.isHideable = false
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.newFoodText.showKeyboard()
        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.new_food_fragment_layout, container, false
        )
        foodName = binding.newFoodText.text.toString()
        datePickerButton = binding.nCalendarButton
        addFoodButton = binding.addFood
        addFoodButton.setOnClickListener {
            insertNewFood()
            dismiss()
        }
        datePickerButton.setOnClickListener {
            CalendarDialog.newInstance(cal).apply {
                setTargetFragment(this@NewFoodFragment, 0)
                show(this@NewFoodFragment.parentFragmentManager, DIALOG_DATE)
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewFoodViewModel::class.java)
    }

    private fun insertNewFood() {
        viewModel._foodName.value = binding.newFoodText.text.toString()
        viewModel.createFood()
        this.binding.newFoodText.setText("", TextView.BufferType.EDITABLE)
    }

    override fun onDateSelected(date: Calendar) {
        viewModel.preparedDate = date
    }
}