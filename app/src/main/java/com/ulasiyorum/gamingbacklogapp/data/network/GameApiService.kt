package com.ulasiyorum.gamingbacklogapp.data.network

import com.ulasiyorum.gamingbacklogapp.data.models.Category
import com.ulasiyorum.gamingbacklogapp.data.models.Game
import com.ulasiyorum.gamingbacklogapp.data.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GameApiService {
    @POST("Games/Login")
    suspend fun login(@Body loginDto: LoginDto): Response<User>

    @POST("Games/Register")
    suspend fun register(@Body registerDto: RegisterDto): Response<User>

    @GET("Games/GetGames")
    suspend fun getGames(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("order") order: Int? = null
    ): Response<List<Game>>

    @GET("Games/Search")
    suspend fun searchGames(@Query("query") query: String): Response<List<Game>>

    @GET("Games/GetCategories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("Games/GetByCategory")
    suspend fun getByCategory(@Query("categoryId") categoryId: Int): Response<List<Game>>

    @POST("Games/AddGameToUser")
    suspend fun addGameToUser(@Body addDto: AddGameToUserDto): Response<User>

    @POST("Games/UpdateUserGame")
    suspend fun updateUserGame(@Body updateDto: UpdateUserGameDto): Response<User>

    @DELETE("Games/RemoveGameFromUser")
    suspend fun removeGameFromUser(@Query("userGameId") userGameId: String): Response<User>

    @GET("Games/GetProfile")
    suspend fun getProfile(@Query("userId") userId: Int): Response<User>
}

data class LoginDto(val email: String, val password: String)
data class RegisterDto(val name: String, val email: String, val password: String)
data class AddGameToUserDto(val userId: Int, val gameId: Int)
data class UpdateUserGameDto(val userGameId: String, val state: Int, val notes: String?)
