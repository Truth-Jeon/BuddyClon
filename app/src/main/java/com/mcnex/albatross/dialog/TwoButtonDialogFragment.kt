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
import com.mcnex.albatross.databinding.DialogTwoButtonBinding
import com.mcnex.albatross.viewmodel.DialogViewModel
import com.mcnex.albatross.viewmodel.MapViewModel

open class TwoButtonDialogFragment : BaseDialogFragment() {

    companion object {
        val DIALOG_DISCONECT   = 0
        val DIALOG_UPDATE   = 1
        val DIALOG_INIT   = 2
        val DIALOG_NOTI   = 3
    }

    var dilog_type = DIALOG_UPDATE

    private val TAG = TwoButtonDialogFragment::class.java.simpleName

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private var _binding: DialogTwoButtonBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DialogTwoButtonBinding.inflate(LayoutInflater.from(context))
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.CustomDialogTheme)
            builder.setView(binding.root)

            when(dilog_type){
                DIALOG_DISCONECT ->  binding.dialogTitleTxt.setText(R.string.disconnect)
                DIALOG_UPDATE ->  binding.dialogTitleTxt.setText(R.string.dialog_map_update_title)
                DIALOG_INIT ->  binding.dialogTitleTxt.setText(R.string.title_init)
                DIALOG_NOTI ->  binding.dialogTitleTxt.setText(R.string.dialog_noti_title)
            }


            binding.btnOk.setOnClickListener { view ->
                run {
                    when(dilog_type){
                        DIALOG_DISCONECT -> viewModel.onDialogEvent(DialogViewModel.DIALOG_DISCONNECT)
                        DIALOG_NOTI -> viewModel.onDialogEvent(DialogViewModel.DIALOG_NOTI)
                        else ->   viewModel.onDialogEvent(DialogViewModel.DIALOG_OK)
                    }

                    dismiss()
                }
            }

            binding.btnCancel.setOnClickListener { view ->
                run {
                    when(dilog_type){
                        DIALOG_NOTI -> viewModel.onDialogEvent(DialogViewModel.DIALOG_DEVICE_SCAN)
                        else ->  viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
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
        Log.d(TAG, "onCancel " )
        when(dilog_type){
            DIALOG_NOTI -> viewModel.onDialogEvent(DialogViewModel.DIALOG_DEVICE_SCAN)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d(TAG, "onDismiss " )
    }
}