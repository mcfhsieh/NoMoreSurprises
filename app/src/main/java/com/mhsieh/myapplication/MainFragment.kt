package com.mhsieh.myapplication

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mhsieh.myapplication.data.FoodData
import com.mhsieh.myapplication.data.FoodViewModel
import com.mhsieh.myapplication.databinding.MainFragmentLayoutBinding
import com.mhsieh.myapplication.databinding.NotificationToggleDialogLayoutBinding
import com.mhsieh.myapplication.notifications.AlarmReceiver
import com.mhsieh.myapplication.notifications.NotificationToggleDialog
import com.mhsieh.myapplication.ui.edit.EditFoodFragmentArgs
import com.mhsieh.myapplication.ui.edit.UndoDeleteDialog
import com.mhsieh.myapplication.ui.newfood.NewFoodFragment
import com.mhsieh.myapplication.util.calcShelfLife
import timber.log.Timber
import java.util.*

class MainFragment : Fragment(), UndoDeleteDialog.Callbacks{
    private var _binding: MainFragmentLayoutBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditFoodFragmentArgs>()
    private var newFoodDialog: NewFoodFragment? = null
    private var notificationSwitchDialog: NotificationToggleDialog? = null
    private lateinit var adapter: FoodAdapter
    private lateinit var viewModel: FoodViewModel
    private var editedFoodId = 0
    private lateinit var navController: NavController
    private lateinit var appBarConfig:AppBarConfiguration


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getArgs()
        navController = findNavController()
        _binding = MainFragmentLayoutBinding.inflate(inflater, container, false)
        val recyclerView = binding.mRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        viewModel = ViewModelProvider(this).get(FoodViewModel::class.java)
        adapter = FoodAdapter(FoodDataListener { foodId ->
            val action = MainFragmentDirections.actionMainFragmentToEditFoodFragment(foodId)
            view?.findNavController()
                ?.navigate(action)
            editedFoodId = foodId
        }).apply {
            onFoodWastedPress = { food ->
                food.spoiled = true
                viewModel.update(food)

            }
        }
        recyclerView.adapter = adapter
        setuptoolbar()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.allDishes.observe(viewLifecycleOwner, Observer {
            it.let {
                adapter.submitList(it)
            }
        })
        viewModel.updateShelfLife()
        binding.newFoodButton.setOnClickListener {
            if (newFoodDialog == null) {
                newFoodDialog = NewFoodFragment.newInstance()
                newFoodDialog?.let { dialog ->
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                }
            }
            else newFoodDialog?.dialog?.show()
        }
    }
    fun setuptoolbar() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.myToolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            appBarConfig = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfig)
        }
    }
    private fun getArgs() {
        var foodId = args.foodId
        if (foodId > 0) {
            UndoDeleteDialog.newInstance().apply {
                setTargetFragment(this@MainFragment, 0)
                show(this@MainFragment.parentFragmentManager, "11")
            }
            Timber.d("EditFood args:$foodId")
        }
        else Timber.d("Bundle is empty")
    }
    override fun onUndoSelected(undo: Boolean) {
        Timber.d("Undo selected")
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        var backupFoodName = sharedPref.getString("foodName", "")
        var datePrepared = sharedPref.getString("datePrepared", "")?.toLong()
        var cal = Calendar.getInstance()
        if (datePrepared != null) {
            cal.timeInMillis = datePrepared
        }
        val deletedFood = FoodData().apply{
            this.foodName = backupFoodName!!
            this.datePrepared = cal
            this.shelfLife = cal.calcShelfLife()
        }
        viewModel.restoreFood(deletedFood)
        sharedPref.edit {
            clear()
        }
        Toast.makeText(requireContext(), "$backupFoodName", Toast.LENGTH_SHORT).show()
    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean {

        return when (item.itemId) {
            R.id.notifications -> {
                Timber.d("notifications selected")
                notificationSwitchDialog = NotificationToggleDialog.newInstance()
                notificationSwitchDialog?.let { dialog ->
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                }
                true
            }
                else -> super.onOptionsItemSelected(item)

                /*if (notificationSwitchDialog == null) {
                    notificationSwitchDialog = NotificationToggleDialog.newInstance()
                    notificationSwitchDialog?.let { dialog ->
                        dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                    }
                    true
                }
                else {
                    notificationSwitchDialog?.dialog?.show()
                    true
                }
            }
            else -> super.onOptionsItemSelected(item)*/
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_settings_menu, menu)
        //menu.findItem(R.id.notifications).isVisible = args.noteIdentifier != null
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding =  null
    }
}