package com.example.fitbody.api

import com.example.fitbody.model.CheckIn
import com.example.fitbody.model.LoginResponse
import com.example.fitbody.model.Progress
import com.example.fitbody.model.Schedule
import com.example.fitbody.model.SimpleResponse
import com.example.fitbody.model.Trainer
import com.example.fitbody.model.Workout
import com.example.fitbody.model.Product
import com.example.fitbody.model.CartItem
import com.example.fitbody.model.WorkoutStatsResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiService {

    @GET("get_trainers.php")
    fun getTrainers(): Call<List<Trainer>>

    @GET("get_workouts.php")
    fun getWorkouts(
        @Query("trainer_id") trainerId: Int
    ): Call<List<Workout>>

    @GET("get_progress.php")
    fun getProgress(
        @Query("user_id") userId: Int
    ): Call<List<Progress>>

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("social_login.php")
    fun socialLogin(
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("social_id") socialId: String,
        @Field("provider") provider: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("add_favorite.php")
    fun addFavorite(
        @Field("user_id") userId: Int,
        @Field("trainer_id") trainerId: Int
    ): Call<SimpleResponse>

    @GET("get_favorites.php")
    fun getFavorites(
        @Query("user_id") userId: Int
    ): Call<List<Trainer>>

    @FormUrlEncoded
    @POST("remove_favorite.php")
    fun removeFavorite(
        @Field("user_id") userId: Int,
        @Field("trainer_id") trainerId: Int
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("add_workout.php")
    fun addWorkout(
        @Field("trainer_id") trainerId: Int,
        @Field("workout_name") workoutName: String,
        @Field("sets_count") setsCount: String,
        @Field("reps_count") repsCount: String,
        @Field("muscle_group") muscleGroup: String
    ): Call<SimpleResponse>

    @GET("get_schedule.php")
    fun getSchedule(
        @Query("user_id") userId: Int
    ): Call<List<Schedule>>

    @FormUrlEncoded
    @POST("add_schedule.php")
    fun addSchedule(
        @Field("user_id") userId: Int,
        @Field("day_name") dayName: String,
        @Field("workout_plan") workoutPlan: String
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("delete_schedule.php")
    fun deleteSchedule(
        @Field("id") id: Int
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("complete_schedule.php")
    fun completeSchedule(
        @Field("id") id: Int
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("checkin.php")
    fun checkIn(
        @Field("user_id") userId: Int,
        @Field("qr_code") qrCode: String
    ): Call<SimpleResponse>

    @FormUrlEncoded
    @POST("change_password.php")
    fun changePassword(
        @Field("user_id") userId: Int,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): Call<SimpleResponse>

    @GET("get_checkin_history.php")
    fun getCheckInHistory(
        @Query("user_id") userId: Int
    ): Call<List<CheckIn>>

    @GET("get_products.php")
    fun getProducts(): Call<List<Product>>

    @FormUrlEncoded
    @POST("add_to_cart.php")
    fun addToCart(
        @Field("user_id") userId: Int,
        @Field("product_id") productId: Int
    ): Call<SimpleResponse>

    @GET("get_cart.php")
    fun getCart(
        @Query("user_id") userId: Int
    ): Call<List<CartItem>>

    @GET("get_workout_stats.php")
    fun getWorkoutStats(
        @Query("user_id") userId: Int
    ): Call<WorkoutStatsResponse>
}