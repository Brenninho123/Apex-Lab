package com.apexlab.game.ios

import com.apexlab.game.ApexLabGame
import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

class IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration()
        return IOSApplication(ApexLabGame(), config)
    }
}

fun main(argv: Array<String>) {
    val pool = NSAutoreleasePool()
    UIApplication.main(argv, null, IOSLauncher::class.java)
    pool.close()
}
