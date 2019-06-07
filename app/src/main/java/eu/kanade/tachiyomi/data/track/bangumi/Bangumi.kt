package eu.kanade.tachiyomi.data.track.bangumi

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.gson.Gson
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.TrackService
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import rx.Completable
import rx.Observable
import uy.kohesive.injekt.injectLazy

class Bangumi(private val context: Context, id: Int) : TrackService(id) {

    override fun getScoreList(): List<String> {
        return IntRange(0, 10).map(Int::toString)
    }

    override fun displayScore(track: Track): String {
        return track.score.toInt().toString()
    }

    override fun add(track: Track): Observable<Track> {
        return api.addLibManga(track)
    }

    override fun update(track: Track): Observable<Track> {
        if (track.total_chapters != 0 && track.last_chapter_read == track.total_chapters) {
            track.status = COMPLETED
        }
        return api.updateLibManga(track)
    }

    override fun bind(track: Track): Observable<Track> {
        return api.findLibManga(track)
                .flatMap { remoteTrack ->
                    if (remoteTrack != null) {
                        track.copyPersonalFrom(remoteTrack)
                        track.library_id = remoteTrack.library_id
                        update(track)
                    } else {
                        // Set default fields if it's not found in the list
                        track.score = DEFAULT_SCORE.toFloat()
                        track.status = DEFAULT_STATUS
                        add(track)
                    }
                }
    }

    override fun search(query: String): Observable<List<TrackSearch>> {
        return api.search(query)
    }

    override fun refresh(track: Track): Observable<Track> {
        return api.findLibManga(track)
                .map { remoteTrack ->
                    if (remoteTrack != null) {
                        track.copyPersonalFrom(remoteTrack)
                        track.total_chapters = remoteTrack.total_chapters
                    }
                    track
                }
    }

    companion object {
        const val READING = 1
        const val COMPLETED = 2
        const val ON_HOLD = 3
        const val DROPPED = 4
        const val PLANNING = 5
        const val REPEATING = 6

        val STATUS = arrayOf(
                "do", "collect", "on_hold", "dropped", "wish", "do"
        )

        const val DEFAULT_STATUS = READING
        const val DEFAULT_SCORE = 0
    }

    override val name = "Bangumi"

    private val gson: Gson by injectLazy()

    private val interceptor by lazy { BangumiInterceptor(this, gson) }

    private val api by lazy { BangumiApi(client, interceptor) }

    override fun getLogo() = R.drawable.shikimori

    override fun getLogoColor() = Color.rgb(0xF0, 0x91, 0x99)

    override fun getStatusList(): List<Int> {
        return listOf(READING, COMPLETED, ON_HOLD, DROPPED, PLANNING, REPEATING)
    }

    override fun getStatus(status: Int): String = with(context) {
        when (status) {
            READING -> getString(R.string.reading)
            COMPLETED -> getString(R.string.completed)
            ON_HOLD -> getString(R.string.on_hold)
            DROPPED -> getString(R.string.dropped)
            PLANNING -> getString(R.string.plan_to_read)
            REPEATING -> getString(R.string.repeating)
            else -> ""
        }
    }

    override fun login(username: String, password: String) = login(password)

    fun login(code: String): Completable {
        val TAG = "FEILONG"
        Log.d(TAG, "login $code")
        return api.accessToken(code).map { oauth: OAuth? ->
            interceptor.newAuth(oauth)
            Log.i(TAG, "login ${oauth}")
            if (oauth != null) {
//                val user = api.getCurrentUser(oauth.access_token)
                Log.i(TAG, "login ${oauth.access_token}, user ${oauth.user_id}")
                Log.i(TAG, "login otauth")
                saveCredentials(oauth.user_id.toString(), oauth.access_token)
            }
        }.doOnError {
            Log.e(TAG, "logout with ${it.message}")
            logout()
        }.toCompletable()
    }

    fun saveToken(oauth: OAuth?) {
        val json = gson.toJson(oauth)
        preferences.trackToken(this).set(json)
    }

    fun restoreToken(): OAuth? {
        return try {
            gson.fromJson(preferences.trackToken(this).get(), OAuth::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun logout() {
        super.logout()
        preferences.trackToken(this).set(null)
        interceptor.newAuth(null)
    }
}
