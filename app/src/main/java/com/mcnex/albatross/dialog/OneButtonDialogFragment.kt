package com.mcnex.albatross.dialog

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcnex.albatross.R
import com.mcnex.albatross.databinding.DeviceConnectingProgressBinding
import com.mcnex.albatross.databinding.DialogOneButtonBinding
import com.mcnex.albatross.databinding.DialogTwoButtonBinding
import com.mcnex.albatross.viewmodel.DialogViewModel
import com.mcnex.albatross.viewmodel.MapViewModel

open class OneButtonDialogFragment : BaseDialogFragment() {

    companion object {
        val DIALOG_CONECT_ERROR   = -1
        val DIALOG_DISCONECTED   = 0
        val DIALOG_UPDATE   = 1
        val DIALOG_INIT   = 2
        val DIALOG_UPDATE_FILE   = 3
        val DIALOG_NET_ERROR   = 4
        val DIALOG_BATTERY_ERROR   = 5
    }


    var dilog_type = DIALOG_DISCONECTED

    private val TAG = OneButtonDialogFragment::class.java.simpleName

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private var _binding: DialogOneButtonBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DialogOneButtonBinding.inflate(LayoutInflater.from(context))
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it, R.style.CustomDialogTheme)

            builder.setView(binding.root)

            when(dilog_type){
                DIALOG_DISCONECTED -> binding.dialogTitleTxt.setText(R.string.disconnect_noti)
                DIALOG_UPDATE -> binding.dialogTitleTxt.setText(R.string.dialog_map_update_success)
                DIALOG_CONECT_ERROR -> binding.dialogTitleTxt.setText(R.string.dialog_connect_error_title)
                DIALOG_UPDATE_FILE -> binding.dialogTitleTxt.setText(R.string.dialog_map_update_fail)
                DIALOG_NET_ERROR -> binding.dialogTitleTxt.setText(R.string.dialog_net_error_title)
                DIALOG_BATTERY_ERROR ->  binding.dialogTitleTxt.setText(R.string.dialog_battery_noti_title)
            }

            binding.btnOk.setOnClickListener { view ->
                run {

                    when(dilog_type){
                        DIALOG_DISCONECTED -> viewModel.onDialogEvent(DialogViewModel.DIALOG_DISCONNECTED)
                        DIALOG_CONECT_ERROR -> viewModel.onDialogEvent(DialogViewModel.DIALOG_DISCONNECTED)
                        DIALOG_UPDATE -> viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
                        DIALOG_UPDATE_FILE -> viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
                        DIALOG_NET_ERROR -> viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
                        DIALOG_BATTERY_ERROR -> viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
                    }

                    dismiss()
                }
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    fun setDialogType(type : Int){
        dilog_type = type
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        when(dilog_type){
            DIALOG_DISCONECTED -> viewModel.onDialogEvent(DialogViewModel.DIALOG_DISCONNECTED)
            DIALOG_UPDATE -> viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}