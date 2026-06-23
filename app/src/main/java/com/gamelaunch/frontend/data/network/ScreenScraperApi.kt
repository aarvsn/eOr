package com.gamelaunch.frontend.data.network

import com.gamelaunch.frontend.data.network.dto.ScraperResponseDto
import com.gamelaunch.frontend.data.network.dto.UserInfoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScreenScraperApi {

    @GET("jeuInfos.php")
    suspend fun getGameInfo(
        @Query("devid") devId: String,
        @Query("devpassword") devPassword: String,
        @Query("softname") softName: String,
        @Query("ssid") ssId: String,
        @Query("sspassword") ssPassword: String,
        @Query("systemeid") systemId: Int,
        @Query("romname") romName: String,
        @Query("md5") md5: String? = null,
        @Query("crc") crc: String? = null,
        @Query("output") output: String = "json"
    ): Response<ScraperResponseDto>

    @GET("userInfos.php")
    suspend fun validateCredentials(
        @Query("devid") devId: String,
        @Query("devpassword") devPassword: String,
        @Query("softname") softName: String,
        @Query("ssid") ssId: String,
        @Query("sspassword") ssPassword: String,
        @Query("output") output: String = "json"
    ): Response<UserInfoDto>
}
