package it.anak.waktusembahyangbrunei

import android.app.Application
import it.anak.waktusembahyangbrunei.data.PrayerRepository
import it.anak.waktusembahyangbrunei.work.RefreshWorker

class WaktuApp : Application() {

    val repository: PrayerRepository by lazy { PrayerRepository(this) }

    override fun onCreate() {
        super.onCreate()
        RefreshWorker.enqueue(this)
    }
}
