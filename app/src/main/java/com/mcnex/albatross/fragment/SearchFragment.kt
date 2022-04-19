package com.mcnex.albatross.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.FloatingSearchView.OnSearchListener
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.mcnex.albatross.R
import com.mcnex.albatross.adapter.GolfSearchAdapter
import com.mcnex.albatross.app.App
import com.mcnex.albatross.app.App.Companion.server_base_url
import com.mcnex.albatross.app.App.Companion.server_fserarch_url
import com.mcnex.albatross.app.App.Companion.server_rserarch_url
import com.mcnex.albatross.databinding.FragmentSearchBinding
import com.mcnex.albatross.model.Golf
import com.mcnex.albatross.model.QuickName
import com.mcnex.albatross.network.NetworkService
import com.mcnex.albatross.viewmodel.EventViewModel
import com.mcnex.albatross.viewmodel.EventViewModel.Companion.NET_ERROR
import com.mcnex.albatross.viewmodel.GolfViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
class SearchFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = SearchFragment::class.java.simpleName

    private var _binding: FragmentSearchBinding? = null

    private val binding get() = _binding!!


    private lateinit var golfSearchAdapter: GolfSearchAdapter

    var quickJob : Job? = null

    private val viewModel: GolfViewModel by activityViewModels()

    private val eventviewModel: EventViewModel by activityViewModels()

    var golf_data : List<Golf>? = null

    var is_data_clear : Boolean = false


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

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val rootview = binding.root

        golfSearchAdapter = GolfSearchAdapter { int -> adapterOnClick(int) }

        val dividerItemDecoration =    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        dividerItemDecoration.setDrawable(context?.let { ContextCompat.getDrawable(it,R.drawable.recyclerview_divider) }!!)
        binding.searchListView.addItemDecoration(dividerItemDecoration)

        binding.searchListView.adapter = golfSearchAdapter

        is_data_clear = arguments?.getBoolean("data_clear", true)!!

        setupFloatingSearch()

        if(is_data_clear){
            golf_data = null
        }

        if(golf_data != null) {
            golfSearchAdapter.submitList(golf_data!!)
            golfSearchAdapter.notifyDataSetChanged()
        }

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

        if(is_data_clear){
            binding.floatingSearchView.setSearchText("")
            is_data_clear = false
        }

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


    //Search View listener
    private fun setupFloatingSearch() {

        binding.floatingSearchView.setOnQueryChangeListener { oldQuery, newQuery ->

            if (oldQuery != "" && newQuery == "") {
                binding.floatingSearchView.clearSuggestions()
            } else if(newQuery.length > 1){
                binding.floatingSearchView.showProgress()

                if(quickJob?.isActive == true){
                    Log.d(TAG, "Start quickJob isActive " )
                    quickJob?.cancel()
                }

                quickJob = lifecycleScope.launch {
                    try {
                        Log.d(TAG, "Start initView pre : aaa " + newQuery)

                        var networkService = Retrofit.Builder().baseUrl(server_base_url).addConverterFactory(GsonConverterFactory.create()).build().create(NetworkService::class.java)

                        networkService.getGolfName(server_rserarch_url + newQuery).cancel()

                        networkService.getGolfName(server_rserarch_url + newQuery).enqueue(object : Callback<List<QuickName>> {
                            override fun onResponse(
                                call: Call<List<QuickName>>?,
                                response: Response<List<QuickName>>?
                            ) {
                                if(response!!.isSuccessful) {
                                    Log.d("test", response.body().toString())
                                    var data = response.body() // GsonConverter를 사용해 데이터매핑

                                    binding.floatingSearchView.swapSuggestions(data)
                                }

                                binding.floatingSearchView.hideProgress()
                            }

                            override fun onFailure(call: Call<List<QuickName>>, t: Throwable) {
                                Log.d("test", "aaa 실패$t")

                                if(!App.IS_NET_STATE_CONNET){
                                    eventviewModel.onEvent(NET_ERROR)
                                }

                                binding.floatingSearchView.hideProgress()
                            }

                        })

                        binding.floatingSearchView.hideProgress()

                    } catch (e: CancellationException){
                        Log.d(TAG, "Start initView pre : EEE " + e )
                    } finally {

                    }

                }


            }else{
                if(quickJob?.isActive == true){
                    quickJob?.cancel()
                }
                binding.floatingSearchView.clearSuggestions()
                binding.floatingSearchView.hideProgress()
            }
        }


        binding.floatingSearchView.setOnSearchListener(object : OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                Log.d(TAG, "onSuggestionClicked()" + searchSuggestion.body)

                requstGolfList(searchSuggestion.body)

                binding.floatingSearchView.clearSearchFocus()
            }

            override fun onSearchAction(query: String) {
                Log.d(TAG, "onSearchAction()" + query)

                requstGolfList(query)
            }
        })


        binding.floatingSearchView.left


        binding.floatingSearchView.setOnFocusChangeListener(object : FloatingSearchView.OnFocusChangeListener {
            override fun onFocus() { //show suggestions when search bar gains focus (typically history suggestions)
                Log.d(TAG, "onFocus()")

                golf_data = ArrayList()

                golfSearchAdapter.submitList(golf_data)
                golfSearchAdapter.notifyDataSetChanged()

                if (binding.floatingSearchView.query.length > 1){

                    binding.floatingSearchView.showProgress()

                if (quickJob?.isActive == true) {
                    quickJob?.cancel()
                }

                quickJob = lifecycleScope.launch {
                    try {

                        var networkService = Retrofit.Builder().baseUrl(server_base_url)
                            .addConverterFactory(GsonConverterFactory.create()).build()
                            .create(NetworkService::class.java)

                        networkService.getGolfName(server_rserarch_url + binding.floatingSearchView.query)
                            .enqueue(object : Callback<List<QuickName>> {
                                override fun onResponse(
                                    call: Call<List<QuickName>>?,
                                    response: Response<List<QuickName>>?
                                ) {
                                    if (response!!.isSuccessful) {
                                        Log.d("test", response.body().toString())
                                        var data = response.body() // GsonConverter를 사용해 데이터매핑

                                        binding.floatingSearchView.swapSuggestions(data)
                                    }

                                    binding.floatingSearchView.hideProgress()
                                }

                                override fun onFailure(call: Call<List<QuickName>>, t: Throwable) {
                                    Log.d("test", " a 실패$t")

                                    if(!App.IS_NET_STATE_CONNET){
                                        eventviewModel.onEvent(NET_ERROR)
                                    }

                                    binding.floatingSearchView.hideProgress()
                                }

                            })
                        binding.floatingSearchView.hideProgress()

                    } catch (e: CancellationException) {
                        Log.d(TAG, "Start initView pre : EEE " + e)
                    } finally {


                    }

                }
                }
            }

            override fun onFocusCleared() { //set the title of the bar so that when focus is returned a new query begins
                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                Log.d(TAG, "onFocusCleared()")
            }
        })

    }


    override fun onPause() {
        super.onPause()
        binding.floatingSearchView.clearSearchFocus()
    }


    private fun requstGolfList(name :String){

        lifecycleScope.launch {
            try {

                var networkService = Retrofit.Builder().baseUrl(server_base_url).addConverterFactory(GsonConverterFactory.create()).build().create(NetworkService::class.java)
                networkService.getGolf(server_fserarch_url + name).enqueue(object : Callback<List<Golf>> {
                    override fun onResponse(
                        call: Call<List<Golf>>?,
                        response: Response<List<Golf>>?
                    ) {
                        if(response!!.isSuccessful) {
                            golf_data = response.body()

                            golfSearchAdapter.submitList(golf_data)
                            golfSearchAdapter.notifyDataSetChanged()

                        }

                        binding.floatingSearchView.hideProgress()
                    }

                    override fun onFailure(call: Call<List<Golf>>, t: Throwable) {
                        Log.d("test", "b 실패$t")

                        if(!App.IS_NET_STATE_CONNET){
                            eventviewModel.onEvent(NET_ERROR)
                        }

                        binding.floatingSearchView.hideProgress()
                    }

                })

                binding.floatingSearchView.hideProgress()

            } catch (e: CancellationException){
                Log.d(TAG, "Start initView pre : EEE " + e )
            } finally {


            }

        }
    }


    /* Opens FlowerDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(position: Int) {
        Log.d(TAG, "onItemClick 1")
        viewModel.onReceivedGolf(golf_data!!.get(position))
    }

}