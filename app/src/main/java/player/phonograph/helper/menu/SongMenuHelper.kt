package player.phonograph.helper.menu

import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import player.phonograph.R
import player.phonograph.dialogs.AddToPlaylistDialog
import player.phonograph.dialogs.DeleteSongsDialog
import player.phonograph.dialogs.SongDetailDialog
import player.phonograph.helper.MusicPlayerRemote
import player.phonograph.interfaces.PaletteColorHolder
import player.phonograph.model.Song
import util.phonograph.tageditor.AbsTagEditorActivity
import util.phonograph.tageditor.SongTagEditorActivity
import player.phonograph.util.BlacklistUtil
import player.phonograph.util.MusicUtil
import player.phonograph.util.NavigationUtil
import player.phonograph.util.RingtoneManager

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object SongMenuHelper {

    @JvmStatic
    fun handleMenuClick(activity: FragmentActivity, song: Song, menuItemId: Int): Boolean {
        when (menuItemId) {
            R.id.action_add_to_playlist -> {
                AddToPlaylistDialog.create(listOf(song))
                    .show(activity.supportFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(song)
                return true
            }
            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(song)
                return true
            }
            R.id.action_tag_editor -> {
                val tagEditorIntent = Intent(activity, SongTagEditorActivity::class.java)
                tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
                if (activity is PaletteColorHolder) tagEditorIntent.putExtra(
                    AbsTagEditorActivity.EXTRA_PALETTE,
                    (activity as PaletteColorHolder).paletteColor
                )
                activity.startActivity(tagEditorIntent)
                return true
            }
            R.id.action_details -> {
                SongDetailDialog.create(song).show(activity.supportFragmentManager, "SONG_DETAILS")
                return true
            }
            R.id.action_add_to_black_list -> {
                BlacklistUtil.addToBlacklist(activity, song)
            }
            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(listOf(song))
                    .show(activity.supportFragmentManager, "DELETE_SONGS")
                return true
            }
            R.id.action_go_to_album -> {
                NavigationUtil.goToAlbum(activity, song.albumId)
                return true
            }
            R.id.action_go_to_artist -> {
                NavigationUtil.goToArtist(activity, song.artistId)
                return true
            }
            R.id.action_set_as_ringtone -> {
                if (RingtoneManager.requiresDialog(activity)) {
                    RingtoneManager.showDialog(activity)
                } else {
                    val ringtoneManager = RingtoneManager()
                    ringtoneManager.setRingtone(activity, song.id)
                }
                return true
            }
            R.id.action_share -> {
                activity.startActivity(
                    Intent.createChooser(
                        MusicUtil.createShareSongFileIntent(
                            song,
                            activity
                        ),
                        null
                    )
                )
                return true
            }
        }
        return false
    }

    const val menuResDefault = R.menu.menu_item_song

    abstract class ClickMenuListener(private val activity: AppCompatActivity, @MenuRes menuRes: Int?) : View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        abstract val song: Song
        protected open var realRes = menuRes ?: menuResDefault

        override fun onClick(v: View) {
            handleMenuButtonClick(v)
        }
        private fun handleMenuButtonClick(v: View) {
            val popupMenu = PopupMenu(activity, v)
            popupMenu.inflate(realRes)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return handleMenuClick(activity, song, item.itemId)
        }
    }
}
