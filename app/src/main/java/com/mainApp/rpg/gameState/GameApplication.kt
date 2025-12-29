package com.mainApp.rpg.gameState

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.mainApp.rpg.playIntegrity.PlayIntegrityToken

class GameApplication: Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        SaveManager.startAutoSave(
            intervalSeconds = 10,
            context = applicationContext
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        SaveManager.flush(applicationContext)
        SaveManager.stopAutoSave()
    }
}