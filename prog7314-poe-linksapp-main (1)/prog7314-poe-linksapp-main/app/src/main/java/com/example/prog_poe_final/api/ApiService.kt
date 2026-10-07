package com.example.prog_poe_final.api

import com.example.prog_poe_final.models.BioUpdateRequest
import com.example.prog_poe_final.models.Notification
import com.example.prog_poe_final.models.Message
import com.example.prog_poe_final.models.MessageRequest
import com.example.prog_poe_final.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register")
    fun register(@Body user: User): Call<Map<String, String>>

    @POST("auth/login")
    fun login(@Body credentials: Map<String, String>): Call<Map<String, Any>>

    @GET("auth/current")
    fun getCurrentUser(): Call<User>

    @GET("auth/search")
    fun searchUsers(@Query("query") query: String): Call<List<User>>

    @POST("chats/create")
    fun createOrFetchChat(@Body chatRequest: Map<String, Int>): Call<Map<String, Any>>

    @POST("chats/sendMessage")
    fun sendMessage(@Body messageRequest: MessageRequest): Call<Map<String, Any>>

    @GET("chats/messages/{chatId}")
    fun getChatMessages(@Path("chatId") chatId: Int): Call<List<Message>>

    @POST("auth/updateFcmToken")
    fun updateFcmToken(@Body body: Map<String, @JvmSuppressWildcards Any>): Call<Map<String, Any>>

    @GET("notifications/{userId}")
    fun getNotifications(@Path("userId") userId: Int): Call<List<Notification>>

    @POST("auth/updateBio")
    fun updateBio(@Body body: BioUpdateRequest): Call<Map<String, Any>>

}