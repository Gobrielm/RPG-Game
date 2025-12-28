package com.example.bikinggame.gameState

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object SaveManager {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private var autoSaveJob: Job? = null
    private var dirty = false

    fun markDirty() {
        dirty = true
    }

    fun startAutoSave(intervalSeconds: Long, context: Context) {
        if (autoSaveJob != null) return
        autoSaveJob = scope.launch {
            while (isActive) {
                delay(intervalSeconds * 1_000)

                if (dirty) {
                    saveLocally(context)
                    saveToCloud()
                    dirty = false
                }
            }
        }
    }

    fun stopAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = null
    }

    fun flush(context: Context) {
        saveLocally(context)
        dirty = false
    }
}
