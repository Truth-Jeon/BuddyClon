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
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcnex.albatross.R
import com.mcnex.albatross.databinding.DeviceConnectingProgressBinding
import com.mcnex.albatross.databinding.DialogRadioButtonBinding
import com.mcnex.albatross.databinding.DialogTwoButtonBinding
import com.mcnex.albatross.viewmodel.DialogViewModel
import com.mcnex.albatross.viewmodel.MapViewModel

open class RadioButtonDialogFragment : BaseDialogFragment() {

    private val TAG = RadioButtonDialogFragment::class.java.simpleName

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private var _binding: DialogRadioButtonBinding? = null
    private val binding get() = _binding!!

    private var languageIndex  = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DialogRadioButtonBinding.inflate(LayoutInflater.from(context))
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.CustomDialogTheme)
            builder.setView(binding.root)

            binding.btnOk.setOnClickListener { view ->
                run {
                    when(binding.radioGroupLanguage.checkedRadioButtonId ){
                        binding.lanRadio01.id ->  viewModel.onDialogEvent(0)
                        binding.lanRadio02.id ->  viewModel.onDialogEvent(1)
                        binding.lanRadio03.id ->  viewModel.onDialogEvent(2)
                    }

                    dismiss()
                }
            }

            binding.btnCancel.setOnClickListener { view ->
                run {
                    viewModel.onDialogEvent(DialogViewModel.DIALOG_CANCEL)
                    dismiss()
                }
            }

            when(languageIndex){
                0 ->      binding.lanRadio01.isChecked = true
                1 ->      binding.lanRadio02.isChecked = true
                2 ->      binding.lanRadio03.isChecked = true
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)

    }

    fun setLanguageIndex(index : Int){

        languageIndex = index

    }


    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.d(TAG, "onCancel " )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d(TAG, "onDismiss " )
    }
}