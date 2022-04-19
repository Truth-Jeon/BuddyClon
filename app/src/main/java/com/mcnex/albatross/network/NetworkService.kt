package com.mcnex.albatross.network

import com.mcnex.albatross.model.Golf
import com.mcnex.albatross.model.GolfInfo
import com.mcnex.albatross.model.Nation
import com.mcnex.albatross.model.QuickName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface NetworkService {


    @GET
    fun getGolfName(@Url name : String ): Call<List<QuickName>>

    @GET
    fun getGolf(@Url name : String ): Call<List<Golf>>

    @GET
    fun getGolfInfo(@Url name : String ): Call<GolfInfo>

    @GET
    fun getNation(@Url name : String ): Call<List<Nation>>

}