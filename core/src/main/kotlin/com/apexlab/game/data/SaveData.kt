package com.apexlab.game.data

import com.badlogic.gdx.Gdx

data class Stats(val bestTime: Float?, val runsCompleted: Int)

data class RunResult(val seconds: Float, val bestTime: Float, val isNewBest: Boolean)

object SaveData {
    private const val PREFS_NAME = "apex-lab-save"
    private const val KEY_BEST_TIME = "best_time_seconds"
    private const val KEY_RUNS_COMPLETED = "runs_completed"
    private const val NO_RECORD = -1f

    private val prefs get() = Gdx.app.getPreferences(PREFS_NAME)

    fun stats(): Stats {
        val store = prefs
        val best = store.getFloat(KEY_BEST_TIME, NO_RECORD)
        return Stats(
            bestTime = if (best >= 0f) best else null,
            runsCompleted = store.getInteger(KEY_RUNS_COMPLETED, 0)
        )
    }

    fun record(seconds: Float): RunResult {
        val store = prefs
        val previous = store.getFloat(KEY_BEST_TIME, NO_RECORD)
        val isNewBest = previous < 0f || seconds < previous
        val best = if (isNewBest) seconds else previous
        store.putFloat(KEY_BEST_TIME, best)
        store.putInteger(KEY_RUNS_COMPLETED, store.getInteger(KEY_RUNS_COMPLETED, 0) + 1)
        store.flush()
        return RunResult(seconds, best, isNewBest)
    }
}
