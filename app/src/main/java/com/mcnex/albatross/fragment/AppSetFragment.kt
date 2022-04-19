package com.mcnex.albatross.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.diasemi.codelesslib.CodelessEvent
import com.mcnex.albatross.R
import com.mcnex.albatross.app.App
import com.mcnex.albatross.app.App.Companion.manager
import com.mcnex.albatross.databinding.FragmentAppBinding
import com.mcnex.albatross.dialog.RadioButtonDialogFragment
import com.mcnex.albatross.dialog.TwoButtonDialogFragment
import com.mcnex.albatross.util.AlbatrossUtil
import com.mcnex.albatross.util.AlbatrossUtil.Companion.LANGUAGE_INDEX
import com.mcnex.albatross.util.Util
import com.mcnex.albatross.viewmodel.DialogViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AppSetFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = AppSetFragment::class.java.simpleName

    private var _binding: FragmentAppBinding? = null

    private val binding get() = _binding!!

    var data_sending : Boolean = false

    val twoButtonDialogFragment : TwoButtonDialogFragment = TwoButtonDialogFragment()

    val radioButtonDialogFragment : RadioButtonDialogFragment = RadioButtonDialogFragment()

    private val dialogViewModel: DialogViewModel by activityViewModels()

    private lateinit var envBuf: ByteArray

    var lan = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        _binding = FragmentAppBinding.inflate(inflater, container, false)
        val rootview = binding.root

        EventBus.getDefault().register(this)


        lifecycleScope.launch {
            val reqBuf = ByteArray(23)
            Arrays.fill(reqBuf, 0x00.toByte())
            App.codlesstutil.forEnv(AlbatrossUtil.ENV_DATA_REQ, reqBuf)

        }

        binding.constraints04.setOnClickListener(){

            radioButtonDialogFragment.setLanguageIndex(lan)
            radioButtonDialogFragment.show(requireActivity().supportFragmentManager, "radioButtonDialogFragment")

        }

        dialogViewModel.dialogEvent.observe(viewLifecycleOwner, Observer { event ->
            event?.let {
                when (event) {
                    in 0..2 -> {
                        if (lan != event) {
                            setENV(AlbatrossUtil.LANGUAGE_INDEX, event.toByte())
                        }
                    }
                }
                dialogViewModel.consumeSignInResponse()
            }
        })

        return rootview
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        _binding = null
    }


    fun btnClick(view: View) {
        when (view.id) {
            R.id.button -> {
                Log.d(TAG, "button")
            }
            else -> {
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun setSendLock(event: CodelessEvent.DspsRxData) {
        Log.d(TAG, "Start setSendLock length : " + event.data.size)

        event.data.let {
            if (it.size > 31) {
                Log.d( TAG, "Start setSendLock data : " + Arrays.toString(it)  )

                if (it[0] == AlbatrossUtil.ACK && it[5] == AlbatrossUtil.ENV  &&it[31] == AlbatrossUtil.DtoM) {

                    data_sending = false

                    envBuf = ByteArray(it[7].toInt())
                    Arrays.fill(envBuf, 0x00.toByte())
                    System.arraycopy(it, 8, envBuf, 0, it[7].toInt())

                    lifecycleScope.launch {

                        lan = envBuf[LANGUAGE_INDEX].toInt()

                        Log.d(TAG, "FW_CHK 1 : " + Util.byteToInt(envBuf.sliceArray(5..8), ByteOrder.LITTLE_ENDIAN))
                        Log.d(TAG, "MAP_CHK 1 : " + Util.byteToInt(envBuf.sliceArray(9..12), ByteOrder.LITTLE_ENDIAN))

                        var ori_date =  String.format("%08d", Util.byteToInt(envBuf.sliceArray(5..8), ByteOrder.LITTLE_ENDIAN))

                        val sdf = SimpleDateFormat("yyyyMMdd")
                        var strDate = "20"+  ori_date.substring(0, 6)
                        var date = Date(sdf.parse(strDate).time)

                        when(App.APP_LANGUAGE){
                            "ko" -> {
                                var formattedDate = SimpleDateFormat("YYYY년 MM월 dd일", Locale.KOREA).format(date)
                                binding.fwText02.text = formattedDate + " ver " +    ori_date.substring(6, 8)
                            }
                            else -> {
                                val formattedDate =SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(date)
                                binding.fwText02.text = formattedDate + " ver " +    ori_date.substring(6, 7)
                            }
                        }

                        ori_date =  String.format("%08d", Util.byteToInt(envBuf.sliceArray(9..12), ByteOrder.LITTLE_ENDIAN))

                        strDate = "20"+  ori_date.substring(0, 6)
                        date = Date(sdf.parse(strDate).time)

                        when(App.APP_LANGUAGE){
                            "ko" -> {
                                var formattedDate = SimpleDateFormat("YYYY년 MM월 dd일", Locale.KOREA).format(date)
                                binding.fwText02.text = formattedDate + " ver " +    ori_date.substring(6, 8)
                            }
                            else -> {
                                val formattedDate =SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(date)
                                binding.fwText02.text = formattedDate + " ver " +    ori_date.substring(6, 7)
                            }
                        }

                        binding.macText02.text = "[" +   manager!!.device.address + "]"

                        when(lan){
                            0 -> binding.lanText02.setText(R.string.language_kr_text)
                            1 -> binding.lanText02.setText(R.string.language_jp_text)
                            2 -> binding.lanText02.setText(R.string.language_en_text)
                        }

                    }

                }
            }
        }
    }


    private fun setENV(option: Int, data : Byte) {

        data_sending = true

        envBuf[option] = data

        App.codlesstutil.forEnv(AlbatrossUtil.ENV_DATA_SET, envBuf)
    }



}