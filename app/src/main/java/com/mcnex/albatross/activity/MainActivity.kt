package com.mcnex.albatross.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.diasemi.codelesslib.CodelessEvent
import com.diasemi.codelesslib.CodelessEvent.*
import com.diasemi.codelesslib.CodelessManager
import com.google.android.material.navigation.NavigationView
import com.mcnex.albatross.R
import com.mcnex.albatross.app.App
import com.mcnex.albatross.app.App.Companion.APP_LANGUAGE
import com.mcnex.albatross.app.App.Companion.DEVICE_NAME
import com.mcnex.albatross.app.App.Companion.ENV_TIME_OUT
import com.mcnex.albatross.app.App.Companion.IS_NET_STATE_CONNET
import com.mcnex.albatross.app.App.Companion.manager
import com.mcnex.albatross.databinding.ActivityMainBinding
import com.mcnex.albatross.dialog.*
import com.mcnex.albatross.event.TimeOutEvent
import com.mcnex.albatross.fragment.*
import com.mcnex.albatross.network.NetworkConnection
import com.mcnex.albatross.util.AlbatrossUtil.Companion.ACK
import com.mcnex.albatross.util.AlbatrossUtil.Companion.DtoM
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_APP_SET
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_DEVICE
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_DEVICE_SET
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_FAQ
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_MAIN
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_MAP_INFO
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_NOTI
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_SEARCH
import com.mcnex.albatross.util.AlbatrossUtil.Companion.MENU_FRAGMENT_VNV
import com.mcnex.albatross.viewmodel.*
import com.mcnex.albatross.viewmodel.DialogViewModel.Companion.DIALOG_DEVICE_SCAN
import com.mcnex.albatross.viewmodel.DialogViewModel.Companion.DIALOG_DISCONNECT
import com.mcnex.albatross.viewmodel.DialogViewModel.Companion.DIALOG_DISCONNECTED
import com.mcnex.albatross.viewmodel.EventViewModel.Companion.BATTERY_ERROR
import com.mcnex.albatross.viewmodel.EventViewModel.Companion.NET_ERROR
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BaseFragment.OnFragmentInteractionListener {

    private val TAG = MainActivity::class.java.simpleName

    private val deviceViewModel: DeviceViewModel by viewModels()

    private val golfViewModel: GolfViewModel by viewModels()

    private val mapViewModel: MapViewModel by viewModels()

    private val dialogViewModel: DialogViewModel by viewModels()

    private val moveViewModel: FragmentMoveViewModel by viewModels()

    private val eventViewModel: EventViewModel by viewModels()

    val lodingProgressDialogFragment : LodingProgressDialogFragment = LodingProgressDialogFragment()

    val progressDialogFragment : ProgressDialogFragment = ProgressDialogFragment()

    val twoButtonDialogFragment : TwoButtonDialogFragment = TwoButtonDialogFragment()

    val oneButtonDialogFragment : OneButtonDialogFragment = OneButtonDialogFragment()

    //For View
    private var status_view = 0
    private var last_view = 0

    //For BLE
    private var bleConnection = false

    private var scanFragment: ScanFragment? = null
    private var notiFragment: Fragment? = null
    private var deviceFragment: Fragment? = null
    private var deviceSetFragment: Fragment? = null
    private var mapInfoFragment: Fragment? = null
    private var searchFragment: Fragment? = null
    private var appSetFragment: Fragment? = null
    private var faqFragment: Fragment? = null
    private var vnvFragment: Fragment? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    var timeoutJob : Job? = null


    var backKeyPressedTime : Long = 0
    var toast : Toast? = null

    var dialogTime : Long = 0

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        APP_LANGUAGE = applicationContext.resources.configuration.locales[0].language

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        binding.btnDisconnect.visibility = View.INVISIBLE

        navView.setNavigationItemSelectedListener(this)

        EventBus.getDefault().register(this)


        deviceViewModel.selectedItem.observe(this, Observer { device ->
            // Perform an action with the latest item data
            Log.d(TAG, "selectedItem :: " + device.name)

            lodingProgressDialogFragment.isCancelable = false
            lodingProgressDialogFragment.setDialogType(LodingProgressDialogFragment.DIALOG_CONNECTING)
            lodingProgressDialogFragment.show(
                supportFragmentManager, "lodingProgressDialogFragment"
            )

            timeoutcheck(ENV_TIME_OUT)

            manager = CodelessManager(baseContext, device)
            manager!!.connect()

        })

        deviceViewModel.event.observe(this, Observer { event ->
            Log.d(TAG, "event ~~~~~~~~~ ++ :: " + event)
            if(event){
                lodingProgressDialogFragment.isCancelable = false
                lodingProgressDialogFragment.setDialogType(LodingProgressDialogFragment.DIALOG_SEARCHING)
                lodingProgressDialogFragment.show(
                    supportFragmentManager, "lodingProgressDialogFragment"
                )
            }else{
                lodingProgressDialogFragment.dismiss()
            }
        })

        golfViewModel.selectedItem.observe(this, Observer { golf ->
            // Perform an action with the latest item data
            Log.d(TAG, "selectedItem  :: " + golf.golf_name.golf_native)

            getFragment(MENU_FRAGMENT_MAP_INFO)?.let {

                val bundle = Bundle()
                bundle.putSerializable("golf_data", golf )
                it.arguments = bundle
                changeFragment(it)
            }
        })


        mapViewModel.show.observe(this, Observer { event ->
            // Perform an action with the latest item data
            Log.d(TAG, "show  :: " + event)
            if(event){
                progressDialogFragment.isCancelable = false
                if (!progressDialogFragment.isAdded) {
                    progressDialogFragment.show(
                        supportFragmentManager, "progressDialogFragment"
                    )
                }
            }else{
                progressDialogFragment.dismiss()
            }
        })

        mapViewModel.progress.observe(this, Observer { event ->
            //            // Perform an action with the latest item data
            Log.d(TAG, "progress  :: " + event)
            when(event){
                -1 -> {
                    progressDialogFragment.dismiss()

                    showDiolog(OneButtonDialogFragment.DIALOG_UPDATE_FILE)
                }
                100 -> {
                    progressDialogFragment.dismiss()

                    showDiolog(OneButtonDialogFragment.DIALOG_UPDATE)
                }
                else -> {
                    if(progressDialogFragment.dialog!!.isShowing){
                        progressDialogFragment.setProgress(event)
                    }
                }
            }
        })




        dialogViewModel.dialogEvent.observe(this, Observer { event ->
            Log.d(TAG, "dialogEvent :: " + event)
            event?.let {
                when (event) {
                    DIALOG_DISCONNECT -> {
                            if (manager != null) {
                                manager!!.disconnect()
                                setBleConnection(false)
                            }

                        showDisConnectedDiolog()
                        dialogViewModel.consumeSignInResponse()

                    }
                    DIALOG_DISCONNECTED -> {
                        getFragment(MENU_FRAGMENT_MAIN)?.let { changeFragment(it) }
                        dialogViewModel.consumeSignInResponse()
                    }
                    DialogViewModel.DIALOG_NOTI -> {
                        var pref =  getSharedPreferences("albatoss", Context.MODE_PRIVATE)
                        var noti_ver = pref.getInt("noti_ver", 0)
                        var noti_check_ver = pref.getInt("noti_check_ver", 0)

                        if (noti_check_ver < noti_ver){
                            binding.mainLayout.btnNoti.setBackgroundResource(R.drawable.ic_top_noti_off)

                            var editor = pref.edit()
                            editor.putInt("noti_check_ver", noti_ver)
                            editor.commit()
                        }

                        val i = Intent(Intent.ACTION_VIEW)
                        when(APP_LANGUAGE){"ko" -> i.data = Uri.parse("https://eyeclonview.com/albatross/notice_ko.html")
                            "ja" -> i.data = Uri.parse("https://eyeclonview.com/albatross/notice_jp.html")
                            else -> i.data = Uri.parse("https://eyeclonview.com/albatross/notice_en.html")
                        }

                        startForResult.launch(i)
                        dialogViewModel.consumeSignInResponse()
                    }
                    DIALOG_DEVICE_SCAN -> {
                        scanFragment?.startDeviceScan()
                    }
                    else -> {

                    }
                }

            }
        })


        moveViewModel.moveEvent.observe(this, Observer { event ->
            when (event) {
                MENU_FRAGMENT_DEVICE_SET -> {
                    getFragment(MENU_FRAGMENT_DEVICE_SET)?.let { changeFragment(it) }
                }
            }
        })

        eventViewModel.onEvent.observe(this, Observer { event ->
                when (event) {
                    NET_ERROR -> {
                        showDiolog(OneButtonDialogFragment.DIALOG_NET_ERROR)
                    }
                    BATTERY_ERROR -> {
                        showDiolog(OneButtonDialogFragment.DIALOG_BATTERY_ERROR)
                    }
                    else -> {
                    }
                }
        })


        var pref =  getSharedPreferences("albatoss", Context.MODE_PRIVATE)
        var noti_ver = pref.getInt("noti_ver", 0)

        intent.getIntExtra("noti_ver", 0).let {
            if(it > noti_ver){
                var editor = pref.edit()
                editor.putInt("noti_ver", it)
                editor.commit()

                noti_ver = it

                getFragment(MENU_FRAGMENT_MAIN)?.let {
                    scanFragment?.isAutoScan = false
                    changeFragment(it)
                }

                showTwoDiolog(TwoButtonDialogFragment.DIALOG_NOTI)
            }else{
                getFragment(MENU_FRAGMENT_MAIN)?.let { changeFragment(it) }
            }
        }



        var noti_check_ver = pref.getInt("noti_check_ver", 0)

        if (noti_check_ver < noti_ver){
            binding.mainLayout.btnNoti.setBackgroundResource(R.drawable.ic_top_noti_on)
        }

        val connection = NetworkConnection(applicationContext)
        connection.observe(this, Observer { isConnected ->
            if (isConnected) {
                IS_NET_STATE_CONNET = true
            } else {
                IS_NET_STATE_CONNET = false
            }
        })

    }

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        scanFragment?.startDeviceScan()
    }


    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            when (status_view) {
                MENU_FRAGMENT_MAP_INFO -> {

                    getFragment(MENU_FRAGMENT_SEARCH)?.let {

                        val bundle = Bundle()
                        bundle.putBoolean("data_clear", false)
                        it.arguments = bundle
                        changeFragment(it)
                    }
                }
                MENU_FRAGMENT_DEVICE_SET -> {
                    getFragment(MENU_FRAGMENT_DEVICE)?.let { changeFragment(it) }
                }
                else ->   {
                    if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                        backKeyPressedTime = System.currentTimeMillis()
                        toast = Toast.makeText(this, getString(R.string.app_finish_message), Toast.LENGTH_LONG)
                        toast!!.show()
                    }else{
                        toast!!.cancel()
                        toast!!.show()
                        super.onBackPressed()
                    }

                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                getFragment(MENU_FRAGMENT_MAIN)?.let { changeFragment(it) }

            }
            R.id.nav_search -> {
                getFragment(MENU_FRAGMENT_SEARCH)?.let {

                    val bundle = Bundle()
                    bundle.putBoolean("data_clear", true)
                    it.arguments = bundle

                    changeFragment(it)
                }
            }
//            R.id.nav_setting -> {
//                getFragment(MENU_FRAGMENT_APP_SET)?.let { changeFragment(it) }
//            }
            R.id.nav_faq -> {

                if(!App.IS_NET_STATE_CONNET){
                    showDiolog(OneButtonDialogFragment.DIALOG_NET_ERROR)
                }else {
                    val i = Intent(Intent.ACTION_VIEW)

                    when(APP_LANGUAGE){
                    "ko" -> i.data = Uri.parse("https://eyeclonview.com/albatross/product_ko.html")
                    "ja" -> i.data = Uri.parse("https://eyeclonview.com/albatross/product_jp.html")
                    else -> i.data = Uri.parse("https://eyeclonview.com/albatross/product_en.html")
                    }
                    startActivity(i)
                }
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    fun changeFragment( fragment :Fragment) {
        val mFragmentManager = supportFragmentManager
        val mFragmentTransaction = mFragmentManager.beginTransaction()

        mFragmentTransaction.replace(R.id.main_fragment_view, fragment).commit()
    }


    fun addFragment( fragment :Fragment) {
        val mFragmentManager = supportFragmentManager
        val mFragmentTransaction = mFragmentManager.beginTransaction()

        mFragmentTransaction.add(R.id.main_fragment_view, fragment)
        mFragmentTransaction.addToBackStack(null)
        mFragmentTransaction.commit()

    }


    fun getFragment(viewId: Int): Fragment? {
        last_view = status_view
        status_view = viewId

        init_main_header(viewId)

        return when (viewId) {
            MENU_FRAGMENT_MAIN -> {
                Log.d(TAG, "MENU_FRAGMENT_MAIN" + bleConnection)
                if (!bleConnection) {
                    if (scanFragment == null) {
                        scanFragment = ScanFragment()
                    }
                    scanFragment?.isAutoScan = true

                    scanFragment
                } else {
                    if (deviceFragment == null) deviceFragment = DeviceFragment()
                    deviceFragment
                }
            }
            MENU_FRAGMENT_NOTI -> {
//                if (notiFragment == null) notiFragment = NotiFragment()
                notiFragment
            }
            MENU_FRAGMENT_DEVICE -> {
                if (deviceFragment == null) deviceFragment = DeviceFragment()
                deviceFragment
            }
            MENU_FRAGMENT_DEVICE_SET -> {
                if (deviceSetFragment == null) deviceSetFragment = DeviceSetFragment()
                deviceSetFragment
            }
            MENU_FRAGMENT_SEARCH -> {
                if (searchFragment == null) searchFragment = SearchFragment()
                searchFragment
            }
            MENU_FRAGMENT_MAP_INFO -> {
                if (mapInfoFragment == null) mapInfoFragment = MapInfoFragment()
                mapInfoFragment
            }
            MENU_FRAGMENT_APP_SET -> {
                if (appSetFragment == null) appSetFragment = AppSetFragment()
                appSetFragment
            }
            MENU_FRAGMENT_FAQ -> {
//                if (faqFragment == null) faqFragment = FAQFragment()
                faqFragment
            }
            MENU_FRAGMENT_VNV -> {
//                if (vnvFragment == null) vnvFragment = VnVFragment()
                vnvFragment
            }
            else -> Fragment()
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDeviceReady(event: Ready) {
        if (manager !== event.manager) return
        Log.d(TAG, "onDeviceReady ed ~~~~~~~~~~~~" + manager!!.isConnected)
        Log.d(TAG, "onDeviceReady cm ~~~~~~~~~~~~" + manager!!.commandMode())
        manager!!.sendCommand("AT+BINREQ")
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onModeChange(event: CodelessEvent.Mode) {
        if (manager !== event.manager) return

        Log.d(TAG, "onModeChange :: " + event.command)
        if (!event.command) {
            lifecycleScope.launch {
                delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
                getFragment(MENU_FRAGMENT_MAIN)?.let { changeFragment(it) }

            }
        } else {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onCodelessLine(event: CodelessLine) {
        if (manager !== event.manager) return
        Log.d(TAG, "onCodelessLine :: " + event.line.text)

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDspsTxFlowControl(event: DspsTxFlowControl) {
        if (manager !== event.manager) return

        Log.d(TAG, "onDspsTxFlowControl :: " + event.flowOn)

    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onConnection(event: CodelessEvent.Connection) {
        Log.d(TAG, "onConnection ing ~~~~~~~~~~~" + manager?.isConnecting)
        Log.d(TAG, "onConnection ed ~~~~~~~~~~~~" + manager?.isConnected)
        Log.d(TAG, "onConnection cm ~~~~~~~~~~~~" + manager?.commandMode())

        if (manager !== event.manager) {
            Log.d(TAG, "onConnection aa")
            return
        }

        if(manager!!.isConnected) {
            Log.d(TAG, "onConnection adress~~~~~~~~~~~~" + manager!!.device.address)
            Log.d(TAG, "onConnection name ~~~~~~~~~~~~" + manager!!.readDeviceName().toString())
            setBleConnection(true)
        }

        if (manager!!.isDisconnected) {
            setBleConnection(false)

            showDisConnectedDiolog()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun setSendLock(event: DspsRxData) {
        Log.d(TAG, "Start setSendLock length : " + event.data.size)

        if(timeoutJob?.isActive == true){
            timeoutJob?.cancel()
        }


        event.data.let {
            if(it.size > 31){
                if (it[0] == ACK && it[31] == DtoM) {

                    if(lodingProgressDialogFragment?.dialog?.isShowing == true){
                        lodingProgressDialogFragment?.dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (manager != null) {
            if (manager!!.isConnected) {
                manager!!.disconnect()
            }
            manager = null
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        Log.d(TAG, "onDestroy")
    }


    fun isBleConnection(): Boolean {
        return bleConnection
    }

    fun setBleConnection(status: Boolean) {
        bleConnection = status
        if (!status) {
            if (manager != null) {
                manager = null
                DEVICE_NAME = ""
            }
            binding.btnDisconnect.visibility = View.INVISIBLE
        }else{
            binding.btnDisconnect.visibility = View.VISIBLE
        }
    }

    fun btnClick(view: View){
        when (view.id) {
            R.id.btn_menu -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.btn_disconnect -> {
                showDisConnectDiolog()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.btn_noti -> {
                if(!App.IS_NET_STATE_CONNET){
                    showDiolog(OneButtonDialogFragment.DIALOG_NET_ERROR)
                }else {
                    val i = Intent(Intent.ACTION_VIEW)

                    when(APP_LANGUAGE){
                        "ko" -> i.data = Uri.parse("https://eyeclonview.com/albatross/notice_ko.html")
                        "ja" -> i.data = Uri.parse("https://eyeclonview.com/albatross/notice_jp.html")
                        else -> i.data = Uri.parse("https://eyeclonview.com/albatross/notice_en.html")
                    }


                    startActivity(i)

                    var pref =  getSharedPreferences("albatoss", Context.MODE_PRIVATE)
                    var noti_ver = pref.getInt("noti_ver", 0)
                    var noti_check_ver = pref.getInt("noti_check_ver", 0)

                    if (noti_check_ver < noti_ver){
                        binding.mainLayout.btnNoti.setBackgroundResource(R.drawable.ic_top_noti_off)

                        var editor = pref.edit()
                        editor.putInt("noti_check_ver", noti_ver)
                        editor.commit()
                    }
                }
            }
            R.id.btn_pre -> {
                onBackPressed()
            }
            R.id.btn_search -> {

                getFragment(MENU_FRAGMENT_SEARCH)?.let {

                    val bundle = Bundle()
                    bundle.putBoolean("data_clear", true)
                    it.arguments = bundle

                    changeFragment(it)
                }


            }

            else -> println("default") }


    }


    fun init_main_header(viewId: Int){
        when (viewId) {
            MENU_FRAGMENT_MAIN -> {
                if(DEVICE_NAME.length == 0){
                    binding.mainLayout.textMainTitle.text = getString(R.string.app_name)
                }else{
                    binding.mainLayout.textMainTitle.text = DEVICE_NAME
                }

                binding.mainLayout.btnMenu.visibility = View.VISIBLE
                binding.mainLayout.btnPre.visibility = View.INVISIBLE
                binding.mainLayout.btnSearch.visibility = View.VISIBLE
                binding.mainLayout.btnNoti.visibility = View.VISIBLE

            }
            MENU_FRAGMENT_NOTI -> {
            }
            MENU_FRAGMENT_DEVICE -> {

                binding.mainLayout.textMainTitle.text = DEVICE_NAME

                binding.mainLayout.btnMenu.visibility = View.VISIBLE
                binding.mainLayout.btnPre.visibility = View.INVISIBLE
                binding.mainLayout.btnSearch.visibility = View.VISIBLE
                binding.mainLayout.btnNoti.visibility = View.VISIBLE

            }
            MENU_FRAGMENT_DEVICE_SET -> {
                binding.mainLayout.textMainTitle.text = getString(R.string.title_device_settting)

                binding.mainLayout.btnMenu.visibility = View.INVISIBLE
                binding.mainLayout.btnPre.visibility = View.VISIBLE
                binding.mainLayout.btnSearch.visibility = View.VISIBLE
                binding.mainLayout.btnNoti.visibility = View.VISIBLE

            }
            MENU_FRAGMENT_SEARCH -> {
                binding.mainLayout.textMainTitle.text = getString(R.string.title_search)

                binding.mainLayout.btnMenu.visibility = View.VISIBLE
                binding.mainLayout.btnPre.visibility = View.INVISIBLE
                binding.mainLayout.btnSearch.visibility = View.INVISIBLE
                binding.mainLayout.btnNoti.visibility = View.VISIBLE

            }
            MENU_FRAGMENT_MAP_INFO -> {
                binding.mainLayout.textMainTitle.text = getString(R.string.title_golf_course)

                binding.mainLayout.btnMenu.visibility = View.INVISIBLE
                binding.mainLayout.btnPre.visibility = View. VISIBLE
                binding.mainLayout.btnSearch.visibility = View.INVISIBLE
                binding.mainLayout.btnNoti.visibility = View.VISIBLE


            }
            MENU_FRAGMENT_APP_SET -> {
            }
            MENU_FRAGMENT_FAQ -> {
            }
            MENU_FRAGMENT_VNV -> {
            }
        }

    }

    fun timeoutcheck(time : Int){

        if(timeoutJob?.isActive == true){
            timeoutJob?.cancel()
        }

        timeoutJob = lifecycleScope.launch {
            try {

                delay(time.toLong())

                if (manager != null) {
                    manager!!.disconnect()
                    setBleConnection(false)
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)

                if(lodingProgressDialogFragment?.dialog?.isShowing == true){
                    lodingProgressDialogFragment?.dismiss()
                }

                if(progressDialogFragment?.dialog?.isShowing == true){
                    progressDialogFragment?.dismiss()
                }

                showDiolog(OneButtonDialogFragment.DIALOG_CONECT_ERROR)

            } catch (e: CancellationException) {
                Log.d(TAG, "timeoutcheck : E " + e)
            }catch (e : Exception){
                Log.d(TAG, "timeoutcheck : E " + e)
            } finally {

            }

        }
    }


    @Subscribe
    fun setTimeOut(event: TimeOutEvent) {
        Log.d(TAG, "setTimeOut : " + event.timeout)
        timeoutcheck(event.timeout)

    }


    private fun dismissDialogs() {
        supportFragmentManager.fragments.takeIf { it.isNotEmpty() }
            ?.map { (it as? DialogFragment)?.dismiss() }
    }


    fun showDisConnectedDiolog(){
        dismissDialogs()

        oneButtonDialogFragment.setDialogType(OneButtonDialogFragment.DIALOG_DISCONECTED)
        oneButtonDialogFragment.show(
            supportFragmentManager, "oneButtonDialogFragment"
        )
    }

    fun showUpdateDiolog(ok : Boolean){
        if(ok) {
            oneButtonDialogFragment.setDialogType(OneButtonDialogFragment.DIALOG_UPDATE)
        }else{
            oneButtonDialogFragment.setDialogType(OneButtonDialogFragment.DIALOG_UPDATE_FILE)
        }
        oneButtonDialogFragment.show(
            supportFragmentManager, "oneButtonDialogFragment"
        )
    }

    fun showDiolog(type : Int){
        if (System.currentTimeMillis() < dialogTime + 500) {
            return
        }

        dialogTime = System.currentTimeMillis()

        oneButtonDialogFragment.setDialogType(type)

        if(!oneButtonDialogFragment.isAdded) {
            oneButtonDialogFragment.show(supportFragmentManager, "oneButtonDialogFragment")
        }

    }

    fun showDisConnectDiolog(){
        twoButtonDialogFragment.setDialogType(TwoButtonDialogFragment.DIALOG_DISCONECT)
        twoButtonDialogFragment.show(
            supportFragmentManager, "twoButtonDialogFragment"
        )
    }


    fun showTwoDiolog(type : Int){

        twoButtonDialogFragment.setDialogType(type)

        twoButtonDialogFragment.show(
            supportFragmentManager, "twoButtonDialogFragment"
        )

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this@MainActivity, "result ok!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "result cancle!", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == 1) {
        }
    }

}
