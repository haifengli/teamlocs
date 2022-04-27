package com.yellowtree.teamlocs.api

import android.content.res.AssetManager
import okhttp3.*
import java.io.IOException
import java.util.*

class RequestInterceptor internal constructor(private val assetManager: AssetManager) : Interceptor {

    companion object {
        private const val TEAM_JSON_FILE_NAME = "teams.json"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val inputStream = assetManager.open(TEAM_JSON_FILE_NAME)
        val json = Scanner(inputStream).useDelimiter("\\A").next()
        return Response.Builder()
            .body(ResponseBody.create(MediaType.parse("application.json"), json))
            .request(chain.request())
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("200 OK")
            .build()
    }
}