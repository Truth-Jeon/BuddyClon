package com.mcnex.albatross.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.diasemi.codelesslib.CodelessEvent
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.mcnex.albatross.R
import com.mcnex.albatross.app.App
import com.mcnex.albatross.app.App.Companion.DEVICE_NAME
import com.mcnex.albatross.app.App.Companion.MAP_MAX_SIZE
import com.mcnex.albatross.app.App.Companion.codlesstutil
import com.mcnex.albatross.app.App.Companion.manager
import com.mcnex.albatross.app.App.Companion.server_base_url
import com.mcnex.albatross.app.App.Companion.server_golf_bin_url
import com.mcnex.albatross.app.App.Companion.server_golfinfo_url
import com.mcnex.albatross.app.App.Companion.server_point_bin_url
import com.mcnex.albatross.databinding.FragmentMapBinding
import com.mcnex.albatross.dialog.TwoButtonDialogFragment
import com.mcnex.albatross.model.Golf
import com.mcnex.albatross.model.GolfInfo
import com.mcnex.albatross.model.Nation
import com.mcnex.albatross.network.NetworkService
import com.mcnex.albatross.util.AlbatrossUtil
import com.mcnex.albatross.util.AlbatrossUtil.Companion.CHUNK_SIZE
import com.mcnex.albatross.util.AlbatrossUtil.Companion.UPDATE_DOWN_START
import com.mcnex.albatross.util.AlbatrossUtil.Companion.UPDATE_GID_CHK
import com.mcnex.albatross.util.AlbatrossUtil.Companion.UPDATE_RESULT_REQ
import com.mcnex.albatross.util.Util
import com.mcnex.albatross.viewmodel.DialogViewModel
import com.mcnex.albatross.viewmodel.DialogViewModel.Companion.DIALOG_OK
import com.mcnex.albatross.viewmodel.EventViewModel
import com.mcnex.albatross.viewmodel.MapViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.getCancellationException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

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
class MapInfoFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = MapInfoFragment::class.java.simpleName

    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!

    var golf_data : Golf? = null

    var golfinfo : GolfInfo? = null

    var downJob : Job? = null

    var bulkCRC : Byte? = null

    val twoButtonDialogFragment : TwoButtonDialogFragment = TwoButtonDialogFragment()

    private val viewModel: MapViewModel by activityViewModels()

    private val dialogViewModel: DialogViewModel by activityViewModels()

    private val eventviewModel: EventViewModel by activityViewModels()


    var data_sending : Boolean = false

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

        EventBus.getDefault().register(this)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val rootview = binding.root

        data_sending = false

        golf_data = arguments?.getSerializable("golf_data") as Golf

        dialogViewModel.dialogEvent.observe(viewLifecycleOwner, Observer { event ->
            Log.d(TAG, "observe ~~~~~~~~~ ++ :: " + event)
            when (event) {
                DIALOG_OK -> {
                    pointbinDown()
                    dialogViewModel.consumeSignInResponse()
                }
            }

        })


        when(App.APP_LANGUAGE){
            "ko", "ja" -> {
                binding.mapTitleText.text = golf_data!!.golf_name.golf_native
                binding.mapText01.text =  String.format("[%s]\n%s", golf_data!!.nation.nation_native, golf_data!!.region.region_native)
            }
            else -> {
                binding.mapTitleText.text = golf_data!!.golf_name.golf_english
                binding.mapText01.text =  String.format("[%s]\n%s", golf_data!!.nation.nation_english, golf_data!!.region.region_english)
            }
        }

        Log.d(TAG, "onCreateView : " +  golf_data!!.golf_name.golf_native)

        requstGolfInfo()

        binding.btnResume.setOnClickListener(){

            twoButtonDialogFragment.show(   requireActivity().supportFragmentManager, "twoButtonDialogFragment")

        }

        binding.btnUpdate.visibility = View.INVISIBLE

        binding.btnUpdate.setOnTouchListener{view : View, motion : MotionEvent -> data_sending}

        binding.btnUpdate.setOnClickListener(){

            data_sending = true
            getENV(AlbatrossUtil.ENV_DATA_REQ)

        }


        return rootview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated ~~~~~~~~~ ++ :: " )

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

    fun File.chunkedSequence(chunk: Int): Sequence<ByteArray> {
        val input = this.inputStream().buffered()
        val buffer = ByteArray(chunk)
        return generateSequence {
            val red = input.read(buffer)
            if (red >= 0) buffer.copyOf(red)
            else {
                input.close()
                null
            }
        }
    }


    private fun requstGolfInfo(){

        lifecycleScope.launch {
            try {
                var networkService = Retrofit.Builder().baseUrl(server_base_url).addConverterFactory(
                    GsonConverterFactory.create()).build().create(NetworkService::class.java)

                String.format(server_golfinfo_url, golf_data!!.nation.nation_code, golf_data!!.region.region_code, golf_data!!.golf_code).let {
                    Log.d(TAG, "requstGolfInfo ddd "  + it )
//                    var text = "api/get.php?what=golf_info&nation=82&region=141&golf=65"
                    networkService.getGolfInfo(it).enqueue(object : Callback<GolfInfo> {
                        override fun onResponse(
                            call: Call<GolfInfo>?,
                            response: Response<GolfInfo>?
                        ) {
                            if(response!!.isSuccessful) {
                                golfinfo = response.body()

                                val sdf = SimpleDateFormat("yyyyMMdd")
                                var strDate = "20"+  golfinfo!!.update_time
                                var date = Date(sdf.parse(strDate).time)

                                var formattedDate: String

                                when(App.APP_LANGUAGE){
                                    "ko" -> {
                                        formattedDate = SimpleDateFormat("YYYY년 MM월 dd일", Locale.KOREA).format(date)
                                    }
                                    else -> {
                                        formattedDate = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(date)
                                    }
                                }

                                binding.mapText02.text =  String.format(getString(R.string.text_golfinfo_value), golfinfo!!.hole_count, formattedDate)

                                if(manager != null) {
                                    golfbinDown()

                                }

                            }

                        }

                        override fun onFailure(call: Call<GolfInfo>, t: Throwable) {
                            Log.d(TAG, "실패$t")
                            if(!App.IS_NET_STATE_CONNET){
                                eventviewModel.onEvent(EventViewModel.NET_ERROR)
                            }

                        }

                    })
                }
            } catch (e: CancellationException){
                Log.d(TAG, "Start requstGolfInfo pre : EEE " + e )
            } finally {


            }

        }
    }


    private fun checkMap(){

        codlesstutil.updateV2Thread(getGolfbin(), UPDATE_GID_CHK, 0, null)

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun setSendLock(event: CodelessEvent.DspsRxData) {

        Log.d(TAG, "Start setSendLock length : " + event.data.size)

        event.data.let {
            if (it.size > 31) {
                if (it[0] == AlbatrossUtil.ACK && it[5] == AlbatrossUtil.MAP_DOWN  &&it[31] == AlbatrossUtil.DtoM) {
                    when(it[6]) {
                        AlbatrossUtil.UPDATE_GID_CHK -> {
                            var updataBuf = ByteArray(4)

                            System.arraycopy( it, 21, updataBuf, 0, 3 )

                            Log.d(TAG, "UPDATE_GID_CHK a: " + golfinfo!!.update_time.toInt())
                            Log.d(TAG, "UPDATE_GID_CHK b : " + Util.byteToInt(updataBuf, ByteOrder.LITTLE_ENDIAN))

                            if( golfinfo!!.update_time.toInt() > Util.byteToInt(updataBuf, ByteOrder.LITTLE_ENDIAN)){

                                lifecycleScope.launch {
                                    try {

                                        var networkService = Retrofit.Builder().baseUrl(server_base_url).addConverterFactory(GsonConverterFactory.create()).build().create(NetworkService::class.java)
                                        networkService.getNation(App.server_notsupport_url + DEVICE_NAME.takeLast(2)).enqueue(object : Callback<List<Nation>> {
                                            override fun onResponse(
                                                call: Call<List<Nation>>?,
                                                response: Response<List<Nation>>?
                                            ) {
                                                if(response!!.isSuccessful) {

                                                    var is_support = true

                                                    response.body().let {
                                                        for(nation in it){
                                                            if(golf_data!!.nation.nation_code == nation.nation_code){
                                                                is_support = false
                                                            }

                                                        }

                                                    }

                                                    if(is_support){
                                                        binding.btnUpdate.visibility = View.VISIBLE
                                                    }

                                                }

                                            }

                                            override fun onFailure(call: Call<List<Nation>>, t: Throwable) {
                                            }

                                        })

                                    } catch (e: CancellationException){
                                    } finally {


                                    }

                                }


                            }else{
                                binding.mapText02.text =  String.format(getString(R.string.text_golfinfo_value), golfinfo!!.hole_count,    getString(R.string.ver_latest_text))
                                binding.btnUpdate.visibility = View.INVISIBLE
                            }

                        }
                        AlbatrossUtil.UPDATE_DOWN_START -> {
                            if(continuation!!.context!!.isActive) {
                                continuation?.resume("aaaaa")
                            }else{
                                Log.d(TAG, "isActive : false")
                            }
                        }
                        AlbatrossUtil.UPDATE_RESULT_REQ -> {
                            Log.d(TAG, "UPDATE_RESULT_REQ printHEX : " +  Util.printHEX(it[24]))

                            if(continuation!!.context!!.isActive) {
                                continuation?.resume("aaaaa")
                            }else{
                                Log.d(TAG, "isActive : false")
                            }

                            if(it[24] == AlbatrossUtil.ACK ){
                                viewModel.onProgress(100)

                                binding.mapText02.text =  String.format(getString(R.string.text_golfinfo_value), golfinfo!!.hole_count,    getString(R.string.ver_latest_text))
                                binding.btnUpdate.visibility = View.INVISIBLE

                            }else{
                                viewModel.onProgress(-1)
                            }

                        }

                    }

                    Log.d( TAG, "GID Format MAP_DOWN bytes : " + Arrays.toString(it)  )

                }else if(it[0] == AlbatrossUtil.ACK && it[5] == AlbatrossUtil.ENV  &&it[31] == AlbatrossUtil.DtoM){

                    data_sending = false

                    var envBuf = ByteArray(it[7].toInt())
                    Arrays.fill(envBuf, 0x00.toByte())
                    System.arraycopy(it, 8, envBuf, 0, it[7].toInt())

                    lifecycleScope.launch {

                      if(envBuf[AlbatrossUtil.BATTERY_INDEX].toInt() > 20){
                          twoButtonDialogFragment.setDialogType(TwoButtonDialogFragment.DIALOG_UPDATE)
                          twoButtonDialogFragment.show(   requireActivity().supportFragmentManager, "twoButtonDialogFragment")
                      }else{
                          eventviewModel.onEvent(EventViewModel.BATTERY_ERROR)
                      }
                    }
                }else if (it[0] == AlbatrossUtil.NACK && it[5] == AlbatrossUtil.MAP_DOWN  &&it[31] == AlbatrossUtil.DtoM  && it[6] == AlbatrossUtil.UPDATE_RESULT_REQ ) {
                    viewModel.onProgress(-1)
                }else{}
            }else if(it.size == 1){

                Log.d(TAG, "printHEX : " +  Util.printHEX(it[0]))

                if(bulkCRC == it[0]){
                    if(continuation?.context!!.isActive) {
                        continuation?.resume("Resumed")
                    }else{
                        Log.d(TAG, "isActive : false")
                    }
                }else{
                    if(downJob?.isActive == true){
                        downJob?.cancel()
                    }else{}

               }
            }else{}
        }
    }


    fun golfbinDown(){
        val file = File(context?.filesDir, "golf.bin")

        if (file.exists()) {

            file.delete()
        }

         var golf_bin_url = server_base_url +  String.format(server_golf_bin_url, golf_data!!.nation.nation_code.toInt(), golf_data!!.region.region_code.toInt(), golf_data!!.golf_code.toInt())

        Log.d(TAG, "golf bin_url: " + golf_bin_url)

        val downloadId = PRDownloader.download(
            golf_bin_url,
            context?.filesDir?.absolutePath,
            "golf.bin"
        )
            .build()
            .setOnProgressListener { }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Log.d(TAG, "golfbinDown onDownloadComplete:")
                    checkMap()
                }

                override fun onError(error: Error) {
                    Log.d(TAG, "onError: $error")

                    if(!App.IS_NET_STATE_CONNET){
                        eventviewModel.onEvent(EventViewModel.NET_ERROR)
                    }

                }
            })

    }


    fun pointbinDown(){

//        viewModel.disable(true)

        val file = File(context?.filesDir, "point.bin")

        if (file.exists()) {
            file.delete()
        }

        var point_bin_url = server_base_url +  String.format(server_point_bin_url, golf_data!!.nation.nation_code.toInt(), golf_data!!.region.region_code.toInt(), golf_data!!.golf_code.toInt())

        Log.d(TAG, "point bin_url: " + point_bin_url)

        val downloadId = PRDownloader.download(
            point_bin_url,
            context?.filesDir?.absolutePath,
            "point.bin"
        )
            .build()
            .setOnProgressListener { }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Log.d(TAG, "pointbinDown onDownloadComplete:")
                    updateMap()
                }

                override fun onError(error: Error) {
                    Log.d(TAG, "onError: $error")
                    viewModel.onProgress(-1)
                }
            })

    }


    fun updateMap(){
        downJob = lifecycleScope.launch {
            try {
                viewModel.disable(true)

                val file = File(context?.filesDir, "point.bin")

                if (file.exists()) {

                    Log.d(TAG, "point size : " + file.length())

                    var sendCount = 0

                    var fileSize = file.length().toInt()

                    if(fileSize > MAP_MAX_SIZE){
                        viewModel.onProgress(-1)
                        return@launch
                    }

                    var chunkCount = fileSize / CHUNK_SIZE

                    if(fileSize % CHUNK_SIZE != 0 ){
                        chunkCount ++
                    }

                    codlesstutil.updateV2Thread(getGolfbin(), UPDATE_DOWN_START, CHUNK_SIZE,    fileSize )

                    pause()

                    file.chunkedSequence(CHUNK_SIZE).forEach {
                        // Do something with `it`

                        viewModel.onProgress(   (100/chunkCount) * sendCount)

                        var crc = codlesstutil.xcrc32(it, CHUNK_SIZE, 0x00.toByte())

                        codlesstutil.sendChunk(it)

                        bulkCRC = crc

                        pause()

                        Log.d(TAG, "it size : " + it.size)

                        Log.d(TAG, "it bulkCRC : " + Util.printHEX(crc))

                        sendCount += 1

                        Log.d(TAG, "it aa : " + sendCount)


                    }

                    viewModel.onProgress(99)

                    codlesstutil.updateV2Thread(getGolfbin(), UPDATE_RESULT_REQ, 0,    null )

                }else{
                    Log.d(TAG, "updateMap : file null")

                    viewModel.onProgress(-1)
                }

            } catch (e: CancellationException){
                Log.d(TAG, "updateMap : EEE " + e )
                viewModel.onProgress(-1)
            } finally {

        }

            }

    }


    fun getGolfbin() : ByteArray{

        var testBuf = ByteArray(16)

        if(false){

            ("82"+  "141" + "65").toInt().let {
                System.arraycopy( codlesstutil.intToByte(it), 0,testBuf, 0, 4 )
            }

            Log.d( TAG, "GID Format bytes : " + Arrays.toString(Util.intToByteArray(8214165))  )

            System.arraycopy( codlesstutil.intToByte(golfinfo!!.update_time.toInt()), 0, testBuf, 13, 3 )
            testBuf[12] = golfinfo!!.hole_count.toByte()

            for(aa in testBuf) {

                Log.d(TAG, "testBuf :  " + Util.printHEX(aa))
            }

            Log.d(TAG, "testBuf tt :  " + golfinfo!!.update_time.toInt())

            Log.d(
                TAG,
                "GID Format 16bytes : " + Arrays.toString(testBuf)
            )

        }else{

            val file = File(context?.filesDir, "golf.bin")

            if (file.exists()) {

                val input = file.inputStream().buffered()
                val buffer = ByteArray(512)

                file.chunkedSequence(512).forEach {
                    // Do something with `it`

                    Log.d(TAG, "it size : " + it.size)

                    testBuf = it
                }


            }

        }

        return testBuf
    }




    private fun getENV(type : Byte) {
        lifecycleScope.launch {
            val reqBuf: ByteArray
            reqBuf = ByteArray(23)
            Arrays.fill(reqBuf, 0x00.toByte())
            App.codlesstutil.forEnv(type, reqBuf)
        }

    }


    private suspend fun pause() = suspendCancellableCoroutine<String> {
        continuation = it

    }

    private var count = 0

    private var continuation: Continuation<String>? = null

    private fun refreshCount() {
        count++
        Log.d(TAG, "refreshCount: $count")

    }

}