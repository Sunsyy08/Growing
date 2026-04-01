package com.project.growing.data.plant

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PlantApi {

    @Multipart
    @POST("create_plant")
    suspend fun registerPlant(
        @Query("user_id")        userId        : Int,
        @Query("plant_name")     plantName     : String,
        @Query("plant_kind")     plantKind     : String,
        @Query("plant_location") plantLocation : String,
        @Query("pot_size")       potSize       : String,
        @Query("water_cycle")    waterCycle    : String,
        @Part                    image         : MultipartBody.Part,
    ): Response<PlantResponse>

    @GET("get_plantid")
    suspend fun getPlantIds(
        @Query("user_id") userId: Int,
    ): Response<List<PlantIdItemDto>>

    // get_plant_image는 FileResponse라 API 호출 대신 URL 직접 사용
    // getPlantImage 제거

    @GET("get_score")
    suspend fun getPlantScore(
        @Query("plant_id") plantId: Int,
    ): Response<PlantScoreResponse>
}