package com.cropwise.app

import android.content.Context
import com.cropwise.app.data.local.SessionStore
import com.cropwise.app.data.remote.Network
import com.cropwise.app.data.repository.CropWiseRepository

/** Manual dependency container created once in MainActivity. */
class AppContainer(context: Context) {
    val session = SessionStore(context.applicationContext)
    private val api = Network.create { repository.token }
    val repository = CropWiseRepository(api, session)
}
