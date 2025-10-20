package com.mzansi.recipes.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mzansi.recipes.data.api.MealDbService
import com.mzansi.recipes.data.db.AppDatabase
import com.mzansi.recipes.data.db.CategoryDao // Required for provideRecipeRepo
import com.mzansi.recipes.data.db.RecipeDao // Required for provideRecipeRepo
import com.mzansi.recipes.data.repo.AuthRepository
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import com.mzansi.recipes.data.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE recipes ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `imageUrl` TEXT, `description` TEXT, PRIMARY KEY(`id`))")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE recipes ADD COLUMN instructions TEXT")
            db.execSQL("ALTER TABLE recipes ADD COLUMN area TEXT")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create new table with the desired schema
            db.execSQL("CREATE TABLE IF NOT EXISTS `recipes_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `imageUrl` TEXT, `category` TEXT, `trending` INTEGER NOT NULL DEFAULT 0, `pendingSync` INTEGER NOT NULL DEFAULT 0, `instructions` TEXT, `area` TEXT, PRIMARY KEY(`id`))")
            // Copy data from old table to new table
            db.execSQL("INSERT INTO `recipes_new` (id, title, imageUrl, category, trending, pendingSync, instructions, area) SELECT id, title, imageUrl, category, trending, pendingSync, instructions, area FROM recipes")
            // Drop the old table
            db.execSQL("DROP TABLE recipes")
            // Rename the new table to the original name
            db.execSQL("ALTER TABLE `recipes_new` RENAME TO recipes")
        }
    }

    // <<< NEW MIGRATION for adding isSavedOffline to recipes table >>>
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE recipes ADD COLUMN isSavedOffline INTEGER NOT NULL DEFAULT 0")
        }
    }

    // Singleton instance for NetworkMonitor
    @Volatile
    private var networkMonitorInstance: NetworkMonitor? = null

    fun provideDb(context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "mzansi.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6) // <<< ADDED MIGRATION_5_6
            .fallbackToDestructiveMigration()
            .build()

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
    fun provideStorage() = FirebaseStorage.getInstance()

    // Updated to provide NetworkMonitor as a singleton
    fun provideNetworkMonitor(context: Context): NetworkMonitor {
        return networkMonitorInstance ?: synchronized(this) {
            networkMonitorInstance ?: NetworkMonitor(context.applicationContext).also {
                networkMonitorInstance = it
            }
        }
    }

    fun provideUserPrefs(context: Context) = UserPreferences(context.dataStore)

    fun provideRecipeRepo(service: MealDbService, recipeDao: RecipeDao, categoryDao: CategoryDao, networkMonitor: NetworkMonitor) =
        RecipeRepository(service, recipeDao, categoryDao, networkMonitor)

    fun provideShoppingRepo(db: AppDatabase, fs: FirebaseFirestore, auth: FirebaseAuth) =
        ShoppingRepository(db.shoppingDao(), fs, auth)

    fun provideCommunityRepo(fs: FirebaseFirestore, auth: FirebaseAuth, storage: FirebaseStorage) =
        CommunityRepository(fs, auth, storage)

    fun provideAuthRepo(auth: FirebaseAuth, fs: FirebaseFirestore) =
        AuthRepository(auth, fs, provideStorage())
}
