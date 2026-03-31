package com.project.growing.data.plant

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PlantApi {

    @Multipart
    @POST("create_plant")
    suspend fun registerPlant(
        @Query("user_id")        userId        : Int,
        @Query("plant_kind")     plantKind     : String,
        @Query("plant_location") plantLocation : String,
        @Query("pot_size")       potSize       : String,
        @Query("water_cycle")    waterCycle    : String,
        @Part                    image         : MultipartBody.Part,
    ): Response<PlantResponse>
}