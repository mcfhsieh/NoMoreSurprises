package com.mhsieh.myapplication.notifications

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mhsieh.myapplication.R
import com.mhsieh.myapplication.data.FoodViewModel
import com.mhsieh.myapplication.databinding.NotificationToggleDialogLayoutBinding
import kotlinx.android.synthetic.main.notification_toggle_dialog_layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationToggleDialog : BottomSheetDialogFragment(){

    private lateinit var binding : NotificationToggleDialogLayoutBinding
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var switch:SwitchMaterial
    lateinit var viewModel: FoodViewModel

    private lateinit var dialog:BottomSheetDialog

    companion object{
        fun newInstance() = NotificationToggleDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val sheet = d.notification_switch_sheet
            behavior = BottomSheetBehavior.from(sheet)
            behavior.isHideable =  true
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.notification_toggle_dialog_layout,container, false
        )
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switch = binding.notificationSwitch
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        switch.isChecked = sharedPref.getBoolean("notification state", false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        Timber.d("onCreate")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FoodViewModel::class.java)
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked == true ){
                switch.setTrackResource(R.color.colorAccent)
                viewModel.notificationsOn = true
                viewModel.scheduleFoodAlarm()
                sharedPref.edit{
                    putBoolean("notification state", viewModel.notificationsOn)
                    commit()
                }


            }
            else{
                viewModel.notificationsOn = false
                sharedPref.edit{
                putBoolean("notification state", viewModel.notificationsOn)
                viewModel.cancelFoodAlarm()
            }
                GlobalScope.launch {
                delay(500)
            }

            }
            GlobalScope.launch {
                delay(500)
                dismiss()
            }

        }
        Timber.d("onActivityCreated")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        //dialog.dismiss()
        Timber.d("onDetach")
    }
}
