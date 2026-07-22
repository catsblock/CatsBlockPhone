package com.catsblock.phone.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Serviço em primeiro plano para gravação automatizada de chamadas telefônicas.
 */
class CallRecorderService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): START_NOT_STICKY {
        return START_STICKY
    }
}
