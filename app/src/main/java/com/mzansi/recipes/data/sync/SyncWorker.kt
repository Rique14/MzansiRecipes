package com.mzansi.recipes.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mzansi.recipes.di.AppModules

class SyncWorker(private val ctx: Context, wp: WorkerParameters) : CoroutineWorker(ctx, wp) {
    override suspend fun doWork(): Result {
        val db = AppModules.provideDb(ctx)
        val auth = AppModules.provideAuth()
        val fs = AppModules.provideFirestore()
        val repo = AppModules.provideShoppingRepo(db, fs, auth)
        return try {
            repo.syncPending()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}