package com.apexlab.game.data

import com.badlogic.gdx.Gdx

object SaveData {
    private const val PREFS_NAME = "apex-lab-save"
    private const val KEY_BEST_TIME = "best_time_seconds"
    private const val KEY_RUNS_COMPLETED = "runs_completed"

    private val prefs get() = Gdx.app.getPreferences(PREFS_NAME)

    fun bestTime(): Float = prefs.getFloat(KEY_BEST_TIME, -1f)

    fun runsCompleted(): Int = prefs.getInteger(KEY_RUNS_COMPLETED, 0)

    fun submitRun(seconds: Float): Boolean {
        val store = prefs
        val current = store.getFloat(KEY_BEST_TIME, -1f)
        val isNewBest = current < 0f || seconds < current
        if (isNewBest) {
            store.putFloat(KEY_BEST_TIME, seconds)
        }
        store.putInteger(KEY_RUNS_COMPLETED, store.getInteger(KEY_RUNS_COMPLETED, 0) + 1)
        store.flush()
        return isNewBest
    }
}
