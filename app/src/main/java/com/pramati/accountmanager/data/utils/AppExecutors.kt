package com.pramati.accountmanager.data.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AppExecutors(val diskIO: Executor?, val networkIO: Executor?, val mainThread: Executor?) {

    companion object {
        private var LOCK = Any()
        private var sInstance: AppExecutors? = null

        fun getInstance(): AppExecutors? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = AppExecutors(
                        Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        MainThreadExecutor()
                    )
                }
            }
            return sInstance
        }
    }


    fun diskIO(): Executor? {
        return diskIO
    }

    fun mainThread(): Executor? {
        return mainThread
    }

    fun networkIO(): Executor? {
        return networkIO
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}