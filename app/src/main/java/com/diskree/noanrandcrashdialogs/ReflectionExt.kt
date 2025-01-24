package com.diskree.noanrandcrashdialogs

import java.lang.reflect.AccessibleObject

@Suppress("UNCHECKED_CAST")
fun <R> Any.getField(name: String, isStatic: Boolean = false): R? =
    javaClass
        .getDeclaredField(name)
        .accessed()
        .get(if (isStatic) null else this) as? R

@Suppress("UNCHECKED_CAST")
fun <R> Any.invokeMethod(
    methodName: String,
    parameterTypes: Array<Class<*>> = arrayOf(),
    vararg args: Any?
): R? = javaClass
    .getDeclaredMethod(methodName, *parameterTypes)
    .accessed()
    .invoke(this, *args) as? R

@Suppress("UNCHECKED_CAST")
fun <R> Class<*>.newInstance(
    parameterTypes: Array<Class<*>>,
    vararg args: Any?
): R? = getDeclaredConstructor(*parameterTypes)
    .apply { isAccessible = true }
    .newInstance(*args) as? R

fun <T : AccessibleObject> T.accessed(): T = apply { isAccessible = true }
