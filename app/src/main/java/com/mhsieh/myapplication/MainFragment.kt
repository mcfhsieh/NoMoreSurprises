package com.mhsieh.myapplication

import android.content.Context
import android.content.SharedPreferences
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
import com.mhsieh.myapplication.notifications.NotificationToggleDialog
import com.mhsieh.myapplication.ui.edit.EditFoodFragmentArgs
import com.mhsieh.myapplication.ui.edit.UndoDeleteDialog
import com.mhsieh.myapplication.ui.newfood.NewFoodFragment
import com.mhsieh.myapplication.util.calcShelfLife
import io.reactivex.internal.operators.observable.ObservableError
import timber.log.Timber
import java.util.*

class MainFragment : Fragment(), UndoDeleteDialog.Callbacks {
    private var _binding: MainFragmentLayoutBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditFoodFragmentArgs>()
    private var newFoodDialog: NewFoodFragment? = null
    private lateinit var adapter: FoodAdapter
    private lateinit var viewModel: FoodViewModel
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    private var mainMenu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getArgs()
        _binding = MainFragmentLayoutBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        navController = findNavController()
        val recyclerView = binding.mRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = FoodAdapter(FoodDataListener { foodId ->
            viewModel.onFoodItemClicked(foodId)
            // editedFoodId = foodId
        }).apply {
            onFoodWastedPress = { food ->
                food.spoiled = true
                viewModel.update(food)
            }
        }
        recyclerView.adapter = adapter
        setUpToolbar()
        setHasOptionsMenu(true)
        /*binding.newFoodButton.setOnClickListener {
            if (newFoodDialog == null) {
                newFoodDialog = NewFoodFragment.newInstance()
                newFoodDialog?.let { dialog ->
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                }
            } else newFoodDialog?.dialog?.show()
        }*/
        viewModel = ViewModelProvider(this).get(FoodViewModel::class.java)
        viewModel.allDishes.observe(viewLifecycleOwner, Observer {
            it.let {
                adapter.submitList(it)
            }
        })
        viewModel.notificationsOn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Toast.makeText(requireContext(), "Notifications Active", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(requireContext(), "Notifications off", Toast.LENGTH_SHORT).show()
        })
        viewModel.navigateToEditFood.observe(viewLifecycleOwner, Observer { foodId ->
            foodId?.let {
                this.findNavController().navigate(
                    MainFragmentDirections.actionMainFragmentToEditFoodFragment(foodId)
                )
                viewModel.onEditFoodNavigated()
            }
        })
        viewModel.navigateToNewFood.observe(viewLifecycleOwner, Observer{
            if(it == true){
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToNewFoodFragment())
            viewModel.onNewFoodDialogNavigated()
            }
        })
        return binding.root
    }

    private fun setUpToolbar() {
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
        } else Timber.d("Bundle is empty")
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
        val deletedFood = FoodData().apply {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)

        return when (item.itemId) {
            R.id.notifications -> {
                if (viewModel.notificationsOn.value == true) {
                    viewModel.turnOffNotifications()
                    viewModel.cancelFoodAlarm(this.requireContext())
                    item.setIcon(R.drawable.ic_outline_add_alert_24)
                    sharedPref?.edit{
                        putBoolean("notifications on", false)
                        commit()
                    }
                } else {
                    viewModel.turnOnNotifications()
                    viewModel.scheduleFoodAlarm(this.requireContext())
                    item.setIcon(R.drawable.alert_white)
                    sharedPref?.edit{
                        putBoolean("notifications on", true)
                        commit()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        inflater.inflate(R.menu.main_settings_menu, menu)
        mainMenu = menu
        var item: MenuItem? = mainMenu?.findItem(R.id.notifications)
        if (sharedPref?.getBoolean("notifications on", false) == true){
            item?.setIcon(R.drawable.alert_white)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("notification state", viewModel.notificationsOn.value!!)
    }

}