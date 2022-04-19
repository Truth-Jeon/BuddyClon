package com.mcnex.albatross.dialog

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.annotation.SuppressLint
import android.app.Dialog
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcnex.albatross.R
import com.mcnex.albatross.databinding.DeviceConnectingProgressBinding
import com.mcnex.albatross.databinding.DialogMapDownBinding
import com.mcnex.albatross.fragment.MapInfoFragment

open class ProgressDialogFragment : BaseDialogFragment() {

    private val TAG = ProgressDialogFragment::class.java.simpleName

    private var _binding: DialogMapDownBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DialogMapDownBinding.inflate(LayoutInflater.from(context))
    }


    @SuppressLint("UseRequireInsteadOfGet", "WrongConstant")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.CustomDialogTheme)

            builder.setView(binding.root)

            binding.progressHorizontal.setProgress(0, false)

            binding.updateMessageTxt.text =  "0  %"
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }


    fun setProgress(progress : Int){
        binding.progressHorizontal.setProgress(progress, true)
        binding.updateMessageTxt.text = progress.toString() + " %"
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}