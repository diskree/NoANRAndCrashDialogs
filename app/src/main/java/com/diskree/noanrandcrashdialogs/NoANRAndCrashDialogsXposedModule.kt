package com.diskree.noanrandcrashdialogs

import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class NoANRAndCrashDialogsXposedModule(
    base: XposedInterface,
    param: XposedModuleInterface.ModuleLoadedParam
) : XposedModule(base, param) {

    override fun onSystemServerLoaded(param: XposedModuleInterface.SystemServerLoadedParam) {
        super.onSystemServerLoaded(param)

        NoANRDialogsHooker.hook(param, this@NoANRAndCrashDialogsXposedModule)
        NoCrashDialogsHooker.hook(param, this@NoANRAndCrashDialogsXposedModule)
    }
}
