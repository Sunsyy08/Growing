package com.project.growing.data.consult

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ConsultApi {

    @POST("consult")
    suspend fun consult(
        @Body request: ConsultRequest,
    ): Response<ConsultResponse>
}