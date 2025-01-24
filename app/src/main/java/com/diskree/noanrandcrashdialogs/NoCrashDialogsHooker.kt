package com.diskree.noanrandcrashdialogs

import android.annotation.SuppressLint
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

@SuppressLint("PrivateApi")
object NoCrashDialogsHooker {

    private const val DIALOG_DATA_CLASS_NAME = "com.android.server.am.AppErrorDialog\$Data"

    private var xposedModule: XposedModule? = null

    fun hook(param: XposedModuleInterface.SystemServerLoadedParam, xposedModule: XposedModule) {
        this.xposedModule = xposedModule
        param.classLoader.apply {
            val controllerClass = loadClass("com.android.server.am.ErrorDialogController")
            val showANRDialogsMethod = controllerClass.getDeclaredMethod(
                "showCrashDialogs",
                loadClass(DIALOG_DATA_CLASS_NAME)
            ).accessed()
            xposedModule.hook(showANRDialogsMethod, CrashDialogShowingHooker::class.java)
        }
    }

    @XposedHooker
    private class CrashDialogShowingHooker : XposedInterface.Hooker {

        companion object {
            @Suppress("unused")
            @JvmStatic
            @BeforeInvocation
            fun beforeShowAnrDialogs(
                callback: XposedInterface.BeforeHookCallback
            ): CrashDialogShowingHooker {
                xposedModule?.log("[NoANRAndCrashDialogs] Hide crash dialog")
                callback.returnAndSkip(null)
                return CrashDialogShowingHooker()
            }
        }
    }
}
