package com.diskree.noanrandcrashdialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

@SuppressLint("PrivateApi")
object NoANRDialogsHooker {

    private const val DIALOG_DATA_CLASS_NAME = "com.android.server.am.AppNotRespondingDialog\$Data"
    private const val WAIT_COMMAND_CODE = 2

    private var xposedModule: XposedModule? = null

    fun hook(param: XposedModuleInterface.SystemServerLoadedParam, xposedModule: XposedModule) {
        this.xposedModule = xposedModule
        param.classLoader.apply {
            val controllerClass = loadClass("com.android.server.am.ErrorDialogController")
            val showANRDialogsMethod = controllerClass.getDeclaredMethod(
                "showAnrDialogs",
                loadClass(DIALOG_DATA_CLASS_NAME)
            ).accessed()
            xposedModule.hook(showANRDialogsMethod, ANRDialogShowingHooker::class.java)
        }
    }

    @XposedHooker
    private class ANRDialogShowingHooker : XposedInterface.Hooker {

        companion object {
            @Suppress("unused")
            @JvmStatic
            @BeforeInvocation
            fun beforeShowAnrDialogs(
                callback: XposedInterface.BeforeHookCallback
            ): ANRDialogShowingHooker {
                try {
                    val controller = callback.thisObject
                    if (controller == null) {
                        callback.returnAndSkip(null)
                        return ANRDialogShowingHooker()
                    }

                    val isSilentANR: Boolean? = controller
                        .getField<Any>("mApp")
                        ?.getField<Any>("mErrorState")
                        ?.invokeMethod("isSilentAnr")
                    if (isSilentANR == null) {
                        callback.returnAndSkip(null)
                        return ANRDialogShowingHooker()
                    }

                    val contexts: List<Context>? = controller.invokeMethod(
                        "getDisplayContexts",
                        arrayOf(Boolean::class.java),
                        isSilentANR
                    )
                    if (contexts.isNullOrEmpty()) {
                        callback.returnAndSkip(null)
                        return ANRDialogShowingHooker()
                    }

                    val service: Any? = controller.getField("mService")
                    if (service == null) {
                        callback.returnAndSkip(null)
                        return ANRDialogShowingHooker()
                    }

                    val data = callback.args[0]
                    val classLoader = data.javaClass.classLoader
                    if (classLoader == null) {
                        callback.returnAndSkip(null)
                        return ANRDialogShowingHooker()
                    }

                    val dialogClass =
                        classLoader.loadClass("com.android.server.am.AppNotRespondingDialog")
                    if (dialogClass == null) {
                        callback.returnAndSkip(null)
                        return ANRDialogShowingHooker()
                    }

                    val activityManagerServiceClass = classLoader
                        .loadClass("com.android.server.am.ActivityManagerService")
                    val dialogDataClass = classLoader.loadClass(DIALOG_DATA_CLASS_NAME)
                    for (context in contexts) {
                        val dialog: Any? = dialogClass.newInstance(
                            arrayOf(
                                activityManagerServiceClass,
                                Context::class.java,
                                dialogDataClass
                            ),
                            service,
                            context,
                            data
                        )
                        if (dialog == null) {
                            callback.returnAndSkip(null)
                            return ANRDialogShowingHooker()
                        }

                        val handler: Handler? = dialog.getField("mHandler")
                        if (handler == null) {
                            callback.returnAndSkip(null)
                            return ANRDialogShowingHooker()
                        }
                        xposedModule?.log("[NoANRAndCrashDialogs] Hide ANR dialog")
                        handler.obtainMessage(WAIT_COMMAND_CODE).sendToTarget()
                    }
                    callback.returnAndSkip(null)
                } catch (e: Throwable) {
                    callback.returnAndSkip(null)
                }
                return ANRDialogShowingHooker()
            }
        }
    }
}
