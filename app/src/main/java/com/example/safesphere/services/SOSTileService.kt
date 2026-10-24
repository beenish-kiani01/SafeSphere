package com.example.safesphere.services

import android.content.Intent
import android.service.quicksettings.TileService
import com.example.safesphere.SOSActivity

class SOSTileService : TileService() {
    override fun onClick() {
        super.onClick()
        // Open the SOS Activity immediately from the lockscreen/drawer
        val intent = Intent(this, SOSActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapse(intent)
    }
}
