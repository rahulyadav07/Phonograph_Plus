package player.phonograph.ui.modules.panel

import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import player.phonograph.R
import player.phonograph.databinding.SlidingMusicPanelLayoutBinding
import player.phonograph.model.NowPlayingScreen
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.service.queue.CurrentQueueState
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import player.phonograph.ui.modules.player.AbsPlayerFragment
import player.phonograph.ui.modules.player.MiniPlayerFragment
import player.phonograph.ui.modules.player.card.CardPlayerFragment
import player.phonograph.ui.modules.player.flat.FlatPlayerFragment
import player.phonograph.util.theme.primaryColor
import player.phonograph.util.theme.themeFooterColor
import player.phonograph.util.theme.updateNavigationbarColor
import player.phonograph.util.theme.updateStatusbarColor
import player.phonograph.util.theme.updateTaskDescriptionColor
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import android.animation.ArgbEvaluator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 *
 *
 * Do not use [setContentView]. Instead, wrap your layout with
 * [wrapSlidingMusicPanel] first and then return it in [createContentView]
 *
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsSlidingMusicPanelActivity :
        AbsMusicServiceActivity(),
        SlidingUpPanelLayout.PanelSlideListener {

    private var playerFragment: AbsPlayerFragment? = null
    private var miniPlayerFragment: MiniPlayerFragment? = null

    private var panelBinding: SlidingMusicPanelLayoutBinding? = null
    private val slidingUpPanelLayout: SlidingUpPanelLayout? get() = panelBinding?.slidingLayout

    val panelViewModel: PanelViewModel by viewModel {
        val paletteColor = playerFragment?.paletteColorState?.value ?: 0
        val highlightColor = if (paletteColor > 0) paletteColor else themeFooterColor(this)
        parametersOf(
            primaryColor(), highlightColor, themeFooterColor(this)
        )
    }

    /**
     * See [wrapSlidingMusicPanel]
     */
    protected abstract fun createContentView(): View

    /**
     * create the actual view (wrapped with panel layout)
     * @param view the "main" view to be wrapped
     * @return actual view that should be the "root" view for [setContentView]
     */
    protected fun wrapSlidingMusicPanel(view: View?): View {
        return SlidingMusicPanelLayoutBinding.inflate(layoutInflater, null, false).let { binding ->
            panelBinding = binding
            binding.contentContainer.also { it.addView(view) }
            binding.slidingLayout // root
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup panel
        setContentView(createContentView())
        panelBinding?.slidingLayout?.also { layout ->
            layout.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    when (panelState) {
                        PanelState.EXPANDED  -> {
                            onPanelSlide(layout, 1f)
                            onPanelExpanded(layout)
                        }

                        PanelState.COLLAPSED -> onPanelCollapsed(layout)
                        else                 -> playerFragment?.onHide()
                    }
                }
            })
            layout.addPanelSlideListener(this)
        }
        miniPlayerFragment =
            supportFragmentManager.findFragmentById(R.id.mini_player_fragment) as MiniPlayerFragment
        miniPlayerFragment?.requireView()?.setOnClickListener { expandPanel() }

        // add fragment
        lifecycleScope.launch {
            val flow = Setting(this@AbsSlidingMusicPanelActivity).Composites[Keys.nowPlayingScreen].flow()
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.distinctUntilChanged().collect { screen ->
                    setupPlayerFragment(screen)
                }
            }
        }

        // states
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                panelViewModel.highlightColor.collect { color ->
                    if (panelState == PanelState.EXPANDED) {
                        animateThemeColorChange(
                            panelViewModel.previousHighlightColor.value,
                            actualStatusbarColor(panelViewModel.highlightColor.value)
                        ) { animator ->
                            updateStatusbarColor(animator.animatedValue as Int)
                        }
                        animateThemeColorChange(
                            panelViewModel.previousHighlightColor.value,
                            color
                        ) { animator ->
                            updateNavigationbarColor(animator.animatedValue as Int)
                            updateTaskDescriptionColor(animator.animatedValue as Int)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                CurrentQueueState.queue.collect { queue ->
                    hideBottomBar(queue.get()?.isEmpty() ?: false)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelThemeColorChange() // just in case
    }

    private fun setupPlayerFragment(screen: NowPlayingScreen) {
        supportFragmentManager.apply {
            beginTransaction().replace(
                R.id.player_fragment_container,
                when (screen) {
                    NowPlayingScreen.FLAT -> FlatPlayerFragment()
                    NowPlayingScreen.CARD -> CardPlayerFragment()
                },
                NOW_PLAYING_FRAGMENT
            ).commit()
            executePendingTransactions()
        }

        playerFragment =
            supportFragmentManager.findFragmentById(R.id.player_fragment_container) as AbsPlayerFragment

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                playerFragment?.paletteColorState?.collect { color -> panelViewModel.updateHighlightColor(color) }
            }
        }
    }

    private val argbEvaluator = ArgbEvaluator()
    override fun onPanelSlide(panel: View, @FloatRange(from = 0.0, to = 1.0) slideOffset: Float) {
        setMiniPlayerFadingProgress(slideOffset)
        cancelThemeColorChange()
        val color: Int =
            argbEvaluator.evaluate(
                slideOffset,
                panelViewModel.activityColor.value,
                panelViewModel.highlightColor.value
            ) as Int
        updateNavigationbarColor(color)
        updateTaskDescriptionColor(color)
        val statusbarColor: Int =
            argbEvaluator.evaluate(
                slideOffset,
                panelViewModel.activityColor.value,
                actualStatusbarColor(panelViewModel.highlightColor.value)
            ) as Int
        updateStatusbarColor(statusbarColor)
    }

    override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
        when (newState) {
            PanelState.COLLAPSED -> onPanelCollapsed(panel)
            PanelState.EXPANDED  -> onPanelExpanded(panel)
            PanelState.ANCHORED  -> collapsePanel() // this fixes a bug where the panel would get stuck for some reason
            else                 -> {}
        }
    }

    @Suppress("DEPRECATION")
    open fun onPanelCollapsed(panel: View?) {
        // restore values
        playerFragment?.setMenuVisibility(false)
        playerFragment?.userVisibleHint = false // todo: remove legacy userVisibleHint
        playerFragment?.onHide()
        panelBackPressedCallback.remove()
    }

    @Suppress("DEPRECATION")
    open fun onPanelExpanded(panel: View?) {
        // setting fragments values
        playerFragment?.setMenuVisibility(true)
        playerFragment?.userVisibleHint = true // todo: remove legacy userVisibleHint
        playerFragment?.onShow()
        onBackPressedDispatcher.addCallback(this, panelBackPressedCallback)
    }

    private val panelBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            collapsePanel()
        }
    }

    val panelState: PanelState?
        get() = slidingUpPanelLayout?.panelState

    fun collapsePanel() {
        slidingUpPanelLayout?.panelState = PanelState.COLLAPSED
    }

    fun expandPanel() {
        slidingUpPanelLayout?.panelState = PanelState.EXPANDED
    }

    fun hideBottomBar(hide: Boolean) {
        if (hide) {
            slidingUpPanelLayout?.panelHeight = 0
            collapsePanel()
        } else {
            slidingUpPanelLayout?.panelHeight = resources.getDimensionPixelSize(R.dimen.mini_player_height)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
            slidingUpPanelLayout!!.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    slidingUpPanelLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    hideBottomBar(false)
                }
            })
        } // don't call hideBottomBar(true) here as it causes a bug with the SlidingUpPanelLayout
    }

    private fun setMiniPlayerFadingProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        val view = miniPlayerFragment?.view ?: return
        val alpha = 1 - progress
        view.also {
            it.alpha = alpha
            // necessary to make the views below clickable
            it.visibility = if (alpha == 0f) View.GONE else View.VISIBLE
        }
    }

    fun setAntiDragView(antiDragView: View?) {
        slidingUpPanelLayout?.setAntiDragView(antiDragView)
    }

    private fun actualStatusbarColor(@ColorInt color: Int): Int =
        if (playerFragment is CardPlayerFragment) Color.TRANSPARENT else color

    override val snackBarContainer: View get() = findViewById(R.id.content_container)

    companion object {
        const val NOW_PLAYING_FRAGMENT = "NowPlayingPlayerFragment"
    }
}
