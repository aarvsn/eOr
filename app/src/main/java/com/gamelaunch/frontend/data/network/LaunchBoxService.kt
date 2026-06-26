package com.gamelaunch.frontend.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface LaunchBoxService {
    @Streaming
    @GET("Metadata.zip")
    suspend fun downloadMetadata(): Response<ResponseBody>
}
