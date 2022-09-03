/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.*
import player.phonograph.notification.ErrorNotification
import java.util.concurrent.locks.Lock

object CoroutineUtil {

    suspend fun coroutineToast(context: Context, text: String, longToast: Boolean = false) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                text,
                if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }

    suspend fun coroutineToast(context: Context, @StringRes res: Int) =
        coroutineToast(context, context.getString(res))

    /**
     * a class to help convert callback-style function to async-coroutine-style function
     */
    class Executor<R>(val block: (Wrapper<R?>) -> Unit) {
        private var holder: Wrapper<R?> = Wrapper(null)
        suspend fun execute(): R {
            block(holder)
            while (holder.content == null) yield()
            return holder.content!!
        }

        class Wrapper<T>(var content: T?)
    }

    fun createDefaultExceptionHandler(TAG: String, defaultMessageHeader: String = "Error!"): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            ErrorNotification.postErrorNotification(
                exception,
                "$defaultMessageHeader:${exception.message}"
            )
        }

    inline fun Lock.use(crossinline block: () -> Unit) {
        lock()
        try {
            block()
        } finally {
            unlock()
        }
    }

    class BackgroundJob(private val callback: () -> Any?) {
        private var disposable: Job? = null
        fun execute(coroutineScope: CoroutineScope) {
            disposable?.cancel()
            disposable = coroutineScope.launch {
                callback.invoke()
            }
        }

        fun isBusy(): Boolean = disposable != null
    }
}
