package com.gamelaunch.frontend

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GameLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Emulators expect a raw file path / file:// URI to the ROM. On targetSdk >= 24 the
        // default VM policy throws FileUriExposedException when a file:// Uri crosses to another
        // app. Relaxing the VM policy (as RetroArch/Daijishō and other Android frontends do)
        // lets us hand the ROM path straight to each emulator, which then reads it with its own
        // storage permissions.
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
    }
}
