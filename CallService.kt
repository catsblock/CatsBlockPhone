package com.catsblock.phone.services

import android.telecom.Call
import android.telecom.InCallService

class CallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
    }
}
