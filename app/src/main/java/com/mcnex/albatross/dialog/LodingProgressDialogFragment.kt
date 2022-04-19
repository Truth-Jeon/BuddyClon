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

open class LodingProgressDialogFragment : BaseDialogFragment() {

    companion object {
        val DIALOG_SEARCHING   = 0
        val DIALOG_CONNECTING   = 1
        val DIALOG_LODING   = 2
    }


    private val TAG = LodingProgressDialogFragment::class.java.simpleName

    var loding_type = DIALOG_SEARCHING

    private var _binding: DeviceConnectingProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DeviceConnectingProgressBinding.inflate(LayoutInflater.from(context))
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.CustomDialogTheme)

            builder.setView(binding.root)

            if(loding_type == 1){
                binding.deviceConnectTxt.setText(R.string.connecting_dialog_message)
            }else{
                binding.deviceConnectTxt.setText(R.string.searching_device)
            }

            ObjectAnimator.ofFloat(binding.connectImg, View.ROTATION, -360f, 0f)
                .apply {
                    duration = 1000
                    repeatCount = INFINITE
                }
                .start()

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)

    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setDialogType(type : Int){
        loding_type = type

    }
}