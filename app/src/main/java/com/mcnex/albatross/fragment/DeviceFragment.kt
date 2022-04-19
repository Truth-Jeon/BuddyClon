package com.mcnex.albatross.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.diasemi.codelesslib.CodelessEvent
import com.diasemi.codelesslib.misc.RuntimePermissionChecker
import com.mcnex.albatross.R
import com.mcnex.albatross.app.App
import com.mcnex.albatross.app.App.Companion.codlesstutil
import com.mcnex.albatross.databinding.FragmentDeviceBinding
import com.mcnex.albatross.util.AlbatrossUtil
import com.mcnex.albatross.util.AlbatrossUtil.Companion.BATTERY_INDEX
import com.mcnex.albatross.util.AlbatrossUtil.Companion.DISTANCE_INDEX
import com.mcnex.albatross.util.AlbatrossUtil.Companion.DISTANCE_METER
import com.mcnex.albatross.util.AlbatrossUtil.Companion.DISTANCE_PRIME_OFF
import com.mcnex.albatross.util.AlbatrossUtil.Companion.DISTANCE_PRIME_ON
import com.mcnex.albatross.util.AlbatrossUtil.Companion.DISTANCE_YARD
import com.mcnex.albatross.util.AlbatrossUtil.Companion.HOLE_INDEX
import com.mcnex.albatross.util.AlbatrossUtil.Companion.HOLE_ONE_LEFT
import com.mcnex.albatross.util.AlbatrossUtil.Companion.HOLE_ONE_RIGHT
import com.mcnex.albatross.util.AlbatrossUtil.Companion.HOLE_TWO
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_DEVICE_SET
import com.mcnex.albatross.util.AlbatrossUtil.Companion.METER_INDEX
import com.mcnex.albatross.util.AlbatrossUtil.Companion.VOLUME_INDEX
import com.mcnex.albatross.viewmodel.FragmentMoveViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
class DeviceFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = DeviceFragment::class.java.simpleName

    private lateinit var envBuf: ByteArray

    private lateinit var permissionChecker: RuntimePermissionChecker //filter 설정값

    private var _binding: FragmentDeviceBinding? = null

    private val binding get() = _binding!!

    var vol_num = 7;
    var is_meter = DISTANCE_METER
    var is_distance = DISTANCE_PRIME_ON
    var is_hole = HOLE_TWO;

    var battery_num = 0;

    var data_sending : Boolean = false

    var initdata : Boolean = false

    private val viewModel: FragmentMoveViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        permissionChecker = RuntimePermissionChecker(activity, savedInstanceState)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        data_sending = false

        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        val rootview = binding.root

        binding.btnSetting.setOnClickListener {
            viewModel.onMoveEvent(MENU_FRAGMENT_DEVICE_SET)
        }

        binding.radioGroupMeter.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.meterRadio01.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.meterRadio02.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}

        binding.radioGroupDistance.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.distanceRadio01.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.distanceRadio02.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}

        binding.radioGroupHole.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.holeRadio01.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.holeRadio02.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}
        binding.holeRadio03.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}

        binding.seekBarVolum.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}

        EventBus.getDefault().register(this)

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
        mtime_Handler.sendMessage(mtime_Handler.obtainMessage(0))
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
//        EventBus.getDefault().unregister(this)
        mtime_Handler.removeMessages(0)
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

                        vol_num = envBuf[0].toInt()
                        is_distance = envBuf[2]
                        is_meter = envBuf[3]
                        is_hole = envBuf[4]
                        battery_num = envBuf[BATTERY_INDEX].toInt()

                        initView()
                    }

                }
            }
        }
    }


    fun initView(){

        Log.d(TAG, "initView: " )

        initdata = true

        binding.seekBarVolum.progress = vol_num
        binding.txtVolum.text = vol_num.toString()

        if (is_meter == DISTANCE_METER){
            binding.meterRadio01.isChecked = true
        }else{
            binding.meterRadio02.isChecked = true
        }

        if (is_distance == DISTANCE_PRIME_ON){
            binding.distanceRadio01.isChecked = true
        }else{
            binding.distanceRadio02.isChecked = true
        }

        when (is_hole){
            HOLE_TWO -> binding.holeRadio01.isChecked = true
            HOLE_ONE_LEFT -> binding.holeRadio02.isChecked = true
            HOLE_ONE_RIGHT -> binding.holeRadio03.isChecked = true
        }

        when(battery_num){
            0 -> binding.imgBattery.setBackgroundResource(R.drawable.ic_battery_0)
            in 1..15 -> binding.imgBattery.setBackgroundResource(R.drawable.ic_battery_1)
            in 16..80 -> binding.imgBattery.setBackgroundResource(R.drawable.ic_battery_2)
            in 81..100 -> binding.imgBattery.setBackgroundResource(R.drawable.ic_battery_3)
        }

        binding.txtBattery.text = battery_num.toString() + "%"


        initButtonLister()

        initdata = false

    }


    private fun setENV(option: Int, data : Byte) {

        data_sending = true

        envBuf[option] = data

        codlesstutil.forEnv(AlbatrossUtil.ENV_DATA_SET, envBuf)
    }


    fun initButtonLister(){
        binding.seekBarVolum.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d(TAG, "onProgressChanged : " + seekBar!!.progress)
                binding.txtVolum.text = seekBar!!.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d(TAG, "onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d(TAG, "onStopTrackingTouch : " + seekBar!!.progress)
                setENV(VOLUME_INDEX, seekBar!!.progress.toByte() )
            }
        })


        binding.radioGroupMeter.setOnCheckedChangeListener{ radioGroup : RadioGroup, id : Int ->
            if(initdata){
                return@setOnCheckedChangeListener
            }

            when(id){
                binding.meterRadio01.id  ->  setENV(METER_INDEX, DISTANCE_METER)
                binding.meterRadio02.id  ->  setENV(METER_INDEX, DISTANCE_YARD)
            }
        }

        binding.radioGroupDistance.setOnCheckedChangeListener{ radioGroup : RadioGroup, id : Int ->

            if(initdata){
                return@setOnCheckedChangeListener
            }

            when(id){
                binding.distanceRadio01.id  ->  setENV(DISTANCE_INDEX, DISTANCE_PRIME_ON)
                binding.distanceRadio02.id  ->  setENV(DISTANCE_INDEX, DISTANCE_PRIME_OFF)
            }
        }

        binding.radioGroupHole.setOnCheckedChangeListener{ radioGroup : RadioGroup, id : Int ->

            if(initdata){
                return@setOnCheckedChangeListener
            }

            when(id){
                binding.holeRadio01.id  ->  setENV(HOLE_INDEX, HOLE_TWO)
                binding.holeRadio02.id  ->  setENV(HOLE_INDEX, HOLE_ONE_LEFT)
                binding.holeRadio03.id  ->  setENV(HOLE_INDEX, HOLE_ONE_RIGHT)
            }
        }
    }


    private val mtime_Handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            try {

                val reqBuf: ByteArray
                reqBuf = ByteArray(23)
                Arrays.fill(reqBuf, 0x00.toByte())

                codlesstutil.forEnv(AlbatrossUtil.ENV_DATA_REQ, reqBuf)

            } catch (e: Exception) {
            }
            sendMessageDelayed(this.obtainMessage(0), 60000)
        }
    }

}