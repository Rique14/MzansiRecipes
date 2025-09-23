package com.mzansi.recipes.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.mzansi.recipes.data.api.MealDbService
import com.mzansi.recipes.data.db.AppDatabase
import com.mzansi.recipes.data.repo.AuthRepository
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import com.mzansi.recipes.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.mzansi.recipes.util.NetworkMonitor

val Context.dataStore by preferencesDataStore("user_prefs")

object AppModules {
    fun provideDb(context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "mzansi.db").build()

    fun provideOkHttp(rapidApiKey: String): OkHttpClient {
        val auth = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("x-rapidapi-host", "themealdb.p.rapidapi.com")
                .addHeader("x-rapidapi-key", rapidApiKey)
                .build()
            chain.proceed(req)
        }
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(log)
            .build()
    }

    fun provideMealDbService(client: OkHttpClient): MealDbService {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl("https://themealdb.p.rapidapi.com")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MealDbService::class.java)
    }

    fun provideAuth() = FirebaseAuth.getInstance()
    fun provideFirestore() = FirebaseFirestore.getInstance()

    fun provideNetworkMonitor(context: Context) = NetworkMonitor(context)

    fun provideUserPrefs(context: Context) = UserPreferences(context.dataStore)
    fun provideRecipeRepo(service: MealDbService, db: AppDatabase) =
        RecipeRepository(service, db.recipeDao())
    fun provideShoppingRepo(db: AppDatabase, fs: FirebaseFirestore, auth: FirebaseAuth) =
        ShoppingRepository(db.shoppingDao(), fs, auth)
    fun provideCommunityRepo(fs: FirebaseFirestore, auth: FirebaseAuth) =
        CommunityRepository(fs, auth)
    fun provideAuthRepo(auth: FirebaseAuth, fs: FirebaseFirestore) =
        AuthRepository(auth, fs)
}