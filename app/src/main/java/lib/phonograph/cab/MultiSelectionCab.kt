/*
 * Copyright (c) 2022 chr_56 & Aidan Follestad (@afollestad) (original designer/author)
 */

package lib.phonograph.cab

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import player.phonograph.R

fun createMultiSelectionCab(
    activity: Activity,
    @IdRes stubId: Int,
    @IdRes inflatedId: Int,
    applyCfg: MultiSelectionCab.() -> Unit
): MultiSelectionCab {
    val stub = activity.findViewById<View>(stubId)
    val toolbar = if (stub is ViewStub) {
        stub.inflatedId = inflatedId
        stub.layoutResource = R.layout.stub_toolbar
        stub.inflate() as Toolbar
    } else {
        throw IllegalStateException(
            "Unable to attach to ${activity.resources.getResourceName(stubId)}, it's not a ViewStub"
        )
    }
    return MultiSelectionCab(activity, toolbar).also(applyCfg)
}

// todo leak check
class MultiSelectionCab internal constructor(
    var activityField: Activity?,
    var toolbarField: Toolbar?
) {

    private val activity: Activity
        get() = activityField ?: throw IllegalStateException("Cab has already destroyed or not created!")
    private val toolbar: Toolbar
        get() = toolbarField ?: throw IllegalStateException("Cab has already destroyed or not created!!")

    private var createCallbacks = mutableListOf<CreateCallback>()
    private var selectCallbacks = mutableListOf<SelectCallback>()
    private var destroyCallbacks = mutableListOf<DestroyCallback>()

    fun show() = toolbar.run {
        status = CabStatus.STATUS_INITIATING()

        translationY = 0f
        alpha = 1f

        navigationIcon = closeable

        title = titleText
        setTitleTextColor(titleTextColor)

        subtitle = subtitleText
        setSubtitleTextColor(subtitleTextColor)

        popupTheme = themeRes

        createCallbacks.forEach { it.invoke(this@MultiSelectionCab, menu) }
        visibility = View.VISIBLE
        bringToFront()

        status = CabStatus.STATUS_AVAILABLE()
    }

    fun hide() = toolbar.run {
        visibility = View.INVISIBLE
    }

    var status: CabStatus = CabStatus.STATUS_DESTROYED() // default
        private set

    var titleText: CharSequence = ""
    @ColorInt
    var titleTextColor: Int = Color.WHITE

    var subtitleText: CharSequence = ""
    @ColorInt
    var subtitleTextColor: Int = Color.WHITE

    @StyleRes
    var themeRes: Int = R.style.ThemeOverlay_AppCompat_DayNight_ActionBar

    var closeable: Drawable = ContextCompat.getDrawable(activity, R.drawable.ic_close_white_24dp)!!.also { it.setTint(titleTextColor) }

    @ColorInt
    var backgroundColor: Int = Color.GRAY

    @MenuRes
    var menuRes: Int = 0

    val menu: Menu? get() = toolbar.menu

    private var menuClickListener = Toolbar.OnMenuItemClickListener { item ->
        selectCallbacks.forEach {
            return@OnMenuItemClickListener it.invoke(item)
        }
        return@OnMenuItemClickListener false
    }

    fun inflateMenuRes(@MenuRes menuRes: Int) {
        toolbar.run {
            if (menuRes != 0) {
                this@MultiSelectionCab.menuRes = menuRes
                inflateMenu(menuRes)
                setOnMenuItemClickListener(menuClickListener)
            } else {
                setOnMenuItemClickListener(null)
            }
        }
    }
    fun onCreate(callback: CreateCallback) {
        createCallbacks.add(callback)
    }

    fun onSelection(callback: SelectCallback) {
        selectCallbacks.add(callback)
    }

    fun onDestroy(callback: DestroyCallback) {
        destroyCallbacks.add(callback)
    }

    @Synchronized
    fun destroy(): Boolean {
        if (status == CabStatus.STATUS_DESTROYED()) return false
        status = CabStatus.STATUS_DESTROYING()

        destroyCallbacks.forEach {
            if (!it.invoke(this)) {
                status = CabStatus.STATUS_AVAILABLE()
                return false
            }
        }
        toolbar.visibility = View.GONE
        toolbarField = null
        activityField = null

        status = CabStatus.STATUS_DESTROYED()
        return true
    }
}

typealias CreateCallback = (cab: MultiSelectionCab, menu: Menu) -> Unit

typealias SelectCallback = (item: MenuItem) -> Boolean

typealias DestroyCallback = (cab: MultiSelectionCab) -> Boolean

@Suppress("ClassName")
sealed class CabStatus() { // todo
    class STATUS_INITIATING : CabStatus()
    class STATUS_AVAILABLE : CabStatus()
    class STATUS_DESTROYING : CabStatus()
    class STATUS_DESTROYED : CabStatus()
}
