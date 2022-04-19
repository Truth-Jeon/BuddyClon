package com.mcnex.albatross.fragment

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diasemi.codelesslib.CodelessCommands
import com.diasemi.codelesslib.CodelessEvent
import com.diasemi.codelesslib.CodelessEvent.ScanRestart
import com.diasemi.codelesslib.CodelessScanner
import com.diasemi.codelesslib.CodelessScanner.AdvData
import com.diasemi.codelesslib.CodelessUtil
import com.diasemi.codelesslib.misc.RuntimePermissionChecker
import com.mcnex.albatross.R
import com.mcnex.albatross.adapter.BLEScanAdapter
import com.mcnex.albatross.app.App
import com.mcnex.albatross.app.App.Companion.DEVICE_NAME
import com.mcnex.albatross.bean.ScanBean
import com.mcnex.albatross.bean.ScanFilter
import com.mcnex.albatross.viewmodel.DeviceViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

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
class ScanFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    private val TAG = ScanFragment::class.java.simpleName


    private lateinit var permissionChecker : RuntimePermissionChecker //filter 설정값


    //for BLE
    private lateinit var scanner: CodelessScanner
    private lateinit var handler: Handler
    private lateinit var scanTimer: Runnable
    private var scanFilter: ScanFilter = ScanFilter()
    private val deviceListSwipeRefresh: SwipeRefreshLayout? = null
    private lateinit var bluetoothScanAdapter: BLEScanAdapter

    private var device: BluetoothDevice? = null

    private var commands: CodelessCommands? = null
    private var bluetoothDeviceList //all Bluetooth Devices
            : ArrayList<BluetoothDevice>? = null
    private var filterBluetoothDeviceList //filtering Devices
            : ArrayList<BluetoothDevice>? = null
    private var advDataList: ArrayList<AdvData>? = null
    private var deviceList: ArrayList<ScanBean>? = null
    private var filterDeviceList: ArrayList<ScanBean>? = null

    private val bleConnection = false
    private var scanning = false
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val SCAN_DURATION : Long = 7000
    private val LIST_UPDATE_INTERVAL = 1000
    private var lastListUpdate: Long = 0

    //for view
    private lateinit var bleListView: RecyclerView
    private val researchBtn: Button? = null
    private val mSearching_frame: LinearLayout? = null

    private val viewModel: DeviceViewModel by activityViewModels()

    var isAutoScan = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        permissionChecker = RuntimePermissionChecker(activity, savedInstanceState)
        permissionChecker.setOneTimeRationale(getString(R.string.dialog_permission_rationale_title))


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootview = inflater.inflate(R.layout.fragment_scan, container, false)

        Log.d(TAG, "onCreateView")

        EventBus.getDefault().register(this)

        handler = Handler(Looper.getMainLooper())
        scanner = CodelessScanner(context)

        bluetoothDeviceList = ArrayList<BluetoothDevice>()
        filterBluetoothDeviceList = ArrayList<BluetoothDevice>()
        deviceList = ArrayList<ScanBean>()
        filterDeviceList = ArrayList<ScanBean>()
        advDataList = ArrayList<AdvData>()


        bluetoothScanAdapter = BLEScanAdapter { int -> adapterOnClick(int) }

        bleListView = rootview.findViewById(R.id.scan_listView) as RecyclerView

        val dividerItemDecoration =    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        dividerItemDecoration.setDrawable(context?.let { ContextCompat.getDrawable(it,R.drawable.recyclerview_divider) }!!)
        bleListView.addItemDecoration(dividerItemDecoration)


        bleListView.adapter = bluetoothScanAdapter


        bluetoothScanAdapter.submitList(filterDeviceList  as MutableList<ScanBean>)
        bluetoothScanAdapter.notifyDataSetChanged()


        scanTimer = Runnable { stopDeviceScan() }

        var aa = rootview.findViewById(R.id.button) as Button
        aa.setOnClickListener { startDeviceScan() }

        if(isAutoScan) {
            startDeviceScan()
        }

        return rootview
    }

    /* Opens FlowerDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(position: Int) {
        stopDeviceScan()
        Log.d(TAG, "onItemClick 1")
        device = filterBluetoothDeviceList!![position]

        DEVICE_NAME = filterDeviceList!![position].name.toString()

        viewModel.onReceivedDevice(device!!)
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
        initScanFilter()
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

    private fun initScanFilter() {


        context?.let {
            it.getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE).let {
                if(it.getBoolean(App.PRE_RSSI_FILTER, false)){

                    try {
                        scanFilter.rssi = Integer.decode(it.getString(App.PRE_RSSI_LEVEL, ""))
                    } catch (e: NumberFormatException) {
                    }

                    scanFilter.codeless = it.getBoolean(App.PRE_CODELESS, false)
                    scanFilter.dsps = it.getBoolean(App.PRE_DSPS, true)
                    scanFilter.suota = it.getBoolean(App.PRE_SUOTA, false)
                    scanFilter.other = it.getBoolean(App.PRE_OTHER, false)
                    scanFilter.unknown = it.getBoolean(App.PRE_UNKNOWN, false)
                    scanFilter.beacon = it.getBoolean(App.PRE_BEACON, false)
                    scanFilter.microsoft = it.getBoolean(App.PRE_MICROSOFT, false)

                    try {
                        val pattern = it.getString(App.STR_NAME, null)
                        if (!TextUtils.isEmpty(pattern)) scanFilter.name =
                            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    } catch (e: PatternSyntaxException) {
                        Log.e(TAG, "Scan filter invalid name pattern", e)
                    }
                    try {
                        val pattern = it.getString(App.STR_ADDRESS, null)
                        if (!TextUtils.isEmpty(pattern)) scanFilter.address =
                            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    } catch (e: PatternSyntaxException) {
                        Log.e(TAG, "Scan filter invalid address pattern", e)
                    }
                    try {
                        val pattern = it.getString(App.STR_ADVDATA, null)
                        if (!TextUtils.isEmpty(pattern)) scanFilter.advData =
                            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    } catch (e: PatternSyntaxException) {
                        Log.e(TAG, "Scan filter invalid advertising data pattern", e)
                    }

                }


            }

        }

    }

    fun startDeviceScan() {
        if ( !scanner.checkScanRequirements ( activity, REQUEST_ENABLE_BLUETOOTH, permissionChecker )){
            return
        }
        if (scanning) stopDeviceScan()
        Log.d(TAG, "Start scanning")
        scanning = true
        bluetoothDeviceList!!.clear()
        deviceList!!.clear()
        filterBluetoothDeviceList!!.clear()
        filterDeviceList!!.clear()
        advDataList!!.clear()

        viewModel.disable(true)

        scanner.startScanning()
        handler.postDelayed(scanTimer, SCAN_DURATION)
        bluetoothScanAdapter.submitList(filterDeviceList!!.toMutableList())
    }


    private fun stopDeviceScan() {
        handler.removeCallbacks(delayedListUpdate)
        if (!scanning) {
            return
        }
        Log.d(TAG, "Stop scanning")
        scanning = false
        handler.removeCallbacks(scanTimer)
        scanner.stopScanning()
        Log.d(TAG, " stopDeviceScan filterDeviceList size :: " + filterDeviceList!!.size)
        viewModel.disable(false)

    }

    private val delayedListUpdate = Runnable { updateList(true) }

    private fun updateList(force: Boolean) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastListUpdate
        if (elapsed < 0 || elapsed > LIST_UPDATE_INTERVAL || force) {
            handler.removeCallbacks(delayedListUpdate)
            lastListUpdate = now

            var old_count = filterDeviceList!!.size

            applyScanFilter()

            if(old_count != filterDeviceList!!.size){
                bluetoothScanAdapter.submitList(filterDeviceList!!.toMutableList())
            }

        } else {
            handler.postDelayed(delayedListUpdate, LIST_UPDATE_INTERVAL - elapsed)
        }
    }


    private fun applyScanFilter() {
        for (i in bluetoothDeviceList!!.indices) {
            val device = bluetoothDeviceList!![i]
            val scanItem: ScanBean = deviceList!![i]
            val advData = advDataList!![i]
            val index = filterBluetoothDeviceList!!.indexOf(device)
            var add = false
            if (advData.codeless && scanFilter.codeless) add = true
            if (!add && advData.dsps && scanFilter.dsps) add = true
            if (!add && advData.suota && scanFilter.suota) add = true
            if (!add && advData.other() && scanFilter.other) add = true
            if (!add && advData.unknown() && scanFilter.unknown) add = true
            if (!add && (advData.iBeacon || advData.dialogBeacon) && scanFilter.beacon) add = true
            if (!add && advData.microsoft && scanFilter.microsoft) add = true
            if (add && scanFilter.name != null) add =
                scanFilter.name!!.matcher(if (scanItem.name != null) scanItem.name else "").matches()
            if (add && scanFilter.address != null) add = scanFilter.address!!.matcher(scanItem.address).matches()
            if (add && scanFilter.advData != null) add =
                scanFilter.advData!!.matcher(CodelessUtil.hex(advData.raw)).matches()
            if (scanItem.signal < scanFilter.rssi && add && index == -1) continue
            if (add) {
                if (index == -1) {
                    filterBluetoothDeviceList!!.add(device)
                    filterDeviceList!!.add(scanItem)
                } else {
                    filterDeviceList!![index] = scanItem
                }
            } else if (index != -1) {
                filterBluetoothDeviceList!!.remove(device)
                filterDeviceList!!.removeAt(index)
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onScanRestart(event: ScanRestart) {
        Log.d(TAG, "onScanRestart")
        if (scanner !== event.scanner) return
        startDeviceScan()
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onScanResult(event: CodelessEvent.ScanResult) {
        if (scanner !== event.scanner) return

        val device = event.device
        val advData = event.advData
        val name = if (advData.name != null) advData.name else device.name
        val description: String = getDeviceDescription(advData)
        val rssi = event.rssi

        if(name == null || !name!!.startsWith(getString(R.string.ble_name))){
            return
        }

        if (bluetoothDeviceList!!.contains(device)) {
            val index = bluetoothDeviceList!!.indexOf(device)
            deviceList!![index] = ScanBean(name, device.address)
            advDataList!![index] = advData
            updateList(false)
        } else {
            bluetoothDeviceList!!.add(device)
            deviceList!!.add(ScanBean(name, device.address))
            advDataList!!.add(advData)
            updateList(true)
        }
    }

    private fun getDeviceDescription(advData: AdvData): String {
        val description = ArrayList<String>()
        if (advData.codeless) description.add(getString(R.string.scan_codeless))
        if (advData.dsps) description.add(getString(R.string.scan_dsps))
        if (advData.suota) description.add(getString(R.string.scan_suota))
        if (advData.iot) description.add(getString(R.string.scan_iot))
        if (advData.wearable) description.add(getString(R.string.scan_wearable))
        if (advData.mesh) description.add(getString(R.string.scan_mesh))
        if (advData.proximity) description.add(getString(R.string.scan_proximity))
        if (advData.iBeacon || advData.dialogBeacon) description.add(
            getString(
                R.string.scan_beacon,
                advData.beaconUuid.toString(),
                advData.beaconMajor,
                advData.beaconMinor
            )
        )
        if (advData.microsoft) description.add(getString(R.string.scan_microsoft))
        val text = StringBuilder()
        for (i in description.indices) {
            if (i > 0) text.append(", ")
            text.append(description[i])
        }
        return text.toString()
    }

}


