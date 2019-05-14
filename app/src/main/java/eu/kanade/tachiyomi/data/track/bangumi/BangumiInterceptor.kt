package eu.kanade.tachiyomi.data.track.bangumi

import android.util.Log
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response


class BangumiInterceptor(val bangumi: Bangumi, val gson: Gson) : Interceptor {

    /**
     * OAuth object used for authenticated requests.
     */
    private var oauth: OAuth? = bangumi.restoreToken()

    fun addTocken(tocken: String, oidFormBody: FormBody): FormBody {
        val newFormBody = FormBody.Builder()
        for (i in 0 until oidFormBody.size()) {
            newFormBody.addEncoded(oidFormBody.encodedName(i), oidFormBody.encodedValue(i))
        }
        newFormBody.add("token", tocken)
        Log.i("BANGUMI","tocken $tocken")
        return newFormBody.build()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val currAuth = oauth ?: throw Exception("Not authenticated with Bangumi")

        val refreshToken = currAuth.refresh_token!!

        // Refresh access token if expired.
        if (currAuth.isExpired()) {
            val response = chain.proceed(BangumiApi.refreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                newAuth(gson.fromJson(response.body()!!.string(), OAuth::class.java))
            } else {
                response.close()
            }
        }
        // Add the authorization header to the original request.
        var authRequest = if (originalRequest.method() == "GET") originalRequest.newBuilder()
//                .addHeader("Authorization", "Bearer ${oauth!!.access_token}")
                .header("User-Agent", "Tachiyomi")
                .build() else originalRequest.newBuilder()
//                .addHeader("Authorization", "Bearer ${oauth!!.access_token}")
                .post(addTocken(oauth!!.access_token, originalRequest.body() as FormBody))
                .header("User-Agent", "Tachiyomi")
                .build()

        return chain.proceed(authRequest)
    }

    fun newAuth(oauth: OAuth?) {
        this.oauth = oauth
        bangumi.saveToken(oauth)
    }
}
