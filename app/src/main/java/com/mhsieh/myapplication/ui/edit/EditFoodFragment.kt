package com.mhsieh.myapplication.ui.edit

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.mhsieh.myapplication.*
import com.mhsieh.myapplication.databinding.EditFoodLayoutBinding
import com.mhsieh.myapplication.util.hideKeyboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class EditFoodFragment : Fragment(), CalendarDialog.Callbacks{

    private val args by navArgs<MainFragmentArgs>()
    private lateinit var binding: EditFoodLayoutBinding
    private lateinit var editText: EditText
    private lateinit var navController: NavController
    lateinit var calendarButton: Button
    lateinit var saveButton: Button
    lateinit var viewModel: EditFoodViewModel
    var foodId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onbackpress = requireActivity().onBackPressedDispatcher.addCallback(this){
            view?.findNavController()?.navigate(R.id.action_editFoodFragment_to_mainFragment)
            Timber.d("backpress handled")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        foodId = args.foodId
        Timber.d("MainFrag Args: $foodId")
        val viewModelFactory = EditFoodViewModelFactory(Application(), foodId)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditFoodViewModel::class.java)
        navController = findNavController()
        binding = EditFoodLayoutBinding.inflate(layoutInflater, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        editText = binding.editTextField
        calendarButton = binding.editCalendarButton
        saveButton = binding.saveButton

        setuptoolbar()
/*        toolbar.setNavigationOnClickListener {
            view?.findNavController()?.navigate(R.id.action_editFoodFragment_to_mainFragment)
            Timber.d("back arrow pressed")
        }*/
        return binding.root
    }

    fun setuptoolbar() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.editToolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        binding.editToolbar.setNavigationOnClickListener {
            view?.findNavController()?.navigate(R.id.action_editFoodFragment_to_mainFragment)
            Timber.d("back arrow pressed")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        calendarButton.setOnClickListener {
            var date = viewModel.editableFood.datePrepared
            CalendarDialog.newInstance(date).apply {
                setTargetFragment(this@EditFoodFragment, 0)
                show(this@EditFoodFragment.parentFragmentManager, "11")
            }
        }

        saveButton.setOnClickListener {
            viewModel.updateNote()
            editText.hideKeyboard()
            it.findNavController()
                .navigate(R.id.action_editFoodFragment_to_mainFragment)
        }

        binding.deleteFood.setOnClickListener {
            foodBackup()
            viewModel.deleteFood()
            val action = EditFoodFragmentDirections.actionEditFoodFragmentToMainFragment(foodId)
            Timber.d("Args sent to mainfrag: $foodId")
            view?.findNavController()?.navigate(action)
            //viewModel.deleteFood()
//            UndoDeleteDialog.newInstance().apply {
//                setTargetFragment(parentFragment, 0)
//                show(this@EditFoodFragment.parentFragmentManager, "11")
//            }
        }
    }

    private fun foodBackup() {
        lifecycleScope.launch {
            val backupFoodName = viewModel.editableFood.foodName
            val datePreparedString = viewModel.editableFood.datePrepared.timeInMillis.toString()
            val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
            sharedPref.edit {
                putString("foodName", backupFoodName)
                putString("datePrepared", datePreparedString)
                commit()
                Timber.d("food backup of: $backupFoodName")
            }
        }
    }

    override fun onDateSelected(date: Calendar) {
        viewModel.newCalendarDate = date
    }


}