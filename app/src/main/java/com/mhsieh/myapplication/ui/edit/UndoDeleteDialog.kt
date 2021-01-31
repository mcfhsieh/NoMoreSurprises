package com.mhsieh.myapplication.ui.edit

import android.app.Dialog
import android.os.Bundle
import android.telecom.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mhsieh.myapplication.R
import com.mhsieh.myapplication.databinding.UndoDeleteDialogLayoutBinding
import kotlinx.android.synthetic.main.new_food_fragment_layout.*
import kotlinx.android.synthetic.main.undo_delete_dialog_layout.*
import kotlinx.coroutines.*
import java.nio.file.Files.delete

class UndoDeleteDialog: BottomSheetDialogFragment(){

    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var binding: UndoDeleteDialogLayoutBinding
    private var undoDelete: Boolean = false

    interface Callbacks{
        fun onUndoSelected(undo:Boolean)
    }

    companion object{
        fun newInstance() = UndoDeleteDialog()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog =  super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener{
            val d = it as BottomSheetDialog
            val sheet = d.undo_bottom_sheet
            behavior = BottomSheetBehavior.from(sheet)
            behavior.isHideable =  false
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.undo_delete_dialog_layout, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.undoButton.setOnClickListener {
            undoDelete =  true
            targetFragment?.let {fragment->
                (fragment as Callbacks).onUndoSelected(undoDelete)
            }
        }
        GlobalScope.launch {
            closeDialog()
        }
    }

    suspend fun closeDialog(){
        delay(3000)
        dialog.dismiss()
    }

}

