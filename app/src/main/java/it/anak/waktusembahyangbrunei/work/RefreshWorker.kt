package it.anak.waktusembahyangbrunei.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import it.anak.waktusembahyangbrunei.WaktuApp
import java.util.concurrent.TimeUnit

/** Keeps prayer times fresh so the tile and complication roll over to the new day. */
class RefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = (applicationContext as WaktuApp).repository.refresh()
        return if (result.error == null) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "prayer_times_refresh"

        fun enqueue(context: Context) {
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request =
                PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
